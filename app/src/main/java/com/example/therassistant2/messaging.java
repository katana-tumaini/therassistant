package com.example.therassistant2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class messaging extends AppCompatActivity {

    private RecyclerView messageRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private TextView chatHeader;
    private ImageButton backButton;
    private ImageView profileImage;

    private ArrayList<Message> messageList;
    private messageadapter messageAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String currentChatId;
    private String receiverId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageRecyclerView = findViewById(R.id.messageRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatHeader = findViewById(R.id.chatUserName);
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Extract receiverId before initializing adapter
        receiverId = getIntent().getStringExtra("receiverId");
        String recipientName = getIntent().getStringExtra("receiverName");

        messageList = new ArrayList<>();
        Context context = this;
        messageAdapter = new messageadapter(messageList, context, receiverId);

        LinearLayoutManager  layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);

        if (recipientName != null)
            chatHeader.setText(recipientName);

        backButton.setOnClickListener(v -> finish());

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(messaging.this, TherapistProfile.class);
            intent.putExtra("therapistId", receiverId);
            startActivity(intent);
        });

        if (receiverId != null && !receiverId.isEmpty())
            checkForExistingChat(receiverId);
        else {
            Toast.makeText(this, "Recipient missing.", Toast.LENGTH_LONG).show();
            finish();
        }

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                if (currentChatId == null) {
                    createChatAndSendMessage(text, receiverId);
                } else {
                    sendMessage(text);
                }
            }
        });
    }

    private void checkForExistingChat(String recipientId) {
        String currentUserId = auth.getCurrentUser().getUid();
        String chatId = currentUserId.compareTo(recipientId) > 0
                ? currentUserId + "_" + recipientId
                : recipientId + "_" + currentUserId;

        DocumentReference chatRef = db.collection("chats").document(chatId);
        
        chatRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                currentChatId = chatId;
                loadMessages(chatId);
            }
            // If chat doesn't exist, don't create it - wait for first message
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Chat check error", Toast.LENGTH_SHORT).show());
    }

    private void createChatAndSendMessage(String messageText, String recipientId) {
        String currentUserId = auth.getCurrentUser().getUid();
        String chatId = currentUserId.compareTo(recipientId) > 0
                ? currentUserId + "_" + recipientId
                : recipientId + "_" + currentUserId;

        DocumentReference chatRef = db.collection("chats").document(chatId);
        
        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participants", Arrays.asList(currentUserId, recipientId));
        chatData.put("createdAt", System.currentTimeMillis());
        chatData.put("createdBy", currentUserId); // Track who created the chat

        chatRef.set(chatData).addOnSuccessListener(aVoid -> {
            currentChatId = chatId;
            loadMessages(chatId);
            sendMessage(messageText);
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to create chat", Toast.LENGTH_SHORT).show());
    }

    private void findOrCreateChat(String recipientId) {

        String currentUserId = auth.getCurrentUser().getUid();

        String chatId = currentUserId.compareTo(recipientId) > 0
                ? currentUserId + "_" + recipientId
                : recipientId + "_" + currentUserId;

        DocumentReference chatRef =
                db.collection("chats").document(chatId);

        chatRef.get().addOnSuccessListener(doc -> {

            if (!doc.exists()) {
                Map<String, Object> chatData = new HashMap<>();
                chatData.put("participants",
                        Arrays.asList(currentUserId, recipientId));
                chatData.put("createdAt", System.currentTimeMillis());

                chatRef.set(chatData);
            }

            currentChatId = chatId;
            loadMessages(chatId);

        }).addOnFailureListener(e ->
                Toast.makeText(this,
                        "Chat error",
                        Toast.LENGTH_SHORT).show());
    }

    private void loadMessages(String chatId) {

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) {
                        Toast.makeText(this, "Error loading messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    messageList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Message msg = doc.toObject(Message.class);
                        // Ensure the message has all required fields
                        if (msg.getText() != null && msg.getUid() != null) {
                            messageList.add(msg);
                        }
                    }

                    messageAdapter.notifyDataSetChanged();

                    // Scroll to latest message if there are messages
                    if (messageList.size() > 0) {
                        messageRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                });
    }

    private void sendMessage(String text) {

        Message message = new Message(
                text,
                System.currentTimeMillis(),
                auth.getCurrentUser().getUid()
        );

        db.collection("chats")
                .document(currentChatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(doc -> {
                    messageInput.setText("");
                    
                    // Update chat document with last message info
                    updateChatLastMessage(text, System.currentTimeMillis());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Message failed",
                                Toast.LENGTH_SHORT).show());
    }

    private void updateChatLastMessage(String lastMessage, long timestamp) {
        db.collection("chats")
                .document(currentChatId)
                .update("lastMessage", lastMessage, "lastTimestamp", timestamp)
                .addOnFailureListener(e -> {
                    // Silent fail - chat list update is not critical
                });
    }
}

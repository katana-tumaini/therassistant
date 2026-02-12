package com.example.therassistant2;

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

    private List<Message> messageList;
    private messageadapter messageAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String currentChatId;
    private String recipientId;

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

        // 🔹 Setup RecyclerView
        messageList = new ArrayList<>();
        messageAdapter = new messageadapter(
                messageList,
                auth.getCurrentUser().getUid()
        );

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        messageRecyclerView.setLayoutManager(layoutManager);
        messageRecyclerView.setAdapter(messageAdapter);

        // 🔹 Get chat info
        recipientId = getIntent().getStringExtra("recipientId");
        String recipientName = getIntent().getStringExtra("recipientName");

        if (recipientName != null)
            chatHeader.setText(recipientName);

        backButton.setOnClickListener(v -> finish());

        profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(messaging.this, TherapistProfile.class);
            intent.putExtra("therapistId", recipientId);
            startActivity(intent);
        });

        if (recipientId != null && !recipientId.isEmpty())
            findOrCreateChat(recipientId);
        else {
            Toast.makeText(this, "Recipient missing.", Toast.LENGTH_LONG).show();
            finish();
        }

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty() && currentChatId != null)
                sendMessage(text);
        });
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

                    if (error != null || value == null)
                        return;

                    messageList.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Message msg = doc.toObject(Message.class);
                        messageList.add(msg);
                    }

                    messageAdapter.notifyDataSetChanged();

                    // Scroll to latest message
                    messageRecyclerView.scrollToPosition(
                            messageList.size() - 1);
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
                .addOnSuccessListener(doc ->
                        messageInput.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Message failed",
                                Toast.LENGTH_SHORT).show());
    }
}

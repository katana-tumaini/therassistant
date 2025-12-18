package com.example.therassistant2;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private ListView messageListView;
    private EditText messageInput;
    private Button sendButton;
    private TextView chatHeader;

    private List<Message> messageList;
    private messageadapter messageAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // This will be set by the findOrCreateChat method
    private String currentChatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Initialize Views
        messageListView = findViewById(R.id.messageListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        chatHeader = findViewById(R.id.chatTitle);

        // 2. Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 3. Get data from the Intent
        String recipientId = getIntent().getStringExtra("recipientId");
        String recipientName = getIntent().getStringExtra("recipientName");

        // Set the recipient's name in the header
        if (recipientName != null) {
            chatHeader.setText(recipientName);
        }

        // 4. Setup the ListView and Adapter
        messageList = new ArrayList<>();
        messageAdapter = new messageadapter(this, messageList, auth.getCurrentUser().getUid());
        messageListView.setAdapter(messageAdapter);

        // 5. Find or create the chat room. This is the key step.
        if (recipientId != null && !recipientId.isEmpty()) {
            findOrCreateChat(recipientId);
        } else {
            Toast.makeText(this, "Error: Recipient not found.", Toast.LENGTH_LONG).show();
            finish(); // Close the activity if there's no one to talk to
        }

        // 6. Set the send button listener
        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            // Only send if there's text AND we have a valid chat room ID
            if (!text.isEmpty() && currentChatId != null) {
                sendMessage(text);
            }
        });
    }

    /**
     * Finds a chat between the current user and the recipient. If it doesn't exist,
     * it creates one in Firestore. It then calls loadMessages().
     *
     * @param recipientId The user ID of the person to chat with.
     */
    private void findOrCreateChat(String recipientId) {
        String currentUserId = auth.getCurrentUser().getUid();

        // Create a consistent, predictable chat ID by sorting the user IDs alphabetically.
        // This ensures both users always find the same chat room.
        final String chatId;
        if (currentUserId.compareTo(recipientId) > 0) {
            chatId = currentUserId + "_" + recipientId;
        } else {
            chatId = recipientId + "_" + currentUserId;
        }

        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // --- CHAT ALREADY EXISTS ---
                    Log.d("Messaging", "Chat room " + chatId + " found. Loading messages.");
                    this.currentChatId = chatId;
                    loadMessages(this.currentChatId); // It's now safe to load messages
                } else {
                    // --- CHAT DOES NOT EXIST, SO CREATE IT ---
                    Log.d("Messaging", "Chat room " + chatId + " not found. Creating...");
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("participants", Arrays.asList(currentUserId, recipientId));
                    chatData.put("createdAt", System.currentTimeMillis());

                    chatRef.set(chatData).addOnSuccessListener(aVoid -> {
                        Log.d("Messaging", "Chat room created successfully.");
                        this.currentChatId = chatId;
                        loadMessages(this.currentChatId); // Load the (empty) message list
                    }).addOnFailureListener(e -> {
                        Toast.makeText(messaging.this, "Error creating chat.", Toast.LENGTH_SHORT).show();
                        Log.e("Messaging", "Error creating chat room", e);
                    });
                }
            } else {
                Toast.makeText(messaging.this, "Error finding chat.", Toast.LENGTH_SHORT).show();
                Log.e("Messaging", "Error getting chat document", task.getException());
            }
        });
    }

    /**
     * Loads all messages for a given chat ID and listens for new ones.
     *
     * @param chatId The document ID of the chat room.
     */
    private void loadMessages(String chatId) {
        if (chatId == null) {
            Log.e("Messaging", "Cannot load messages, chatId is null.");
            return;
        }

        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Messaging", "Listen failed.", error);
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        messageAdapter.notifyDataSetChanged();
                        messageListView.setSelection(messageAdapter.getCount() - 1);
                    }
                });
    }

    /**
     * Creates a Message object and saves it to the current chat's subcollection in Firestore.
     *
     * @param text The content of the message.
     */
    private void sendMessage(String text) {
        if (currentChatId == null) {
            Toast.makeText(this, "Cannot send message, no active chat.", Toast.LENGTH_SHORT).show();
            return;
        }

        Message message = new Message(
                text,
                System.currentTimeMillis(),
                auth.getCurrentUser().getUid()
        );

        db.collection("chats").document(currentChatId).collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    // Clear the input field on success
                    messageInput.setText("");
                    Log.d("Messaging", "Message sent successfully!");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show();
                    Log.e("Messaging", "Error sending message", e);
                });
    }
}
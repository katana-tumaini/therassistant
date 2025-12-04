package com.example.therassistant2;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class messaging extends AppCompatActivity {

    private ListView messageListView;
    private EditText messageInput;
    private Button sendButton;

    private List<Message> messageList;
    private messageadapter messageAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // This must be passed from previous activity
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageListView = findViewById(R.id.messageListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // ✅ Get chatId from Intent
        chatId = getIntent().getStringExtra("chatId");

        messageList = new ArrayList<>();
        messageAdapter = new messageadapter(this, messageList, auth.getCurrentUser().getUid());
        messageListView.setAdapter(messageAdapter);

        loadMessages();

        sendButton.setOnClickListener(v -> {
            String text = messageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });
    }

    // ✅ Load messages for THIS chat only
    private void loadMessages() {
        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        messageAdapter.notifyDataSetChanged();
                    }
                });
    }

    // ✅ Send message
    private void sendMessage(String text) {
        Message message = new Message(
                text,
                System.currentTimeMillis(),
                auth.getCurrentUser().getUid()
        );

        db.collection("messages")
                .document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(ref -> messageInput.setText(""));
    }
}

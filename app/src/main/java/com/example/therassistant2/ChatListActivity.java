package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private Button newchatbutton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();

        chatRecyclerView = findViewById(R.id.chatListView);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        chatRecyclerView.setAdapter(chatAdapter);

        newchatbutton = findViewById(R.id.NewChatButton);
        newchatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatListActivity.this, therapists.class);
                startActivity(intent);
            }
        });

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this, "Error loading chats: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        chatList.clear();
                        chatAdapter.notifyDataSetChanged();
                        return;
                    }

                    chatList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        String chatId = doc.getId();
                        List<String> participants = (List<String>) doc.get("participants");

                        String lastMessage = doc.getString("lastMessage");
                        Long lastTimestamp = doc.getLong("lastTimestamp");

                        if (lastMessage == null) lastMessage = "No messages yet";
                        if (lastTimestamp == null) lastTimestamp = 0L;

                        // Find other user ID
                        String otherUserId = null;
                        if (participants != null) {
                            for (String id : participants) {
                                if (!id.equals(currentUserId)) {
                                    otherUserId = id;
                                    break;
                                }
                            }
                        }

                        if (otherUserId != null) {
                            fetchUserDetailsAndAddChat(chatId, otherUserId, lastMessage, lastTimestamp);
                        }
                    }
                });
    }

    private void fetchUserDetailsAndAddChat(String chatId, String otherUserId, String lastMsg, long lastTs) {
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    String firstName = documentSnapshot.getString("firstName");
                    String lastName = documentSnapshot.getString("lastName");

                    if (firstName == null) firstName = "";
                    if (lastName == null) lastName = "";

                    Chat chat = new Chat(chatId, firstName, lastName, lastMsg, lastTs);

                    chatList.add(chat);

                    // Sort newest -> oldest
                    Collections.sort(chatList, (a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

                    chatAdapter.notifyDataSetChanged();
                });
    }
}

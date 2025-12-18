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

import com.google.android.material.floatingactionbutton.FloatingActionButton; // 1. IMPORT THIS
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
        // Assuming your ChatAdapter constructor is (Context, List<Chat>)
        chatAdapter = new ChatAdapter(this, chatList);
        chatRecyclerView.setAdapter(chatAdapter);


        newchatbutton = findViewById(R.id.NewChatButton);
        newchatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This will take the user to the therapists list screen
                Intent intent = new Intent(ChatListActivity.this, therapists.class);
                startActivity(intent);
            }
        });

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        // Using a temporary list to avoid flickering while processing
                        List<Chat> tempChatList = new ArrayList<>();
                        if (value.isEmpty()) {
                            // If there are no chats, clear the list and update UI
                            chatList.clear();
                            chatAdapter.notifyDataSetChanged();
                            return;
                        }

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String chatId = doc.getId();
                            List<String> participants = (List<String>) doc.get("participants");

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
                                fetchUserDetailsAndAddChat(chatId, otherUserId);
                            }
                        }
                    }
                });
    }

    private void fetchUserDetailsAndAddChat(String chatId, String otherUserId) {
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        // Get latest message for this chat
                        db.collection("chats").document(chatId) // Corrected path
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .addSnapshotListener((querySnapshot, e) -> {
                                    if (e != null) {
                                        // Handle error
                                        return;
                                    }

                                    String lastMsg = "No messages yet";
                                    long lastTs = 0L;
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        DocumentSnapshot msgDoc = querySnapshot.getDocuments().get(0);
                                        lastMsg = msgDoc.getString("text") != null ? msgDoc.getString("text") : "";
                                        Long ts = msgDoc.getLong("timestamp");
                                        lastTs = ts != null ? ts : 0L;
                                    }

                                    Chat chat = new Chat(chatId, firstName, lastName, lastMsg, lastTs);

                                    // Find and update or add the chat item
                                    int existingIndex = -1;
                                    for (int i = 0; i < chatList.size(); i++) {
                                        if (chatList.get(i).getChatId().equals(chatId)) {
                                            existingIndex = i;
                                            break;
                                        }
                                    }

                                    if (existingIndex != -1) {
                                        chatList.set(existingIndex, chat);
                                    } else {
                                        chatList.add(chat);
                                    }

                                    // Sort newest -> oldest by lastTimestamp
                                    Collections.sort(chatList, (a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

                                    chatAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }
}
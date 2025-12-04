package com.example.therassistant2;

import android.os.Bundle;
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
import java.util.Comparator;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

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
                        chatList.clear();

                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String chatId = doc.getId();
                            List<String> participants = (List<String>) doc.get("participants");

                            // Find other user id
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
                                fetchUserDetailsAndAddChat(chatId, otherUserId, participants);
                            }
                        }
                    }
                });
    }

    private void fetchUserDetailsAndAddChat(String chatId, String otherUserId, List<String> participants) {
        db.collection("users").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        // Get latest message for this chat
                        db.collection("messages")
                                .document(chatId)
                                .collection("messages")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    String lastMsg = "";
                                    long lastTs = 0L;
                                    if (!querySnapshot.isEmpty()) {
                                        DocumentSnapshot msgDoc = querySnapshot.getDocuments().get(0);
                                        lastMsg = msgDoc.getString("text") != null ? msgDoc.getString("text") : "";
                                        Long ts = msgDoc.getLong("timestamp");
                                        lastTs = ts != null ? ts : 0L;
                                    }

                                    Chat chat = new Chat(chatId, firstName, lastName, participants, lastMsg, lastTs);

                                    boolean exists = false;
                                    for (Chat c : chatList) {
                                        if (c.getChatId().equals(chatId)) { exists = true; break; }
                                    }

                                    if (!exists) {
                                        chatList.add(chat);
                                    } else {
                                        // replace existing entry
                                        for (int i = 0; i < chatList.size(); i++) {
                                            if (chatList.get(i).getChatId().equals(chatId)) {
                                                chatList.set(i, chat);
                                                break;
                                            }
                                        }
                                    }

                                    // Sort newest -> oldest by lastTimestamp (null-safe)
                                    Collections.sort(chatList, (a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

                                    chatAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }
}

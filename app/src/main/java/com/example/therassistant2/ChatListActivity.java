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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
    private Button retryButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Check Google Play Services availability first
        if (!checkGooglePlayServices()) {
            Toast.makeText(this, "Google Play Services not available. Some features may not work.", Toast.LENGTH_LONG).show();
        }

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

        // Add retry button functionality (you can add this button to your layout if needed)
        retryButton = findViewById(R.id.RetryButton);
        if (retryButton != null) {
            retryButton.setOnClickListener(v -> {
                Toast.makeText(this, "Retrying to load chats...", Toast.LENGTH_SHORT).show();
                loadChats();
            });
        }

        loadChats();
    }

    private boolean checkGooglePlayServices() {
        GoogleApiAvailability gApi = GoogleApiAvailability.getInstance();
        int resultCode = gApi.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        } else if (gApi.isUserResolvableError(resultCode)) {
            // Show dialog to resolve the issue
            gApi.getErrorDialog(this, resultCode, 9000).show();
        } else {
            Toast.makeText(this, "This device is not supported for Google Play Services.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void loadChats() {
        try {
            db.collection("chats")
                    .whereArrayContains("participants", currentUserId)
                    .orderBy("lastTimestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener((value, error) -> {

                        if (error != null) {
                            String errorMessage = error.getMessage();
                            if (errorMessage != null && errorMessage.contains("Unknown calling package name")) {
                                Toast.makeText(this, "Google Play Services error. Please restart app or check device settings.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Error loading chats: " + errorMessage, Toast.LENGTH_SHORT).show();
                            }
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
        } catch (Exception e) {
            Toast.makeText(this, "Failed to initialize chat loading: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void fetchUserDetailsAndAddChat(String chatId, String otherUserId, String lastMsg, long lastTs) {
        // Try therapists collection first, then fallback to users
        db.collection("therapists").document(otherUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Found in therapists collection
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");

                        if (firstName == null) firstName = "";
                        if (lastName == null) lastName = "";

                        Chat chat = new Chat(chatId, firstName, lastName, lastMsg, lastTs);
                        addChatToList(chat);
                    } else {
                        // Try users collection as fallback
                        db.collection("users").document(otherUserId).get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        String firstName = userDoc.getString("firstName");
                                        String lastName = userDoc.getString("lastName");

                                        if (firstName == null) firstName = "";
                                        if (lastName == null) lastName = "";

                                        Chat chat = new Chat(chatId, firstName, lastName, lastMsg, lastTs);
                                        addChatToList(chat);
                                    }
                                });
                    }
                });
    }

    private void addChatToList(Chat chat) {
        chatList.add(chat);

        // Sort newest -> oldest
        Collections.sort(chatList, (a, b) -> Long.compare(b.getLastTimestamp(), a.getLastTimestamp()));

        chatAdapter.notifyDataSetChanged();
    }
}

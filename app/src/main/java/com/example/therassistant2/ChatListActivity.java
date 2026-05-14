package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        // Check Google Play Services
        if (!checkGooglePlayServices()) {
            Toast.makeText(this,
                    "Google Play Services not available",
                    Toast.LENGTH_LONG).show();
        }

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            finish();
            return;
        }

        currentUserId = auth.getCurrentUser().getUid();

        // RecyclerView
        chatRecyclerView = findViewById(R.id.chatListView);
        chatRecyclerView.setLayoutManager(
                new LinearLayoutManager(this));

        chatList = new ArrayList<>();

        chatAdapter = new ChatAdapter(this, chatList);

        chatRecyclerView.setAdapter(chatAdapter);

        // Buttons
        newchatbutton = findViewById(R.id.NewChatButton);

        newchatbutton.setOnClickListener(v -> {
            Intent intent =
                    new Intent(ChatListActivity.this,
                            therapists.class);

            startActivity(intent);
        });

        retryButton = findViewById(R.id.RetryButton);

        if (retryButton != null) {
            retryButton.setOnClickListener(v -> loadChats());
        }

        loadChats();
    }

    private boolean checkGooglePlayServices() {

        GoogleApiAvailability gApi =
                GoogleApiAvailability.getInstance();

        int resultCode =
                gApi.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;

        } else if (gApi.isUserResolvableError(resultCode)) {

            gApi.getErrorDialog(this,
                    resultCode,
                    9000).show();

        } else {

            Toast.makeText(this,
                    "Google Play Services unsupported",
                    Toast.LENGTH_LONG).show();
        }

        return false;
    }

    private void loadChats() { 

        db.collection("chats")
                .whereArrayContains("participants",
                        currentUserId)
                .orderBy("lastTimestamp",
                        Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null) {

                        Toast.makeText(this,
                                "Error loading chats: "
                                        + error.getMessage(),
                                Toast.LENGTH_LONG).show();

                        return;
                    }

                    if (value == null) {
                        return;
                    }

                    chatList.clear();

                    if (value.isEmpty()) {

                        chatAdapter.notifyDataSetChanged();

                        Toast.makeText(this,
                                "No chats found",
                                Toast.LENGTH_SHORT).show();

                        return;
                    }

                    for (DocumentSnapshot doc :
                            value.getDocuments()) {

                        String chatId = doc.getId();

                        List<String> participants =
                                (List<String>) doc.get("participants");

                        String lastMessage =
                                doc.getString("lastMessage");

                        Long lastTimestamp =
                                doc.getLong("lastTimestamp");

                        if (lastMessage == null)
                            lastMessage = "No messages yet";

                        if (lastTimestamp == null)
                            lastTimestamp = 0L;

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

                            fetchUserDetailsAndAddChat(
                                    chatId,
                                    otherUserId,
                                    lastMessage,
                                    lastTimestamp
                            );
                        }
                    }
                });
    }

    private void fetchUserDetailsAndAddChat(String chatId,
                                            String otherUserId,
                                            String lastMsg,
                                            long lastTs) {

        // 🔥 Fetch from Realtime Database
        DatabaseReference therapistRef =
                FirebaseDatabase.getInstance()
                        .getReference("therapists")
                        .child(otherUserId);

        therapistRef.get().addOnSuccessListener(snapshot -> {

            if (snapshot.exists()) {

                String firstName =
                        snapshot.child("firstName")
                                .getValue(String.class);

                String lastName =
                        snapshot.child("lastName")
                                .getValue(String.class);

                if (firstName == null)
                    firstName = "";

                if (lastName == null)
                    lastName = "";

                Chat chat = new Chat(
                        chatId,
                        firstName,
                        lastName,
                        lastMsg,
                        lastTs
                );

                addChatToList(chat);

            } else {

                // 🔥 Fallback to users node
                DatabaseReference userRef =
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(otherUserId);

                userRef.get().addOnSuccessListener(userSnapshot -> {

                    if (userSnapshot.exists()) {

                        String firstName =
                                userSnapshot.child("firstName")
                                        .getValue(String.class);

                        String lastName =
                                userSnapshot.child("lastName")
                                        .getValue(String.class);

                        if (firstName == null)
                            firstName = "";

                        if (lastName == null)
                            lastName = "";

                        Chat chat = new Chat(
                                chatId,
                                firstName,
                                lastName,
                                lastMsg,
                                lastTs
                        );

                        addChatToList(chat);
                    }
                });
            }
        });
    }

    private void addChatToList(Chat chat) {

        chatList.add(chat);

        Collections.sort(chatList,
                (a, b) -> Long.compare(
                        b.getLastTimestamp(),
                        a.getLastTimestamp()
                ));

        chatAdapter.notifyDataSetChanged();
    }
}
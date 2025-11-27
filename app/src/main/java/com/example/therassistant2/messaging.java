package com.example.therassistant2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class messaging extends AppCompatActivity {

    private ListView chatListView, messageListView;
    private EditText messageInput;
    private Button sendButton;
    private FloatingActionButton addChatButton;
    private Map<String, List<Message>> chatMessages;
    private String currentChatId;
    private List<Chat> chatList;
    private ChatAdapter chatAdapter;
    private messageadapter messageAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        chatListView = findViewById(R.id.chatListView);
        messageListView = findViewById(R.id.messageListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        addChatButton = findViewById(R.id.addChatButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        chatMessages = new HashMap<>();
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatList);
        chatListView.setAdapter(chatAdapter);

        loadChats();

        addChatButton.setOnClickListener(v -> showAddChatDialog());

        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty() && currentChatId != null) {
                Message message = new Message(messageText, System.currentTimeMillis(), auth.getCurrentUser().getUid());
                sendMessageToFirebase(message);
            }
        });
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String chatId = document.getId();
                            List<String> participants = (List<String>) document.get("participants");
                            for (String userId : participants) {
                                if (!userId.equals(auth.getCurrentUser().getUid())) {
                                    fetchUserDetails(userId, chatId);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchUserDetails(String userId, String chatId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName");
                        String lastName = documentSnapshot.getString("lastName");
                        Chat chat = new Chat(chatId, firstName, lastName);
                        chatList.add(chat);
                        chatAdapter.notifyDataSetChanged();

                        if (!chatMessages.containsKey(chatId)) {
                            chatMessages.put(chatId, new ArrayList<Message>());
                        }
                        loadMessages(chatId);
                    }
                });
    }

    private void loadMessages(String chatId) {
        db.collection("messages").document(chatId).collection("messages")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Message> messages = chatMessages.get(chatId);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String text = document.getString("text");
                            long timestamp = document.getLong("timestamp");
                            String senderId = document.getString("senderId");
                            Message message = new Message();
                            message.setText(text);
                            message.setTimestamp(timestamp);
                            message.setSenderId(senderId);
                            messages.add(message);
                        }
                        if (currentChatId != null && currentChatId.equals(chatId)) {
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void sendMessageToFirebase(Message message) {
        db.collection("messages").document(currentChatId).collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    chatMessages.get(currentChatId).add(message);
                    messageAdapter.notifyDataSetChanged();
                    messageInput.setText("");
                });
    }

    private void showAddChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Chat");

        final EditText input = new EditText(this);
        input.setHint("Enter email");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                fetchUserByEmail(email);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void fetchUserByEmail(String email) {
        db.collection("users").whereEqualTo("email", email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userId = document.getId();
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String chatId = db.collection("chats").document().getId();

                            Chat newChat = new Chat(chatId, firstName, lastName);
                            chatList.add(newChat);
                            chatAdapter.notifyDataSetChanged();

                            List<String> participants = new ArrayList<>();
                            participants.add(auth.getCurrentUser().getUid());
                            participants.add(userId);

                            Chat chat = new Chat(chatId, firstName, lastName, participants);
                            db.collection("chats").document(chatId).set(chat);

                            chatMessages.put(chatId, new ArrayList<>());
                            openChat(chatId);
                        }
                    }
                });
    }

    void openChat(String chatId) {
        currentChatId = chatId;
        messageAdapter = new messageadapter(this, chatMessages.get(currentChatId));
        messageListView.setAdapter(messageAdapter);
        chatListView.setVisibility(View.GONE);
        messageListView.setVisibility(View.VISIBLE);
        messageInput.setVisibility(View.VISIBLE);
        sendButton.setVisibility(View.VISIBLE);
    }

    private class ChatAdapter extends ArrayAdapter<Chat> {
        public ChatAdapter(Context context, List<Chat> chats) {
            super(context, 0, chats);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Chat chat = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_chat, parent, false);
            }

            TextView chatName = convertView.findViewById(R.id.chatName);
            chatName.setText(chat.getFirstName() + " " + chat.getLastName());

            convertView.setOnClickListener(v -> openChat(chat.getChatId()));

            return convertView;
        }
    }

    private class messageadapter extends ArrayAdapter<Message> {
        public messageadapter(Context context, List<Message> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Message message = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_message, parent, false);
            }

            TextView messageText = convertView.findViewById(R.id.messageText);
            TextView messageTimestamp = convertView.findViewById(R.id.messageTimestamp);
            TextView messageSenderId = convertView.findViewById(R.id.messageSenderId);

            if (message != null) {
                messageText.setText(message.getText());
               // messageTimestamp.setText(DateFormat.getTimeFormat(DateFormat.is24HourFormat()).format(new Date(message.getTimestamp())));
                messageSenderId.setText(message.getSenderId());
            }

            return convertView;
        }

    }
}

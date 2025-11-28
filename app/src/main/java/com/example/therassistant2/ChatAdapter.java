package com.example.therassistant2;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;

    public ChatAdapter(List<Chat> chatList) {
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);

        holder.userName.setText(chat.getFirstName() + " " + chat.getLastName());

        // Optional: show last message preview if you add it to model later
        if (chat.getLastMessage() != null) {
            holder.lastMessage.setText(chat.getLastMessage());
        } else {
            holder.lastMessage.setText("No messages yet");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), messaging.class);
            intent.putExtra("chatId", chat.getChatId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView userName, lastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}

package com.example.therassistant2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final List<Chat> chats;
    private final Context context;

    public ChatAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        Chat chat = chats.get(position);

        String name = chat.getDisplayName();
        holder.userName.setText(name == null || name.isEmpty() ? "Unknown User" : name);

        String lastMsg = chat.getLastMessage();
        holder.lastMessage.setText(lastMsg == null || lastMsg.isEmpty() ? "Tap to view messages" : lastMsg);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, messaging.class);
            intent.putExtra("chatId", chat.getChatId());

            // Optional but recommended if your messaging needs it
            intent.putExtra("recipientName", name);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, lastMessage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }
}

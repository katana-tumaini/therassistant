package com.example.therassistant2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class messageadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private String currentUserId;

    private static final int VIEW_SENT = 0;
    private static final int VIEW_RECEIVED = 1;

    public messageadapter(List<Message> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getSenderId().equals(currentUserId)
                ? VIEW_SENT
                : VIEW_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view;

        if (viewType == VIEW_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sent_message, parent, false);
            return new SentViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_received_message, parent, false);
            return new ReceivedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecyclerView.ViewHolder holder,
            int position) {

        Message message = messages.get(position);

        String formattedTime = DateFormat
                .getTimeInstance(DateFormat.SHORT)
                .format(new Date(message.getTimestamp()));

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).messageText.setText(message.getText());
            ((SentViewHolder) holder).messageTime.setText(formattedTime);
        } else {
            ((ReceivedViewHolder) holder).messageText.setText(message.getText());
            ((ReceivedViewHolder) holder).messageTime.setText(formattedTime);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // Sent message holder
    static class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        SentViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }

    // Received message holder
    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        ReceivedViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}

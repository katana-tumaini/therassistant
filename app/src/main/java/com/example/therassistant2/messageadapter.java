package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class messageadapter extends ArrayAdapter<Message> {

    private String currentUserId;

    public messageadapter(Context context, List<Message> messages, String currentUserId) {
        super(context, 0, messages);
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Message message = getItem(position);

        if (message == null) return convertView;

        boolean isSentByUser = message.getSenderId().equals(currentUserId);

        if (convertView == null) {
            if (isSentByUser) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_sent_message, parent, false);
            } else {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_received_message, parent, false);
            }
        }

        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView messageTime = convertView.findViewById(R.id.messageTime);

        messageText.setText(message.getText());

        String formattedTime = DateFormat.getTimeInstance(DateFormat.SHORT)
                .format(new Date(message.getTimestamp()));

        messageTime.setText(formattedTime);

        return convertView;
    }
}

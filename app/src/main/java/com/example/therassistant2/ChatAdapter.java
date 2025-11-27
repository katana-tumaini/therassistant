package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

public class ChatAdapter extends ArrayAdapter<Chat> {

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
        if (chat != null) {
            chatName.setText(chat.getFirstName() + " " + chat.getLastName());
        }

        convertView.setOnClickListener(v -> {
            // Open the chat when the item is clicked
            if (getContext() instanceof messaging) {
                ((messaging) getContext()).openChat(chat.getChatId());
            }
        });

        return convertView;
    }
}

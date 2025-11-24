package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class messageadapter extends ArrayAdapter<Message> {

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
            messageTimestamp.setText(DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(message.getTimestamp())));
            messageSenderId.setText(message.getSenderId());
        }

        return convertView;
    }
}

package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class messageadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Message> messageList;
    Context context;
    String receiverId;
    int sender_view_type = 1;
    int receiver_view_type = 2;


    public messageadapter(ArrayList<Message> messageList, Context context ) {
        this.context = context;
        this.messageList = messageList;
    }

    public messageadapter(ArrayList<Message> messageList, Context context, String receiverId) {
        this.messageList = messageList;
        this.context = context;
        this.receiverId = receiverId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == sender_view_type){
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent_message, parent,false);
            return new SenderViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent,false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position){
        if(messageList.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())){
            return sender_view_type;
        }
        else{
            return receiver_view_type;
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        if(holder.getClass() == SenderViewHolder.class){
            ((SenderViewHolder)holder).senderText.setText(message.getText());
            // Set timestamp for sent message
            if (message.getTimestamp() > 0) {
                ((SenderViewHolder)holder).senderTime.setText(formatTimestamp(message.getTimestamp()));
            }

        }
        else{
            ((ReceiverViewHolder)holder).receiverText.setText(message.getText());
            // Set timestamp for received message
            if (message.getTimestamp() > 0) {
                ((ReceiverViewHolder)holder).receiverTime.setText(formatTimestamp(message.getTimestamp()));
            }
        }

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverText, receiverTime;

        public ReceiverViewHolder(View itemView){
            super(itemView);
            receiverText = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.receiverTime);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView senderText, senderTime;

        public SenderViewHolder(View itemView){
            super(itemView);
            senderText = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }

    // Helper method to format timestamp into readable time
    private String formatTimestamp(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

}
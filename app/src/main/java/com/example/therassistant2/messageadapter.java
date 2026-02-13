package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class messageadapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Message> messageList;
    Context context;

    int sender_view_type = 1;
    int receiver_view_type = 2;


    public messageadapter(Context context, ArrayList<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
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

        }
        else{
            ((ReceiverViewHolder)holder).receiverText.setText(message.getText());
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

}
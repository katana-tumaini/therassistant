package com.example.therassistant2;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder> {

    private ArrayList<Client> clientsList;
    private Context context;

    public ClientAdapter(ArrayList<Client> clientsList, Context context) {
        this.clientsList = clientsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.client_item, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = clientsList.get(position);
        holder.clientNameTextView.setText(client.getClientFirstName() + " " + client.getClientLastName());
        holder.clientEmailTextView.setText(client.getClientEmail());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewClientActivity.class);
            intent.putExtra("clientId", client.getClientId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return clientsList.size();
    }

    public static class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView clientNameTextView, clientEmailTextView;

        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            clientNameTextView = itemView.findViewById(R.id.clientNameTextView);
            clientEmailTextView = itemView.findViewById(R.id.clientEmailTextView);
        }
    }
}

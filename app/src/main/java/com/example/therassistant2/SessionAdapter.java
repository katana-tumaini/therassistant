package com.example.therassistant2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<Session> sessions;

    public SessionAdapter(List<Session> sessions) {
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessions.get(position);

        holder.dateTextView.setText(session.getDate());
        holder.timeTextView.setText(session.getTime());
        holder.detailsTextView.setText(session.getDetails());
        holder.clientNameTextView.setText(session.getClientFirstName() + " " + session.getClientLastName());
        holder.therapistNameTextView.setText(session.getTherapistName()); // Directly set the therapist name
    }


    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView timeTextView;
        TextView detailsTextView;
        TextView clientNameTextView;
        TextView therapistNameTextView;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            detailsTextView = itemView.findViewById(R.id.detailsTextView);
            clientNameTextView = itemView.findViewById(R.id.clientNameTextView);
            therapistNameTextView = itemView.findViewById(R.id.therapistNameTextView);
        }
    }
}

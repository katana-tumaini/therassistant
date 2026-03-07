package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookingRequestsAdapter extends RecyclerView.Adapter<BookingRequestsAdapter.ViewHolder> {

    private List<ModelBookingRequests> list;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;

    public BookingRequestsAdapter(List<ModelBookingRequests> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView clientName, date, time;
        Button acceptBtn, declineBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            clientName = itemView.findViewById(R.id.clientNameText);
            date = itemView.findViewById(R.id.dateText);
            time = itemView.findViewById(R.id.timeText);

            acceptBtn = itemView.findViewById(R.id.acceptButton);
            declineBtn = itemView.findViewById(R.id.declineButton);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking_request, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ModelBookingRequests request = list.get(position);

        holder.clientName.setText(request.getClientName());
        holder.date.setText("Date: " + request.getDate());
        holder.time.setText("Time: " + request.getTime());

        holder.acceptBtn.setOnClickListener(v -> acceptRequest(request));

        holder.declineBtn.setOnClickListener(v -> declineRequest(request));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void acceptRequest(ModelBookingRequests request) {

        // Update request status
        db.collection("bookingRequests")
                .document(request.getRequestId())
                .update("status", "accepted");

        // Create session
        Session session = new Session(
                request.getDate(),
                request.getTime(),
                request.getMeetingType(),
                "client@email.com",
                request.getClientName(),
                "",
                "Therapist",
                "therapist@email.com"
        );

        db.collection("sessions").add(session);

        Toast.makeText(context, "Session Accepted", Toast.LENGTH_SHORT).show();
    }

    private void declineRequest(ModelBookingRequests request) {

        db.collection("bookingRequests")
                .document(request.getRequestId())
                .update("status", "declined");

        Toast.makeText(context, "Session Declined", Toast.LENGTH_SHORT).show();
    }
}
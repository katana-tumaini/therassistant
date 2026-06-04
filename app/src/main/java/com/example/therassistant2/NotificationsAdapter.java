package com.example.therassistant2;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private final Context context;
    private final FirebaseFirestore db;
    private final List<ModelNotification> notificationList;

    public NotificationsAdapter(Context context, FirebaseFirestore db, List<ModelNotification> notificationList) {
        this.context = context;
        this.db = db;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ModelNotification notification = notificationList.get(position);

        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getMessage());

        // Format relative notification time (e.g. "Just now", "4 hours ago", "Yesterday")
        if (notification.getTimestamp() > 0) {
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    notification.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
            holder.timestampTextView.setText(relativeTime);
            holder.timestampTextView.setVisibility(View.VISIBLE);
        } else {
            holder.timestampTextView.setVisibility(View.GONE);
        }

        // Elegantly emphasize unread items or adjust transience relative to status
        if (!notification.isRead()) {
            holder.itemView.setBackgroundResource(R.drawable.unread_card_background);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.card_background);
            holder.itemView.setAlpha(0.85f); // Softening read messages slightly
        }

        // Workflow details: Identify therapist booking requests and show interactive buttons!
        if ("booking_request".equals(notification.getType()) && "pending".equals(notification.getBookingStatus())) {
            holder.actionButtonsLayout.setVisibility(View.VISIBLE);

            // Set up click events to run interactive actions directly via Firebase
            holder.acceptButton.setOnClickListener(v -> {
                updateBookingStatus(notification, "accepted", holder.actionButtonsLayout);
            });

            holder.declineButton.setOnClickListener(v -> {
                updateBookingStatus(notification, "declined", holder.actionButtonsLayout);
            });
        } else if ("booking_request".equals(notification.getType()) && !"pending".equals(notification.getBookingStatus())) {
            // If already processed, show the resulting stamp label in the message or card
            holder.actionButtonsLayout.setVisibility(View.GONE);
            String statusUpper = notification.getBookingStatus().substring(0, 1).toUpperCase() + notification.getBookingStatus().substring(1);
            holder.messageTextView.setText(notification.getMessage() + " (Status: " + statusUpper + ")");
        } else {
            holder.actionButtonsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the status of physical bookings and alerts the servers.
     */
    private void updateBookingStatus(ModelNotification notification, String status, LinearLayout buttonsLayout) {
        String bookingId = notification.getBookingId();
        String notificationId = notification.getId();

        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(context, "Error: Booking key reference not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Update status on notifications source object
        db.collection("notifications").document(notificationId)
                .update("bookingStatus", status)
                .addOnSuccessListener(aVoid -> {
                    buttonsLayout.setVisibility(View.GONE);
                    Toast.makeText(context, "Booking successfully " + status, Toast.LENGTH_SHORT).show();

                    // 2. Update parent transaction booking item
                    db.collection("bookings").document(bookingId)
                            .update("status", status);

                    // 3. Auto-notify the client of this decision!
                    sendResponseNotificationToClient(notification, status);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Database write failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendResponseNotificationToClient(ModelNotification sourceNotification, String status) {
        // Find which client sent the booking (the client profile node or custom payload attribute)
        // Usually, therapists push records into firestore client sub-collections
        String clientId = sourceNotification.getUserId(); // Standard receiver key

        Map<String, Object> clientNotification = new HashMap<>();
        clientNotification.put("userId", clientId);
        clientNotification.put("title", "Booking Update");
        clientNotification.put("message", "Your therapist has " + status + " your booking for " +
                sourceNotification.getBookingDate() + " at " + sourceNotification.getBookingTime());
        clientNotification.put("timestamp", System.currentTimeMillis());
        clientNotification.put("read", false);
        clientNotification.put("type", "accepted".equals(status) ? "booking_accepted" : "booking_declined");
        clientNotification.put("bookingId", sourceNotification.getBookingId());

        db.collection("notifications")
                .add(clientNotification)
                .addOnSuccessListener(docRef -> Log.d("Adapter", "Dispatched response notification for client"))
                .addOnFailureListener(e -> Log.e("Adapter", "Dispatch error: ", e));
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView timestampTextView;
        LinearLayout actionButtonsLayout;
        Button acceptButton;
        Button declineButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notificationTitle);
            messageTextView = itemView.findViewById(R.id.notificationMessage);
            timestampTextView = itemView.findViewById(R.id.notificationTimestamp);

            // Link custom elements for therapist workflows
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsContainer);
            acceptButton = itemView.findViewById(R.id.btnAccept);
            declineButton = itemView.findViewById(R.id.btnDecline);
        }
    }
}
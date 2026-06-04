package com.example.therassistant2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private static final String TAG = "NotificationsActivity";
    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<ModelNotification> notificationList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration notificationsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();

        // Pass Database and Activity context to adapter for managing Accept/Decline Actions in Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        adapter = new NotificationsAdapter(this, db, notificationList);
        recyclerView.setAdapter(adapter);


        findViewById(android.R.id.content).postDelayed(() -> {

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", userId);
            notification.put("title", "Test Notification");
            notification.put("message", "Notifications are working!");
            notification.put("timestamp", System.currentTimeMillis());
            notification.put("read", false);

            FirebaseFirestore.getInstance()
                    .collection("notifications")
                    .add(notification)
                    .addOnSuccessListener(doc ->
                            Toast.makeText(this,
                                    "Test notification created",
                                    Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    e.getMessage(),
                                    Toast.LENGTH_LONG).show());

        }, 3000);

        // 1. Fetch, upload and sync registration Cloud Messaging token for this device
        syncDeviceFCMToken();

        // 2. Attach real-time Firestore listener to automatically update without lag
        setupNotificationsListener();
    }

    private void syncDeviceFCMToken() {
        if (auth.getCurrentUser() == null) return;
        String userId = auth.getCurrentUser().getUid();

        // Get dynamic FCM service push token for this Android handset
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM push registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "Device FCM Registration Token: " + token);

                    // Update the user details on security-authorized Firestore document
                    Map<String, Object> tokenMap = new HashMap<>();
                    tokenMap.put("fcmToken", token);

                    db.collection("users").document(userId)
                            .update(tokenMap)
                            .addOnFailureListener(e -> {
                                // Create user profile document with the token if it doesn't exist yet
                                db.collection("users").document(userId).set(tokenMap, com.google.firebase.firestore.SetOptions.merge());
                            });
                });
    }

    private void setupNotificationsListener() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please log in to view notifications", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Snapshot listener synchronizes lists in real-time, order descending by relative timestamp
        notificationsListener = db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Realtime Firestore subscription listen failed: ", error);
                        // Safe offline fallback
                        loadNotificationsFallback(userId);
                        return;
                    }

                    if (querySnapshots != null) {
                        notificationList.clear();

                        for (DocumentSnapshot doc : querySnapshots.getDocuments()) {
                            ModelNotification notification = doc.toObject(ModelNotification.class);
                            if (notification != null) {
                                notification.setId(doc.getId()); // Retain documents' Firestore Key
                                notificationList.add(notification);

                                // Automatically mark notifications read once opened in app
                                if (!notification.isRead()) {
                                    doc.getReference().update("read", true);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadNotificationsFallback(String userId) {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    notificationList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        ModelNotification notification = doc.toObject(ModelNotification.class);
                        if (notification != null) {
                            notification.setId(doc.getId());
                            notificationList.add(notification);

                            if (!notification.isRead()) {
                                doc.getReference().update("read", true);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Crucial callback step: unregister the stream event listener to clean memory and reject leaks
        if (notificationsListener != null) {
            notificationsListener.remove();
        }
    }
}
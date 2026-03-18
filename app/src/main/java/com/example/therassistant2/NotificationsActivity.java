package com.example.therassistant2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<ModelNotification> notificationList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        notificationList = new ArrayList<>();
        adapter = new NotificationsAdapter(notificationList);

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadNotifications();
    }

    private void loadNotifications() {

        String userId = auth.getCurrentUser().getUid();

        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {

                    notificationList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().update("read", true);
                    }

                    for (DocumentSnapshot doc : query.getDocuments()) {

                        ModelNotification notification =
                                doc.toObject(ModelNotification.class);

                        notificationList.add(notification);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

}
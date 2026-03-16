package com.example.therassistant2;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BookingRequests extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingRequestsAdapter adapter;
    private List<ModelBookingRequests> requestList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_requests);

        recyclerView = findViewById(R.id.requestsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new BookingRequestsAdapter(requestList, this);

        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        loadRequests();
    }

    private void loadRequests() {

        String therapistId = auth.getCurrentUser().getUid();

        db.collection("bookingRequests")
                .whereEqualTo("therapistId", therapistId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    requestList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        ModelBookingRequests request = doc.toObject(ModelBookingRequests.class);

                        if (request != null) {
                            request.setRequestId(doc.getId());
                            requestList.add(request);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load requests",
                                Toast.LENGTH_SHORT).show());
    }
}

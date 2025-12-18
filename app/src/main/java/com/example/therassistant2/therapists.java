package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class therapists extends AppCompatActivity {
    private static final String TAG = "therapists";
    private RecyclerView recyclerView;
    private TherapistAdapter adapter;
    private List<Therapist> therapistList;
    private DatabaseReference therapistsRef;
    private ImageView messageIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapists);

        recyclerView = findViewById(R.id.therapistsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        therapistList = new ArrayList<>();
        adapter = new TherapistAdapter(therapistList, this);
        recyclerView.setAdapter(adapter);

        therapistsRef = FirebaseDatabase.getInstance().getReference("therapists");

        therapistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                therapistList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Therapist therapist = postSnapshot.getValue(Therapist.class);
                    if (therapist != null) {
                        // Using setters to ensure data is set correctly

                        String uid = postSnapshot.getKey();
                        therapist.setUid(uid);

                        therapist.setName(therapist.getFirstName());
                        therapist.settherapisttype(therapist.gettherapisttype());
                        therapist.setPhoneNumber(therapist.getPhoneNumber());
                        therapist.setAvailability(therapist.getAvailability());
                        therapist.setProfileImageUrl(therapist.getProfileImageUrl());
                        therapist.setFirstName(therapist.getFirstName());
                        therapist.setLastName(therapist.getLastName());
                        therapist.setEmail(therapist.getEmail());


                        // Log the therapist information for debugging
                        Log.d(TAG, "Therapist: " + therapist.toString());
                        therapistList.add(therapist);
                    } else {
                        Log.e(TAG, "Therapist data is null");
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(therapists.this, "Failed to load therapists", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle click events for therapist items
        adapter.setOnItemClickListener(new TherapistAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Therapist therapist) {
                Intent intent = new Intent(therapists.this, TherapistProfile.class);
                intent.putExtra("therapist", therapist);
                startActivity(intent);
            }
            @Override
            public void onMessageClick(Therapist therapist) {
                Intent intent = new Intent(therapists.this, messaging.class);
                intent.putExtra("recipientId", therapist.getUid());
                intent.putExtra("recipientName", therapist.getFirstName() + " " + therapist.getLastName());
                startActivity(intent);
            }
        });


    }
}

package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private CardAdapter adapter;
    private List<Therapist> therapistList;

    private DatabaseReference therapistsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapists);

        recyclerView = findViewById(R.id.cardStackRecycler);

        therapistList = new ArrayList<>();
        adapter = new CardAdapter(this, therapistList);

        // CUSTOM STACK LAYOUT (overlapping cards)
        recyclerView.setLayoutManager(new RecyclerView.LayoutManager() {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.MATCH_PARENT
                );
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }

            @Override
            public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
                detachAndScrapAttachedViews(recycler);

                int itemCount = Math.min(getItemCount(), 3);

                for (int i = itemCount - 1; i >= 0; i--) {
                    View view = recycler.getViewForPosition(i);
                    addView(view);

                    measureChildWithMargins(view, 0, 0);
                    layoutDecorated(view, 0, 0, getWidth(), getHeight());
                }
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        therapistsRef = FirebaseDatabase.getInstance().getReference("therapists");

        loadTherapistsFromFirebase();
        attachItemTouchHelper();
    }

    private void loadTherapistsFromFirebase() {
        therapistsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                therapistList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    Therapist therapist = postSnapshot.getValue(Therapist.class);

                    if (therapist != null) {
                        String uid = postSnapshot.getKey();
                        therapist.setUid(uid);

                        // OPTIONAL: If your Therapist has firstName + lastName in Firebase,
                        // you can create a display name here if needed
                        if (therapist.getFirstName() != null && therapist.getLastName() != null) {
                            therapist.setName(therapist.getFirstName() + " " + therapist.getLastName());
                        }

                        Log.d(TAG, "Loaded therapist: " + therapist.getFirstName() + " " + therapist.getLastName());
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
    }

    private void attachItemTouchHelper() {

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.UP) {

                    @Override
                    public boolean onMove(
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                        int position = viewHolder.getAdapterPosition();
                        if (position == RecyclerView.NO_POSITION) return;

                        Therapist therapist = therapistList.get(position);

                        if (direction == ItemTouchHelper.LEFT) {
                            // SKIP
                            therapistList.remove(position);
                            adapter.notifyDataSetChanged();

                        } else if (direction == ItemTouchHelper.UP) {
                            // ACCEPT → messaging
                            openMessaging(therapist);

                            therapistList.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void openMessaging(Therapist therapist) {

        Intent intent = new Intent(therapists.this, messaging.class);

        // These match what your old code used:
        intent.putExtra("recipientId", therapist.getUid());
        intent.putExtra("recipientName",
                therapist.getFirstName() + " " + therapist.getLastName());

        // Extra useful data if you want:
        intent.putExtra("therapistType", therapist.gettherapisttype());
        intent.putExtra("profileImageUrl", therapist.getProfileImageUrl());

        startActivity(intent);
    }
}

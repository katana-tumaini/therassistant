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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                if (getItemCount() == 0) {
                    detachAndScrapAttachedViews(recycler);
                    return;
                }
                
                detachAndScrapAttachedViews(recycler);

                int itemCount = Math.min(getItemCount(), 3);
                int width = getWidth();
                int height = getHeight();

                for (int i = itemCount - 1; i >= 0; i--) {
                    View view = recycler.getViewForPosition(i);
                    addView(view);

                    measureChildWithMargins(view, 0, 0);
                    
                    // Center the cards with proper margins
                    int left = (width - getDecoratedMeasuredWidth(view)) / 2;
                    int top = (height - getDecoratedMeasuredHeight(view)) / 2;
                    
                    layoutDecorated(view, left, top, 
                            left + getDecoratedMeasuredWidth(view), 
                            top + getDecoratedMeasuredHeight(view));
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
                            // SAVE to Interested In list
                            saveToInterestedList(therapist);

                            therapistList.remove(position);
                            adapter.notifyDataSetChanged();
                        }
                    }
                };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void saveToInterestedList(Therapist therapist) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (therapist.getUid() == null) {
            Toast.makeText(this, "Could not save therapist", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference interestRef = FirebaseDatabase.getInstance()
                .getReference("interests")
                .child(user.getUid())
                .child(therapist.getUid());

        Map<String, Object> values = new HashMap<>();
        values.put("uid", therapist.getUid());
        values.put("firstName", therapist.getFirstName());
        values.put("lastName", therapist.getLastName());
        values.put("name", therapist.getName());
        values.put("therapistType", therapist.gettherapisttype());
        values.put("availability", therapist.getAvailability());
        values.put("profileImageUrl", therapist.getProfileImageUrl());
        values.put("timestamp", System.currentTimeMillis());

        interestRef.setValue(values)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(therapists.this,
                                "Added to Interested In",
                                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(therapists.this,
                                "Failed to save therapist",
                                Toast.LENGTH_SHORT).show());
    }
}

package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class InterestedTherapistsActivity extends AppCompatActivity {

    private static final String TAG = "InterestedTherapists";

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private InterestedTherapistAdapter adapter;
    private List<Therapist> interestedList;

    private DatabaseReference interestsRef;
    private ValueEventListener interestsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interested_therapists);

        recyclerView = findViewById(R.id.recyclerInterestedTherapists);
        emptyStateText = findViewById(R.id.emptyInterestedText);

        interestedList = new ArrayList<>();
        adapter = new InterestedTherapistAdapter(interestedList, this);
        adapter.setOnTherapistClickListener(therapist -> openBookingActivity(therapist));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        interestsRef = FirebaseDatabase.getInstance()
                .getReference("interests")
                .child(user.getUid());

        attachSwipeToDelete();
        loadInterestedTherapists();
    }

    private void loadInterestedTherapists() {
        interestsListener = interestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                interestedList.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Therapist therapist = child.getValue(Therapist.class);
                    if (therapist != null) {
                        if (therapist.getUid() == null) {
                            therapist.setUid(child.getKey());
                        }
                        if (therapist.getFirstName() != null && therapist.getLastName() != null) {
                            therapist.setName(therapist.getFirstName() + " " + therapist.getLastName());
                        }
                        interestedList.add(therapist);
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load interested therapists", error.toException());
                Toast.makeText(InterestedTherapistsActivity.this,
                        "Failed to load list", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (interestedList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openBookingActivity(Therapist therapist) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra("therapistId", therapist.getUid());
        intent.putExtra("therapistName", buildDisplayName(therapist));
        intent.putExtra("therapistEmail", therapist.getEmail());
        intent.putExtra("availability", therapist.getAvailability());
        startActivity(intent);
    }

    private String buildDisplayName(Therapist therapist) {
        String name = "";
        if (therapist.getFirstName() != null && therapist.getLastName() != null) {
            name = therapist.getFirstName() + " " + therapist.getLastName();
        }
        if (name.trim().isEmpty()) {
            name = therapist.getName();
        }
        if (name == null || name.trim().isEmpty()) {
            name = "Therapist";
        }
        return name.trim();
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                Therapist therapist = interestedList.get(position);
                if (therapist.getUid() != null) {
                    interestsRef.child(therapist.getUid()).removeValue();
                }
                interestedList.remove(position);
                adapter.notifyItemRemoved(position);
                updateEmptyState();
            }
        };

        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (interestsRef != null && interestsListener != null) {
            interestsRef.removeEventListener(interestsListener);
        }
    }
}

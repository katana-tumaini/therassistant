package com.example.therassistant2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.CalendarView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;

public class calendar extends AppCompatActivity {

    private CalendarView calendarView;
    private HashSet<String> occupiedDays;
    private boolean isTherapist;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        occupiedDays = new HashSet<>();
        isTherapist = getIntent().getBooleanExtra("isTherapist", false);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("sessions");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String date = year + "-" + (month + 1) + "-" + dayOfMonth;
                if (occupiedDays.contains(date)) {
                    showOptionsDialog(date);
                } else {
                    if (isTherapist) {
                        addSession(date);
                    } else {
                        Toast.makeText(calendar.this, "This day is free.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        loadOccupiedDays();
    }

    private void loadOccupiedDays() {
        String userId = mAuth.getCurrentUser().getUid();

        dbRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot sessionSnapshot : dataSnapshot.getChildren()) {
                    String date = sessionSnapshot.child("date").getValue(String.class);
                    occupiedDays.add(date);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(calendar.this, "Failed to load occupied days", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSession(String date) {
        String userId = mAuth.getCurrentUser().getUid();
        String sessionDetails = "Therapy session"; // You can modify this to get details from the user

        HashMap<String, Object> sessionData = new HashMap<>();
        sessionData.put("date", date);
        sessionData.put("details", sessionDetails);
        sessionData.put("userId", userId);

        dbRef.push().setValue(sessionData)
                .addOnSuccessListener(aVoid -> {
                    occupiedDays.add(date);
                    Toast.makeText(this, "Session added on " + date, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSession(String date) {
        String userId = mAuth.getCurrentUser().getUid();

        dbRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot sessionSnapshot : dataSnapshot.getChildren()) {
                    String sessionDate = sessionSnapshot.child("date").getValue(String.class);
                    if (sessionDate.equals(date)) {
                        sessionSnapshot.getRef().removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    occupiedDays.remove(date);
                                    Toast.makeText(calendar.this, "Session deleted on " + date, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(calendar.this, "Failed to delete session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(calendar.this, "Failed to find session to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOptionsDialog(final String date) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options")
                .setItems(new CharSequence[]{"Delete Session"}, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                deleteSession(date);
                                break;
                        }
                    }
                });
        builder.create().show();
    }
}
package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpcomingSessionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SessionAdapter sessionAdapter;
    private List<Session> sessionList;
    private String currentUserEmail;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_sessions);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sessionList = new ArrayList<>();
        sessionAdapter = new SessionAdapter(sessionList);
        recyclerView.setAdapter(sessionAdapter);

        // Get the current user's email and name
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            currentUserEmail = user.getEmail();
            currentUserName = user.getDisplayName(); // Assuming the display name is used for the therapist's name
            Log.d("UpcomingSessionsActivity", "Current User Email: " + currentUserEmail);
            Log.d("UpcomingSessionsActivity", "Current User Name: " + currentUserName);
            fetchSessionsFromDatabase();
        } else {
            Log.e("UpcomingSessionsActivity", "User is not logged in.");
            redirectToLogin();
        }
    }

    private void fetchSessionsFromDatabase() {
        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        sessionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                sessionList.clear();
                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {

                    String date = sessionSnapshot.child("date").getValue(String.class);
                    String time = sessionSnapshot.child("time").getValue(String.class);
                    String details = sessionSnapshot.child("details").getValue(String.class);
                    String clientEmail = sessionSnapshot.child("clientEmail").getValue(String.class);
                    String clientFirstName = sessionSnapshot.child("clientFirstName").getValue(String.class);
                    String clientLastName = sessionSnapshot.child("clientLastName").getValue(String.class);
                    String therapistName = sessionSnapshot.child("therapistName").getValue(String.class);
                    String therapistEmail = sessionSnapshot.child("therapistEmail").getValue(String.class); // Added therapistEmail field

                    // Only create session if the essential fields are non-null
                    Session session = new Session();
                    session.setDate(date);
                    session.setTime(time);
                    session.setDetails(details != null ? details : ""); // Use an empty string if details are null
                    session.setClientEmail(clientEmail != null ? clientEmail : "");
                    session.setClientFirstName(clientFirstName != null ? clientFirstName : "");
                    session.setClientLastName(clientLastName != null ? clientLastName : "");
                    session.setTherapistName(therapistName != null ? therapistName : "");
                    session.setTherapistEmail(therapistEmail != null ? therapistEmail : ""); // Set therapistEmail

                    // Check if the session is relevant to the current user
                    if (Objects.equals(therapistEmail, currentUserEmail)) {
                        sessionList.add(session);
                    } else if (Objects.equals(clientEmail, currentUserEmail)) {
                        sessionList.add(session);
                    }

                    Log.d("UpcomingSessionsActivity", String.valueOf(sessionList.size()));
                }
                sessionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UpcomingSessionsActivity", "Failed to read sessions.", error.toException());
            }
        });
    }

    private void redirectToLogin() {
        // Redirect to login activity or show an error message
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

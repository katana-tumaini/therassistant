package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class TherapistHome extends AppCompatActivity {

    private CardView clientsButton, BookingRequests, addSessionButton, upcomingSessionsButton;
    private ImageView profileCircle, notificationBell;
    private TextView notificationBadge;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_home);

        clientsButton = findViewById(R.id.clientsCard);
        BookingRequests = findViewById(R.id.bookingRequestsCard);
        addSessionButton = findViewById(R.id.addSessionCard);
        upcomingSessionsButton = findViewById(R.id.upcomingSessionsCard);
        profileCircle = findViewById(R.id.profileCircle);
        notificationBell = findViewById(R.id.notificationBell);
        notificationBadge = findViewById(R.id.notificationBadge);



        clientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, ClientActivity.class);
                startActivity(intent);
            }
        });

        BookingRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, BookingRequests.class);
                startActivity(intent);
            }
        });

        addSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, AddSession.class);
                startActivity(intent);
            }
        });

        upcomingSessionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, UpcomingSessionsActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton messageButton = findViewById(R.id.messageButton);
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, messaging.class);
                startActivity(intent);
            }
        });

        profileCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileOptions();
            }
        });
    }

    private void loadNotificationCount() {

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("read", false)
                .get()
                .addOnSuccessListener(query -> {

                    int count = query.size();

                    if (count > 0) {
                        notificationBadge.setText(count > 9 ? "9+" : String.valueOf(count));
                        notificationBadge.setVisibility(View.VISIBLE);
                    } else {
                        notificationBadge.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotificationCount();
    }
    private void showProfileOptions() {
        String[] options = {"Settings", "Logout"};
        new AlertDialog.Builder(this)
                .setTitle("Profile Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Handle Settings click
                        openSettings();
                    } else if (which == 1) {
                        // Handle Logout click
                        logout();
                    }
                })
                .show();
    }

    private void openSettings() {
        Intent intent = new Intent(TherapistHome.this, settings.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(TherapistHome.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

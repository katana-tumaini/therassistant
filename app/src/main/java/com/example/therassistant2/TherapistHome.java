package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TherapistHome extends AppCompatActivity {

    private ImageButton clientsButton, calendarButton, addSessionButton, upcomingSessionsButton;
    private ImageView profileCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_home);

        clientsButton = findViewById(R.id.clientsButton);
        calendarButton = findViewById(R.id.calendarButton);
        addSessionButton = findViewById(R.id.addSessionButton);
        upcomingSessionsButton = findViewById(R.id.upcomingSessionsButton);
        profileCircle = findViewById(R.id.profileCircle);

        clientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, ClientActivity.class);
                startActivity(intent);
            }
        });

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistHome.this, calendar.class);
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

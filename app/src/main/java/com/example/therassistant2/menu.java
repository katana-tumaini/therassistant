package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class menu extends AppCompatActivity {

    private ImageView profileCircle;
    private ImageButton therapistsButton;
    private ImageButton calendarButton;
    private ImageButton upcomingSessionsButton; // New button declaration
    private FloatingActionButton messageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        profileCircle = findViewById(R.id.profileCircle);
        therapistsButton = findViewById(R.id.therapistsButton);
        calendarButton = findViewById(R.id.calendarButton);
        upcomingSessionsButton = findViewById(R.id.upcomingSessionsButton); // Initialize new button
        messageButton = findViewById(R.id.messageButton);

        profileCircle.setOnClickListener(v -> showProfileOptions());
        therapistsButton.setOnClickListener(v -> openTherapistsActivity());
        calendarButton.setOnClickListener(v -> openCalendarActivity());
        upcomingSessionsButton.setOnClickListener(v -> openUpcomingSessionsActivity()); // Set click listener
        messageButton.setOnClickListener(v -> openMessagesActivity());
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

    private void openTherapistsActivity() {
        Intent intent = new Intent(this, therapists.class);
        startActivity(intent);
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(this, calendar.class);
        startActivity(intent);
    }

    private void openMessagesActivity() {
        Intent intent = new Intent(this, ChatListActivity.class);
        startActivity(intent);
    }

    private void openUpcomingSessionsActivity() {
        Intent intent = new Intent(this, UpcomingSessionsActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, settings.class);
        startActivity(intent);
    }

    private void logout() {
        Intent intent = new Intent(menu.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

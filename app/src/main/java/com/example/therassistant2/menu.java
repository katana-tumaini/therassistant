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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class menu extends AppCompatActivity {

    private ImageView profileCircle;
    private LinearLayout therapistsButton;
    private LinearLayout calendarButton;
    private LinearLayout upcomingSessionsButton;
    private FloatingActionButton messageButton;
    private TextView welcomeText;
    private FirebaseAuth auth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        profileCircle = findViewById(R.id.profileCircle);
        therapistsButton = findViewById(R.id.therapistsButton);
        calendarButton = findViewById(R.id.calendarButton);
        upcomingSessionsButton = findViewById(R.id.upcomingSessionsButton);
        messageButton = findViewById(R.id.messageButton);
        welcomeText = findViewById(R.id.welcomeText);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setGreetingWithUserName();
        profileCircle.setOnClickListener(v -> showProfileOptions());
        therapistsButton.setOnClickListener(v -> openTherapistsActivity());
        calendarButton.setOnClickListener(v -> openCalendarActivity());
        upcomingSessionsButton.setOnClickListener(v -> openUpcomingSessionsActivity());
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

    private void setGreetingWithUserName() {

        String greeting = getGreetingMessage();

        if (auth.getCurrentUser() == null) {
            welcomeText.setText(greeting);
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {

                    String firstName = documentSnapshot.getString("firstName");

                    if (firstName == null || firstName.trim().isEmpty()) {
                        welcomeText.setText(greeting);
                    } else {
                        welcomeText.setText(greeting + ", " + firstName) ;
                    }
                })
                .addOnFailureListener(e -> {
                    welcomeText.setText(greeting );
                });
    }

    private String getGreetingMessage() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return "Good Morning";
        } else if (hour >= 12 && hour < 18) {
            return "Good Afternoon";
        } else if (hour >= 18 && hour < 23) {
            return "Good Evening";
        } else {
            return "Good Night";
        }
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

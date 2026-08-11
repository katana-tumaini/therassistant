package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class menu extends AppCompatActivity {

    private ImageView profileCircle, notificationBell;
    private LinearLayout therapistsButton;
    private LinearLayout calendarButton;
    private FloatingActionButton messageButton;
    private TextView welcomeText, notificationBadge;
    private CardView nextSessionCard;
    private TextView nextSessionTherapist, nextSessionDateTime, nextSessionDetails, nextSessionEmpty;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private DatabaseReference sessionsRef;
    private ValueEventListener sessionsListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        profileCircle = findViewById(R.id.profileCircle);
        therapistsButton = findViewById(R.id.therapistsButton);
        calendarButton = findViewById(R.id.calendarButton);
        messageButton = findViewById(R.id.messageButton);
        welcomeText = findViewById(R.id.welcomeText);
        notificationBell = findViewById(R.id.notificationBell);
        notificationBadge = findViewById(R.id.notificationBadge);
        nextSessionCard = findViewById(R.id.nextSessionCard);
        nextSessionTherapist = findViewById(R.id.nextSessionTherapist);
        nextSessionDateTime = findViewById(R.id.nextSessionDateTime);
        nextSessionDetails = findViewById(R.id.nextSessionDetails);
        nextSessionEmpty = findViewById(R.id.nextSessionEmpty);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setGreetingWithUserName();
        profileCircle.setOnClickListener(v -> showProfileOptions());
        therapistsButton.setOnClickListener(v -> openTherapistsActivity());
        calendarButton.setOnClickListener(v -> openCalendarActivity());
        messageButton.setOnClickListener(v -> openMessagesActivity());
        notificationBell.setOnClickListener(v -> openNotificationsActivity());
        nextSessionCard.setOnClickListener(v -> openUpcomingSessionsActivity());

        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        loadNextSession();

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
        loadNextSession();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sessionsRef != null && sessionsListener != null) {
            sessionsRef.removeEventListener(sessionsListener);
        }
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
    private void openNotificationsActivity() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }
    private void openTherapistsActivity() {
        Intent intent = new Intent(this, therapists.class);
        startActivity(intent);
    }

    private void openCalendarActivity() {
        Intent intent = new Intent(this, BookingActivity.class);
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

    private void loadNextSession() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Log.d("MENU_SESSION", "No logged-in user email, cannot load next session");
            showNoNextSession();
            return;
        }

        String currentUserEmail = user.getEmail();
        Log.d("MENU_SESSION", "Loading sessions for: " + currentUserEmail);

        if (sessionsListener != null) {
            sessionsRef.removeEventListener(sessionsListener);
        }

        sessionsListener = sessionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("MENU_SESSION", "Sessions snapshot count: " + snapshot.getChildrenCount());

                Session nextSession = null;
                Date nextDate = null;
                Session nearestPastSession = null;
                Date nearestPastDate = null;
                Date now = new Date();

                for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                    String therapistEmail = getString(sessionSnapshot, "therapistEmail");
                    String clientEmail = getString(sessionSnapshot, "clientEmail");

                    if (!currentUserEmail.equalsIgnoreCase(therapistEmail)
                            && !currentUserEmail.equalsIgnoreCase(clientEmail)) {
                        continue;
                    }

                    String date = getString(sessionSnapshot, "date");
                    String time = getString(sessionSnapshot, "time");

                    if (date.isEmpty() || time.isEmpty()) {
                        Log.d("MENU_SESSION", "Skipping session with empty date/time");
                        continue;
                    }

                    Date sessionDate = parseSessionDateTime(date, time);
                    if (sessionDate == null) {
                        Log.d("MENU_SESSION", "Could not parse date/time: " + date + " " + time);
                        continue;
                    }

                    Log.d("MENU_SESSION", "Candidate session: " + date + " " + time
                            + " | therapist=" + therapistEmail + " | client=" + clientEmail);

                    if (sessionDate.after(now)) {
                        if (nextDate == null || sessionDate.before(nextDate)) {
                            nextDate = sessionDate;
                            nextSession = buildSessionFromSnapshot(sessionSnapshot);
                            Log.d("MENU_SESSION", "New nearest upcoming session selected");
                        }
                    } else {
                        if (nearestPastDate == null || sessionDate.after(nearestPastDate)) {
                            nearestPastDate = sessionDate;
                            nearestPastSession = buildSessionFromSnapshot(sessionSnapshot);
                        }
                    }
                }

                Session sessionToDisplay = nextSession != null ? nextSession : nearestPastSession;
                Log.d("MENU_SESSION", "Final session to display: " + sessionToDisplay);
                runOnUiThread(() -> displayNextSession(sessionToDisplay));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MENU_SESSION", "Session load cancelled: " + error.getMessage());
                showNoNextSession();
            }
        });
    }

    private Date parseSessionDateTime(String date, String time) {
        String[] patterns = {
                "d/M/yyyy HH:mm",
                "d/M/yyyy H:mm",
                "dd/MM/yyyy HH:mm",
                "dd/MM/yyyy H:mm"
        };

        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.US).parse(date.trim() + " " + time.trim());
            } catch (ParseException ignored) {
            }
        }

        return null;
    }

    private Session buildSessionFromSnapshot(DataSnapshot sessionSnapshot) {
        Session session = new Session();
        session.setDate(getString(sessionSnapshot, "date"));
        session.setTime(getString(sessionSnapshot, "time"));
        session.setDetails(getString(sessionSnapshot, "details"));
        session.setClientEmail(getString(sessionSnapshot, "clientEmail"));
        session.setClientFirstName(getString(sessionSnapshot, "clientFirstName"));
        session.setClientLastName(getString(sessionSnapshot, "clientLastName"));
        session.setTherapistName(getString(sessionSnapshot, "therapistName"));
        session.setTherapistEmail(getString(sessionSnapshot, "therapistEmail"));
        return session;
    }

    private String getString(DataSnapshot snapshot, String key) {
        String value = snapshot.child(key).getValue(String.class);
        return value == null ? "" : value.trim();
    }

    private void displayNextSession(Session session) {
        if (session == null) {
            showNoNextSession();
            return;
        }

        nextSessionEmpty.setVisibility(View.GONE);
        nextSessionTherapist.setVisibility(View.VISIBLE);
        nextSessionDateTime.setVisibility(View.VISIBLE);
        nextSessionDetails.setVisibility(View.VISIBLE);

        nextSessionTherapist.setText(session.getTherapistName());
        nextSessionDateTime.setText(session.getDate() + " at " + session.getTime());
        nextSessionDetails.setText(session.getDetails());
    }

    private void showNoNextSession() {
        nextSessionEmpty.setVisibility(View.VISIBLE);
        nextSessionTherapist.setVisibility(View.GONE);
        nextSessionDateTime.setVisibility(View.GONE);
        nextSessionDetails.setVisibility(View.GONE);
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

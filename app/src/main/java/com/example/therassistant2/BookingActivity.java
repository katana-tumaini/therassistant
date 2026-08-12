package com.example.therassistant2;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView availabilityText, therapistNameHeader;
    private FloatingActionButton bookNow;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private DatabaseReference sessionsRef;
    private DatabaseReference clientsRef;
    private DatabaseReference therapistsRef;

    private String therapistId;
    private String therapistName;
    private String therapistEmail;
    private CalendarDay selectedDate;

    private final int SESSION_LIMIT = 2;

    // therapist availability
    private final int AVAILABLE_START = 9;  // 9AM
    private final int AVAILABLE_END = 17;   // 5PM

    private Map<String, Integer> bookingCountMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        calendarView = findViewById(R.id.calendarView);
        therapistNameHeader = findViewById(R.id.therapistNameHeader);
        availabilityText = findViewById(R.id.selectedDateText);
        bookNow = findViewById(R.id.bookNow);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            clientsRef = FirebaseDatabase.getInstance().getReference("clients").child(currentUser.getUid());
        }
        therapistsRef = FirebaseDatabase.getInstance().getReference("therapists");

        therapistId = getIntent().getStringExtra("therapistId");
        therapistName = getIntent().getStringExtra("therapistName");
        therapistEmail = getIntent().getStringExtra("therapistEmail");
        String availability = getIntent().getStringExtra("availability");

        if (therapistName != null) {
            therapistNameHeader.setText(therapistName);
        }

        if (availability != null) {
            availabilityText.setText("Available: " + availability);
        }

            calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = date;
            availabilityText.setText("Available: " + availability +
                        "\nSelected: " + formatDate(date));
        });

        bookNow.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(this,
                        "Select a date first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            showBookingDialog();
        });

        loadAllBookings();
    }

    // Load all bookings and color calendar
    private void loadAllBookings() {

        db.collection("bookingRequests")
                .whereEqualTo("therapistId", therapistId)
                .get()
                .addOnSuccessListener(query -> {

                    bookingCountMap.clear();
                    calendarView.removeDecorators();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String date = doc.getString("date");
                        if (date == null) continue;
                        int count = bookingCountMap.getOrDefault(date, 0);
                        bookingCountMap.put(date, count + 1);
                    }

                    applyDecorators();
                });
    }

    private void applyDecorators() {

        for (Map.Entry<String, Integer> entry : bookingCountMap.entrySet()) {

            String dateStr = entry.getKey();
            int count = entry.getValue();

            CalendarDay day = stringToCalendarDay(dateStr);

            if (count == 1) {
                calendarView.addDecorator(
                        new DateColorDecorator(day, Color.GREEN));
            } else if (count >= SESSION_LIMIT) {
                calendarView.addDecorator(
                        new DateColorDecorator(day, Color.RED));
            }
        }
    }

    private void showBookingDialog() {

        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialogue_booking, null);

        TextView dateText = dialogView.findViewById(R.id.dialogDate);
        Button timeButton = dialogView.findViewById(R.id.btnPickTime);
        RadioGroup meetingGroup = dialogView.findViewById(R.id.meetingTypeGroup);
        Button confirmButton = dialogView.findViewById(R.id.btnConfirmBooking);

        String formattedDate = formatDate(selectedDate);
        dateText.setText("Date: " + formattedDate);

        final String[] selectedTime = {""};

        timeButton.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            TimePickerDialog picker = new TimePickerDialog(this,
                    (TimePicker view, int hour, int minute) -> {

                        if (hour < AVAILABLE_START || hour >= AVAILABLE_END) {
                            Toast.makeText(this,
                                    "Select time within availability",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        selectedTime[0] = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                hour, minute);

                        timeButton.setText(selectedTime[0]);

                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);

            picker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        confirmButton.setOnClickListener(v -> {

            if (selectedTime[0].isEmpty()) {
                Toast.makeText(this,
                        "Pick a time",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int meetingId = meetingGroup.getCheckedRadioButtonId();
            if (meetingId == -1) {
                Toast.makeText(this,
                        "Select meeting type",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String meetingType = meetingId ==
                    R.id.radioPhysical ? "Physical" : "Virtual";

            checkAndBook(formattedDate, selectedTime[0], meetingType);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void checkAndBook(String requestDate, String time, String meetingType) {

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (therapistId == null) {
            Toast.makeText(this, "Therapist not selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Select a date first", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentCount = bookingCountMap.getOrDefault(requestDate, 0);

        if (currentCount >= SESSION_LIMIT) {
            Toast.makeText(this,
                    "Fully booked for this day",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String sessionDate = formatDateForSession(selectedDate);
        fetchDetailsAndSave(requestDate, sessionDate, time, meetingType);
    }

    private void fetchDetailsAndSave(String requestDate, String sessionDate, String time, String meetingType) {

        Runnable fetchClientAndSave = () -> {
            if (clientsRef == null) {
                Toast.makeText(BookingActivity.this, "Client data not available", Toast.LENGTH_SHORT).show();
                return;
            }

            clientsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    FirebaseUser user = auth.getCurrentUser();
                    String clientEmail = user != null ? user.getEmail() : "";
                    String clientFirstName = "";
                    String clientLastName = "";

                    if (snapshot.exists()) {
                        String email = snapshot.child("email").getValue(String.class);
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);

                        if (email != null) clientEmail = email;
                        if (firstName != null) clientFirstName = firstName;
                        if (lastName != null) clientLastName = lastName;
                    }

                    saveBookingAndSession(requestDate, sessionDate, time, meetingType,
                            clientEmail, clientFirstName, clientLastName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(BookingActivity.this,
                            "Failed to load client details",
                            Toast.LENGTH_SHORT).show();
                }
            });
        };

        if (therapistEmail == null || therapistEmail.isEmpty()) {
            therapistsRef.child(therapistId).child("email")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            therapistEmail = snapshot.getValue(String.class);
                            if (therapistEmail == null) therapistEmail = "";
                            fetchClientAndSave.run();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(BookingActivity.this,
                                    "Failed to load therapist details",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            fetchClientAndSave.run();
        }
    }

    private void saveBookingAndSession(String requestDate, String sessionDate, String time,
                                       String meetingType, String clientEmail,
                                       String clientFirstName, String clientLastName) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String displayTherapistName = therapistName != null ? therapistName : therapistNameHeader.getText().toString();
        String displayTherapistEmail = therapistEmail != null ? therapistEmail : "";

        String sessionId = sessionsRef.push().getKey();

        Map<String, Object> session = new HashMap<>();
        session.put("date", sessionDate);
        session.put("time", time);
        session.put("details", meetingType);
        session.put("clientEmail", clientEmail);
        session.put("clientFirstName", clientFirstName);
        session.put("clientLastName", clientLastName);
        session.put("therapistName", displayTherapistName);
        session.put("therapistEmail", displayTherapistEmail);

        Map<String, Object> booking = new HashMap<>();
        booking.put("therapistId", therapistId);
        booking.put("therapistName", displayTherapistName);
        booking.put("therapistEmail", displayTherapistEmail);
        booking.put("clientId", user.getUid());
        booking.put("clientName", (clientFirstName + " " + clientLastName).trim());
        booking.put("clientEmail", clientEmail);
        booking.put("clientFirstName", clientFirstName);
        booking.put("clientLastName", clientLastName);
        booking.put("date", requestDate);
        booking.put("time", time);
        booking.put("meetingType", meetingType);
        booking.put("status", "pending");
        booking.put("sessionId", sessionId);
        booking.put("timestamp", System.currentTimeMillis());

        if (sessionId == null) {
            db.collection("bookingRequests")
                    .add(booking)
                    .addOnSuccessListener(doc -> {
                        Toast.makeText(BookingActivity.this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                        loadAllBookings();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(BookingActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show());
            return;
        }

        sessionsRef.child(sessionId).setValue(session)
                .addOnSuccessListener(aVoid -> db.collection("bookingRequests")
                        .add(booking)
                        .addOnSuccessListener(doc -> {
                            Toast.makeText(BookingActivity.this, "Booking request sent!", Toast.LENGTH_SHORT).show();
                            loadAllBookings();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(BookingActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(BookingActivity.this, "Failed to save session", Toast.LENGTH_SHORT).show());
    }

    private String formatDate(CalendarDay date) {
        return String.format(Locale.getDefault(),
                "%04d-%02d-%02d",
                date.getYear(),
                date.getMonth(),
                date.getDay());
    }

    private String formatDateForSession(CalendarDay date) {
        return String.format(Locale.getDefault(),
                "%d/%d/%d",
                date.getDay(),
                date.getMonth(),
                date.getYear());
    }

    private CalendarDay stringToCalendarDay(String dateStr) {

        String[] parts = dateStr.split("-");

        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);

        return CalendarDay.from(year, month, day);
    }

    // Decorator
    public static class DateColorDecorator implements DayViewDecorator {

        private final CalendarDay date;
        private final int color;

        public DateColorDecorator(CalendarDay date, int color) {
            this.date = date;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(date);
        }

        @Override
        public void decorate(@NonNull DayViewFacade view) {
            view.setBackgroundDrawable(new ColorDrawable(color));
        }
    }
}
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
    private TextView availabilityText;
    private FloatingActionButton bookNow;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String therapistId;
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
        availabilityText = findViewById(R.id.selectedDateText);
        bookNow = findViewById(R.id.bookNow);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        therapistId = getIntent().getStringExtra("therapistId");

        availabilityText.setText("Available: 9:00 AM - 5:00 PM");

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            selectedDate = date;
            availabilityText.setText("Available: 9:00 AM - 5:00 PM\nSelected: "
                    + formatDate(date));
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

        db.collection("bookings")
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

    private void checkAndBook(String date, String time, String meetingType) {

        int currentCount = bookingCountMap.getOrDefault(date, 0);

        if (currentCount >= SESSION_LIMIT) {
            Toast.makeText(this,
                    "Fully booked for this day",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> booking = new HashMap<>();
        booking.put("therapistId", therapistId);
        booking.put("userId", auth.getCurrentUser().getUid());
        booking.put("date", date);
        booking.put("time", time);
        booking.put("meetingType", meetingType);
        booking.put("timestamp", System.currentTimeMillis());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(this,
                            "Session booked!",
                            Toast.LENGTH_SHORT).show();

                    loadAllBookings();
                });
    }

    private String formatDate(CalendarDay date) {
        return String.format(Locale.getDefault(),
                "%04d-%02d-%02d",
                date.getYear(),
                date.getMonth(),
                date.getDay());
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
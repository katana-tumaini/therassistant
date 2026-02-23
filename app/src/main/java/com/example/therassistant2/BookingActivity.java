package com.example.therassistant2;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView selectedDateText;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String therapistId;
    private final int SESSION_LIMIT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        calendarView = findViewById(R.id.materialCalendar);
        selectedDateText = findViewById(R.id.selectedDateText);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        therapistId = getIntent().getStringExtra("therapistId");

        calendarView.setOnDateChangedListener((widget, date, selected) -> {

            String formattedDate = formatDate(date);
            selectedDateText.setText("Selected: " + formattedDate);

            checkAvailabilityAndBook(date, formattedDate);
        });
    }

    private void checkAvailabilityAndBook(CalendarDay date, String formattedDate) {

        db.collection("bookings")
                .whereEqualTo("therapistId", therapistId)
                .whereEqualTo("date", formattedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int bookingCount = queryDocumentSnapshots.size();

                    if (bookingCount >= SESSION_LIMIT) {

                        calendarView.addDecorator(
                                new DateColorDecorator(date, Color.RED)
                        );

                        Toast.makeText(this,
                                "Fully booked for this date",
                                Toast.LENGTH_SHORT).show();

                    } else {

                        calendarView.addDecorator(
                                new DateColorDecorator(date, Color.GREEN)
                        );

                        bookSession(formattedDate);
                    }
                });
    }

    private void bookSession(String formattedDate) {

        Map<String, Object> booking = new HashMap<>();
        booking.put("therapistId", therapistId);
        booking.put("userId", auth.getCurrentUser().getUid());
        booking.put("date", formattedDate);
        booking.put("timestamp", System.currentTimeMillis());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(doc -> Toast.makeText(this,
                        "Session booked!",
                        Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        "Booking failed",
                        Toast.LENGTH_SHORT).show());
    }

    private String formatDate(CalendarDay date) {
        return String.format(Locale.getDefault(),
                "%04d-%02d-%02d",
                date.getYear(),
                date.getMonth(),
                date.getDay());
    }

    // decorator
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
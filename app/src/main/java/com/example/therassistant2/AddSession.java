package com.example.therassistant2;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddSession extends AppCompatActivity {

    private EditText sessionDateEditText, sessionTimeEditText, sessionDetailsEditText, clientEmailEditText, clientFirstNameEditText, clientLastNameEditText;
    private Button saveSessionButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_session);

        databaseReference = FirebaseDatabase.getInstance().getReference("sessions");
        mAuth = FirebaseAuth.getInstance();

        sessionDateEditText = findViewById(R.id.sessionDateEditText);
        sessionTimeEditText = findViewById(R.id.sessionTimeEditText);
        sessionDetailsEditText = findViewById(R.id.sessionDetailsEditText);
        clientEmailEditText = findViewById(R.id.clientEmailEditText);
        clientFirstNameEditText = findViewById(R.id.clientFirstNameEditText);
        clientLastNameEditText = findViewById(R.id.clientLastNameEditText);
        saveSessionButton = findViewById(R.id.saveSessionButton);

        calendar = Calendar.getInstance();

        sessionDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AddSession.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                sessionDateEditText.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        sessionTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddSession.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                sessionTimeEditText.setText(String.format("%02d:%02d", hourOfDay, minute));
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        saveSessionButton.setOnClickListener(v -> {
            String date = sessionDateEditText.getText().toString();
            String time = sessionTimeEditText.getText().toString();
            String details = sessionDetailsEditText.getText().toString();
            String clientEmail = clientEmailEditText.getText().toString();
            String clientFirstName = clientFirstNameEditText.getText().toString();
            String clientLastName = clientLastNameEditText.getText().toString();

            if (date.isEmpty() || time.isEmpty() || details.isEmpty() || clientEmail.isEmpty() || clientFirstName.isEmpty() || clientLastName.isEmpty()) {
                Toast.makeText(AddSession.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                fetchTherapistDetailsAndSaveSession(date, time, details, clientEmail, clientFirstName, clientLastName);
            }
        });
    }

    private void fetchTherapistDetailsAndSaveSession(String date, String time, String details, String clientEmail, String clientFirstName, String clientLastName) {
        String therapistUid = mAuth.getCurrentUser().getUid();
        DatabaseReference therapistRef = FirebaseDatabase.getInstance().getReference("therapists").child(therapistUid);

        therapistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String therapistFirstName = dataSnapshot.child("firstName").getValue(String.class);
                    String therapistLastName = dataSnapshot.child("lastName").getValue(String.class);
                    String therapistEmail = dataSnapshot.child("email").getValue(String.class); // Fetch the therapist's email
                    String therapistName = therapistFirstName + " " + therapistLastName;
                    saveSessionToDatabase(date, time, details, clientEmail, clientFirstName, clientLastName, therapistName, therapistEmail);
                } else {
                    Toast.makeText(AddSession.this, "Therapist not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddSession.this, "Failed to fetch therapist: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSessionToDatabase(String date, String time, String details, String clientEmail, String clientFirstName, String clientLastName, String therapistName, String therapistEmail) {
        String sessionId = databaseReference.push().getKey();
        Map<String, Object> session = new HashMap<>();
        session.put("date", date);
        session.put("time", time);
        session.put("details", details);
        session.put("therapistName", therapistName);  // Save therapistName
        session.put("therapistEmail", therapistEmail); // Save therapistEmail
        session.put("clientEmail", clientEmail);
        session.put("clientFirstName", clientFirstName);
        session.put("clientLastName", clientLastName);

        if (sessionId != null) {
            databaseReference.child(sessionId).setValue(session)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddSession.this, "Session added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddSession.this, "Failed to add session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

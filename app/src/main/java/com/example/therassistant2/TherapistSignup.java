package com.example.therassistant2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class TherapistSignup extends AppCompatActivity {
    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextConfirmPassword, editTextTherapistType, editTextAvailability, editTextPhoneNumber;
    Button buttonSignup;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_signup);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextTherapistType = findViewById(R.id.editTextTherapistType);
        editTextAvailability = findViewById(R.id.editTextAvailability);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String therapistType = editTextTherapistType.getText().toString().trim();
                String availability = editTextAvailability.getText().toString().trim();
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || therapistType.isEmpty() || availability.isEmpty() || phoneNumber.isEmpty()) {
                    Toast.makeText(TherapistSignup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(TherapistSignup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(TherapistSignup.this, task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(firstName + " " + lastName)
                                            .build();
                                    user.updateProfile(profileUpdates).addOnCompleteListener(profileUpdateTask -> {
                                        if (profileUpdateTask.isSuccessful()) {
                                            Map<String, Object> therapist = new HashMap<>();
                                            therapist.put("firstName", firstName);
                                            therapist.put("lastName", lastName);
                                            therapist.put("email", email);
                                            therapist.put("therapistType", therapistType);
                                            therapist.put("availability", availability);
                                            therapist.put("phoneNumber", phoneNumber);

                                            databaseReference.child("therapists").child(user.getUid())
                                                    .setValue(therapist)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(TherapistSignup.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(TherapistSignup.this, TherapistHome.class);
                                                        intent.putExtra("firstName", firstName);
                                                        intent.putExtra("lastName", lastName);
                                                        intent.putExtra("email", email);
                                                        intent.putExtra("therapistType", therapistType);
                                                        intent.putExtra("availability", availability);
                                                        intent.putExtra("phoneNumber", phoneNumber);
                                                        startActivity(intent);
                                                        finish();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(TherapistSignup.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Toast.makeText(TherapistSignup.this, "Profile update failed: " + profileUpdateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(TherapistSignup.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}

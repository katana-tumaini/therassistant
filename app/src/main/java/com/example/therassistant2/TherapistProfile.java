package com.example.therassistant2;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class TherapistProfile extends AppCompatActivity {
    private ImageView profileImageView;
    private TextView nameTextView, typeTextView, contactInfoTextView, availabilityTextView, phoneNumberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        typeTextView = findViewById(R.id.typeTextView);

        contactInfoTextView = findViewById(R.id.contactInfoTextView);
        availabilityTextView = findViewById(R.id.availabilityTextView);
        phoneNumberTextView = findViewById(R.id.phoneNumberTextView);


        Therapist therapist = (Therapist) getIntent().getSerializableExtra("therapist");

        if (therapist != null) {
            nameTextView.setText(therapist.getName());
            typeTextView.setText(therapist.gettherapisttype());
            contactInfoTextView.setText(therapist.getEmail());
            phoneNumberTextView.setText(therapist.getPhoneNumber());
            availabilityTextView.setText(therapist.getAvailability());


            if (therapist.getProfileImageUrl() != null && !therapist.getProfileImageUrl().isEmpty()) {
                Glide.with(this).load(therapist.getProfileImageUrl()).into(profileImageView);
            }
        }
    }
}

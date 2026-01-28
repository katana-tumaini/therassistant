package com.example.therassistant2;

import android.os.Bundle;
import android.text.TextUtils;
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

            // ✅ Best name (First + Last OR name)
            String fullName = "";
            if (!TextUtils.isEmpty(therapist.getFirstName()) || !TextUtils.isEmpty(therapist.getLastName())) {
                fullName = (therapist.getFirstName() == null ? "" : therapist.getFirstName()) +
                        " " +
                        (therapist.getLastName() == null ? "" : therapist.getLastName());
                fullName = fullName.trim();
            }

            if (TextUtils.isEmpty(fullName)) fullName = therapist.getName();
            if (TextUtils.isEmpty(fullName)) fullName = "Therapist";

            nameTextView.setText(fullName);

            typeTextView.setText(
                    TextUtils.isEmpty(therapist.gettherapisttype()) ? "Therapist" : therapist.gettherapisttype()
            );

            contactInfoTextView.setText(
                    TextUtils.isEmpty(therapist.getEmail()) ? "Not provided" : therapist.getEmail()
            );

            phoneNumberTextView.setText(
                    TextUtils.isEmpty(therapist.getPhoneNumber()) ? "Not provided" : therapist.getPhoneNumber()
            );

            availabilityTextView.setText(
                    TextUtils.isEmpty(therapist.getAvailability()) ? "No availability set" : therapist.getAvailability()
            );

            if (!TextUtils.isEmpty(therapist.getProfileImageUrl())) {
                Glide.with(this)
                        .load(therapist.getProfileImageUrl())
                        .placeholder(R.drawable.baseline_account_circle_24)
                        .error(R.drawable.baseline_account_circle_24)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.baseline_account_circle_24);
            }
        }
    }
}

package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class TherapistProfile extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView nameTextView, typeTextView, contactInfoTextView, availabilityTextView, phoneNumberTextView;
    private ExtendedFloatingActionButton bookNow;
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

        bookNow = findViewById(R.id.bookNow);


        Therapist therapist = (Therapist) getIntent().getSerializableExtra("therapist");

        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TherapistProfile.this, BookingActivity.class);

                // Use the same full name logic as displayed in profile
                String fullName = "";
                if (!TextUtils.isEmpty(therapist.getFirstName()) || !TextUtils.isEmpty(therapist.getLastName())) {
                    fullName = (therapist.getFirstName() == null ? "" : therapist.getFirstName()) +
                            " " +
                            (therapist.getLastName() == null ? "" : therapist.getLastName());
                    fullName = fullName.trim();
                }
                if (TextUtils.isEmpty(fullName)) fullName = therapist.getName();
                if (TextUtils.isEmpty(fullName)) fullName = "Therapist";

                intent.putExtra("therapistId", therapist.getUid());
                intent.putExtra("therapistName", fullName);
                intent.putExtra("availability", therapist.getAvailability());

                startActivity(intent);
            }
        });

        if (therapist != null) {

            //  (First + Last OR name)
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

package com.example.therassistant2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class Report extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        TextView emergencyContactsTextView = findViewById(R.id.emergencyContactsTextView);
        TextView emailTextView = findViewById(R.id.emailTextView);

        String emergencyContacts = "Emergency Contacts:\n\n" +
                "1. Mental Health Hotline: 1-800-273-TALK (8255)\n" +
                "2. Emergency Services: 911\n" +
                "3. Local Crisis Center: 1-800-999-9999\n\n";

        String emails = "Contact the Software Team:\n\n" +
                "1. support@therassistant.com\n" +
                "2. feedback@therassistant.com\n";

        emergencyContactsTextView.setText(emergencyContacts);
        emailTextView.setText(emails);

        emailTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:support@therassistant.com"));
                startActivity(Intent.createChooser(emailIntent, "Send Feedback"));
            }
        });
    }
}

package com.example.therassistant2;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.therassistant2.R;

public class ClientProfileActivity extends AppCompatActivity {

    private TextView clientNameTextView, clientDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        clientNameTextView = findViewById(R.id.clientNameTextView);
        clientDetailsTextView = findViewById(R.id.clientDetailsTextView);

        // Get client details from intent or Firebase
        String clientName = "Client Name"; // Replace with actual data
        String clientDetails = "Client Details"; // Replace with actual data

        clientNameTextView.setText(clientName);
        clientDetailsTextView.setText(clientDetails);
    }
}

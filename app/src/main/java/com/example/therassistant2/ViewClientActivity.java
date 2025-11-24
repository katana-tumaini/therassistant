package com.example.therassistant2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewClientActivity extends AppCompatActivity {

    private TextView clientNameTextView, clientEmailTextView, clientDiagnosisTextView;
    private DatabaseReference databaseReference;
    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_client);

        clientNameTextView = findViewById(R.id.clientNameTextView);
        clientEmailTextView = findViewById(R.id.clientEmailTextView);
        clientDiagnosisTextView = findViewById(R.id.clientDiagnosisTextView);

        clientId = getIntent().getStringExtra("clientId");

        databaseReference = FirebaseDatabase.getInstance().getReference("clients").child(clientId);

        loadClientDetails();
    }

    private void loadClientDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Client client = dataSnapshot.getValue(Client.class);
                if (client != null) {
                    clientNameTextView.setText(client.getClientFirstName() + " " + client.getClientLastName());
                    clientEmailTextView.setText(client.getClientEmail());
                    clientDiagnosisTextView.setText(client.getClientDiagnosis());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }
}

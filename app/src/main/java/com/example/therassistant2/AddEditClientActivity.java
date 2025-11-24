package com.example.therassistant2;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddEditClientActivity extends AppCompatActivity {

    private EditText clientFirstNameEditText, clientLastNameEditText, clientEmailEditText, clientDiagnosisEditText;
    private Button saveClientButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_client);

        clientFirstNameEditText = findViewById(R.id.clientFirstNameEditText);
        clientLastNameEditText = findViewById(R.id.clientLastNameEditText);
        clientEmailEditText = findViewById(R.id.clientEmailEditText);
        clientDiagnosisEditText = findViewById(R.id.clientDiagnosisEditText);
        saveClientButton = findViewById(R.id.saveClientButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("clients");

        saveClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveClient();
            }
        });
    }

    private void saveClient() {
        String clientFirstName = clientFirstNameEditText.getText().toString().trim();
        String clientLastName = clientLastNameEditText.getText().toString().trim();
        String clientEmail = clientEmailEditText.getText().toString().trim();
        String clientDiagnosis = clientDiagnosisEditText.getText().toString().trim();

        if (TextUtils.isEmpty(clientFirstName) || TextUtils.isEmpty(clientLastName) || TextUtils.isEmpty(clientEmail) || TextUtils.isEmpty(clientDiagnosis)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String clientId = databaseReference.push().getKey();
        Client client = new Client(clientId, clientFirstName, clientLastName, clientEmail, clientDiagnosis, userId);

        if (clientId != null) {
            databaseReference.child(clientId).setValue(client).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddEditClientActivity.this, "Client saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddEditClientActivity.this, "Failed to save client", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}

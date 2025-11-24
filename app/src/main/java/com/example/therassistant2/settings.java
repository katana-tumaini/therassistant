package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class settings extends AppCompatActivity {

    private Button editInfoButton;
    private Button aboutUsButton;
    private Button reportProblemButton;
    private FirebaseAuth mAuth;
    private DatabaseReference clientsReference;
    private DatabaseReference therapistsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        editInfoButton = findViewById(R.id.editInfoButton);
        aboutUsButton = findViewById(R.id.aboutUsButton);
        reportProblemButton = findViewById(R.id.reportProblemButton);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        clientsReference = FirebaseDatabase.getInstance().getReference("clients").child(userId);
        therapistsReference = FirebaseDatabase.getInstance().getReference("therapists").child(userId);

        editInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAccountTypeAndNavigate();
            }
        });

        aboutUsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settings.this, Aboutus.class);
                startActivity(intent);
            }
        });

        reportProblemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(settings.this, Report.class);
                startActivity(intent);
            }
        });
    }

    private void checkAccountTypeAndNavigate() {
        clientsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot clientSnapshot) {
                if (clientSnapshot.exists()) {
                    // It's a client
                    Intent intent = new Intent(settings.this, Editinfo.class);
                    startActivity(intent);
                } else {
                    therapistsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot therapistSnapshot) {
                            if (therapistSnapshot.exists()) {
                                // It's a therapist
                                Intent intent = new Intent(settings.this, EditTherapistActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(settings.this, "Account type not found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(settings.this, "Failed to retrieve account type", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(settings.this, "Failed to retrieve account type", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

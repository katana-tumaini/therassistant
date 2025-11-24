package com.example.therassistant2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ClientActivity extends AppCompatActivity {

    private RecyclerView clientsRecyclerView;
    private ClientAdapter clientAdapter;
    private ArrayList<Client> clientsList;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FloatingActionButton addClientButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        clientsRecyclerView = findViewById(R.id.clientsRecyclerView);
        clientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        clientsList = new ArrayList<>();
        clientAdapter = new ClientAdapter(clientsList, this);
        clientsRecyclerView.setAdapter(clientAdapter);

        addClientButton = findViewById(R.id.addClientButton);
        addClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientActivity.this, AddEditClientActivity.class));
            }
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("clients");

        loadClients();
    }

    private void loadClients() {
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference.orderByChild("therapistId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                clientsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Client client = snapshot.getValue(Client.class);
                    if (client != null) {
                        clientsList.add(client);
                    }
                }
                clientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ClientActivity.this, "Failed to load clients: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

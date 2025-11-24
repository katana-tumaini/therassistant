package com.example.therassistant2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditTherapistActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 1;

    private ImageView profileImageView;
    private Button uploadImageButton;
    private EditText editFirstName, editLastName, editEmail, editPassword, editTherapistType, editAvailability, editPhoneNumber;
    private Button saveButton;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_therapist);

        profileImageView = findViewById(R.id.profileImageView);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        editFirstName = findViewById(R.id.editFirstName);
        editLastName = findViewById(R.id.editLastName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editTherapistType = findViewById(R.id.editTherapistType);
        editAvailability = findViewById(R.id.editAvailability);
        editPhoneNumber = findViewById(R.id.editPhoneNumber);
        saveButton = findViewById(R.id.saveButton);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("therapists").child(userId);
        storageReference = FirebaseStorage.getInstance().getReference("therapist_profile_images").child(userId);

        uploadImageButton.setOnClickListener(v -> pickImageFromGallery());

        saveButton.setOnClickListener(v -> saveTherapistInfo());
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child("profile.jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    databaseReference.child("profileImageUrl").setValue(uri.toString());
                                    Toast.makeText(EditTherapistActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                    // Load the image into the ImageView using Glide
                                    Glide.with(EditTherapistActivity.this).load(uri).into(profileImageView);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditTherapistActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTherapistInfo() {
        String firstName = editFirstName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String therapistType = editTherapistType.getText().toString().trim();
        String availability = editAvailability.getText().toString().trim();
        String phoneNumber = editPhoneNumber.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ||
                therapistType.isEmpty() || availability.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(EditTherapistActivity.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Therapist therapist = new Therapist(mAuth.getUid(), firstName + " " + lastName, therapistType, email, availability, phoneNumber, firstName, lastName, email, password);
        databaseReference.setValue(therapist)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditTherapistActivity.this, "Info saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditTherapistActivity.this, "Failed to save info", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

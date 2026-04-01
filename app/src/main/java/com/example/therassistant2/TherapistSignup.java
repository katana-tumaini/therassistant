package com.example.therassistant2;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class TherapistSignup extends AppCompatActivity {

    EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword,
            editTextConfirmPassword, editTextTherapistType, editTextAvailability, editTextPhoneNumber;

    Button buttonSignup, buttonUploadPhoto;
    ImageView imageProfile;

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    private Uri imageUri;
    private String uploadedImageUrl = "";


    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            imageProfile.setImageURI(imageUri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_therapist_signup);

        
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        
        imageProfile = findViewById(R.id.imageProfile);
        buttonUploadPhoto = findViewById(R.id.buttonUploadPhoto);

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextTherapistType = findViewById(R.id.editTextTherapistType);
        editTextAvailability = findViewById(R.id.editTextAvailability);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        buttonSignup = findViewById(R.id.buttonSignup);

        
        buttonUploadPhoto.setOnClickListener(v -> openFileChooser());

    
        buttonSignup.setOnClickListener(v -> signupUser());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void signupUser() {

        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String therapistType = editTextTherapistType.getText().toString().trim();
        String availability = editTextAvailability.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() ||
                therapistType.isEmpty() || availability.isEmpty() || phoneNumber.isEmpty()) {

            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

    
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    if (imageUri != null) {
                        uploadImageAndSaveUser(user, firstName, lastName, email,
                                therapistType, availability, phoneNumber);
                    } else {
                        saveUserToDatabase(user, firstName, lastName, email,
                                therapistType, availability, phoneNumber, "");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageAndSaveUser(FirebaseUser user,
                                        String firstName, String lastName, String email,
                                        String therapistType, String availability, String phoneNumber) {

        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + ".jpg");

        fileRef.putFile(imageUri)
                .continueWithTask(task -> fileRef.getDownloadUrl())
                .addOnSuccessListener(uri -> {

                    uploadedImageUrl = uri.toString();

                    saveUserToDatabase(user, firstName, lastName, email,
                            therapistType, availability, phoneNumber, uploadedImageUrl);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show());
    }

    private void saveUserToDatabase(FirebaseUser user,
                                   String firstName, String lastName, String email,
                                   String therapistType, String availability,
                                   String phoneNumber, String profileImageUrl) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName + " " + lastName)
                .build();

        user.updateProfile(profileUpdates);

        Map<String, Object> therapist = new HashMap<>();
        therapist.put("firstName", firstName);
        therapist.put("lastName", lastName);
        therapist.put("email", email);
        therapist.put("therapistType", therapistType);
        therapist.put("availability", availability);
        therapist.put("phoneNumber", phoneNumber);
        therapist.put("profileImageUrl", profileImageUrl);

        databaseReference.child("therapists").child(user.getUid())
                .setValue(therapist)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, TherapistHome.class);
                    intent.putExtra("profileImageUrl", profileImageUrl);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Database error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
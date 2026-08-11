package com.example.therassistant2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int SPLASH_DELAY_MS = 2000;

    private Intent pendingRouteIntent = null;
    private boolean splashTimerFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        LottieAnimationView lottie = findViewById(R.id.lottieSplash);
        lottie.playAnimation();

        requestNotificationPermissionIfNeeded();

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d("FCM_TOKEN", token);
                });

        determineStartDestination();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashTimerFinished = true;
            tryRoute();
        }, SPLASH_DELAY_MS);
    }

    private void determineStartDestination() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            pendingRouteIntent = new Intent(this, MainActivity.class);
            tryRoute();
        } else {
            checkUserTypeAndRoute(currentUser.getUid());
        }
    }

    private void checkUserTypeAndRoute(String userId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("therapists").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            pendingRouteIntent = new Intent(SplashActivity.this, TherapistHome.class);
                        } else {
                            pendingRouteIntent = new Intent(SplashActivity.this, menu.class);
                        }
                        tryRoute();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("SplashActivity", "User type lookup failed", error.toException());
                        pendingRouteIntent = new Intent(SplashActivity.this, MainActivity.class);
                        tryRoute();
                    }
                });
    }

    private void tryRoute() {
        if (splashTimerFinished && pendingRouteIntent != null) {
            pendingRouteIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(pendingRouteIntent);
            finish();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }
}

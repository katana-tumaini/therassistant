package com.example.therassistant2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "therassistant_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("FCM_TEST", "Service created");
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d("FCM_TEST", "New token: " + token);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            String uid =
                    FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid();

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCM_TEST", "Message received!");

        String title = "Therassistant";
        String body = "New notification";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();

            Log.d("FCM_TEST", "Title: " + title);
            Log.d("FCM_TEST", "Body: " + body);
        }

        showNotification(title, body);
    }
    private void showNotification(String title, String message) {

        createNotificationChannel();

        Intent intent =
                new Intent(this, NotificationsActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        notificationManager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Therassistant Notifications",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Booking, chat and therapist notifications"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
package com.example.therassistant2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class Aboutus extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);

        TextView aboutUsTextView = findViewById(R.id.aboutUsTextView);
        String aboutUsText = "Welcome to Therassistant!\n\n" +
                "Therassistant is a state-of-the-art platform designed to enhance the therapeutic experience for both therapists and clients. " +
                "Our aim is to bridge the gap between traditional therapy and modern technology by offering a comprehensive suite of tools that facilitate seamless communication and efficient management of therapy sessions.\n\n" +
                "Our Mission:\n" +
                "To provide a reliable and user-friendly platform that supports mental health professionals in delivering effective therapy while empowering clients to take control of their mental health journey.\n\n" +
                "Key Features:\n" +
                "- Effortless Scheduling: Easily schedule and manage therapy sessions, ensuring that no appointments are missed.\n" +
                "- Schedule Synchronization: Synchronize your personal calendar with therapy appointments to keep your life organized.\n" +
                "- Personalized Profiles: View and edit your personal information, including your profile picture, to maintain a personalized touch.\n" +
                "- Secure Messaging: Communicate securely between therapists and clients, ensuring confidentiality and privacy.\n" +
                "- Emergency Contacts: Access a list of emergency contacts for immediate help in urgent situations.\n\n" +
                "Why Choose Therassistant?\n" +
                "Therassistant is built with the understanding that mental health is paramount. We recognize the challenges both therapists and clients face in managing therapy sessions and the importance of having a reliable system to support this process. " +
                "With Therassistant, you can focus more on the therapeutic process and less on the logistics.\n\n" +
                "Our platform is designed with a user-centric approach, ensuring that all features are intuitive and easy to use. We continuously work on improving our services based on user feedback, striving to provide the best possible experience for our users.\n\n" +
                "Join us on our mission to make therapy more accessible, efficient, and effective. Together, we can create a healthier and more balanced life for everyone involved.\n\n" +
                "Thank you for choosing Therassistant. Your mental health is our priority.";

        aboutUsTextView.setText(aboutUsText);
    }
}

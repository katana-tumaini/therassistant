package com.example.therassistant2;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private static final int MAX_VISIBLE_CARDS = 3;

    private final List<Therapist> therapists;
    private final Context context;

    public CardAdapter(Context context, List<Therapist> therapists) {
        this.context = context;
        this.therapists = therapists;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_therapist, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        Therapist therapist = therapists.get(position);

        String fullName = "";

        if (!TextUtils.isEmpty(therapist.getFirstName()) || !TextUtils.isEmpty(therapist.getLastName())) {
            fullName = (therapist.getFirstName() == null ? "" : therapist.getFirstName()) +
                    " " +
                    (therapist.getLastName() == null ? "" : therapist.getLastName());
            fullName = fullName.trim();
        }

        if (TextUtils.isEmpty(fullName)) fullName = therapist.getName();
        if (TextUtils.isEmpty(fullName)) fullName = "Therapist";

       /* String age = therapist.getAge();
        if (!TextUtils.isEmpty(age)) {
            holder.txtNameAge.setText(fullName + ", " + age);
        } else {
            holder.txtNameAge.setText(fullName);
        }*/

        String type = therapist.gettherapisttype();
        if (TextUtils.isEmpty(type)) type = "Therapist";
        holder.txtTherapistType.setText(type);

        String imageUrl = therapist.getProfileImageUrl();
        Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(holder.imgProfile);

        // STACK EFFECT
        float scale = 1 - (position * 0.08f);
        float translationY = position * 60f;

        holder.itemView.setScaleX(scale);
        holder.itemView.setScaleY(scale);
        holder.itemView.setTranslationY(translationY);
        holder.itemView.setAlpha(1 - (position * 0.10f));

        // TAP CARD → OPEN TherapistProfile
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TherapistProfile.class);
            intent.putExtra("therapist", therapist);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(therapists.size(), MAX_VISIBLE_CARDS);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProfile;
        TextView txtNameAge, txtTherapistType;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.therapist_image);
            txtNameAge = itemView.findViewById(R.id.therapist_name);
            txtTherapistType = itemView.findViewById(R.id.therapist_type);
        }
    }
}

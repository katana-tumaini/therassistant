package com.example.therassistant2;

import android.content.Context;
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

public class InterestedTherapistAdapter extends RecyclerView.Adapter<InterestedTherapistAdapter.ViewHolder> {

    private final List<Therapist> therapists;
    private final Context context;
    private OnTherapistClickListener listener;

    public InterestedTherapistAdapter(List<Therapist> therapists, Context context) {
        this.therapists = therapists;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_interested_therapist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Therapist therapist = therapists.get(position);

        String fullName = "";
        if (!TextUtils.isEmpty(therapist.getFirstName()) || !TextUtils.isEmpty(therapist.getLastName())) {
            fullName = (therapist.getFirstName() == null ? "" : therapist.getFirstName())
                    + " "
                    + (therapist.getLastName() == null ? "" : therapist.getLastName());
            fullName = fullName.trim();
        }
        if (TextUtils.isEmpty(fullName)) {
            fullName = therapist.getName();
        }
        if (TextUtils.isEmpty(fullName)) {
            fullName = "Therapist";
        }

        String type = therapist.gettherapisttype();
        if (TextUtils.isEmpty(type)) {
            type = "Therapist";
        }

        holder.name.setText(fullName);
        holder.type.setText(type);

        String imageUrl = therapist.getProfileImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile_placeholder)
                    .error(R.drawable.default_profile_placeholder)
                    .centerCrop()
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.default_profile_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTherapistClick(therapist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return therapists.size();
    }

    public void setOnTherapistClickListener(OnTherapistClickListener listener) {
        this.listener = listener;
    }

    public interface OnTherapistClickListener {
        void onTherapistClick(Therapist therapist);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, type;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.interestedTherapistImage);
            name = itemView.findViewById(R.id.interestedTherapistName);
            type = itemView.findViewById(R.id.interestedTherapistType);
        }
    }
}

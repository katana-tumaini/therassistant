package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TherapistAdapter extends RecyclerView.Adapter<TherapistAdapter.TherapistViewHolder> {
    private List<Therapist> therapistList;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public TherapistAdapter(List<Therapist> therapistList, Context context) {
        this.therapistList = therapistList;
        this.context = context;
    }

    @NonNull
    @Override
    public TherapistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_therapist, parent, false);
        return new TherapistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TherapistViewHolder holder, int position) {
        Therapist therapist = therapistList.get(position);
        holder.nameTextView.setText(therapist.getName());
        holder.typeTextView.setText(therapist.gettherapisttype());
        holder.therapistsecondname.setText(therapist.getLastName());
        // set image
        if (therapist.getProfileImageUrl() != null && !therapist.getProfileImageUrl().isEmpty()) {
            Glide.with(context).load(therapist.getProfileImageUrl()).into(holder.profileImageView);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(therapist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return therapistList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(Therapist therapist);
    }

    public static class TherapistViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView, typeTextView, therapistsecondname;

        public TherapistViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            nameTextView = itemView.findViewById(R.id.therapistName);
            typeTextView = itemView.findViewById(R.id.therapistType);
            therapistsecondname = itemView.findViewById(R.id.therapistSecondName);

        }
    }
}

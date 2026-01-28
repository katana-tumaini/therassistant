package com.example.therassistant2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Import Glide
import com.example.therassistant2.R;
import com.example.therassistant2.Therapist;

import java.util.List;

public class TherapistAdapter extends RecyclerView.Adapter<TherapistAdapter.ViewHolder> {

    private List<Therapist> therapistList;
    private Context context;
    // Keep your listener for handling clicks
    private OnItemClickListener onItemClickListener;

    public TherapistAdapter(List<Therapist> therapistList, Context context) {
        this.therapistList = therapistList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_therapist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Therapist therapist = therapistList.get(position);

        // Set the text data
        holder.name.setText(therapist.getFirstName() + " " + therapist.getLastName());
        holder.age.setText(String.valueOf(therapist.getAge())); // Assuming getAge() returns an int
        holder.type.setText(therapist.gettherapisttype()); // Assuming you have this field

        // Load the image using Glide
        Glide.with(context)
                .load(therapist.getProfileImageUrl()) // Assuming you have a URL for the picture
                .placeholder(R.drawable.default_profile_placeholder) // Show while loading
                .error(R.drawable.default_profile_placeholder) // Show if URL is invalid
                .into(holder.image);

        // Set listeners for clicks (no changes needed here)
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

    // ViewHolder now includes all the new views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, age, type;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.therapist_name);
            age = itemView.findViewById(R.id.therapist_age);
            type = itemView.findViewById(R.id.therapist_type);
            image = itemView.findViewById(R.id.therapist_image);
        }
    }

    // Interface for click events (no changes needed here)
    public interface OnItemClickListener {
        void onItemClick(Therapist therapist);
        void onMessageClick(Therapist therapist);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
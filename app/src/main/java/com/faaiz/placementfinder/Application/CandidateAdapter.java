package com.faaiz.placementfinder.Application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.faaiz.placementfinder.Profile.ResumeActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUserList;
    private List<String> userId;
    private List<Boolean> applicationStatus;
    private String jobId;

    public CandidateAdapter(Context context, List<User> mUserList, List<String> userId, List<Boolean> applicationStatus, String jobId) {
        mContext = context;
        this.mUserList = mUserList;
        this.userId = userId;
        this.applicationStatus = applicationStatus;
        this.jobId = jobId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.candidates_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        User user = mUserList.get(position);

        // Bind data to views
        holder.tvName.setText(user.getName());
        holder.tvRole.setText(user.getJobTitle() + " at " + user.getCompanyName());

        Picasso.get()
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.profile) // Placeholder while loading
                .error(R.drawable.profile) // Error placeholder
                .into(holder.profilePhoto);

        holder.candidateItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, ResumeActivity.class);
                i.putExtra("isEmployer", true);
                i.putExtra("user", user);
                i.putExtra("jobId", jobId);
                i.putExtra("userId", userId.get(position));
                i.putExtra("isAccepted", applicationStatus.get(position));
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView profilePhoto;
        TextView tvName, tvRole;
        CardView candidateItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profilePhoto);
            tvName = itemView.findViewById(R.id.tvName);
            tvRole = itemView.findViewById(R.id.tvRole);
            candidateItem = itemView.findViewById(R.id.candidateItem);
        }
    }
}


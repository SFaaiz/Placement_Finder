package com.faaiz.placementfinder.Jobs;

import static com.faaiz.placementfinder.Jobs.JobListAdapter.getTimeAgo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.JobViewHolder> {

    Context context;
    List<JobPost> list;
    RoomDB roomDB;

    public ApplicationsAdapter(Context context, List<JobPost> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_job_item, parent,false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        JobPost jobPost = list.get(position);

        // Set job role and company name
        holder.jobRole.setText(jobPost.getRoleToHire());
        holder.companyName.setText(jobPost.getCompanyName());

        // Set job location and experience
        holder.jobLocation.setText(jobPost.getCity());
        holder.jobExperience.setText(jobPost.getMinExperience());

        // Set min qualification and salary
        holder.minQualification.setText(jobPost.getMinQualification());
        holder.salary.setText(jobPost.getMinSalary() + " - " + jobPost.getMaxSalary());

        if(jobPost.getTimeStamp() != 0L){
            holder.timeStamp.setText(getTimeAgo(jobPost.getTimeStamp()));
        }

        holder.container.setOnClickListener(v -> {
            // Handle click on applications
            Intent i = new Intent(context, ViewJobActivity.class);
            i.putExtra("jobId", jobPost.getId());
            context.startActivity(i);
        });

        holder.applications.setVisibility(View.GONE);
        holder.textStatus.setVisibility(View.VISIBLE);
        holder.editApply.setText("Applied");

        holder.tvStatus.setVisibility(View.VISIBLE);
        holder.threeDots.setVisibility(View.INVISIBLE);


        getApplicationStatus(jobPost.getJobId(), new ApplicationStatusCallback() {
            @Override
            public void onStatusReceived(String status) {
                // Use the status value here
                Log.d("TAG", "Application status: " + status);
                holder.tvStatus.setText(status);
                if(status.equals("Accepted")){
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.success));
                }else if(status.equals("Rejected")){
                    holder.tvStatus.setTextColor(context.getResources().getColor(R.color.error));
                }
            }
        });

        holder.editApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Already applied for this job", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getApplicationStatus(String jobId, ApplicationStatusCallback callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("Applications").child(jobId).child(userId);

        applicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Boolean applicationStatus = dataSnapshot.getValue(Boolean.class);
                    if (applicationStatus != null) {
                        if (applicationStatus) {
                            callback.onStatusReceived("Accepted");
                        } else {
                            callback.onStatusReceived("Pending");
                        }
                    } else {
                        callback.onStatusReceived("Rejected");
                    }
                } else {
                    callback.onStatusReceived("Rejected");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "Error getting application status: " + error.getMessage());
                callback.onStatusReceived("Error");
            }
        });
    }

    public interface ApplicationStatusCallback {
        void onStatusReceived(String status);
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder{

        CardView container;
        TextView jobRole, companyName, jobLocation, jobExperience, minQualification, salary, applications, editApply, timeStamp, textStatus, tvStatus;
        ImageView threeDots;
        public JobViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.jobItem);

            jobRole = itemView.findViewById(R.id.text_job_role);
            companyName = itemView.findViewById(R.id.text_company_name);
            jobLocation = itemView.findViewById(R.id.text_job_location);
            jobExperience = itemView.findViewById(R.id.text_job_experience);
            minQualification = itemView.findViewById(R.id.text_min_qualification);
            salary = itemView.findViewById(R.id.text_salary);
            applications = itemView.findViewById(R.id.text_applications);
            editApply = itemView.findViewById(R.id.text_edit);
            timeStamp = itemView.findViewById(R.id.tvTimeStamp);
            textStatus = itemView.findViewById(R.id.textStatus);
            tvStatus = itemView.findViewById(R.id.tvStatusGrey);

            threeDots = itemView.findViewById(R.id.three_dots);

        }
    }
}

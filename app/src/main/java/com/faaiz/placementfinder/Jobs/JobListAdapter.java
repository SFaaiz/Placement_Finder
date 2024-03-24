package com.faaiz.placementfinder.Jobs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Post.PostActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> implements Filterable {

    Context context;
    List<JobPost> list;
    List<JobPost> listFull;
    JobClickListener jobClickListener;
    String userType;
    RoomDB roomDB;
    boolean isSavedJobs;

    public JobListAdapter(Context context, List<JobPost> list, List<JobPost> listFull, JobClickListener jobClickListener, String userType, boolean isSavedJobs) {
        this.context = context;
        this.list = list;
        this.listFull = listFull;
        this.jobClickListener = jobClickListener;
        this.userType = userType;
        roomDB = RoomDB.getInstance(context);
        this.isSavedJobs = isSavedJobs;
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_job_item, parent,false);
        return new JobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobListAdapter.JobViewHolder holder, @SuppressLint("RecyclerView") int position) {
        JobPost jobPost = list.get(position);

        if(isSavedJobs && !jobPost.isJobSaved()){
            return;
        }

        // Set job role and company name
        holder.jobRole.setText(jobPost.getRoleToHire());
        holder.companyName.setText(jobPost.getCompanyName());

        // Set job location and experience
        holder.jobLocation.setText(jobPost.getCity());
        holder.jobExperience.setText(jobPost.getMinExperience());

        // Set min qualification and salary
        holder.minQualification.setText(jobPost.getMinQualification());
        holder.salary.setText(jobPost.getMinSalary() + " - " + jobPost.getMaxSalary());

        // Set click listeners for applications and edit
        holder.container.setOnClickListener(v -> {
            // Handle click on applications
            if (jobClickListener != null) {
                jobClickListener.setOnJobClick(jobPost, context);
            }
        });

//        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                jobClickListener.setOnJobLongClick(jobPost,holder.container,context);
//                return true;
//            }
//        });

        if(userType.equals("user")){
            holder.applications.setVisibility(View.INVISIBLE);
            holder.editApply.setText("Apply");
            if(jobPost.isJobSaved()){
                holder.threeDots.setImageResource(R.drawable.ic_saved);
            }else{
                holder.threeDots.setImageResource(R.drawable.ic_save);
            }

        }

        holder.applications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "Applications Clicked", Toast.LENGTH_SHORT).show();
            }
        });

        holder.editApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "Edit Clicked", Toast.LENGTH_SHORT).show();

                if(userType.equals("user")){
                    Toast.makeText(context, "Apply clicked!", Toast.LENGTH_SHORT).show();
                }else{
                    Intent  i =new Intent(context, PostActivity.class);
                    i.putExtra("message", "editPost");
                    i.putExtra("position", position);
                    context.startActivity(i);
                }



            }
        });

        // Set click listener for three dots
        holder.threeDots.setOnClickListener(v -> {
            if(userType.equals("user")){
                if(jobPost.isJobSaved()){
                    holder.threeDots.setImageResource(R.drawable.ic_save);
                    jobPost.setJobSaved(false);
                    if(isSavedJobs){
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount()); // Update list size after removing item
                    }

                }else{
                    holder.threeDots.setImageResource(R.drawable.ic_saved);
                    jobPost.setJobSaved(true);
                }
                roomDB.dao().updateJobPost(jobPost);

            }else{
                // Handle click on three dots
                // You can show a popup menu or perform any other action here
                PopupMenu popupMenu = new PopupMenu(context, holder.threeDots);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_delete) {
                        // Call the deleteItem method passing the position
                        deleteItem(position, jobPost);
                        return true;
                    }
                    return false;
                });
                popupMenu.show();
            }

        });
    }

//    private void updateJobSaveStatus(int position, JobPost jobPost){
//        User user = roomDB.dao().getUser();
//        List<String> savedJobs = user.getSavedJobs();
//        if(savedJobs == null) {
//            savedJobs = new ArrayList<>();
//        }
//        if(jobPost.isJobSaved()){
//            savedJobs.add()
//        }
//    }


    ProgressDialog progressDialog;
    private void deleteItem(int position, JobPost job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this job post?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    progressDialog = new ProgressDialog(context);
                    progressDialog.setMessage("Deleting item...");
                    progressDialog.show();

                    deleteJobFromEverywhere(job,position);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteJobFromEverywhere(JobPost jobPost, int position){
        roomDB.dao().deleteJobPost(jobPost);
        List<String> jobIds = roomDB.dao().getEmployer().getJobsPosted();
        int actualPosition = jobIds.size() - position - 1;
        String jobId = jobIds.get(actualPosition);
        jobIds.remove(actualPosition);
        roomDB.dao().updateJobId(jobIds);

        // Delete from "Jobs" node
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");
        jobsRef.child(jobId).removeValue();

        // Delete from employer's "jobsPosted" node
        String employerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference employerJobsRef = FirebaseDatabase.getInstance().getReference("Employers")
                .child(employerId)
                .child("jobsPosted");

        // Fetch the list of jobIds
        employerJobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<String> jobsId = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        jobsId.add(snapshot.getValue(String.class));
                    }

                    // Remove the jobId of the current job
                    jobsId.remove(jobId);

                    // Update the list in the database
                    employerJobsRef.setValue(jobsId);

                    // Remove item from RecyclerView
                    list.remove(position);
                    notifyItemRemoved(position);

                    progressDialog.dismiss();
                    Toast.makeText(context, "Job Post Deleted Successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
                // Handle onCancelled event
            }
        });

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    class JobViewHolder extends RecyclerView.ViewHolder{

        CardView container;
        TextView jobRole, companyName, jobLocation, jobExperience, minQualification, salary, applications, editApply;
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

            threeDots = itemView.findViewById(R.id.three_dots);

        }
    }
}

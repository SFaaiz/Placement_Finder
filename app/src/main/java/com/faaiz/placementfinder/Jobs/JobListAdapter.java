package com.faaiz.placementfinder.Jobs;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.faaiz.placementfinder.Application.EmployerApplicationsActivity;
import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Post.PostActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class JobListAdapter extends RecyclerView.Adapter<JobListAdapter.JobViewHolder> implements Filterable{

    Context context;
    List<JobPost> list;
    List<JobPost> listFull;
    JobClickListener jobClickListener;
    String userType;
    RoomDB roomDB;
    boolean isSavedJobs;
    private static ProgressDialog progressDialog;

    public JobListAdapter(Context context, List<JobPost> list, JobClickListener jobClickListener, String userType, boolean isSavedJobs) {
        this.context = context;
        this.list = list;
        listFull = new ArrayList<>();
        listFull.addAll(list);
        this.jobClickListener = jobClickListener;
        this.userType = userType;
        roomDB = RoomDB.getInstance(context);
        this.isSavedJobs = isSavedJobs;
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

        if(jobPost.getTimeStamp() != 0L){
            holder.timeStamp.setText(getTimeAgo(jobPost.getTimeStamp()));
        }


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
                Intent i = new Intent(context, EmployerApplicationsActivity.class);
                i.putExtra("jobId", jobPost.getJobId());
                context.startActivity(i);
            }
        });

        if(jobPost.isJobApplied()){
            holder.editApply.setText("Applied");
        }

        holder.editApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(context, "Edit Clicked", Toast.LENGTH_SHORT).show();

                if(userType.equals("user")){
                    if(jobPost.isJobApplied()){
                        Toast.makeText(context, "Already applied for this job", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(checkProfileProgress()){
                        showApplyJobConfirmationDialog(context, jobPost);
                    }else{
                        showProfileCompletionDialog(context);
                    }
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

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                System.out.println("constraint = " + constraint);
                String filterPattern = constraint.toString().toLowerCase().trim();
                System.out.println("filter pattern = " + filterPattern);
                FilterResults results = new FilterResults();

                List<JobPost> filteredList = new ArrayList<>();

                if (filterPattern.isEmpty()) {
                    filteredList.addAll(listFull);
                } else {
                    for (JobPost post : listFull) {
                        // Filtering logic based on roleToHire and companyName
                        if ((post.getRoleToHire() != null && post.getRoleToHire().toLowerCase().contains(filterPattern)) ||
                                (post.getCompanyName() != null && post.getCompanyName().toLowerCase().contains(filterPattern))) {
                            filteredList.add(post);
                        }
                    }
                }

                for(JobPost j : filteredList){
                    System.out.println(j.getCompanyName() + " , " + j.getRoleToHire());
                }

                // Sort the filtered list by timestamp in descending order
                Collections.sort(filteredList, new Comparator<JobPost>() {
                    @Override
                    public int compare(JobPost o1, JobPost o2) {
                        return Long.compare(o2.getTimeStamp(), o1.getTimeStamp());
                    }
                });

                results.values = filteredList;
                return results;
            }



            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.values != null) {
                    System.out.println("Filter results = " + results.values);
                    list.clear();
//                    list.addAll((List<JobPost>) results.values);
                    List<JobPost> temp = (List<JobPost>) results.values;

                    for(JobPost j : temp){
                        list.add(j);
                    }
                    notifyDataSetChanged();
                }else{
                    System.out.println("results are null");
                }
            }
        };
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

    private boolean checkProfileProgress(){
        User user = roomDB.dao().getUser();
        if(user.getGrade()==null || user.getGrade().isEmpty() || user.getCompanyName()==null || user.getCompanyName().isEmpty() || user.getSkills()==null || user.getSkills().size()==0 || user.getProjectTitle()==null || user.getProjectTitle().isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public static void showProfileCompletionDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Complete Your Profile");
        builder.setMessage("Please complete your profile before applying for this job.");
        builder.setPositiveButton("OK", null); // Only OK button

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showApplyJobConfirmationDialog(Context context, JobPost jobPost) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Apply for Job");
        builder.setMessage("Are you sure you want to apply for " + jobPost.getRoleToHire() + " in " + jobPost.getCity() + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Task to perform when user clicks "Yes"
                // For example, call a method to apply for the job
//                showAppliedForJobDialog(context, role);
                applyJobProgressDialog(context);
                updateApplicationStatus(jobPost);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Task to perform when user clicks "No"
                dialog.dismiss(); // Cancel the dialog
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void updateApplicationStatus(JobPost jobPost){
        User user = roomDB.dao().getUser();
        List<String> appliedJobs =  user.getAppliedJobs();
        if(appliedJobs == null){
            appliedJobs  = new ArrayList<>();
        }
        appliedJobs.add(jobPost.getJobId());
        user.setAppliedJobs(appliedJobs);
        jobPost.setJobApplied(true);
        roomDB.dao().updateJobPost(jobPost);
        roomDB.dao().updateUser(user);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        usersRef.child("appliedJobs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the current list of jobsPosted
                List<String> appliedJobs = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String jobValue = snapshot.getValue(String.class);
                        appliedJobs.add(jobValue);
                    }
                }

                // Add the new value to the list
                appliedJobs.add(jobPost.getJobId());


                usersRef.child("appliedJobs").setValue(appliedJobs).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("Applications").child(jobPost.getJobId());

                            applicationsRef.child(userId).setValue(false);

                            progressDialog.dismiss();
                            showAppliedForJobDialog(context, jobPost.getRoleToHire());
                            notifyDataSetChanged();

                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(context, "Couldn't Apply For the Job", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                progressDialog.dismiss();
                Toast.makeText(context, "Couldn't Apply For the Job", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public static void showAppliedForJobDialog(Context context, String role) {
        // Inflate the custom layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_applied_for_job, null);

        // Create AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Set the custom layout to the dialog
        builder.setView(dialogView);

        // Create and show the dialog
        AlertDialog alertDialog = builder.create();

        TextView dialogMessage = dialogView.findViewById(R.id.tv_desc);
        dialogMessage.setText(role);

        alertDialog.show();
    }


    public static void applyJobProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Applying for job...");
        progressDialog.setCancelable(false); // Set to true if you want it to be cancelable
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Set the style of the ProgressDialog
        progressDialog.show();
    }


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
        TextView jobRole, companyName, jobLocation, jobExperience, minQualification, salary, applications, editApply, timeStamp;
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

            threeDots = itemView.findViewById(R.id.three_dots);

        }
    }

    public static String getTimeAgo(long timestamp) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - timestamp;

        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff);
        long hours = TimeUnit.MILLISECONDS.toHours(timeDiff);
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long months = days / 30;
        long years = months / 12;

        if (years > 0) {
            return years + (years == 1 ? " year ago" : " years ago");
        } else if (months > 0) {
            return months + (months == 1 ? " month ago" : " months ago");
        } else if (days > 0) {
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (hours > 0) {
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (minutes > 0) {
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "just now";
        }
    }
}

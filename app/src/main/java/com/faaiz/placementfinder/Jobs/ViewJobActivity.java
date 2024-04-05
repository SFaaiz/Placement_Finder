package com.faaiz.placementfinder.Jobs;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.JobApplicationHelper;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityViewJobBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewJobActivity extends AppCompatActivity {

    ActivityViewJobBinding binding;
    RoomDB roomDB;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityViewJobBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int id = getIntent().getIntExtra("jobId", -1);
        roomDB = RoomDB.getInstance(this);
        database = FirebaseDatabase.getInstance();

        setJobData(id);

        MySharedPreferences sp = new MySharedPreferences(this);
        String userType = sp.getUserType();
        if(userType.equals("employer")){
            binding.bottomBar.setVisibility(View.INVISIBLE);
        }



    }

    private void setJobData(int id){

        JobPost jobPost = roomDB.dao().getJobPost(id);
        if(jobPost.isJobApplied()){
            binding.btnApply.setVisibility(View.GONE);
            binding.btnSave.setVisibility(View.GONE);
            binding.space.setVisibility(View.GONE);
            binding.btnCall.setVisibility(View.VISIBLE);
        }
        binding.textJobRole.setText(jobPost.getRoleToHire());
        binding.textSalary.setText(jobPost.getMinSalary() + " - " + jobPost.getMaxSalary());
        binding.tvLocation.setText(jobPost.getCity());
        binding.tvExperience.setText(jobPost.getMinExperience());
        binding.tvQualification.setText(jobPost.getMinQualification());

        for(String item: jobPost.getSkillsRequired()){
            Chip chip = new Chip(this);
            chip.setText(item);
            binding.chipGroup.addView(chip);
        }

        binding.tvDesc.setText(jobPost.getJobDescription());

        if(jobPost.getEmployerId() != null){
            setCompanyDetails(jobPost.getEmployerId());
        }

        binding.btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkProfileProgress()){
                    showApplyJobConfirmationDialog(ViewJobActivity.this, jobPost);
                }else{
                    showProfileCompletionDialog(ViewJobActivity.this);
                }
            }
        });


    }

    public boolean checkProfileProgress(){
        User user = roomDB.dao().getUser();
        if(user.getGrade()==null || user.getGrade().isEmpty() || user.getCompanyName()==null || user.getCompanyName().isEmpty() || user.getSkills()==null || user.getSkills().size()==0 || user.getProjectTitle()==null || user.getProjectTitle().isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public void showProfileCompletionDialog(Context context) {
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
                            showAppliedForJobDialog(ViewJobActivity.this, jobPost.getRoleToHire());


                        }else{
                            progressDialog.dismiss();
                            Toast.makeText(ViewJobActivity.this, "Couldn't Apply For the Job", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                progressDialog.dismiss();
                Toast.makeText(ViewJobActivity.this, "Couldn't Apply For the Job", Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void showAppliedForJobDialog(Context context, String role) {
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

        binding.btnApply.setVisibility(View.GONE);
        binding.btnCall.setVisibility(View.VISIBLE);
    }

    ProgressDialog progressDialog;


    public void applyJobProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Applying for job...");
        progressDialog.setCancelable(false); // Set to true if you want it to be cancelable
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Set the style of the ProgressDialog
        progressDialog.show();
    }

    private void setCompanyDetails(String employerId){
        DatabaseReference employerRef = database.getReference("Employers").child(employerId);
        employerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Employer employer = dataSnapshot.getValue(Employer.class);
                    if (employer != null) {
                        // Do something with the employer if needed
                        binding.tvCompany.setText(employer.getCompanyName());
                        binding.tvContact.setText(employer.getName());
                        binding.textCompanyAddress.setText(employer.getCompanyAddress());
                        binding.textCompanyDescription.setText(employer.getCompanyDescription());

                        binding.btnCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(employer.getMobile() != null){
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:+91" + employer.getMobile().trim()));
                                    startActivity(intent);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }
}
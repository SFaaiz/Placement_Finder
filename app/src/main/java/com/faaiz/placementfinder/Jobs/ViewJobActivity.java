package com.faaiz.placementfinder.Jobs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityViewJobBinding;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
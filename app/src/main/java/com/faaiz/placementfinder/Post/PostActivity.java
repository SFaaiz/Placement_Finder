package com.faaiz.placementfinder.Post;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Toast;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Jobs.JobsFragment;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.Profile.ProfileSkillsActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityPostBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase db;
    ProgressDialog progressDialog;
    RoomDB roomDB;
    ActivityPostBinding binding;
    String jobId;
    boolean updateJob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        roomDB = RoomDB.getInstance(this);

        setChipGroupVisibility();
        setSkillsAdapter();
        setJObRoleAdapter();
        setCityAdapter();
        setMinQualificationAdapter();
        setMinExpAdapter();

        String message = getIntent().getStringExtra("message");

        updateJob = false;
        if(message != null && message.equals("editPost")){
            int position = getIntent().getIntExtra("position", -1);
            binding.saveBtn.setText("Update");
            updateJob = true;
            binding.actvHire.setEnabled(false);
            fetchPost(position);
        }

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidPost()){
                    showProgressDialog();
                    postJobOnFB();
                }
            }
        });



    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(PostActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    /*
    1,2,3,4,5,6,7

     */

    JobPost existingJobPost;
    private void fetchPost(int position){
        List<JobPost> allJobPosts = roomDB.dao().getAllJobPosts();
        existingJobPost = allJobPosts.get(position);

        int totalJobs = allJobPosts.size();
        Log.d(TAG, "fetchPost: totaljobs = " + totalJobs + " , position = " + position);
        // Calculate the actual position based on the reversed order
        int actualPosition = (totalJobs - position) - 1;
        List<String> jobIds = roomDB.dao().getEmployer().getJobsPosted();
        Log.d(TAG, "fetchPost: actual position = " + actualPosition + " , jobids = " + jobIds.size());
        jobId = jobIds.get(actualPosition);

        binding.actvHire.setText(existingJobPost.getRoleToHire());
        binding.actvCity.setText(existingJobPost.getCity());

        if(existingJobPost.getSkillsRequired() != null && !existingJobPost.getSkillsRequired().isEmpty()){
            binding.chipGroup.setVisibility(View.VISIBLE);
            for(String item: existingJobPost.getSkillsRequired()){
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setCloseIconVisible(true);
                chip.setOnCloseIconClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        binding.chipGroup.removeView(chip);
                        setChipGroupVisibility();
                    }
                });
                binding.chipGroup.addView(chip);
            }
        }

        binding.etMinSalary.setText(existingJobPost.getMinSalary());
        binding.etMaxSalary.setText(existingJobPost.getMaxSalary());
        binding.autoCompleteMinExperience.setText(existingJobPost.getMinExperience());
        binding.autoCompleteMinQualification.setText(existingJobPost.getMinQualification());
        binding.etJobDescription.setText(existingJobPost.getJobDescription());
    }

    private void updateJobPost(){
// Create a reference to the specific job ID under the "Jobs" node
        DatabaseReference jobRef = FirebaseDatabase.getInstance().getReference().child("Jobs").child(jobId);

// Create a new JobPost object with updated values
        JobPost updatedJobPost = new JobPost(/* pass updated field values here */);

// Set the value of the specific job ID to the updated JobPost object
        jobRef.setValue(updatedJobPost)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle success
                        Log.d(TAG, "JobPost overwritten successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e(TAG, "Error overwriting JobPost: " + e.getMessage());
                    }
                });

    }


    private void postJobOnFB(){
        // Assuming you have the binding object named 'binding'

        String roleToHire = binding.actvHire.getText().toString();
        String city = binding.actvCity.getText().toString();

        List<String> skillsRequired = new ArrayList<>();
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            View view = binding.chipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                skillsRequired.add(chip.getText().toString());
            }
        }

        String minSalary = binding.etMinSalary.getText().toString();
        String maxSalary = binding.etMaxSalary.getText().toString();
        String minExperience = binding.autoCompleteMinExperience.getText().toString();
        String minQualification = binding.autoCompleteMinQualification.getText().toString();
        String jobDescription = binding.etJobDescription.getText().toString();

        String companyName = roomDB.dao().getEmployer().getCompanyName();
        long timeStamp = System.currentTimeMillis();

        JobPost job = new JobPost(roleToHire, city, skillsRequired, minSalary, maxSalary, minExperience, minQualification, jobDescription, companyName, mAuth.getCurrentUser().getUid(), timeStamp);


        String jobIdTemp;

        // check if we have to update
        if(updateJob){
            jobIdTemp = jobId;
            job.setId(existingJobPost.getId());
        }else{
            jobIdTemp = UUID.randomUUID().toString();
        }

        job.setJobId(jobIdTemp);

        saveJob(job,jobIdTemp);


//        String jobId = UUID.randomUUID().toString();

    }

    private void saveJob(JobPost job, String jobId){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");

        reference.child(jobId).setValue(job).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    if(updateJob){
                        roomDB.dao().updateJobPost(job);
                        dismissProgressDialog();
                        goToJobsFragment();
                    }else{
                        addPostId(jobId, job);
                    }


                }else{
                    Log.d(TAG, "onComplete: Failure " + task.getException());
                    Toast.makeText(PostActivity.this, "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void goToJobsFragment(){
        Intent i = new Intent(PostActivity.this, MainActivity.class);
        i.putExtra("gotoJobs", true);
        startActivity(i);
        finish();
    }

    private void addPostId(String jobId, JobPost jobPost){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Employers").child(mAuth.getCurrentUser().getUid()).child("jobsPosted");

// Retrieve the current list of jobsPosted
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the current list of jobsPosted
                List<String> currentJobsPosted = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String jobValue = snapshot.getValue(String.class);
                        currentJobsPosted.add(jobValue);
                    }
                }

                // Add the new value to the list
                currentJobsPosted.add(jobId);

                // Update the jobsPosted list with the modified list
                userRef.setValue(currentJobsPosted).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            roomDB.dao().insertJob(jobPost);
                            List<String> jobsPosted = roomDB.dao().getEmployer().getJobsPosted();
                            if(jobsPosted == null){
                                jobsPosted = new ArrayList<>();
                            }
                            jobsPosted.add(jobId);
                            roomDB.dao().updateJobId(jobsPosted);
                            dismissProgressDialog();
                            Toast.makeText(PostActivity.this, "Job Posted Successfully", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Data has been saved into firebase and room db");

                            goToJobsFragment();
//                            finish();
                        }else{
                            Toast.makeText(PostActivity.this, "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                Toast.makeText(PostActivity.this, "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
            }


        });
    }



//    int n=0;
//    private void loadFrag(Fragment f){
//        FragmentManager fm = requireActivity().getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        if(n==0){
//            ft.add(R.id.frame, f);
//            n++;
//        }else{
//            ft.replace(R.id.frame, f);
//        }
//        ft.commit();
//    }

//    private void gotoJobFragment(){
//        NavHostFragment navHostFragment = (NavHostFragment) requireActivity().getSupportFragmentManager().findFragmentById(R.id.jobs);
//// Get the NavController associated with the NavHostFragment
//        NavController navController = navHostFragment.getNavController();
//        navController.navigate(R.id.jobs);
//    }

    public void setMinExpAdapter(){
        List<String> minExpList = new ArrayList<>(Arrays.asList("Fresher", "6 months", "1 year", "2 years", "3 years", "5 years", "7 years", "10 years"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, minExpList) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = minExpList;
                        filterResults.count = minExpList.size();
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        binding.autoCompleteMinExperience.setAdapter(adapter);

        binding.autoCompleteMinExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.autoCompleteMinExperience.showDropDown();
            }
        });

        binding.autoCompleteMinExperience.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    binding.autoCompleteMinExperience.showDropDown();
                }
            }
        });


        binding.autoCompleteMinExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.autoCompleteMinExperience.getText().toString().isEmpty()){
                    binding.autoCompleteMinExperience.setText("");
                }

                binding.autoCompleteMinExperience.showDropDown();
            }
        });

        binding.autoCompleteMinExperience.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                binding.autoCompleteMinExperience.clearFocus();
                binding.autoCompleteMinExperience.setError(null);
            }
        });
    }

    public void setMinQualificationAdapter(){
        List<String> minQualificationList = new ArrayList<>(Arrays.asList("<10th pass", "10th pass or above", "12th pass or above", "Graduate or above", "Post Graduate"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, minQualificationList) {
            @NonNull
            @Override
            public Filter getFilter() {
                return new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults filterResults = new FilterResults();
                        filterResults.values = minQualificationList;
                        filterResults.count = minQualificationList.size();
                        return filterResults;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        notifyDataSetChanged();
                    }
                };
            }
        };
        binding.autoCompleteMinQualification.setAdapter(adapter);

        binding.autoCompleteMinQualification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.autoCompleteMinQualification.showDropDown();
            }
        });

        binding.autoCompleteMinQualification.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    binding.autoCompleteMinQualification.showDropDown();
                }
            }
        });


        binding.autoCompleteMinQualification.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                binding.autoCompleteMinQualification.clearFocus();
                binding.autoCompleteMinQualification.setError(null);
            }
        });

    }

    private boolean isValidPost(){
        if (!isValidJobRole()) {
            // Show error message for job role
            return false;
        }
        if (!isValidCity()) {
            // Show error message for city
            return false;
        }
        if (!isValidSkills()) {
            // Show error message for skills
            return false;
        }
        if (!isValidSalary()) {
            // Show error message for salary
            return false;
        }
        if (!isValidExperience()) {
            // Show error message for experience
            return false;
        }
        if (!isValidQualification()) {
            // Show error message for qualification
            return false;
        }
        if (!isValidJobDescription()) {
            // Show error message for job description
            return false;
        }

        String companyDescription = roomDB.dao().getEmployer().getCompanyDescription();
        if(companyDescription == null || companyDescription.isEmpty()){
            Toast.makeText(this, "Please complete your profile first", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void setCityAdapter(){
        List<String> cityList = getCityNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, cityList);
        binding.actvCity.setAdapter(adapter);
    }

    public List<String> getCityNames() {
        List<String> cityList = new ArrayList<>();
        try {
            // Open the raw resource file
            InputStream inputStream = getResources().openRawResource(R.raw.indian_cities);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line from the file
            String line;
            StringBuilder text = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                text.append(line);
            }

            // Use regex to extract city names between '|' characters
            Pattern pattern = Pattern.compile("\\b(\\w+(?:\\s+\\w+)*)\\b");
            Matcher matcher = pattern.matcher(text.toString());
            while (matcher.find()) {
                cityList.add(matcher.group(1));
            }

            // Close the input stream
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityList;
    }

    private void setChipGroupVisibility(){

// Check if ChipGroup has at least one chip
        if (binding.chipGroup.getChildCount() > 0) {
            // At least one chip is present, make the ChipGroup visible
            binding.chipGroup.setVisibility(View.VISIBLE);
        } else {
            // No chips present, hide the ChipGroup
            binding.chipGroup.setVisibility(View.GONE);
        }

    }

    private void setJObRoleAdapter(){
        List<String> jobRolesArray = getJobRoleList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, jobRolesArray);
        binding.actvHire.setAdapter(adapter);
    }

    private void setSkillsAdapter(){
        List<String> skillsArray = getSkillsList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, skillsArray);
        binding.actvSkills.setAdapter(adapter);

        binding.actvSkills.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSkill = adapter.getItem(position);

            boolean skillExists = false;

            // Iterate over each chip in the ChipGroup
            for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                String chipText = chip.getText().toString();

                // Compare the selected skill with the text of each chip
                if (selectedSkill.equals(chipText)) {
                    skillExists = true;
                    break;
                }
            }

            if (skillExists) {
                // Skill already exists, show toast message
                Toast.makeText(this, "Skill already added", Toast.LENGTH_SHORT).show();
            } else {
                addChip(selectedSkill);
            }

            binding.actvSkills.setText(""); // Clear the text after selection
            setChipGroupVisibility();
        });
    }

    private List<String> getSkillsList(){
        InputStream inputStream = getResources().openRawResource(R.raw.linkedin_skills);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> skillsArray = new ArrayList<>();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Add each line to the skillsArray
                skillsArray.add(line.trim()); // trim() removes leading and trailing whitespace
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the reader to free up resources
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return skillsArray;
    }

    private List<String> getJobRoleList(){
        InputStream inputStream = getResources().openRawResource(R.raw.job_titles);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> jobRolesArray = new ArrayList<>();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Add each line to the skillsArray
                jobRolesArray.add(line.trim()); // trim() removes leading and trailing whitespace
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                // Close the reader to free up resources
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jobRolesArray;
    }

    private void addChip(String skill) {
        Chip chip = new Chip(this);
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.chipGroup.removeView(chip);
                setChipGroupVisibility();
            }
        });
//        chip.setOnCloseIconClickListener(v ->
////                binding.chipGroup.removeView(chip);
////                setChipGroupVisibility();
//        );
        binding.chipGroup.addView(chip);
    }

    private boolean isValidJobRole() {
        String jobRole = binding.actvHire.getText().toString().trim();
        if (jobRole.isEmpty()) {
            binding.actvHire.setError("Please enter a job role");
            return false;
        }
        return true;
    }

    private boolean isValidCity() {
        String city = binding.actvCity.getText().toString().trim();
        if (city.isEmpty()) {
            binding.actvCity.setError("Please enter a city");
            return false;
        }
        return true;
    }

    private boolean isValidSkills() {
        if (binding.chipGroup.getChildCount() == 0) {
            Toast.makeText(this, "Please select at least One Skill", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean isValidSalary() {
        String minSalary = binding.etMinSalary.getText().toString().trim();
        String maxSalary = binding.etMaxSalary.getText().toString().trim();
        // Perform salary validation as needed
        if(minSalary.length() <=3){
            binding.etMinSalary.setError("Please Enter valid Min Salary");
            return false;
        }
        if(maxSalary.length() >= 8 || maxSalary.length() <= 3){
            binding.etMaxSalary.setError("Please Enter valid Max Salary");
            return false;
        }
        return true;
    }

    private boolean isValidExperience() {
        String experience = binding.autoCompleteMinExperience.getText().toString().trim();
        if (experience.isEmpty()) {
            binding.autoCompleteMinExperience.setError("Please select minimum experience");
            return false;
        }
        return true;
    }

    private boolean isValidQualification() {
        String qualification = binding.autoCompleteMinQualification.getText().toString().trim();
        if (qualification.isEmpty()) {
            binding.autoCompleteMinQualification.setError("Please select minimum qualification");
            return false;
        }
        return true;
    }

    private boolean isValidJobDescription() {
        String jobDescription = binding.etJobDescription.getText().toString().trim();
        if (jobDescription.isEmpty()) {
            binding.etJobDescription.setError("Please enter job description");
            return false;
        }
        return true;
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this); // Replace 'this' with your activity or fragment context
            progressDialog.setTitle("Posting Job...");
            progressDialog.setMessage("Please wait while we post your job"); // Set the message to be displayed
            progressDialog.setCancelable(false); // Make the dialog not cancelable
            progressDialog.setCanceledOnTouchOutside(false); // Make the dialog not dismiss when touched outside
        }
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
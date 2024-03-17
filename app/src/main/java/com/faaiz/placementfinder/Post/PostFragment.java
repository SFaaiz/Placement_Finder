package com.faaiz.placementfinder.Post;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Jobs.JobsFragment;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.FragmentPostBinding;
import com.faaiz.placementfinder.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PostFragment newInstance(String param1, String param2) {
        PostFragment fragment = new PostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    FragmentPostBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase db;
    ProgressDialog progressDialog;
    RoomDB roomDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        roomDB = RoomDB.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        setChipGroupVisibility();
        setSkillsAdapter();
        setJObRoleAdapter();
        setCityAdapter();
        setMinQualificationAdapter();
        setMinExpAdapter();

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidPost()){
                    showProgressDialog();
                    postJobOnFB();
                }
            }
        });

        return view;
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

        JobPost job = new JobPost(roleToHire, city, skillsRequired, minSalary, maxSalary, minExperience, minQualification, jobDescription);

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Jobs");
        String jobId = UUID.randomUUID().toString();
        reference.child(jobId).setValue(job).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    addPostId(jobId, job);

                }else{
                    Log.d(TAG, "onComplete: Failure " + task.getException());
                    Toast.makeText(requireContext(), "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                            dismissProgressDialog();
                            Toast.makeText(requireContext(), "Job Posted Successfully", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: Data has been saved into firebase and room db");
                            ((MainActivity) requireActivity()).loadFrag(new JobsFragment());
                            ((MainActivity) requireActivity()).updateSelectedItem(R.id.jobs);
                        }else{
                            Toast.makeText(requireContext(), "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
                Toast.makeText(requireContext(), "Couldn't Post the Job", Toast.LENGTH_SHORT).show();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, minExpList);
        binding.autoCompleteMinExperience.setAdapter(adapter);
        binding.autoCompleteMinExperience.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, minQualificationList);
        binding.autoCompleteMinQualification.setAdapter(adapter);
        binding.autoCompleteMinQualification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.autoCompleteMinQualification.showDropDown();
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
        return true;
    }

    public void setCityAdapter(){
        List<String> cityList = getCityNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, cityList);
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

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, jobRolesArray);
        binding.actvHire.setAdapter(adapter);
    }

    private void setSkillsAdapter(){
        List<String> skillsArray = getSkillsList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, skillsArray);
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
                Toast.makeText(requireContext(), "Skill already added", Toast.LENGTH_SHORT).show();
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
        Chip chip = new Chip(requireContext());
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
            Toast.makeText(requireContext(), "Please select at least One Skill", Toast.LENGTH_SHORT).show();
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
            progressDialog = new ProgressDialog(requireContext()); // Replace 'this' with your activity or fragment context
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
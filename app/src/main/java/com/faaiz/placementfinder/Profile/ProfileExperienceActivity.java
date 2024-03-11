package com.faaiz.placementfinder.Profile;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.Authentication.PersonalDetailsActivity;
import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityProfileExperienceBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfileExperienceActivity extends AppCompatActivity {

    RoomDB roomDB;
    ActivityProfileExperienceBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    String userId;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileExperienceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomDB = RoomDB.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        fetchData();

        binding.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()){
                    showProgressDialog();
                    saveData();
                }
            }
        });

    }

    private void saveData(){
        String companyName = binding.etCompanyName.getText().toString();
        String jobTitle = binding.etJobTitle.getText().toString();
        String startDate = binding.etStartDate.getText().toString();
        String endDate = binding.etEndDate.getText().toString();
        String experienceDescription = binding.etExpDescription.getText().toString();

// Now you can use these String variables as needed, such as storing them in a Map
        Map<String, Object> updates = new HashMap<>();
        updates.put("companyName", companyName);
        updates.put("jobTitle", jobTitle);
        updates.put("startDate", startDate);
        updates.put("endDate", endDate);
        updates.put("experienceDescription", experienceDescription);

// Update the user data in the database
        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    roomDB.dao().updateUserExperience(companyName, jobTitle, startDate, endDate, experienceDescription);
                    dismissProgressDialog();
                    Intent i = new Intent(ProfileExperienceActivity.this, MainActivity.class);
                    i.putExtra("goToProfileFragment", true);
                    startActivity(i);
                    finish();
                    Log.d(TAG, "onComplete: User data updated in firebase");
                }else{
                    dismissProgressDialog();
                    Toast.makeText(ProfileExperienceActivity.this, "data could not be updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                }
            }
        });

    }

    private boolean isValid(){
        if(binding.etCompanyName.getText().toString().isEmpty()){
            binding.etCompanyName.setError("Company Name Cannot Be Empty");
            return false;
        }
        if(binding.etJobTitle.getText().toString().isEmpty()){
            binding.etJobTitle.setError("Job Title Cannot Be Empty");
            return false;
        }
        if(binding.etStartDate.getText().toString().isEmpty()){
            binding.etStartDate.setError("Cannot Be Empty");
            return false;
        }
        if(binding.etEndDate.getText().toString().isEmpty()){
            binding.etEndDate.setError("Cannot Be Empty");
            return false;
        }
        if(binding.etExpDescription.getText().toString().isEmpty()){
            binding.etExpDescription.setError("Please provide some description");
            return false;
        }
        return true;
    }

    private void fetchData(){
        User user = roomDB.dao().getUser();
        if(user != null){
            if(user.getCompanyName() !=  null && !user.getCompanyName().isEmpty()){
                binding.etCompanyName.setText(user.getCompanyName().toString());
                binding.etJobTitle.setText(user.getJobTitle().toString());
                binding.etStartDate.setText(user.getStartDate().toString());
                binding.etEndDate.setText(user.getEndDate().toString());
                if(user.getExperienceDescription() != null){
                    binding.etExpDescription.setText(user.getExperienceDescription().toString());
                }
            }
        }else{
            Log.d(TAG, "fetchData: User is null");
        }
        
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this); // Replace 'this' with your activity or fragment context
            progressDialog.setTitle("Saving data...");
            progressDialog.setMessage("Please wait while we save your data"); // Set the message to be displayed
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
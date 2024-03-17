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

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityProfileEducationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfileEducationActivity extends AppCompatActivity {
    RoomDB roomDB;
    ActivityProfileEducationBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    String userId;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileEducationBinding.inflate(getLayoutInflater());
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
        String course = binding.etCourse.getText().toString();
        String university = binding.etUniversity.getText().toString();
        String field = binding.etField.getText().toString();
        String grade = binding.etGrade.getText().toString();
        String year = binding.etYear.getText().toString();

// Now you can use these String variables as needed, such as storing them in a Map
        Map<String, Object> updates = new HashMap<>();
        updates.put("degree", course);
        updates.put("university", university);
        updates.put("field", field);
        updates.put("grade", grade);
        updates.put("year", year);

// Update the user data in the database
        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    roomDB.dao().updateUserEducation(course,university,field,grade,year);
                    dismissProgressDialog();
//                    Intent i = new Intent(ProfileEducationActivity.this, MainActivity.class);
//                    i.putExtra("goToProfileFragment", true);
//                    startActivity(i);
                    finish();
                    Log.d(TAG, "onComplete: User data updated in firebase");
                }else{
                    dismissProgressDialog();
                    Toast.makeText(ProfileEducationActivity.this, "data could not be updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                }
            }
        });

    }

    private boolean isValid(){
        if(binding.etCourse.getText().toString().isEmpty()){
            binding.etCourse.setError("Course Cannot Be Empty");
            return false;
        }
        if(binding.etUniversity.getText().toString().isEmpty()){
            binding.etUniversity.setError("University Cannot Be Empty");
            return false;
        }
        if(binding.etField.getText().toString().isEmpty()){
            binding.etField.setError("Field Cannot Be Empty");
            return false;
        }
        if(binding.etGrade.getText().toString().isEmpty()){
            binding.etGrade.setError("Grade Cannot Be Empty");
            return false;
        }
        if(binding.etYear.getText().toString().isEmpty()){
            binding.etYear.setError("Year Cannot Be Empty");
            return false;
        }
        return true;
    }

    private void fetchData(){
        User user = roomDB.dao().getUser();
        if(user != null){
            if(user.getDegree() !=  null && !user.getDegree().isEmpty()){
                binding.etCourse.setText(user.getDegree().toString());
                binding.etUniversity.setText(user.getUniversity().toString());
                binding.etField.setText(user.getField().toString());
                if(user.getGrade() != null){
                    binding.etGrade.setText(user.getGrade().toString());
                }
                if(user.getYear() != null){
                    binding.etYear.setText(user.getYear().toString());
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
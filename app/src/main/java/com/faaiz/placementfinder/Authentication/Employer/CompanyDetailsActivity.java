package com.faaiz.placementfinder.Authentication.Employer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityCompanyDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CompanyDetailsActivity extends AppCompatActivity {

    ActivityCompanyDetailsBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    String user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCompanyDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Employers");

        user_id = getUserId();

        retrieveData();

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValid()){
                    saveData();
                    Intent i = new Intent(CompanyDetailsActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
    }

    private String getUserId(){
        MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
        String userId = mySharedPreferences.getUserId();
        if (userId == null) {
            userId = auth.getCurrentUser().getUid();
        }
        return userId;
    }

    private void saveData(){
        // Create a map to hold the updated user data
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", binding.etName.getText().toString());
        updates.put("companyName", binding.etCompanyName.getText().toString());
        updates.put("companyAddress", binding.etAddress.getText().toString());
        updates.put("hasEnteredCompanyDetails", true);

// Update the user data in the database
        reference.child(user_id).updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    MySharedPreferences sp = new MySharedPreferences(CompanyDetailsActivity.this);
                    sp.saveUserProgress("mainActivity");
                    Log.d(TAG, "onComplete: User data updates in firebase");
                }else{
                    Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                }
            }
        });
    }

    private void retrieveData(){
        reference.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Employer data exists
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String mobile = dataSnapshot.child("mobile").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);

                    // Now you have the name, mobile, and email of the employer
                    // Use this data as needed
                    binding.etName.setText(name);
                    binding.etEmail.setText(email);
                    binding.etPhone.setText(mobile);
                } else {
                    // Employer data doesn't exist
                    // Handle this case accordingly
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors or cancellation
            }
        });

    }

    private boolean isValid() {
        String company = binding.etCompanyName.getText().toString();
        String name = binding.etName.getText().toString();
        String address = binding.etAddress.getText().toString();

        if(company.isEmpty() || isBlank(company)){
            binding.etCompanyName.setError("Company Name is invalid");
            binding.etCompanyName.requestFocus();
            return false;
        }

        if(name.length() < 5 || isBlank(name)){
            binding.etName.setError("Name is not valid");
            binding.etName.requestFocus();
            return false;
        }

        if(address.isEmpty() || isBlank(address)){
            binding.etAddress.setError("Company Name is invalid");
            binding.etAddress.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean isBlank(String s){
        for(int i=0; i<s.length(); i++){
            if(s.charAt(i) != ' ') return false;
        }
        return true;
    }
}
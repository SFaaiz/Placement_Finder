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
import com.faaiz.placementfinder.databinding.ActivityPersonalDetailsBinding;
import com.faaiz.placementfinder.databinding.ActivityProfileExperienceBinding;
import com.faaiz.placementfinder.databinding.ActivityProfilePersonalDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ProfilePersonalDetailsActivity extends AppCompatActivity {

    RoomDB roomDB;
    ActivityProfilePersonalDetailsBinding binding;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    String userId;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilePersonalDetailsBinding.inflate(getLayoutInflater());
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
        String name = binding.etName.getText().toString();
        String contact = binding.etContact.getText().toString();
        String location = binding.etLocation.getText().toString();

// Now you can use these String variables as needed, such as storing them in a Map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("mobile", contact);
        updates.put("location", location);

// Update the user data in the database
        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    roomDB.dao().updateUserPersonal(name,contact,location);
                    dismissProgressDialog();
//                    Intent i = new Intent(ProfilePersonalDetailsActivity.this, MainActivity.class);
//                    i.putExtra("goToProfileFragment", true);
//                    startActivity(i);
                    finish();
                    Log.d(TAG, "onComplete: User data updated in firebase");
                }else{
                    dismissProgressDialog();
                    Toast.makeText(ProfilePersonalDetailsActivity.this, "data could not be updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                }
            }
        });

    }

    private boolean isValid(){
        if(binding.etName.getText().toString().isEmpty()){
            binding.etName.setError("Name Cannot Be Empty");
            return false;
        }
        if(binding.etContact.getText().toString().isEmpty()){
            binding.etContact.setError("Contact Cannot Be Empty");
            return false;
        }
        if(binding.etLocation.getText().toString().isEmpty()){
            binding.etLocation.setError("Location Cannot Be Empty");
            return false;
        }
        return true;
    }

    private void fetchData(){
        User user = roomDB.dao().getUser();
        if(user != null){
            if(user.getName() !=  null && !user.getName().isEmpty()){
                binding.etName.setText(user.getName().toString());
                binding.etContact.setText(user.getMobile().toString());
                binding.etLocation.setText(user.getLocation().toString());
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
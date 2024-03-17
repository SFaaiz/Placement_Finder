package com.faaiz.placementfinder.Profile;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.faaiz.placementfinder.Converters;
import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityProfileEducationBinding;
import com.faaiz.placementfinder.databinding.ActivityProfileSkillsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

public class ProfileSkillsActivity extends AppCompatActivity {

    ActivityProfileSkillsBinding binding;
    RoomDB roomDB;
    FirebaseAuth mAuth;
    DatabaseReference reference;
    String userId;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileSkillsBinding.inflate(getLayoutInflater());
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

        List<String> skillsArray = getSkillsList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, skillsArray);
        binding.autoCompleteTextView.setAdapter(adapter);

        binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
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
                Toast.makeText(getApplicationContext(), "Skill already added", Toast.LENGTH_SHORT).show();
            } else {
                addChip(selectedSkill);
            }

            binding.autoCompleteTextView.setText(""); // Clear the text after selection
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

    private void addChip(String skill) {
        Chip chip = new Chip(this);
        chip.setText(skill);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> binding.chipGroup.removeView(chip));
        binding.chipGroup.addView(chip);
    }

    private void saveData(){
        List<String> chipTextList = new ArrayList<>();

// Loop through the child views of the ChipGroup
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            View childView = binding.chipGroup.getChildAt(i);

            // Check if the child view is an instance of Chip
            if (childView instanceof Chip) {
                Chip chip = (Chip) childView;
                chipTextList.add(chip.getText().toString());
            }
        }


// Now you can use these String variables as needed, such as storing them in a Map
        Map<String, Object> updates = new HashMap<>();
        updates.put("skills", chipTextList);

// Update the user data in the database
        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    roomDB.dao().updateSkills(chipTextList);
                    dismissProgressDialog();
//                    Intent i = new Intent(ProfileSkillsActivity.this, MainActivity.class);
//                    i.putExtra("goToProfileFragment", true);
//                    startActivity(i);
                    finish();
                    Log.d(TAG, "onComplete: User data updated in firebase");
                }else{
                    dismissProgressDialog();
                    Toast.makeText(ProfileSkillsActivity.this, "data could not be updated", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                }
            }
        });

    }

    private boolean isValid(){
        if(binding.chipGroup.getChildCount() > 0){
            return true;
        }
        return false;
    }

    private void fetchData(){
        User user = roomDB.dao().getUser();
        if(user != null){
            if(user.getSkills() != null && !user.getSkills().isEmpty()){
                for(String item: user.getSkills()){
                    Chip chip = new Chip(this);
                    chip.setText(item);
                    chip.setCloseIconVisible(true);
                    chip.setOnCloseIconClickListener(v -> binding.chipGroup.removeView(chip));
                    binding.chipGroup.addView(chip);
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
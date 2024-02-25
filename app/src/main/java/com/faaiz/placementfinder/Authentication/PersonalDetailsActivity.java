package com.faaiz.placementfinder.Authentication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityPersonalDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalDetailsActivity extends AppCompatActivity {

    ActivityPersonalDetailsBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        boolean isGoogleRegistration = i.getBooleanExtra("isGoogleRegistration", false);

        if(!isGoogleRegistration){
            binding.etNameLayout.setVisibility(View.GONE);
            binding.etMobileLayout.setVisibility(View.GONE);
        }

        // Read university names from the text file based on certain conditions
        List<String> universityNames = readDatasetFromRawFile(R.raw.dataset1, "# University Names");
        System.out.println(universityNames);
        List<String> degrees = readDatasetFromRawFile(R.raw.dataset1, "# Degrees");
        List<String> fields = readDatasetFromRawFile(R.raw.dataset1, "# Fields");

        // Create an ArrayAdapter using the list of university names
        ArrayAdapter<String> adapterUniversity = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, universityNames);
        ArrayAdapter<String> adapterDegrees = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, degrees);
        ArrayAdapter<String> adapterFields = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, fields);

        // Set the adapter for the AutoCompleteTextView
        binding.actvUniversity.setAdapter(adapterUniversity);
        binding.actvCourse.setAdapter(adapterDegrees);
        binding.actvField.setAdapter(adapterFields);

        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.actvUniversity.getText().toString().trim().isEmpty()){
                    binding.actvUniversity.setError("Please Select An Option");
                }
                else if(binding.actvCourse.getText().toString().trim().isEmpty()){
                    binding.actvUniversity.setError("Please Select An Option");
                }
                else if(binding.actvLocation.getText().toString().trim().isEmpty()){
                    binding.actvUniversity.setError("Please Select An Option");
                }
                else{
                    if(isGoogleRegistration){
                        if(!isValidUser(binding.etName.getText().toString(), binding.etMobile.getText().toString())){
                            return;
                        }
                    }
                    // Later, when the user enters personal details, update hasEnteredPersonalDetails to true
                    String userId = auth.getCurrentUser().getUid();
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                    System.out.println("Now hasEnteredPersonalDetails will be updated");
                    // Assume you want to update hasEnteredPersonalDetails to true
//                    userRef.child("hasEnteredPersonalDetails").setValue(true)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "onComplete: Data has been updated");
//                                    } else {
//                                        Log.d(TAG, "onComplete: Failure " + task.getException());
//                                    }
//                                }
//                            });

                    // Create a map to hold the updated user data
                    Map<String, Object> updates = new HashMap<>();
                    if(isGoogleRegistration){
                        updates.put("name", binding.etName.getText().toString());
                        updates.put("mobile",binding.etMobile.getText().toString());
                    }
                    updates.put("university", binding.actvUniversity.getText().toString());
                    updates.put("degree", binding.actvCourse.getText().toString());
                    updates.put("field", binding.actvField.getText().toString());
                    updates.put("email",auth.getCurrentUser().getEmail());
                    updates.put("location", binding.actvLocation.getText().toString());
                    updates.put("hasEnteredPersonalDetails", true); // Assuming this is how you update this field

// Update the user data in the database
                    userRef.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                MySharedPreferences sp = new MySharedPreferences(PersonalDetailsActivity.this);
                                sp.saveUserProgress("mainActivity");
                                Log.d(TAG, "onComplete: User data updates in firebase");
                            }else{
                                Log.d(TAG, "onComplete: user data could not be updated, " + task.getException());
                            }
                        }
                    });

                    Intent i = new Intent(PersonalDetailsActivity.this, MainActivity.class);
                    startActivity(i);

                    finish();
                }
            }
        });

    }

    private boolean isValidUser(String name, String mobile) {
        if (name.length() < 5 || containsNonAlphabetic(name) || isBlank(name)) {
            binding.etName.setError("Name is not valid");
            binding.etName.requestFocus();
            return false;
        } else if (mobile.length() != 10 || containsNonDigit(mobile)) {
            binding.etMobile.setError("Mobile No. is not valid");
            binding.etMobile.requestFocus();
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

    private static boolean containsNonDigit(String input) {
        // Define a regular expression pattern that matches anything other than 0-9
        Pattern pattern = Pattern.compile("[^0-9]");

        // Create a matcher object for the input string
        Matcher matcher = pattern.matcher(input);

        // Return true if a match is found, indicating non-digit characters
        return matcher.find();
    }

    private static boolean containsNonAlphabetic(String input) {
        // Define a regular expression pattern that matches anything other than a-z (case-insensitive) and space
        Pattern pattern = Pattern.compile("[^a-zA-Z\\s]");

        // Create a matcher object for the input string
        Matcher matcher = pattern.matcher(input);

        // Return true if a match is found, indicating non-alphabetic characters
        return matcher.find();
    }


    private List<String> readDatasetFromRawFile(int rawResourceId, String datasetMarker) {
        List<String> dataset = new ArrayList<>();
        boolean isReadingDataset = false;

        try {
            // Open the file from the res/raw folder
            InputStream inputStream = getResources().openRawResource(rawResourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            // Read each line and add it to the list when the marker is encountered and conditions are met
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(datasetMarker)) {
                    isReadingDataset = true;
                } else if (isReadingDataset && line.trim().isEmpty()) {
                    isReadingDataset = false;
                } else if (isReadingDataset) {
                    // Add the line to the dataset if conditions are met
                    dataset.add(line);
                }
            }

            // Close the resources
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataset;
    }

}
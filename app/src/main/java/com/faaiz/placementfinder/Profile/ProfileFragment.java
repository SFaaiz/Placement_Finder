package com.faaiz.placementfinder.Profile;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.skydoves.expandablelayout.ExpandableLayout;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        sp = new MySharedPreferences(requireContext());
        roomDB = RoomDB.getInstance(requireContext());
    }

    @NonNull FragmentProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase db;
    MySharedPreferences sp;
    Uri imageUri;
    String userType;
    RoomDB roomDB;
    private ExpandableLayout expandableLayout;
    private TextView titleTextView;
    private EditText contentEditText;
    ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        userType = sp.getUserType();

//        if(user.getDisplayName()!=null || !user.getDisplayName().isEmpty()){
//            binding.tvDisplayName.setText(user.getDisplayName());
//        }
        binding.tvEmailId.setText(user.getEmail());

        setUserData();


        binding.btnPersonalDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(requireContext(), ProfilePersonalDetailsActivity.class);
                startActivity(i);
            }
        });
        binding.btnEducationalDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(requireContext(), ProfileEducationActivity.class);
                startActivity(i);
            }
        });
        binding.btnExperienceDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(requireContext(), ProfileExperienceActivity.class);
                startActivity(i);
            }
        });
        binding.btnSkillDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(requireContext(), ProfileSkillsActivity.class);
                startActivity(i);
            }
        });
        binding.btnProjectDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(requireContext(), ProfileProjectActivity.class);
                startActivity(i);
            }
        });
        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        binding.viewResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkProfileProgress()){
                    Intent i = new Intent(requireContext(), ResumeActivity.class);
                    startActivity(i);
                }else{
                    Toast.makeText(requireContext(), "Please Complete Your Profile", Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.saveEmpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isValidEmployer()){
                    showProgressDialog();
                    saveEmployerData();
                }
            }
        });
        return view;
    }

    private void saveEmployerData() {
        String companyName = binding.etCompanyName.getText().toString();
        String personName = binding.etPersonName.getText().toString();
        String phone = binding.etPhone.getText().toString();
        String address = binding.etAddress.getText().toString();
        String companyDesc = binding.etCompDesc.getText().toString();

        Map<String, Object> updates = new HashMap<>();
        updates.put("companyName", companyName);
        updates.put("name", personName);
        updates.put("mobile", phone);
        updates.put("companyAddress", address);
        updates.put("companyDescription", companyDesc);

        DatabaseReference empRef = db.getReference("Employers").child(mAuth.getCurrentUser().getUid());
        empRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update the local Room database
                roomDB.dao().updateEmpCompDetails(personName,phone,companyName,address,companyDesc);

                dismissProgressDialog();
                clearFocus();
                Log.d(TAG, "onComplete: User data updated in Firebase");
                Toast.makeText(requireContext(), "Data Saved Successfully", Toast.LENGTH_SHORT).show();
            } else {
                dismissProgressDialog();
                Toast.makeText(requireContext(), "Data could not be updated", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onComplete: User data could not be updated: " + task.getException());
            }
        });
    }

    private void clearFocus(){
        binding.etCompanyName.clearFocus();
        binding.etPersonName.clearFocus();
        binding.etPhone.clearFocus();
        binding.etAddress.clearFocus();
        binding.etCompDesc.clearFocus();
    }


    private boolean checkProfileProgress(){
        User user = roomDB.dao().getUser();
        if(user.getGrade()==null || user.getGrade().isEmpty() || user.getCompanyName()==null || user.getCompanyName().isEmpty() || user.getSkills()==null || user.getSkills().size()==0 || user.getProjectTitle()==null || user.getProjectTitle().isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    private void setUserData(){
        if(userType.equals("user")){
            binding.employerContent.setVisibility(View.GONE);
            binding.profileContent.setVisibility(View.VISIBLE);
            User user = roomDB.dao().getUser();
            if (user != null) {
                // User data received, update UI or perform other actions
                binding.tvDisplayName.setText(user.getName());
                setUserProfile(user.getProfilePhotoUrl());
                Log.d(TAG, "username = " + user.getName());
            } else {
                Log.d(TAG, "User data is null");
            }
        }else{
            binding.profileContent.setVisibility(View.GONE);
            binding.employerContent.setVisibility(View.VISIBLE);
            Employer employer = roomDB.dao().getEmployer();
            if (employer != null) {
                // User data received, update UI or perform other actions
                binding.tvDisplayName.setText(employer.getName());
                setUserProfile(employer.getProfilePhotoUrl());
                fetchEmployerData();
                Log.d(TAG, "username = " + employer.getName());
            } else {
                Log.d(TAG, "User data is null");
            }
        }
    }

    private boolean isValidEmployer() {
        if (binding.etCompanyName.getText().toString().isEmpty()) {
            binding.etCompanyName.setError("Company Name Cannot Be Empty");
            return false;
        }
        if (binding.etPersonName.getText().toString().isEmpty()) {
            binding.etPersonName.setError("Person Name Cannot Be Empty");
            return false;
        }
        if (binding.etPhone.getText().toString().isEmpty()) {
            binding.etPhone.setError("Phone Cannot Be Empty");
            return false;
        }
        if (binding.etAddress.getText().toString().isEmpty()) {
            binding.etAddress.setError("Address Cannot Be Empty");
            return false;
        }
        if (binding.etCompDesc.getText().toString().isEmpty()) {
            binding.etCompDesc.setError("Company Description Cannot Be Empty");
            return false;
        }
        return true;
    }


    private void fetchEmployerData(){
        Employer employer = roomDB.dao().getEmployer();
        if(employer != null){
            binding.etCompanyName.setText(employer.getCompanyName());
            binding.etPersonName.setText(employer.getName());
            binding.etPhone.setText(employer.getMobile());
            binding.etAddress.setText(employer.getCompanyAddress());
            if(employer.getCompanyDescription() != null && !employer.getCompanyDescription().isEmpty()){
                binding.etCompDesc.setText(employer.getCompanyDescription());
            }

        }else{
            Log.d(TAG, "fetchData: Employer is null");
        }

    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(requireContext()); // Replace 'this' with your activity or fragment context
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

    private void setUserProfile(String profilePhotoUrl){
        Log.d(TAG, "setUserProfile: " + profilePhotoUrl);
        if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
            Picasso.get()
                    .load(profilePhotoUrl)
                    .placeholder(R.drawable.profile) // Placeholder while loading
                    .error(R.drawable.profile) // Error placeholder
                    .into(binding.profilePhoto);
        } else {
            // If user has not uploaded a profile photo, try loading Gmail profile photo
            Uri gmailProfilePhotoUrl = user.getPhotoUrl();
            if (gmailProfilePhotoUrl != null) {
                Picasso.get()
                        .load(gmailProfilePhotoUrl)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.profilePhoto);
            } else {
                // If neither Firebase nor Gmail profile photo is available, load default image from drawable
                Picasso.get()
                        .load(R.drawable.profile)
                        .into(binding.profilePhoto);
            }
        }

    }

    static final int PICK_IMAGE = 10;
    private void selectImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK && data!=null) {
            imageUri = data.getData();
            Glide.with(ProfileFragment.this)
                    .load(imageUri)
                    .into(binding.profilePhoto);
            uploadImageToFirebaseStorage();
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (imageUri != null) {
            // Get a reference to the Firebase Storage location where you want to store the image
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images");

            // Create a reference to the file to upload
            StorageReference imageRef = storageRef.child(generateImageFileName());

            // Upload the file to Firebase Storage
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Image uploaded successfully, now get the image URL
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Get the image URL and store it under the user node in Firebase Database
                            String imageUrl = uri.toString();
                            saveImageUrlToFirebaseDatabase(imageUrl);
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred during the upload process
                        Log.e(TAG, "Error uploading image to Firebase Storage: " + e.getMessage());
                    });
        }
    }

    private String generateImageFileName() {
        // Get the current timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Generate a random string (you can customize the length as needed)
        String randomString = UUID.randomUUID().toString().substring(0, 6); // Using the first 6 characters of the UUID

        // Concatenate the timestamp and random string to create the file name
        return "image_" + timeStamp + "_" + randomString + ".jpg";
    }


    private void saveImageUrlToFirebaseDatabase(String imageUrl) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef;
            if(userType.equals("user")){
                userRef = db.getReference("Users").child(userId);
            }else{
                userRef = db.getReference("Employers").child(userId);
            }

            // Update the user node with the image URL
            userRef.child("profilePhotoUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        // Image URL saved successfully
                        if(userType.equals("user")){
                            roomDB.dao().updateProfile(imageUrl);
                        }else{
                            roomDB.dao().updateEmployerProfile(imageUrl);
                        }

                        Log.d(TAG, "Image URL saved to Firebase Database: " + imageUrl);
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred while saving the image URL
                        Log.e(TAG, "Error saving image URL to Firebase Database: " + e.getMessage());
                    });
        }
    }

}
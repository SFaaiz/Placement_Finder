package com.faaiz.placementfinder.Profile;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.content.Intent;
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
import java.util.Locale;
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



//        titleTextView.setOnClickListener(v -> {
//            if (expandableLayout.isExpanded()) {
//                expandableLayout.collapse();
//                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, com.skydoves.expandablelayout.R.drawable.ic_arrow_down, 0);
//            } else {
//                expandableLayout.expand();
//                titleTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, com.skydoves.expandablelayout.R.drawable.ic_arrow_down, 0);
//            }
//        });
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
                Intent i = new Intent(requireContext(), ResumeActivity.class);
                startActivity(i);
            }
        });
        return view;
    }

    private void setUserData(){
        if(userType.equals("user")){
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
            Employer employer = roomDB.dao().getEmployer();
            if (employer != null) {
                // User data received, update UI or perform other actions
                binding.tvDisplayName.setText(employer.getName());
                setUserProfile(employer.getProfilePhotoUrl());
                Log.d(TAG, "username = " + employer.getName());
            } else {
                Log.d(TAG, "User data is null");
            }
        }
    }

    private void setUserProfile(String profilePhotoUrl){
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
                        roomDB.dao().updateProfile(imageUrl);
                        Log.d(TAG, "Image URL saved to Firebase Database: " + imageUrl);
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors that occurred while saving the image URL
                        Log.e(TAG, "Error saving image URL to Firebase Database: " + e.getMessage());
                    });
        }
    }

}
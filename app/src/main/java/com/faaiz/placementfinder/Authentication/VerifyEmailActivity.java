package com.faaiz.placementfinder.Authentication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.Authentication.Employer.MobileVerificationActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityVerifyEmailBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VerifyEmailActivity extends AppCompatActivity {

    ActivityVerifyEmailBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifyEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Log.d(TAG, "onSuccess: Email verification link is sent");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "onFailure: Link couldn't be sent");
                            }
                        });

        final boolean[] flag = {false};
        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        boolean isVerified = checkEmailVerificationStatus();
                        if(isVerified){
                            checkUserStatusAndNavigate();
                        }else{
                            if(!flag[0]){
                                flag[0] =true;
                                run();
                            }else{
                                Toast.makeText(VerifyEmailActivity.this, "Please Verify Your Email First", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, 1000);


            }
        });
    }

    private void checkUserStatusAndNavigate(){
        String userId = mAuth.getCurrentUser().getUid(); // Replace with the actual user ID

        DatabaseReference regularUserRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference employerRef = FirebaseDatabase.getInstance().getReference("Employers").child(userId);

        MySharedPreferences sp = new MySharedPreferences(this);

        regularUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data exists under "Users" path
                    // This user is a regular user
                    // Proceed with regular user logic
                    sp.saveUserProgress("personalDetailsActivity");
                    Intent i =new Intent(VerifyEmailActivity.this, PersonalDetailsActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    // Data doesn't exist under "Users" path
                    // Check if data exists under "Employers" path
                    employerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Data exists under "Employers" path
                                // This user is an employer
                                // Proceed with employer logic
                                sp.saveUserProgress("mobileVerificationActivity");
                                Intent i =new Intent(VerifyEmailActivity.this, MobileVerificationActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                // No data exists under "Employers" path
                                // This user doesn't exist as a regular user or an employer
                                // Handle the case accordingly
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors or cancellation
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors or cancellation
            }
        });

    }

    boolean flag=false;
    private boolean checkEmailVerificationStatus() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d(TAG, "onClick: " + firebaseUser.getEmail() + "\n" + firebaseUser.getDisplayName());
        firebaseUser.reload();
        if(!flag) {
            flag = true;
            checkEmailVerificationStatus();
        }
        return firebaseUser.isEmailVerified();
    }
}
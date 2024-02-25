package com.faaiz.placementfinder.Authentication.Employer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityMobileVerificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    ActivityMobileVerificationBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    String mVerificationId;
    String user_id;
    FirebaseUser firebaseUser;
    String mobile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Employers");

        user_id = getUserId();
        firebaseUser = auth.getCurrentUser();

        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        binding.sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCodeSent){
                    binding.progressBar.setVisibility(View.VISIBLE);
                }
                mobile = binding.etMobile.getText().toString();
                if(mobile.length() == 10){

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(auth)
                                    .setPhoneNumber("+91" + mobile)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(MobileVerificationActivity.this)                 // (optional) Activity for callback binding
                                    // If no activity is passed, reCAPTCHA verification can not be used.
                                    .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }else{
                    binding.etMobile.setError("Mobile Number should be 10 digit");
                    binding.etMobile.requestFocus();
                }
            }
        });

        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = binding.etOtp.getText().toString();
                verifyOTP(mVerificationId, otp);
            }
        });


    }

    private CompletableFuture<String> getMobile() {
        CompletableFuture<String> future = new CompletableFuture<>();
        reference.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String mob = "";
                if (dataSnapshot.exists()) {
                    Employer employer = dataSnapshot.getValue(Employer.class);
                    if (employer != null) {
                        mob = employer.getMobile();
                    }
                }
                future.complete(mob);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return future;
    }

    boolean isCodeSent = false;

    private String getUserId(){
        MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
        String userId = mySharedPreferences.getUserId();
        if (userId == null) {
            userId = auth.getCurrentUser().getUid();
        }
        return userId;
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);
            binding.progressBar.setVisibility(View.GONE);

            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);
            binding.progressBar.setVisibility(View.GONE);

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                Log.d(TAG, "onVerificationFailed: Invalid request");
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d(TAG, "onVerificationFailed: The SMS quota for the project has been exceeded");
            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);
            binding.progressBar.setVisibility(View.GONE);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            isCodeSent = true;
//            mResendToken = token;

//            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        }
    };

    private void verifyOTP(String verificationId, String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Toast.makeText(MobileVerificationActivity.this, "Phone Verified Successfully", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();
                            String mobileNo = user.getPhoneNumber().substring(3);

                            auth.updateCurrentUser(firebaseUser);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("mobileVerified", true);
                            updates.put("mobile", mobileNo);

                            // update phone verification status
                            reference.child(user_id).updateChildren(updates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        MySharedPreferences sp = new MySharedPreferences(MobileVerificationActivity.this);
                                        sp.saveUserProgress("companyDetailsActivity");
                                        Log.d(TAG, "onComplete: Data has been updated");
                                    } else {
                                        Log.d(TAG, "onComplete: Failure " + task.getException());
                                    }
                                }
                            });

                            // Update UI
                            Intent i = new Intent(MobileVerificationActivity.this, CompanyDetailsActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(MobileVerificationActivity.this, "The verification code entered was invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }



}
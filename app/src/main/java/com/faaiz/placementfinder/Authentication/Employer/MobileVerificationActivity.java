package com.faaiz.placementfinder.Authentication.Employer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MobileVerificationActivity extends AppCompatActivity {

    ActivityMobileVerificationBinding binding;
    FirebaseAuth auth;
    DatabaseReference reference;
    String mVerificationId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("Employers");

        binding.proceedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        binding.sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = binding.etMobile.getText().toString();
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

            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);

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

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
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
                            String userId = auth.getCurrentUser().getUid();

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("mobileVerified", true);

                            // update phone verification status
                            reference.child(userId).updateChildren(updates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
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
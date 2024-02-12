package com.faaiz.placementfinder.Authentication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.Authentication.Employer.CompanyDetailsActivity;
import com.faaiz.placementfinder.Authentication.Employer.MobileVerificationActivity;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityRegistrationBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    ActivityRegistrationBinding binding;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    ProgressDialog progressDialog;
    FirebaseDatabase db;
    DatabaseReference reference;
    DatabaseReference employerReference;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    boolean isEmployer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if the email is verified
            if (!currentUser.isEmailVerified()) {
                // Navigate to VerifyEmailActivity
                startActivity(new Intent(RegistrationActivity.this, VerifyEmailActivity.class));
                finish();  // Finish the current activity to prevent going back
            } else {
                checkPersonalDetailsStatusAndNavigate(false);
            }
        } else {
            // User is not authenticated, show the login screen
            setContentView(binding.getRoot()); // Set your login layout here
            // Further code for handling login, authentication, etc.
        }


        isEmployer = getIntent().getBooleanExtra("isEmployer", false);

        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");
        employerReference = db.getReference("Employers");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e("Google Sign In", "Connection failed: " + connectionResult.getErrorMessage());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        binding.googleSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog();
                signUpWithGoogle();
            }
        });

//        Log.d("TAG", "onCreate: " + FirebaseAuth.getInstance().getCurrentUser().getEmail());



        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.etName.getText().toString();
                String mobile = binding.etMobile.getText().toString();
                String email = binding.etEmail.getText().toString();
                String password = binding.etPassword.getText().toString();

                if(isValidUser(name, mobile, email, password)){
                    showLoadingDialog();
                    // Call this method when you want to create a new user
                    createUserWithEmailAndPassword(email, mobile, name, password);
                }


            }
        });

        binding.btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    private CompletableFuture<Boolean> hasEnteredPersonalDetails() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean hasEnteredDetails = false;
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        hasEnteredDetails = user.isHasEnteredPersonalDetails();
                    }
                }
                future.complete(hasEnteredDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }


    boolean isMobileVerified = false;
    boolean hasEnteredCompanyDetails = false;
    private CompletableFuture<Boolean> checkEmployerProgress() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Employers").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Employer employer = dataSnapshot.getValue(Employer.class);
                    if (employer != null) {
                        isMobileVerified = employer.isMobileVerified();
                        hasEnteredCompanyDetails = employer.isHasEnteredCompanyDetails();
                        Log.d(TAG, "onDataChange: isMobileVerified : " + isMobileVerified + ", hasEnteredCompanyDetails : " + hasEnteredCompanyDetails);
                    }
                }
                future.complete(isMobileVerified);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    private void navigateEmployer(){
        if(!isMobileVerified){
            Intent i = new Intent(RegistrationActivity.this, MobileVerificationActivity.class);
            startActivity(i);
        }
        else if(!hasEnteredCompanyDetails){
            Intent i = new Intent(RegistrationActivity.this, CompanyDetailsActivity.class);
            startActivity(i);
        }
        else{
            Intent i = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    private void showLoadingDialog(){
        progressDialog = new ProgressDialog(RegistrationActivity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setTitle("Logging you in");
        progressDialog.show();
    }

    private void signUpWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        progressDialog.dismiss();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e("Google Sign Up", "Failed. " + result.getStatus().getStatusMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d("Google Sign Up", "signUpWithCredential:success");
                            Toast.makeText(RegistrationActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            // You can get the user information using: task.getResult().getUser()
                            if(isEmployer){
                                checkEmployerProgress();
                                navigateEmployer();
                                return;
                            }

                            saveDataInFireBase();
                            checkPersonalDetailsStatusAndNavigate(true);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google Sign Up", "signUpWithCredential:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Sign Up Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveDataInFireBase(){
        String userId = mAuth.getCurrentUser().getUid();
//        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data exists for this user
                    // Proceed with your logic here
                    Log.d(TAG, "onDataChange: Existing user");
                } else {
                    // No data exists for this user
                    // Handle the case accordingly
                    Log.d(TAG, "onDataChange: New user");
                    User newUser = new User("", "", "", "", "", "", "", false);

                    // Save the new user to the database
                    reference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "onComplete: Data saved into database");
                            }else{
                                Log.d(TAG, "onComplete: Data couldn't be saved into database, " + task.getException());
                            }
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

    private void checkPersonalDetailsStatusAndNavigate(boolean isGoogleRegistration){
        // Call the method and handle the CompletableFuture
        CompletableFuture<Boolean> hasEnteredDetailsFuture = hasEnteredPersonalDetails();
        // Attach a listener to the CompletableFuture
        hasEnteredDetailsFuture.thenAccept(hasEnteredDetails -> {
            Log.d(TAG, "hasEnteredPersonalDetails: true or false --> " + hasEnteredDetails);

            // Inside this block, you can now use the retrieved boolean value
            if (hasEnteredDetails) {
                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                // You can navigate to another activity or perform further actions here
                Intent i = new Intent(RegistrationActivity.this, PersonalDetailsActivity.class);
                i.putExtra("isGoogleRegistration", isGoogleRegistration);
                startActivity(i);
            }
            finish();
        });
    }


    private boolean isValidUser(String name, String mobile, String email, String password){
        if(name.length() < 5 || containsNonAlphabetic(name) || isBlank(name)){
            binding.etName.setError("Name is not valid");
            binding.etName.requestFocus();
            return false;
        }
        else if(mobile.length() != 10 || containsNonDigit(mobile)){
            binding.etMobile.setError("Mobile No. is not valid");
            binding.etMobile.requestFocus();
            return false;
        }
        else if(!isValidEmail(email)){
            binding.etEmail.setError("Email address is invalid");
            binding.etEmail.requestFocus();
            return false;
        }
        else if(password.length() < 8){
            binding.etPassword.setError("Password must be at least 8 characters long");
            binding.etPassword.requestFocus();
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

    public static boolean isValidEmail(String email) {
        // Define the email pattern using a regular expression
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(emailRegex);

        // Create a matcher with the input email string
        Matcher matcher = pattern.matcher(email);

        // Check if the email matches the pattern
        return matcher.matches();
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

    private void createUserWithEmailAndPassword(String email, String mobile, String name, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            // Sign up success
                            Log.d("SignUpActivity", "createUserWithEmail:success");
                            Toast.makeText(RegistrationActivity.this, "Welcome! " + name,
                                    Toast.LENGTH_SHORT).show();
                            String userId = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: userid " + userId);

                            if(isEmployer){
                                Employer employer = new Employer(name,email,mobile,false,false);
                                employerReference.child(userId).setValue(employer).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Log.d(TAG, "onComplete: Data has been saved into firebase");
                                        }else{
                                            Log.d(TAG, "onComplete: Failure " + task.getException());
                                        }
                                    }
                                });
                            }
                            else{
                                User user = new User(name, mobile, email, false);

                                // After successfully creating the user account and storing basic details, set hasEnteredPersonalDetails to false
                                if (mAuth.getCurrentUser() != null) {
                                    reference.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Log.d(TAG, "onComplete: Data has been saved into firebase");
                                            }else{
                                                Log.d(TAG, "onComplete: Failure " + task.getException());
                                            }
                                        }
                                    });
                                }
                            }


                            // You can navigate to another activity or perform further actions here
                            Intent i = new Intent(RegistrationActivity.this, VerifyEmailActivity.class);
                            startActivity(i);

                            finish();
                        } else {
                            // If sign up fails, display a message to the user.
                            String errorMsg = task.getException().getMessage();
                            Log.w("SignUpActivity", "createUserWithEmail:failure : " + errorMsg, task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid email format
                                Toast.makeText(RegistrationActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                // User with this email already exists
                                Toast.makeText(RegistrationActivity.this, "User with this email already exists", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseNetworkException) {
                                // Network error
                                Toast.makeText(RegistrationActivity.this, "Network error occurred", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other errors
                                Toast.makeText(RegistrationActivity.this, "An internal error has occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}
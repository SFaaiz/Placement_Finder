package com.faaiz.placementfinder.Authentication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.faaiz.placementfinder.Authentication.Employer.CompanyDetailsActivity;
import com.faaiz.placementfinder.Authentication.Employer.MobileVerificationActivity;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog progressDialog;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

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

        binding.googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog();
                signInWithGoogle();
            }
        });

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.etEmail.getText().toString();
                String password = binding.etPassword.getText().toString();

                if(isValidUser(email, password)){
                    showLoadingDialog();
                    // Call this method when you want to authenticate the user
                    signInWithEmailAndPassword(email, password);
                }


            }
        });

        binding.forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(i);

            }
        });

        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                boolean goToLoginType = intent.getBooleanExtra("goToLoginType",false);
                if(goToLoginType){
                    Intent i = new Intent(LoginActivity.this, LoginTypeActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
                Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(i);
                finish();
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

    private void showLoadingDialog(){
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setTitle("Logging you in");
        progressDialog.show();
    }

    private void signInWithGoogle() {
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
                Log.e("Google Sign In", "Failed. " + result.getStatus().getStatusMessage());
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
                            Log.d("Google Sign In", "signInWithCredential:success");
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            // saving user id to avoid conflict after phone auth
                            saveUserId();

                            // check user type and navigate accordingly
                            isEmployer();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google Sign In", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    private boolean isValidUser(String email, String password){
        if(!isValidEmail(email)){
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

    private void saveUserId(){
        MySharedPreferences mySharedPreferences = new MySharedPreferences(this);
        mySharedPreferences.saveUserId(mAuth.getCurrentUser().getUid());
    }

    private void signInWithEmailAndPassword(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            saveUserId();
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(!user.isEmailVerified()){
                                user.sendEmailVerification();
                                Toast.makeText(LoginActivity.this, "Please Verify Your Email First", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Sign in success
                            Log.d("LoginActivity", "signInWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Login successful!",
                                    Toast.LENGTH_SHORT).show();

                            isEmployer();


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginActivity", "signInWithEmail:failure , " + task.getException().getMessage(), task.getException());
                            // If the login fails
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // Invalid login credentials
                                Toast.makeText(LoginActivity.this, "Invalid login credentials", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                // User not found or disabled
                                Toast.makeText(LoginActivity.this, "User not found or disabled", Toast.LENGTH_SHORT).show();
                            } else if (task.getException() instanceof FirebaseNetworkException) {
                                // Network error
                                Toast.makeText(LoginActivity.this, "Network error occurred", Toast.LENGTH_SHORT).show();
                            } else {
                                // Other errors
                                Toast.makeText(LoginActivity.this, "An internal error has occurred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    boolean employer = false;
    private void isEmployer(){
        String userId = mAuth.getCurrentUser().getUid(); // Replace with the actual user ID

        DatabaseReference regularUserRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        DatabaseReference employerRef = FirebaseDatabase.getInstance().getReference("Employers").child(userId);
        MySharedPreferences sp = new MySharedPreferences(LoginActivity.this);

        regularUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Data exists under "Users" path
                    // This user is a regular user
                    sp.saveUserType("user");
                    employer = false;
                    Log.d(TAG, "onDataChange: checking employer status = " + employer);
                    navigateUser();
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
                                sp.saveUserType("employer");
                                employer = true;
                                Log.d(TAG, "onDataChange: checking employer status = " + employer);
                                checkEmployerProgress();
                            }else{
                                showOptionsBottomSheet();
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

    private void navigateUser(){
        // Call the method and handle the CompletableFuture
        CompletableFuture<Boolean> hasEnteredDetailsFuture = hasEnteredPersonalDetails();

        // Attach a listener to the CompletableFuture
        hasEnteredDetailsFuture.thenAccept(hasEnteredDetails -> {
            Log.d(TAG, "hasEnteredPersonalDetails: true or false --> " + hasEnteredDetails);

        MySharedPreferences sp = new MySharedPreferences(LoginActivity.this);

            // Inside this block, you can now use the retrieved boolean value
            if (hasEnteredDetails) {
                sp.saveUserProgress("mainActivity");
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                sp.saveUserProgress("personalDetailsActivity");
                // You can navigate to another activity or perform further actions here
                Intent i = new Intent(LoginActivity.this, PersonalDetailsActivity.class);
                startActivity(i);
            }
        });
    }

    // Method to show BottomSheetDialog with options
    private void showOptionsBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(view);

        // Find views and set click listeners for user and employer options
        Button userOption = view.findViewById(R.id.btn_user);
        Button employerOption = view.findViewById(R.id.btn_employer);
        userOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle user option click
                // Proceed as user
                bottomSheetDialog.dismiss();
                saveUserData(true);
            }
        });

        employerOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle employer option click
                // Proceed as employer
                bottomSheetDialog.dismiss();
                saveUserData(false);
            }
        });

        bottomSheetDialog.show();
    }

    private void saveUserData(boolean isUser) {
        DatabaseReference databaseReference;
        String userId = mAuth.getCurrentUser().getUid();
        if (isUser) {
            // Save data under "Users" node
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            User newUser = new User(mAuth.getCurrentUser().getDisplayName(), "", mAuth.getCurrentUser().getEmail(), "", "", "", "", false);
            databaseReference.setValue(newUser);
        } else {
            // Save data under "Employers" node
            databaseReference = FirebaseDatabase.getInstance().getReference("Employers").child(userId);
            Employer employer = new Employer(mAuth.getCurrentUser().getDisplayName(),mAuth.getCurrentUser().getEmail(),"",false,false);
            databaseReference.setValue(employer);
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Data saved successfully
                Log.d(TAG, "Data saved successfully for " + (isUser ? "user" : "employer"));
                if(isUser){
                    Intent i = new Intent(LoginActivity.this, PersonalDetailsActivity.class);
                    i.putExtra("isGoogleRegistration", true);
                    startActivity(i);
                }else{
                    Intent i = new Intent(LoginActivity.this, MobileVerificationActivity.class);
                    startActivity(i);
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to save data
                Log.e(TAG, "Failed to save data for " + (isUser ? "user" : "employer"), databaseError.toException());
            }
        });
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
                        navigateEmployer();
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
        MySharedPreferences sp = new MySharedPreferences(LoginActivity.this);
        if(!isMobileVerified){
            sp.saveUserProgress("mobileVerificationActivity");
            Intent i = new Intent(LoginActivity.this, MobileVerificationActivity.class);
            startActivity(i);
        }
        else if(!hasEnteredCompanyDetails){
            sp.saveUserProgress("companyDetailsActivity");
            Intent i = new Intent(LoginActivity.this, CompanyDetailsActivity.class);
            startActivity(i);
        }
        else{
            sp.saveUserProgress("mainActivity");
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
        finish();
    }
}
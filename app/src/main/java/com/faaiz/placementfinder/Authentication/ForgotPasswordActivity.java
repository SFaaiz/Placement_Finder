package com.faaiz.placementfinder.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityForgotPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForgotPasswordActivity extends AppCompatActivity {

    ActivityForgotPasswordBinding binding;
    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.etEmail.getText().toString();
                if(isValidEmail(email)){
                    sendPasswordResetEmail(email);
                }else{
                    Toast.makeText(ForgotPasswordActivity.this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Password reset email sent successfully
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Password reset email sent. Please check your email.",
                                    Toast.LENGTH_LONG).show();
                            Intent i = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            // Failed to send password reset email
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Failed to send password reset email. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

}
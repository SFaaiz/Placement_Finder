package com.faaiz.placementfinder.Authentication;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.faaiz.placementfinder.Authentication.Employer.CompanyDetailsActivity;
import com.faaiz.placementfinder.Authentication.Employer.MobileVerificationActivity;
import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.MySharedPreferences;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivitySplashScreenBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.CompletableFuture;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        binding.lottieAnim.animate();

        animateImageView();

        binding.lottieAnim
                .addAnimatorUpdateListener(
                        (animation) -> {
                            // Do something.
                        });
        binding.lottieAnim
                .playAnimation();

        Intent i;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        MySharedPreferences sp = new MySharedPreferences(this);

        if (currentUser != null) {
            String progress = sp.getUserProgress();
            switch(progress){
                case "emailActivity":
                    i = new Intent(SplashScreen.this, VerifyEmailActivity.class);
                    break;
                case "personalDetailsActivity":
                    i = new Intent(SplashScreen.this, PersonalDetailsActivity.class);
                    break;
                case "mobileVerificationActivity":
                    i = new Intent(SplashScreen.this, MobileVerificationActivity.class);
                    break;
                case "companyDetailsActivity":
                    i = new Intent(SplashScreen.this, CompanyDetailsActivity.class);
                    break;
                case "mainActivity":
                    i = new Intent(SplashScreen.this, MainActivity.class);
                    break;
                default:
                    i = new Intent();
            }
        } else {
            // User is not authenticated, show the login screen
            i = new Intent(SplashScreen.this, LoginTypeActivity.class);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(i);
                finish(); // this will be removed from the stack, upon pressing back u won't come back to it
            }
        }, 2500);

    }



    private void animateImageView() {
        // Set initial properties of the ImageView
        binding.titleLogo.setAlpha(0f);
        binding.titleLogo.setScaleX(0.5f);
        binding.titleLogo.setScaleY(0.5f);

        // Animate alpha property from 0 to 1 (0% to 100%) in 2 seconds
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(binding.titleLogo, "alpha", 0f, 1f);
        alphaAnimator.setDuration(2000);

        // Animate scaleX and scaleY properties from 0.5 to 1 (50% to 100%) in 2 seconds
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(binding.titleLogo, "scaleX", 0.5f, 1f);
        scaleXAnimator.setDuration(2000);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(binding.titleLogo, "scaleY", 0.5f, 1f);
        scaleYAnimator.setDuration(2000);

        // Start both animations together
        alphaAnimator.start();
        scaleXAnimator.start();
        scaleYAnimator.start();
    }
}
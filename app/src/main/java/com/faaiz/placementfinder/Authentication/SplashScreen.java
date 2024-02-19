package com.faaiz.placementfinder.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivitySplashScreenBinding;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        binding.lottieAnim.animate();

        binding.lottieAnim
                .addAnimatorUpdateListener(
                        (animation) -> {
                            // Do something.
                        });
        binding.lottieAnim
                .playAnimation();


        Intent i = new Intent(SplashScreen.this, LoginTypeActivity.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(i);
                finish(); // this will be removed from the stack, upon pressing back u won't come back to it
            }
        }, 2000);

    }
}
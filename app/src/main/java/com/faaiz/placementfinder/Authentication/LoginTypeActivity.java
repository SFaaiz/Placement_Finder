package com.faaiz.placementfinder.Authentication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityLoginTypeBinding;

public class LoginTypeActivity extends AppCompatActivity {

    ActivityLoginTypeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginTypeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.employee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginTypeActivity.this, RegistrationActivity.class);
                startActivity(i);
            }
        });

        binding.employer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginTypeActivity.this, RegistrationActivity.class);
                i.putExtra("isEmployer", true);
                startActivity(i);
            }
        });

        binding.tvLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(LoginTypeActivity.this, LoginActivity.class);
                i.putExtra("goToLoginType", true);
                startActivity(i);
            }
        });
    }
}
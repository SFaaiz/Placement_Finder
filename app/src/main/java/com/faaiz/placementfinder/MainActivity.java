package com.faaiz.placementfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.faaiz.placementfinder.Application.ApplicationFragment;
import com.faaiz.placementfinder.Authentication.LoginTypeActivity;
import com.faaiz.placementfinder.Home.HomeFragment;
import com.faaiz.placementfinder.Jobs.JobsFragment;
import com.faaiz.placementfinder.Post.PostFragment;
import com.faaiz.placementfinder.Profile.ProfileFragment;
import com.faaiz.placementfinder.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private static final int RC_NOTIFICATION = 99;
    ActivityMainBinding binding;
    Toolbar toolbar;
    BottomNavigationView bnview;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bnview = findViewById(R.id.bnview);
        toolbar = findViewById(R.id.toolbar);
        bnview.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        mAuth = FirebaseAuth.getInstance();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }

        // Load the font file from the resources
        Typeface customFont = ResourcesCompat.getFont(this, R.font.luckiest_guy);
        TextView tvTitle = findViewById(R.id.tb_title);
        // Set the custom font to a TextView
        tvTitle.setTypeface(customFont);

        loadFrag(new HomeFragment());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,binding.drawerLayout, toolbar,
                R.string.openDrawer, R.string.closeDrawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        bnview.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.home){
                    loadFrag(new HomeFragment());
                }else if(id == R.id.jobs){
                    loadFrag(new JobsFragment());
                }else if(id == R.id.post){
                    loadFrag(new PostFragment());
                }else if(id == R.id.application){
                    loadFrag(new ApplicationFragment());
                }else{
                    loadFrag(new ProfileFragment());
                }
                return true;
            }
        });

        binding.navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.logout){
                    showLogoutConfirmationDialog();
                    return true;
                }
                return false;
            }
        });

    }

    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout Confirmation");
        builder.setMessage("Are you sure you want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logoutUser();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(MainActivity.this, "User logged out successfully", Toast.LENGTH_SHORT).show();
        clearSharedPreferences();
        Intent i = new Intent(MainActivity.this, LoginTypeActivity.class);
        startActivity(i);
        finish();
    }

    private void clearSharedPreferences(){
        MySharedPreferences sp = new MySharedPreferences(this);
        sp.clearUserId();
        sp.clearUserProgress();
        sp.clearUserType();
    }

    @Override
    public void onBackPressed() {
        // Get the currently selected fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);

        // Check if the current fragment is not the home fragment
        if (!(currentFragment instanceof HomeFragment)) {
            // Navigate to the home fragment
            bnview.setSelectedItemId(R.id.home);
        } else {
            // Exit the app
            super.onBackPressed();
        }
    }
    int n=0;
    private void loadFrag(Fragment f){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if(n==0){
            ft.add(R.id.frame, f);
            n++;
        }else{
            ft.replace(R.id.frame, f);
        }
        ft.commit();
    }



    public static void showNotificationPermissionDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Notification Permission");
        builder.setMessage("This app requires notification permission to function properly.");
        builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openNotificationSettings(context);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private static void openNotificationSettings(Context context) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }
        context.startActivity(intent);
    }
}
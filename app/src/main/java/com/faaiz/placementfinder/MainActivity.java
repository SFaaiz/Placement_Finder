package com.faaiz.placementfinder;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.faaiz.placementfinder.Application.ApplicationFragment;
import com.faaiz.placementfinder.Authentication.LoginTypeActivity;
import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.Home.HomeFragment;
import com.faaiz.placementfinder.Jobs.JobsFragment;
import com.faaiz.placementfinder.Post.PostFragment;
import com.faaiz.placementfinder.Profile.ProfileFragment;
import com.faaiz.placementfinder.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int RC_NOTIFICATION = 99;
    ActivityMainBinding binding;
    Toolbar toolbar;
    BottomNavigationView bnview;
    FirebaseAuth mAuth;
    RoomDB roomDB;
    FirebaseDatabase database;
    MySharedPreferences sp;
    private String userType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bnview = findViewById(R.id.bnview);
        toolbar = findViewById(R.id.toolbar);
        bnview.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        sp = new MySharedPreferences(this);
        userType = sp.getUserType();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATION);
        }

        // Load the font file from the resources
        Typeface customFont = ResourcesCompat.getFont(this, R.font.luckiest_guy);
        TextView tvTitle = findViewById(R.id.tb_title);
        // Set the custom font to a TextView
        tvTitle.setTypeface(customFont);

        if(userType.equals("user")){
            BottomNavigationView bottomNavigationView = findViewById(R.id.bnview);
            Menu menu = bottomNavigationView.getMenu();
            MenuItem item = menu.findItem(R.id.post); // Replace "your_item_id" with the ID of the item you want to hide
            item.setVisible(false);
        }

        boolean goToProfile = getIntent().getBooleanExtra("goToProfileFragment", false);
        if(goToProfile){
            loadFrag(new ProfileFragment());
        }else{
            loadFrag(new HomeFragment());
        }


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,binding.drawerLayout, toolbar,
                R.string.openDrawer, R.string.closeDrawer);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        roomDB = RoomDB.getInstance(this);
        insertData();

        bnview.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(userType.equals("user")){
                    if(id == R.id.home){
                        loadFrag(new HomeFragment());
                    }else if(id == R.id.jobs){
                        loadFrag(new JobsFragment());
                    }
                    else if(id == R.id.post){
                        loadFrag(new PostFragment());
                    }
                    else if(id == R.id.application){
                        loadFrag(new ApplicationFragment());
                    }else{
                        loadFrag(new ProfileFragment());
                    }
                }else{
                    if(id == R.id.home){
                        loadFrag(new HomeFragment());
                    }else if(id == R.id.jobs){
                        loadFrag(new JobsFragment());
                    }
                    else if(id == R.id.post){
                        loadFrag(new PostFragment());
                    }
                    else if(id == R.id.application){
                        loadFrag(new ApplicationFragment());
                    }else{
                        loadFrag(new ProfileFragment());
                    }
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

    private void insertData(){
        // Check if user exists in the database
        if(userType.equals("user")){
            User existingUser = roomDB.dao().getUser();
            if (existingUser == null) {
                // User does not exist, insert a new user
                getUserData(mAuth.getCurrentUser().getUid(), new UserDataCallback() {
                    @Override
                    public void onUserDataReceived(User user) {
                        roomDB.dao().insertUser(user);
                        Log.d(TAG, "user model: " + user.toString());
                    }
                });

                Log.d(TAG, "Inserted new user");
            } else {
                Log.d(TAG, "User already exists");
                Log.d(TAG, "user model: " + existingUser.toString());
            }
        }else{
            Employer existingEmployer = roomDB.dao().getEmployer();
            if (existingEmployer == null) {
                // User does not exist, insert a new user
                getEmployerData(mAuth.getCurrentUser().getUid(), new EmployerDataCallback() {
                    @Override
                    public void onEmployerDataReceived(Employer employer) {
                        roomDB.dao().insertEmployer(employer);
                    }
                });

                Log.d(TAG, "Inserted new employer");
            } else {
                Log.d(TAG, "employer already exists");
            }
        }

    }

    public void getUserData(String userId, UserDataCallback callback) {
        DatabaseReference userRef = database.getReference("Users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        callback.onUserDataReceived(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }

    private void getEmployerData(String userId, EmployerDataCallback callback){
        // If the user's data doesn't exist in the "users" node, check the "employers" node
        DatabaseReference employerRef = database.getReference("Employers").child(userId);
        employerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Employer employer = dataSnapshot.getValue(Employer.class);
                    if (employer != null) {
                        // Do something with the employer if needed
                        callback.onEmployerDataReceived(employer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }


    public interface UserDataCallback {
        void onUserDataReceived(User user);
    }

    public interface EmployerDataCallback {
        void onEmployerDataReceived(Employer employer);
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
        clearRoomDB();
        googleSignOut();
    }

    private void clearRoomDB(){
// Run the delete operation on a background thread
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Perform the delete operation
                roomDB.clearAllTables();
            }
        });

    }

    private void googleSignOut(){
        // Initialize GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

// Build a GoogleSignInClient with the options specified by gso
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

// Call signOut() method to sign out the user
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Sign out successful
                            // Clear cached account information
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(MainActivity.this, "Signed out successfully", Toast.LENGTH_SHORT).show();
                            clearSharedPreferences();
                            Intent i = new Intent(MainActivity.this, LoginTypeActivity.class);
                            startActivity(i);
                            finish();
                            // Proceed with your desired actions after sign out
                            // For example, navigate to the sign-in screen or display a message
                        } else {
                            // Sign out failed
                            // Handle the failure scenario, if needed
                            Log.w(TAG, "signOut:failure", task.getException());
                            // Optionally, display an error message to the user
                            Toast.makeText(MainActivity.this, "Sign out failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

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
    public void loadFrag(Fragment f){
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

    public void updateSelectedItem(int itemId) {
        bnview.setSelectedItemId(itemId);
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
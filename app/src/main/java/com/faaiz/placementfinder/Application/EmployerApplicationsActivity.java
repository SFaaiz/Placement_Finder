package com.faaiz.placementfinder.Application;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.ActivityEmployerApplicationsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EmployerApplicationsActivity extends AppCompatActivity {

    private ActivityEmployerApplicationsBinding binding;
    private CandidateAdapter adapter;
    String jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerApplicationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean finish = getIntent().getBooleanExtra("finish", false);
        if(finish){
            finish();
        }
        jobId = getIntent().getStringExtra("jobId");


        getUserIdsForJob(jobId);


    }

    private void getUserIdsForJob(String jobId) {
        DatabaseReference applicationsRef = FirebaseDatabase.getInstance().getReference("Applications").child(jobId);

        System.out.println("jobid = " + jobId);
        applicationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> userIds = new ArrayList<>();
                List<Boolean> applicationStatus = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey(); // Get user id
                    Boolean status = snapshot.getValue(Boolean.class); // Get application status

                    System.out.println("userId = " + userId);
                    userIds.add(userId);
                    applicationStatus.add(status);
                }

                // Do something with userIds and applicationStatus lists

                System.out.println("userid size = " + userIds.size());
                fetchUsers(userIds, applicationStatus);
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Log.d("TAG", "onCancelled: error in getting user id = " + databaseError.getMessage());
            }
        });
    }

    private void fetchUsers(List<String> userIds, List<Boolean> applicationStatus) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        List<User> candidates = new ArrayList<>();
        int totalUsers = userIds.size(); // Total number of user IDs
        AtomicInteger usersProcessed = new AtomicInteger(0); // Counter to track the number of users processed

        for (String userId : userIds) {
            DatabaseReference userRef = usersRef.child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            // Do something with the user object
                            System.out.println("candidate = " + user.getName());
                            candidates.add(user);
                        }
                    } else {
                        // User does not exist for this userId
                    }

                    // Increment the counter
                    int processed = usersProcessed.incrementAndGet();

                    // Check if all users have been processed
                    if (processed == totalUsers) {
                        // Call setAdapter only when all users have been processed
                        setAdapter(candidates, userIds, applicationStatus);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                    Log.d("TAG", "onCancelled: error in fetching ");
                }
            });
        }
    }


    private void setAdapter(List<User> candidateList, List<String> userId, List<Boolean> applicationStatus){
        // Initialize the adapter
        adapter = new CandidateAdapter(this, candidateList, userId, applicationStatus, jobId);
        binding.recyclerView.setHasFixedSize(true);

        System.out.println("Setting adapter : list size = " + candidateList.size());
        // Set layout manager and adapter to RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
    }


}

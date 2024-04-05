package com.faaiz.placementfinder.Jobs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.ActivityJobSearchBinding;
import com.faaiz.placementfinder.databinding.FragmentAllJobsBinding;

import java.util.List;

public class JobSearchActivity extends AppCompatActivity {

    private ActivityJobSearchBinding binding;
    RoomDB roomDB;
    List<JobPost> jobPosts;
    JobListAdapter jobListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Show keyboard and focus on the SearchView
        binding.searchView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT);
        }

        binding.recyclerView.setVisibility(View.INVISIBLE);

        roomDB = RoomDB.getInstance(this);
        jobPosts = roomDB.dao().getAllJobPosts();


        // Initialize the adapter only once
        jobListAdapter = new JobListAdapter(this, jobPosts, jobClickListener, "user", false);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(jobListAdapter);

        // Set the query text listener
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.recyclerView.setVisibility(View.VISIBLE);
                // Perform searching based on query here
                System.out.println("query = " + query);
                jobListAdapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                jobListAdapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public JobClickListener jobClickListener = new JobClickListener() {

        @Override
        public void setOnJobClick(JobPost job, Context context) {
            Intent i = new Intent(context, ViewJobActivity.class);
            i.putExtra("jobId", job.getId());
            startActivity(i);
        }

        @Override
        public void setOnJobLongClick(JobPost job, CardView card, Context context) {
            Toast.makeText(context, "Job Post Long Clicked", Toast.LENGTH_SHORT).show();
        }
    };

}
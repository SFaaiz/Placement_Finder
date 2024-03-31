package com.faaiz.placementfinder.Home;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Jobs.JobClickListener;
import com.faaiz.placementfinder.Jobs.JobListAdapter;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.User;
import com.faaiz.placementfinder.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomDB = RoomDB.getInstance(requireContext());
    }

    FragmentHomeBinding binding;
    RoomDB roomDB;
    JobListAdapter jobListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        tryFilterList();

        return view;
    }

    private void tryFilterList() {
        List<JobPost> jobPosts = roomDB.dao().getAllJobPosts();
        User user = roomDB.dao().getUser();

        Log.d("FragmentHome", "Job posts: " + jobPosts);
        Log.d("FragmentHome", "User: " + user);

        if (jobPosts != null && user != null) {
            List<JobPost> recommendedJobs = filterList(jobPosts, user);
            Log.d("FragmentHome", "Recommended jobs: " + recommendedJobs);
            updateJobList(recommendedJobs);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryFilterList(); // Recursive call to try again after 1 second
                }
            }, 1000); // Retry after 1 second
        }
    }



    public List<JobPost> filterList(List<JobPost> jobPosts, User user) {
        List<String> skills = user.getSkills();
        String jobTitle = user.getJobTitle();

        System.out.println("jobposts size = " + jobPosts.size());
        List<JobPost> recommended = new ArrayList<>();
        for (JobPost jobPost : jobPosts) {
            // Check if job title matches or contains the user's job title
            if (jobPost.getRoleToHire().toLowerCase().contains(jobTitle.toLowerCase()) ||
                    jobTitle.toLowerCase().contains(jobPost.getRoleToHire().toLowerCase())) {
                recommended.add(jobPost);
            } else {
                // Check if any required skill matches the user's skills
                List<String> requiredSkills = jobPost.getSkillsRequired();
                for (String skill : skills) {
                    if (requiredSkills.contains(skill)) {
                        recommended.add(jobPost);
                        break; // No need to check further if one skill matches
                    }
                }
            }
        }
        System.out.println("recommended size " + recommended.size());
        return recommended;
    }



    public void updateJobList(List<JobPost> list) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        JobClickListener jobClickListener = ((MainActivity) requireActivity()).jobClickListener;
        jobListAdapter = new JobListAdapter(requireContext(),list,jobClickListener, "user", false);
        binding.recyclerView.setAdapter(jobListAdapter);
        binding.progressBar.setVisibility(View.GONE);
    }
}
package com.faaiz.placementfinder.Jobs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.FragmentJobsBinding;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JobsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JobsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static JobsFragment newInstance(String message) {
        JobsFragment fragment = new JobsFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }


    RoomDB roomDB;
    List<JobPost> jobPosts;
    FragmentJobsBinding binding;
    JobListAdapter jobListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomDB = RoomDB.getInstance(requireContext());
        jobPosts = roomDB.dao().getAllJobPosts();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentJobsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        updateJobList(jobPosts);


        return view;
    }

    public void deleteJobPost(){

    }

    public void loadFrag(Fragment f, String message) {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
            f.setArguments(getArguments(message));
            ft.replace(R.id.frame, f);

        ft.commit();
    }

    private Bundle getArguments(String message) {
        Bundle args = new Bundle();
        args.putString("message", message);
        return args;
    }

    public void updateJobList(List<JobPost> list) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        JobClickListener jobClickListener = ((MainActivity) requireActivity()).jobClickListener;
        jobListAdapter = new JobListAdapter(requireContext(),list,jobClickListener, "employer", false);
        binding.recyclerView.setAdapter(jobListAdapter);
    }

}
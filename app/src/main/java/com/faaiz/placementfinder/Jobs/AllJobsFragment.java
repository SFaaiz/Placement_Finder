package com.faaiz.placementfinder.Jobs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.FragmentAllJobsBinding;
import com.faaiz.placementfinder.databinding.FragmentJobsBinding;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllJobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AllJobsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AllJobsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AllJobsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AllJobsFragment newInstance(String param1, String param2) {
        AllJobsFragment fragment = new AllJobsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    RoomDB roomDB;
    List<JobPost> jobPosts;
    FragmentAllJobsBinding binding;
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
        binding = FragmentAllJobsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        updateJobList(jobPosts);


        return view;
    }

    public void updateJobList(List<JobPost> list) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        JobClickListener jobClickListener = ((MainActivity) requireActivity()).jobClickListener;
        jobListAdapter = new JobListAdapter(requireContext(),list,jobClickListener, "user", false);
        binding.recyclerView.setAdapter(jobListAdapter);
    }
}
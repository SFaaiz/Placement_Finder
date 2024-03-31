package com.faaiz.placementfinder.Application;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faaiz.placementfinder.Database.RoomDB;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.Jobs.ApplicationsAdapter;
import com.faaiz.placementfinder.Jobs.JobClickListener;
import com.faaiz.placementfinder.Jobs.JobListAdapter;
import com.faaiz.placementfinder.MainActivity;
import com.faaiz.placementfinder.R;
import com.faaiz.placementfinder.databinding.FragmentAllJobsBinding;
import com.faaiz.placementfinder.databinding.FragmentApplicationBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApplicationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ApplicationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ApplicationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApplicationFragment newInstance(String param1, String param2) {
        ApplicationFragment fragment = new ApplicationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    FragmentApplicationBinding binding;
    RoomDB roomDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        roomDB = RoomDB.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentApplicationBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        List<JobPost> allJobs = roomDB.dao().getAllJobPosts();
        if(allJobs == null){
            allJobs = new ArrayList<>();
        }
        List<JobPost> appliedJobs = new ArrayList<>();
        for(JobPost jobPost : allJobs){
            if(jobPost.isJobApplied()){
                appliedJobs.add(jobPost);
            }
        }
        updateJobList(appliedJobs);
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
        return view;
    }

    public void updateJobList(List<JobPost> list) {
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ApplicationsAdapter aa = new ApplicationsAdapter(requireContext(),list);
        binding.recyclerView.setAdapter(aa);
    }
}
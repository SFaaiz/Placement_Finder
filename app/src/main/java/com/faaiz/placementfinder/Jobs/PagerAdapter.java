package com.faaiz.placementfinder.Jobs;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AllJobsFragment();
            case 1:
                return new SavedJobsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2; // Number of tabs
    }
}

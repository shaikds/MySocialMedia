package com.shaikds.togather.adapters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.shaikds.togather.view.fragment.FragmentProfileGroupsPage;
import com.shaikds.togather.view.fragment.FragmentProfileStorePage;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    private static final int NUM_ITEMS = 2;
    //private View profileView;
    private String uid;

    public ProfilePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, View profileView, String uid) {
        super(fragmentManager, lifecycle);
        //this.profileView = profileView;
        this.uid = uid;
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            // Groups
            case 1: // Groups fragment.
                return new FragmentProfileGroupsPage(1, "קבוצות", uid);
            default:
                return new FragmentProfileStorePage(0, "חנות", uid);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }


}

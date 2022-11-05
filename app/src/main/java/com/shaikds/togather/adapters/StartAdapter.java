package com.shaikds.togather.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.shaikds.togather.view.fragment.FragmentStart1;
import com.shaikds.togather.view.fragment.FragmentStart2;
import com.shaikds.togather.view.fragment.FragmentStart3;

public class StartAdapter extends FragmentStateAdapter {
    private static final int NUM_ITEMS = 3;

    public StartAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            // Start Screens
            case 1:
                return new FragmentStart2(1,"fragmentStart2");
            case 2:
                return new FragmentStart3(2,"fragmentStart3");
            default:
                return new FragmentStart1(0,"fragmentStart1");
        }
    }

    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }
}

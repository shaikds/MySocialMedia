package com.shaikds.togather.view.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.StartAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class FragmentStartViewPager extends Fragment {
    ViewPager2 vp;
    TabLayout tabLayout;
    private StartAdapter startAdapter;

    public FragmentStartViewPager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start_view_pager, container, false);
        vp = v.findViewById(R.id.start_frag_vp2);
        tabLayout = v.findViewById(R.id.start_frag_tab_layout);
        startAdapter = new StartAdapter(getChildFragmentManager(),getLifecycle());
        vp.setAdapter(startAdapter);


        new TabLayoutMediator(tabLayout, vp,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("");
                    } else if (position == 1) {
                        tab.setText("");
                    } else if (position == 2) {
                        tab.setText("");
                    }
                }
        ).attach();
        return v;
    }
}
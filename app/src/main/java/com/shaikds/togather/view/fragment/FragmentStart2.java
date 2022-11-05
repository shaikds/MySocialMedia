package com.shaikds.togather.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shaikds.togather.R;


public class FragmentStart2 extends Fragment {
    private Bundle mBundle;
    private int page;
    private String title;

    public FragmentStart2(int page, String title) {
        mBundle = new Bundle();
        mBundle.putString("startPageTitle", title);
        mBundle.putInt("startPageId", page);
        this.setArguments(mBundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start_2, container, false);
        page = getArguments().getInt("startPageId", 1);
        title = getArguments().getString("startPageTitle");
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
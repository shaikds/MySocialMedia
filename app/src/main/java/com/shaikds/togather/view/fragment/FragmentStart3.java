package com.shaikds.togather.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.shaikds.togather.R;
import com.shaikds.togather.view.activity.MainActivity;

public class FragmentStart3 extends Fragment {
    private Bundle mBundle;
    private int page;
    private String title;
    Button btnStartApp;

    public FragmentStart3(int page, String title) {
        mBundle = new Bundle();
        mBundle.putString("startPageTitle", title);
        mBundle.putInt("startPageId", page);
        this.setArguments(mBundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_start_3, container, false);
        page = getArguments().getInt("startPageId", 2);
        title = getArguments().getString("startPageTitle");
        btnStartApp = v.findViewById(R.id.frag_start_3_start_btn);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnStartApp.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
            requireActivity().finish();
        });
    }
}
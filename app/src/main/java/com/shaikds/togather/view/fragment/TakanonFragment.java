package com.shaikds.togather.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.shaikds.togather.R;

public class TakanonFragment extends Fragment {

    public TakanonFragment() {
        //required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.takanon, container, false);
        ImageButton ibBack = v.findViewById(R.id.takanon_back_ib);


        ibBack.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });

        return v;
    }


}

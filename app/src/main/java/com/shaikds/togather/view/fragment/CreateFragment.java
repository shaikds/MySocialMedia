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

import com.shaikds.togather.view.activity.UploadPostActivity;
import com.shaikds.togather.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class CreateFragment extends Fragment implements View.OnClickListener {
    FloatingActionButton fab;
    Button btnCreate, btnAsk;
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://mysocialmedia-eb38f-default-rtdb.firebaseio.com/");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.create_fragment, container, false);
        fab = v.findViewById(R.id.create_info_fab);
        fab.setOnClickListener(view -> {
            getChildFragmentManager().beginTransaction().replace(R.id.create_frag_container, new TakanonFragment()).commit();

        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnCreate = getActivity().findViewById(R.id.create_group);
        btnAsk = getActivity().findViewById(R.id.create_power_group);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String currentUID = user.getUid();


        btnCreate.setOnClickListener(this);
        btnAsk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent postActivityIntent = new Intent(getActivity(), UploadPostActivity.class);
        getChildFragmentManager().popBackStack();
        switch (v.getId()) {

            case R.id.create_group:
                postActivityIntent.putExtra("title", "Create Group");
                startActivity(postActivityIntent);
                getActivity().finish();
                break;

            //TODO : Ask for Group
            case R.id.create_power_group:
                postActivityIntent.putExtra("title", "Create Power Group");
                startActivity(postActivityIntent);
                getActivity().finish();
                break;

        }
    }

}

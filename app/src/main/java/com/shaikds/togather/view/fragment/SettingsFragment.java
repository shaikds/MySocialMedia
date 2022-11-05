package com.shaikds.togather.view.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.LocationFilterAdapter;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;


public class SettingsFragment extends Fragment implements LocationFilterAdapter.OnClickedLocation {
    RecyclerView rvLocations;
    Button btnFilter;
    RadioGroup radioGroup;
    RadioButton radioButton;
    Bundle mBundle = new Bundle();
    NavController navController;
    LocationFilterAdapter adapter;
    BottomNavigationView bottomNavigationView;
    ArrayList<String> checkedCities = new ArrayList<>();
    MainSearchPostsViewModel groupViewModel;

    public SettingsFragment() {
        //required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE);
        btnFilter = v.findViewById(R.id.settings_search_btn);
        radioGroup = v.findViewById(R.id.settings_frag_radio_group);
        rvLocations = v.findViewById(R.id.settings_search_rv);
        adapter = new LocationFilterAdapter(this, v.getContext());
        ViewCompat.setNestedScrollingEnabled(rvLocations, false); // for checkbox wont be recycled.
        rvLocations.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        rvLocations.setAdapter(adapter);


        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        groupViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        btnFilter.setOnClickListener(v1 -> {
            //final List<Post> filteredPosts = groupViewModel.filterByLocation(checkedCities);
            radioButton = view.findViewById(radioGroup.getCheckedRadioButtonId());
            mBundle.putString("settingsFilter", radioButton.getText().toString());
            if (checkedCities.size() > 0) {
                mBundle.putStringArrayList("cityArray", checkedCities); //put array in bundle
            } else {
                mBundle.putStringArrayList("cityArray", null);
            }
            navController.navigate(R.id.action_settingsFragment_to_searchFragment, mBundle);//navigate with bundle
        });
    }

    @Override
    public void onClickedLocation(String city, boolean isChecked) {
        if (isChecked) { // city checked -- > add to list
            checkedCities.add(city);
        } else { // city unchecked -- > remove from list.
            checkedCities.remove(city);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomNavigationView.setVisibility(View.VISIBLE);
    }
}
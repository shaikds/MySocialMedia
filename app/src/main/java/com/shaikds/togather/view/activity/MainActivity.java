package com.shaikds.togather.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.shaikds.togather.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";
    FirebaseAuth mAuth;
    NavController navController;
    NavHostFragment navHostFragment;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.frame_main_layout);
        navController = navHostFragment.getNavController();
        mAuth = FirebaseAuth.getInstance();
        navController.navigate(R.id.profileFragment);


        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNav);
        bottomNavigationView.setSelectedItemId(R.id.search_bottom);
        bottomNavigationView.setItemIconTintList(null);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.fragmentBeManager) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.fragmentGroupView) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.otherUserProfileFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.settingsFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.usersFragment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (destination.getId() == R.id.fragmentPayment) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);

            }
        });
        setResourceLocale(new Locale("he"));


    }

    private void setResourceLocale(Locale locale) {
        getBaseContext().getResources().getConfiguration().setLocale(locale);
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context
            context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNav = item -> {
        int fragmentId;
        switch (item.getItemId()) {
            case R.id.profile_bottom:
                navController.navigate(R.id.profileFragment);
                break;
            case R.id.search_bottom:
                navController.navigate(R.id.searchFragment);

                break;
            case R.id.add_bottom:
                navController.navigate(R.id.createFragment);
                break;


        }
        return true;
    };
}
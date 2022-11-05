package com.shaikds.togather.model;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class Router {
    private NavController navController;
    private View view ;

    public Router(View view , NavController navController) {
        this.view = view;
        this.navController = navController;
        this.navController = Navigation.findNavController(view);
    }

    public NavController getNavController() {
        return navController;
    }


}

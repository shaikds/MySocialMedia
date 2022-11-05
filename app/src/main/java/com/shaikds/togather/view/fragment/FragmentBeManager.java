package com.shaikds.togather.view.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.shaikds.togather.repository.IsraelLocationsRepo;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;

public class FragmentBeManager extends Fragment {
    private static final String TAG = "FragmentBeManager";
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    NavController navController;
    EditText etMinPeople, etMaxPeople, etGroupPrice, etLocation, etOriginalPrice;
    TextView tvEndDate, etShipmentMethod;
    BottomNavigationView bottomNavigationView;
    Button btnBeManager;
    ProgressBar pb;
    IsraelLocationsRepo locationsRepo;
    MainSearchPostsViewModel postsViewModel;
    SearchUsersViewModel usersViewModel;
    Post currentPost;
    private DatePickerDialog.OnDateSetListener datePickerDialog;
    private String endTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_be_manager, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view); // find view by id method.

        etLocation.setKeyListener(null);
        locationsRepo = new IsraelLocationsRepo();
        List<String> locationList = locationsRepo.readFromFile(requireActivity());
        String[] locationString = locationsRepo.castToString(locationList);
        navController = Navigation.findNavController(view); // get nav controller

        final String currentUID = firebaseUser.getUid();
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        User specificUser = usersViewModel.getSpecificUser(currentUID);
        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        Bundle args = getArguments();
        if (args == null) {
            navController.navigate(R.id.action_fragmentBeManager_to_fragmentGroupView);// will end eventually in search fragment --> bundle is null.
        }
        datePickerDialog = (datePicker, year, month, day) -> {
            month = month + 1;

            if (month < 10) {
                endTime = day + "/0" + month + "/" + year;
            } else if (day < 10) {
                endTime = "0" + day + "/" + month + "/" + year;
            } else {
                endTime = day + "/" + month + "/" + year;
            }

            tvEndDate.setText(endTime);
        };
        //locations on click listener
        etLocation.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("בחר/י מיקום");
            builder.setIcon(R.drawable.ic_baseline_add_location_24);
            builder.setItems(locationString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkLocationClicked(which, locationList);
                }
            });
            builder.show();
        });

        //shipment method on click
        etShipmentMethod.setOnClickListener(view1 -> {
            createShipmentMethodDialog();
        });

        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        currentPost = postsViewModel.getSelectedGroup().getValue();

        tvEndDate.setOnClickListener(v -> showDateDialog());

        btnBeManager.setOnClickListener(v -> {
            if (checkFieldsLogic()) { //
                currentPost.setUid(specificUser.getUid());// hes already a member of this group --> no need to add him again to group users list.
                currentPost.setUrl(specificUser.getUrl());//manager profile url
                currentPost.setEmail(specificUser.getEmail());
                currentPost.setGroupManager(specificUser.getName());
                currentPost.setGroupPrice(etGroupPrice.getText().toString());
                currentPost.setOriginalPrice(etOriginalPrice.getText().toString());
                currentPost.setEndTime(tvEndDate.getText().toString());
                currentPost.setMinPeople(etMinPeople.getText().toString());
                currentPost.setMaxPeople(etMaxPeople.getText().toString());
                currentPost.setLocation(etLocation.getText().toString());
                Log.d(TAG, "onViewCreated: Values are FINE. congratulation on new MANAGER");
                Toast.makeText(getContext(), "בהצלחה במכירה", Toast.LENGTH_SHORT).show();
                postsViewModel.updatePostsList(currentPost);
                args.putString("userType","manager");
                navController.navigate(R.id.action_fragmentBeManager_to_fragmentGroupView, args);

            }
        });


    }

    private void findViews(View view) {
        etShipmentMethod = view.findViewById(R.id.be_manager_shipment_method);
        tvEndDate = view.findViewById(R.id.be_manager_end_date);
        etGroupPrice = view.findViewById(R.id.be_manager_group_price);
        etMaxPeople = view.findViewById(R.id.be_manager_max_people);
        etMinPeople = view.findViewById(R.id.be_manager_min_people);
        btnBeManager = view.findViewById(R.id.be_manager_btn);
        etLocation = view.findViewById(R.id.be_manager_location);
        etOriginalPrice = view.findViewById(R.id.be_manager_original_price);
        pb = view.findViewById(R.id.be_manager_pb);

    }


    // all ui is clickable ? and show the page loader.
    private void isClickable(boolean clickable) {
        etOriginalPrice.setClickable(clickable);
        etGroupPrice.setClickable(clickable);
        etMinPeople.setClickable(clickable);
        etMaxPeople.setClickable(clickable);
        etLocation.setClickable(clickable);
        tvEndDate.setClickable(clickable);
        btnBeManager.setClickable(clickable);
    }


    private void showDateDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH)+1;

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                datePickerDialog,
                year, month, day);
        dialog.setTitle("רק תאריך סיום ואתם מתחילים לעשות כסף. ");
        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private boolean checkFieldsLogic() {
        isClickable(false);
        pb.setVisibility(View.VISIBLE);
        try {
            if (etGroupPrice.getText().toString().equals("") || Integer.parseInt(etGroupPrice.getText().toString()) <= 0
                    || Integer.parseInt(etGroupPrice.getText().toString()) >= Integer.parseInt(etOriginalPrice.getText().toString())) {
                etGroupPrice.setError("ערך גדול ממחיר המוצר המקורי");
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            } else if (etLocation.getText().toString().equals("")) {
                etLocation.setError("ערך לא יכול להיות ריק");
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            } else if (etMaxPeople.getText().toString().equals("") || Integer.parseInt(etMaxPeople.getText().toString()) < currentPost.getUsersCount()
                    || Integer.parseInt(etMaxPeople.getText().toString()) < Integer.parseInt(etMinPeople.getText().toString())) {
                etMaxPeople.setError("חייב להיות גדול ממספר האנשים בקבוצה");
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            } else if (tvEndDate.getText().toString().equals("")) {
                //now lets work on our end date.
                showDateDialog();
                Toast.makeText(getContext(), "אנא בחר תאריך כדי להתקדם", Toast.LENGTH_SHORT).show();
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            } else if (etMinPeople.getText() == null || etMinPeople.getText().toString().equals("0")) {
                isClickable(true);
                etMinPeople.setError("לא יכול להיות שווה ל-0");
                pb.setVisibility(View.GONE);
                return false;
            } else if (etOriginalPrice.getText().toString().equals("")) {
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            } else if (etShipmentMethod.getText().toString().equals("")) {
                isClickable(true);
                pb.setVisibility(View.GONE);
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "אחד הפרטים שהוזנו אינו תקין", Toast.LENGTH_SHORT).show();
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }

        return true;
    }


    //Checking which category clicked and writes it to tvCategory.
    private void checkLocationClicked(int which, List<String> categories) {
        String categoryName = categories.get(which);
        etLocation.setText(categoryName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    private void createShipmentMethodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final String[] shipmentMethodList = new String[]{getString(R.string.shipment_take_away), getString(R.string.shipment_delivery), getString(R.string.shipment_method_all)};
        builder.setTitle("בחרו אופן אספקת הפריט").setItems(shipmentMethodList, (dialogInterface, i) -> {
            if (i == 0) {
                currentPost.setShipmentMethod(shipmentMethodList[0]);
                etShipmentMethod.setText(shipmentMethodList[0]);
            } else if (i == 1) {
                currentPost.setShipmentMethod(shipmentMethodList[1]);
                etShipmentMethod.setText(shipmentMethodList[1]);

            } else {
                currentPost.setShipmentMethod(shipmentMethodList[2]);
                etShipmentMethod.setText(shipmentMethodList[2]);
            }
        }).create().show();
    }
}
package com.shaikds.togather.view.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.UUID;

public class FragmentEditGroup extends Fragment {
    private static final String TAG = "FragmentEditGroup";
    private static final int PICK_IMAGE_REQUEST = 22;
    String endTime;
    EditText etTitle, etDescription, etLocation, etGroupPrice;
    TextView tvEndDate, tvShipmentMethod;
    ImageView ivGroupImg;
    ProgressBar pb;
    Button btnFinish;
    BottomNavigationView bottomNavigationView;
    private DatePickerDialog.OnDateSetListener datePickerDialog;
    private MainSearchPostsViewModel postsViewModel;
    private NavController navController;
    Post currentPost;
    private User specificUser;
    private Uri selectedUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        bottomNavigationView.setVisibility(View.GONE);
        handleOnBackPressed();
        return inflater.inflate(R.layout.fragment_edit_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViewsByID(view);

        navController = Navigation.findNavController(view);
        Bundle args = getArguments();
        if (args == null) {
            navController.navigate(R.id.action_fragmentEditGroup_to_fragmentGroupView);// will end eventually in search fragment --> bundle is null.
        } else {
            specificUser = (User) args.getSerializable("specificUser");
        }
        postsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        currentPost = postsViewModel.getSelectedGroup().getValue();
        groupDataToUI(currentPost); // attach data to ui.
//        postsViewModel.getAllPosts().getValue().
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

        //select image with permission.
        ivGroupImg.setOnClickListener(v -> {
            selectImage();
        });

        //show dialog for shipment method
        tvShipmentMethod.setOnClickListener(view1 -> createShipmentMethodDialog());

        // show date dialog.
        tvEndDate.setOnClickListener(v -> showDateDialog());

        //update post list in db.
        btnFinish.setOnClickListener(v -> {
            if (checkFieldsLogic()) {
                StorageReference ref = FirebaseStorage.getInstance().getReference().child("groups/" + currentPost.getGroupId() +
                        "/" + UUID.randomUUID().toString());
                try {
                    ref.putFile(selectedUri).addOnCompleteListener(task -> {
                        final Task downloadUrlTask = ref.getDownloadUrl().addOnSuccessListener(uri -> {
                            currentPost.setPostImageUri(uri.toString());
                        });
                    });
                } catch (Exception e) {
                    //User didnt change the photo -->do nothing.
                }

                //all fields are fine --> get all texts and edit it through view model.
                currentPost.setEndTime(tvEndDate.getText().toString());
                currentPost.setDescription(etDescription.getText().toString());
                currentPost.setLocation(etLocation.getText().toString());
                currentPost.setGroupPrice(etGroupPrice.getText().toString());
                currentPost.setTitle(etTitle.getText().toString());
                postsViewModel.updatePostsList(currentPost);
                postsViewModel.select(currentPost);
                Toast.makeText(getContext(), "פרטי הקבוצה שונו", Toast.LENGTH_SHORT).show();
                navController.navigate(R.id.action_fragmentEditGroup_to_fragmentGroupView, args);
                Log.d(TAG, "onViewCreated: Fields in Edit Group is FINE.");
            }
        });


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == getActivity().RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            selectedUri = data.getData();
            try {
                Picasso.get().load(selectedUri).into(ivGroupImg);
            } catch (Exception e) {
                // Log the exception
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getContext(), "לא נבחרה תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDateDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH) + 1;

        DatePickerDialog dialog = new DatePickerDialog(getContext(),
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                datePickerDialog,
                year, month, day);
        dialog.setTitle("רק תאריך סיום ואתם מתחילים לעשות כסף. ");
        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }    //find views by id.

    private void findViewsByID(@NonNull View view) {
        etTitle = view.findViewById(R.id.edit_group_title);
        etDescription = view.findViewById(R.id.edit_group_description);
        etLocation = view.findViewById(R.id.edit_group_place);
        tvEndDate = view.findViewById(R.id.edit_group_btn_end_date);
        etGroupPrice = view.findViewById(R.id.edit_group_price);
        btnFinish = view.findViewById(R.id.edit_group_btn);
        pb = view.findViewById(R.id.edit_group_pb);
        ivGroupImg = view.findViewById(R.id.edit_frag_iv);
        tvShipmentMethod = view.findViewById(R.id.edit_group_et_shipment);
    }

    // all ui is clickable ? and show the page loader.
    private void isClickable(boolean clickable) {
        etTitle.setClickable(clickable);
        etGroupPrice.setClickable(clickable);
        etLocation.setClickable(clickable);
        etDescription.setClickable(clickable);
        tvEndDate.setClickable(clickable);
        tvShipmentMethod.setClickable(clickable);
    }

    private void groupDataToUI(Post post) {
        try {
            Picasso.get().load(currentPost.getPostImageUri()).into(ivGroupImg);
            etTitle.setText(post.getTitle());
            etGroupPrice.setText(post.getGroupPrice());
            etLocation.setText(post.getLocation());
            etDescription.setText(post.getDescription());
            tvEndDate.setText(post.getEndTime());
            tvShipmentMethod.setText(post.getShipmentMethod());

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "groupDataToUI: loading data from post to UI didnt go very well..CHECK");
        }
    }

    private boolean checkFieldsLogic() {
        isClickable(false);
        pb.setVisibility(View.VISIBLE);

        if (etTitle.getText().toString().equals("") || etTitle.getText().length() < 6) {
            etTitle.setError("ערך זה צריך להיות גדול מ6 תווים");
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }
        if (etGroupPrice.getText().toString().equals("") || Integer.parseInt(etGroupPrice.getText().toString()) <= 0
                || Integer.parseInt(etGroupPrice.getText().toString()) >= Integer.parseInt(currentPost.getOriginalPrice())) {
            etGroupPrice.setError("משהו לא מסתדר...");
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }
        if (etLocation.getText().toString().equals("")) {
            etLocation.setError("ערך לא יכול להיות ריק");
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }
        if (etDescription.getText().toString().equals("") || etDescription.getText().length() < 4) {
            etDescription.setError("נא לתקן");
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }
        if (tvEndDate.getText().toString().equals("")) {
            //now lets work on our end date.
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }
        if (tvShipmentMethod.getText().toString().equals("")) {
            //now lets work on our end date.
            isClickable(true);
            pb.setVisibility(View.GONE);
            return false;
        }

        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    // Select Image method
    private void selectImage() {
        // self permission check first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    // Explain to the user why we need to read the contacts
                }

                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant that should be quite unique

                return;
            }
        }
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "בחרו תמונה דרך אחת האופציות הבאות..."),
                PICK_IMAGE_REQUEST);
    }

    private void createShipmentMethodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final String[] shipmentMethodList = new String[]{getString(R.string.shipment_take_away), getString(R.string.shipment_delivery), getString(R.string.shipment_method_all)};
        builder.setTitle("בחרו אופן אספקת הפריט").setItems(shipmentMethodList, (dialogInterface, i) -> {
            if (i == 0) {
                currentPost.setShipmentMethod(shipmentMethodList[0]);
            } else if (i == 1) {
                currentPost.setShipmentMethod(shipmentMethodList[1]);

            } else {
                currentPost.setShipmentMethod(shipmentMethodList[3]);
            }
            // anyway..
            groupDataToUI(currentPost);
        }).create().show();
    }


    private void handleOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.fragmentGroupView, getArguments());
            }
        });
    }
}
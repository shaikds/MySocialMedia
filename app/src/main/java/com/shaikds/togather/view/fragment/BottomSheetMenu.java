package com.shaikds.togather.view.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.view.activity.LoginActivity;
import com.shaikds.togather.viewmodel.AuthViewModel;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BottomSheetMenu extends BottomSheetDialogFragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference reference;
    CardView cvLogout, cvDbDeleteUser, cvGroupDeleteUser;
    FirebaseAuth mAuth;
    DatabaseReference df;
    AuthViewModel authViewModel;
    FirebaseUser mCurrentUser;
    String mCurrentUid;
    String url;
    Bundle bundle;
    MainSearchPostsViewModel groupsViewModel;
    BottomSheetMenu fragment = this;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(AuthViewModel.class);
        authViewModel.getLoggedStatus().observe(this, aBoolean -> {
            if (!aBoolean) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            } else {
                Toast.makeText(getContext(), "Click The Button", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);

        df = FirebaseDatabase.getInstance("https://mysocialmedia-eb38f-default-rtdb.firebaseio.com/").getReference("All Users");
        cvDbDeleteUser = view.findViewById(R.id.cv_delete);
        cvLogout = view.findViewById(R.id.cv_logout);
        cvGroupDeleteUser = view.findViewById(R.id.cv_delete_user);
        mAuth = FirebaseAuth.getInstance();
        bundle = this.getArguments();
        if (bundle != null) { // bundle empty check.
            final String userType = bundle.getString("userType");
            final boolean isPurchaseStarted = bundle.getBoolean("isPurchaseStarted");
            if (userType != null) {
                if (userType.equals("manager")) {   //manager -> only leave group btn shown.
                    cvGroupDeleteUser.setVisibility(View.VISIBLE);
                    cvLogout.setVisibility(View.GONE);
                    cvDbDeleteUser.setVisibility(View.GONE);
                } else if (userType.equals("user")) {    //user --> all buttons are gone.
                    cvGroupDeleteUser.setVisibility(View.VISIBLE);
                    cvDbDeleteUser.setVisibility(View.GONE);
                    cvLogout.setVisibility(View.GONE);
                } else {//not registered.
                    cvGroupDeleteUser.setVisibility(View.GONE);
                    cvDbDeleteUser.setVisibility(View.GONE);
                    cvLogout.setVisibility(View.GONE);
                }
            } else {//user type null --> means its from profile fragment call.
                cvGroupDeleteUser.setVisibility(View.GONE);
                cvDbDeleteUser.setVisibility(View.VISIBLE);
                cvLogout.setVisibility(View.VISIBLE);
            }
            if (isPurchaseStarted) {
                //if group started purchasing so dont let anyone leave..
                //TODO: MAYBE AT THE FUTURE USER CAN LEAVE ONLY
                cvGroupDeleteUser.setVisibility(View.GONE);
            }
        } else { // bundle's empty --> this bottom sheet menu called from profile.
            cvGroupDeleteUser.setVisibility(View.GONE);
            cvDbDeleteUser.setVisibility(View.VISIBLE);
            cvLogout.setVisibility(View.VISIBLE);
        }


        FirebaseUser user = mAuth.getCurrentUser();
        mCurrentUid = user.getUid();
        String currentUID = user.getUid();
        mCurrentUser = mAuth.getCurrentUser();
        groupsViewModel = new ViewModelProvider(requireActivity()).get(MainSearchPostsViewModel.class);
        reference = db.collection("user").

                document(currentUID);
        //try and catch.
        reference.get().
                addOnCompleteListener(task -> {
                    if (task.getResult().exists()) {
                        url = task.getResult().getString("url");
                    } else {

                    }
                });


        //On clicks
        cvGroupDeleteUser.setOnClickListener(v -> leaveGroup(groupsViewModel.getSelectedGroup().getValue()));
        cvLogout.setOnClickListener(view12 -> logout());

        cvDbDeleteUser.setOnClickListener(view1 -> {
            int store;
            int groups;
            try {
                store = Integer.parseInt(bundle.getString("store"));
            } catch (NumberFormatException e) {
                store = 0;
            }
            try {
                groups = Integer.parseInt(bundle.getString("groups"));

            } catch (NumberFormatException e) {
                groups = 0;

            }

            if (store > 0 || groups > 0) {
                Toast.makeText(getContext(), "יש למחוק ולצאת מכלל הקבוצות לפני מחיקה!", Toast.LENGTH_LONG).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("מחיקת משתמש")
                    .setMessage("האם אתה בטוח שברצונך למחוק את המשתמש לתמיד?")
                    .setPositiveButton("כן", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteAuth();// auth has to be deleted before firestore database.
                        }
                    }).setNegativeButton("לא", (dialogInterface, i) -> {
            });
            builder.create();
            builder.show();
        });

        return view;
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("התנתקות מהמערכת").setMessage("האם אתה בטוח שברצונך להתנתק?")
                .setPositiveButton("התנתק", (dialogInterface, i) -> {
                    authViewModel.signOut();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    this.getActivity().finish();
                })
                .setNegativeButton("לא", (dialogInterface, i) -> {
                });
        builder.create();
        builder.show();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
    }

    public void leaveGroup(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("יציאה מהקבוצה").setMessage("האם אתה בטוח שברצונך לצאת מהקבוצה?")
                .setPositiveButton("כן", (dialogInterface, i) -> {
                    groupsViewModel.removeUserFromGroup(post, mCurrentUser.getUid());
                    Toast.makeText(requireContext(), "יצאת מהקבוצה בהצלחה", Toast.LENGTH_SHORT).show();
                    fragment.dismiss();
                }).setNegativeButton("לא", (dialogInterface, i) -> {
            fragment.dismiss();
        });
        builder.create();
        builder.show();
    }

    // auth remove.
    private void deleteAuth() {
        final EditText edittext = new EditText(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(edittext);
        builder.setTitle("מחיקת משתמש").setMessage("הקשת סיסמת משתמש מחדש")
                .setPositiveButton("מחיקה", (dialogInterface, i) -> {
                    deleteFirestoreNStorage();
                    String userPassword = edittext.getText().toString();
                    authViewModel.deleteUserAuth(mCurrentUser.getEmail(), userPassword);
                    Toast.makeText(requireContext(), "מחקת את המשתמש בהצלחה", Toast.LENGTH_SHORT).show();
                    fragment.dismiss();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    getActivity().finish();
                }).setNegativeButton("ביטול", (dialogInterface, i) -> {
            fragment.dismiss();
        });
        builder.create();
        builder.show();
    }

    // image storage and fire store remove.
    private void deleteFirestoreNStorage() {
        reference.delete()
                .addOnSuccessListener(aVoid -> { // firestore
                    FirebaseFirestore.getInstance().collection("user").document(mCurrentUid)
                            .delete().addOnCompleteListener(task -> {
                        StorageReference ref = FirebaseStorage.getInstance().getReference();
                        ref.child("Profile Images").child(mCurrentUid).delete();
                    });

                });
    }
}


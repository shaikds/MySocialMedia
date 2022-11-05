package com.shaikds.togather.view.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.adapters.PurchaseMembersAdapter;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.CodeViewModel;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;


public class FragmentPayment extends Fragment implements PurchaseMembersAdapter.OnClickedView {

    NavController navController;
    Button btnFinish;
    TextView tvPhone;
    RecyclerView rvUsers;
    Bundle mBundle;
    CodeViewModel codeViewModel;
    SearchUsersViewModel usersViewModel;
    MainSearchPostsViewModel postsViewModel;
    private PurchaseMembersAdapter membersAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_payment, container, false);
        btnFinish = v.findViewById(R.id.payment_btn);
        tvPhone = v.findViewById(R.id.payment_tv_manager_phone);
        rvUsers = v.findViewById(R.id.payment_rv_phone);


        mBundle = this.getArguments(); // bundle from group view.
        if (mBundle == null) {
            navController.navigate(R.id.action_fragmentPayment_to_fragmentGroupView); // will send to search user fragment eventually.
        }
        if (mBundle.get("userType").equals("manager")) {    //manager
            tvPhone.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
        } else {    //user
            tvPhone.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
        }

        handleOnBackPressed();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        final String groupId = mBundle.getString("groupId", "");
        final String managerUid = mBundle.getString("managerUid", "");


        btnFinish.setOnClickListener(v -> navController.navigate(R.id.action_fragmentPayment_to_fragmentGroupView, mBundle));

        tvPhone.setOnClickListener(v -> {
            final String phone = tvPhone.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // View Models
        usersViewModel = new ViewModelProvider(requireActivity()).get(SearchUsersViewModel.class);
        // Users list recycler.
        final List<User> users = usersViewModel.getGroupUsersDetail(); // get users
        final User managerUser = usersViewModel.getSpecificUser(managerUid);
        tvPhone.setText(managerUser.getPhone());
        final User mUser = usersViewModel.getMyUser();
        if (mBundle.get("userType").equals("manager") && users.contains(mUser)) {
            users.remove(mUser);
        }
        //  Initialize adapter
        membersAdapter = new PurchaseMembersAdapter(getContext(), this);

        //Unique codes ViewModel
        codeViewModel = new ViewModelProvider(getActivity()).get(CodeViewModel.class);
        codeViewModel.setGroupId(groupId, users); // gets all the codes.
        //Start the Recycler View before all that data loading.
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        membersAdapter.setUsers(users);
        rvUsers.setAdapter(membersAdapter);

        codeViewModel.getAllCodes().observe(getViewLifecycleOwner(), codeList -> {
            if (codeList != null && codeList.size() > 0) {
                // show code dialog
                membersAdapter.setCodesList(codeList);
                rvUsers.setAdapter(membersAdapter);
            }
        });

        codeViewModel.getIsLoadedLiveData().observe(getViewLifecycleOwner(), isDataLoaded -> {
            if (isDataLoaded) {
                showCodeDialog(codeViewModel.getMyCode());
            }
        });

    }


    @Override
    public void viewClicked(List<User> userList, int position) {
        // show details of user
        try {
            final String phone = userList.get(position).getPhone();
            Toast.makeText(getActivity(), "שם הלקוח : " + userList.get(position).getName(), Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "מייל הלקוח : " + userList.get(position).getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(getActivity(), getView(), "לא נמצא טלפון", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    private void showCodeDialog(String code) {
        //MANAGER ONLY EARLY EXIT
        if (code == null) {
            if (!mBundle.get("userType").equals("user")) {
                return;
            }//User
            code = getString(R.string.refresh_page);
        }
        // USER
        if (!mBundle.get("userType").equals("manager")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(getString(R.string.payment_dialog_title)).setMessage(getString(R.string.purchase_group_dialog) + "\n" + code)
                    .setNeutralButton(getString(R.string.thank_you), (dialogInterface, i) -> dialogInterface.dismiss()).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (codeViewModel.getAllCodes().getValue() == null) {
            mBundle.remove("managerUid");
            codeViewModel.getIsLoadedLiveData().postValue(false);
            codeViewModel.setDestroyedGroupId();
            return;
        }
        codeViewModel.getAllCodes().getValue().clear();
        codeViewModel.getIsLoadedLiveData().postValue(false);
        mBundle.remove("managerUid");
        codeViewModel.setDestroyedGroupId();
    }

    private void handleOnBackPressed() {
        requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navController.navigate(R.id.fragmentGroupView, mBundle);
            }
        });
    }
}
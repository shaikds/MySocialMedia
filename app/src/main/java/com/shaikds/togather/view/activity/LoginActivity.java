package com.shaikds.togather.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.AuthViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    EditText etPhoneNumber, etPass;
    SearchUsersViewModel usersViewModel;
    Button loginBtn, registerSignUp;
    CheckBox checkBox;
    ProgressBar pb;
    FirebaseAuth mAuth;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etPhoneNumber = findViewById(R.id.login_phone_number_et);
        etPass = findViewById(R.id.login_password_et);
        loginBtn = findViewById(R.id.login_btn);
        registerSignUp = findViewById(R.id.login_register_btn);
        checkBox = findViewById(R.id.login_pass_checkbox);
        pb = findViewById(R.id.login_pb);
        mAuth = FirebaseAuth.getInstance();
        loginBtn.setEnabled(false);

        usersViewModel = new ViewModelProvider(this).get(SearchUsersViewModel.class);
        usersViewModel.getUsersLiveData().observe(this, users -> {
        });

        usersViewModel.getIsUsersLoadedLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoaded) {
                if (isLoaded) {
                    loginBtn.setEnabled(true);
                }
            }
        });

        //default setting.
        etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //check box transform listener.
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        //register button in login activity
        registerSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginBtn.setOnClickListener(view -> {
            final String phoneNumber = etPhoneNumber.getText().toString();
            final String password = etPass.getText().toString();
            if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(password)) {
                pb.setVisibility(View.VISIBLE);
                checkUserNumberPass(phoneNumber, password);
            } else {
                pb.setVisibility(View.GONE);
                Toast.makeText(this, "מלאו את כל השדות", Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.getApplication())).get(AuthViewModel.class);

    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        String s = etPass.getText().toString();
        SharedPreferences sp = getSharedPreferences("u_password",MODE_PRIVATE);
        sp.edit().putString("u_password",s).apply();
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfDBContainsUser(); // sends to main activity.

    }

    private void checkIfDBContainsUser() {

        authViewModel.getUserData().observe(this, firebaseUser -> {
            try {
                if (firebaseUser != null) {
                    if (firebaseUser.getUid() != null)
                        sendUserToMainActivity();
                }

            } catch (Exception e) {
                Log.d(TAG, "checkIfDBContainsUser: " + e.getMessage());
            }
        });

    }


    // TODO : FOR EMAIL RESET AT THE FUTURE
    // checks password and phone , if phone exists - > get user email, and send it to sign in with it's mail.
    private void checkUserNumberPass(String phoneNumber, String password) {
        boolean isExistsOnFirestore = false;
        List<User> users = usersViewModel.getUsersLiveData().getValue();
        if (users.size() > 0) {
            for (User user : users) {
                if (user.getPhone().equals(phoneNumber) && user.getPassword().equals(password)) {
                    // Login success
                    isExistsOnFirestore = true;
                    authViewModel.signIn(user.getEmail(), password);
                    pb.setVisibility(View.INVISIBLE);
                    return;
                }
            }
            // if after all that nothing happened--> try to check if exists only in auth.
            if (!isExistsOnFirestore) {
                authViewModel.signIn(String.format("%s@%s.%s", phoneNumber, phoneNumber, phoneNumber), password);
                pb.setVisibility(View.INVISIBLE);
                return;
            }
            // if we got till here theres not user exists with this pass and phone number.
            Toast.makeText(this, "משתמש לא קיים במערכת", Toast.LENGTH_SHORT).show();
        }
    }
}
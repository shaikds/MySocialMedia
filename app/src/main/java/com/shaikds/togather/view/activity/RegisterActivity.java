package com.shaikds.togather.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.shaikds.togather.view.fragment.TakanonFragment;
import com.shaikds.togather.viewmodel.AuthViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    EditText etPhoneNumber, etPass, etConfirmPass, etVerifyCode;
    Button registerLoginScreenBtn, signupBtn, btnVerify;
    TextView tvTakanon;
    CheckBox checkBox;
    ProgressBar pb;
    List<String> phoneNumbers;
    SearchUsersViewModel usersViewModel;
    private AuthViewModel authViewModel;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //find view by id
        tvTakanon = findViewById(R.id.register_takanon_tv);
        etVerifyCode = findViewById(R.id.register_sms_verify_btn);
        etPhoneNumber = findViewById(R.id.register_phone_et);
        etPass = findViewById(R.id.register_password_et);
        etConfirmPass = findViewById(R.id.register_confirm_password_et);
        registerLoginScreenBtn = findViewById(R.id.register_login_btn);
        signupBtn = findViewById(R.id.register_signup_btn);
        pb = findViewById(R.id.register_progressbar);
        btnVerify = findViewById(R.id.register_verify_btn);
        checkBox = findViewById(R.id.register_password_checkbox);
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences sp = getSharedPreferences("register_takanon_details", MODE_PRIVATE);
        etPhoneNumber.setText(sp.getString("register_phone", ""));
        etPass.setText(sp.getString("register_pass", ""));
        sp.edit().remove("register_phone").apply();
        sp.edit().remove("register_pass").apply();

        tvTakanon.setPaintFlags(tvTakanon.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        tvTakanon.setOnClickListener(view -> {
            SharedPreferences.Editor edit = sp.edit();
            edit.putString("register_phone", etPhoneNumber.getText().toString());
            edit.putString("register_pass", etPass.getText().toString());
            edit.apply();
            getSupportFragmentManager().beginTransaction().replace(R.id.register_holder, new TakanonFragment()).commit();
        });

        etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        etConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        usersViewModel = new ViewModelProvider(this).get(SearchUsersViewModel.class);
        phoneNumbers = new ArrayList<>();
        usersViewModel.getUsersLiveData().observe(this, userList -> {
            if (userList != null) {
                for (User user : userList) {
                    if (user.getPhone() != null) {
                        phoneNumbers.add(user.getPhone());
                    }
                }
            }
        });
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                etConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                etConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        signupBtn.setOnClickListener(view -> {
            String phone = etPhoneNumber.getText().toString();
            String password = etPass.getText().toString();
            String confirmPassword = etConfirmPass.getText().toString();

            if (!TextUtils.isEmpty(phone) && !password.equals("") && !confirmPassword.equals("") && password.length() >= 6) {
                if (password.equals(confirmPassword)) {
                    pb.setVisibility(View.VISIBLE);
                    // phone
                    if (etPhoneNumber.getText().toString() == null || etPhoneNumber.getText().toString().length() > 10 || etPhoneNumber.getText().toString().length() < 10) {
                        etPhoneNumber.setError("בעיה בקליטת הטלפון");
                    } else if (phoneNumbers.contains(etPhoneNumber.getText().toString())) {
                        // user exists in db ?
                        pb.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "משתמש קיים במערכת !", Toast.LENGTH_SHORT).show();
                    } else {
                        // user not exists in db .
                        final String phoneNumber = etPhoneNumber.getText().toString();
                        etConfirmPass.setVisibility(View.INVISIBLE);
                        tvTakanon.setVisibility(View.INVISIBLE);
                        signupBtn.setVisibility(View.INVISIBLE);
                        checkBox.setVisibility(View.INVISIBLE);
                        etPass.setVisibility(View.INVISIBLE);
                        btnVerify.setVisibility(View.VISIBLE);
                        etPhoneNumber.setEnabled(false);
                        etVerifyCode.setVisibility(View.VISIBLE);
                        //register
                        authViewModel.sendVerificationCode(RegisterActivity.this, phoneNumber, password);
                    }
                } else { // passwords arent match.
                    etConfirmPass.setError("הסיסמא לא זהה");
                }

            } else { // 1 of texts are empty.
                Toast.makeText(RegisterActivity.this, "מלאו את כל הפרטים הנדרשים", Toast.LENGTH_SHORT).show();
            }
        });

        //verify phone number . 1st step.
        btnVerify.setOnClickListener(view -> {
            if (etVerifyCode.getText().toString().equals("")) {
                etVerifyCode.setError("קוד לא תקין");
            } else {
                final String codeSelected = etVerifyCode.getText().toString();
                final String phoneSelected = etPhoneNumber.getText().toString();
                final String passwordSelected = etPass.getText().toString();
                sp.edit().putString("u_password",passwordSelected).apply();
                btnVerify.setEnabled(false);
                authViewModel.verifySmsCode(codeSelected, phoneSelected, passwordSelected);
                pb.setVisibility(View.VISIBLE);
            }
        });

        // go to register screen
        registerLoginScreenBtn.setOnClickListener(view -> sendUserToLoginActivity());

        //users view model --> when logged in go to create user activity .
        authViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.getApplication())).get(AuthViewModel.class);
        authViewModel.getUserData().observe(this, firebaseUser -> { //
            if (firebaseUser != null) {
                if (firebaseUser.getEmail() != null) {
                    sendUserToCreateActivity(etPass.getText().toString()); // ( show the password fragment )
                }
            } else {
                Toast.makeText(getApplicationContext(), "No User Has Found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendUserToCreateActivity(String password) {
        Intent intent = new Intent(RegisterActivity.this, CreateProfile.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("password", password);
        startActivity(intent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


}

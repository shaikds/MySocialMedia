package com.shaikds.togather.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.shaikds.togather.view.fragment.TouchedDialogFragment;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfile extends AppCompatActivity implements View.OnFocusChangeListener {

    String currentUID;
    SearchUsersViewModel usersViewModel;
    private boolean isTouched = false;
    EditText etWeb, etName, etProf, etCompany, etBio, etEmail, etPhone;
    Button btn;
    ProgressBar pb;
    DocumentReference documentReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    User user;
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private String oldEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        findViewsById();

        user = new User();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUID = user.getUid();
        documentReference = db.collection("user").document(currentUID);


        etPhone.setEnabled(false);
        etName.setOnFocusChangeListener(this);
        etBio.setOnFocusChangeListener(this);
        etCompany.setOnFocusChangeListener(this);
        etEmail.setOnFocusChangeListener(this);
        etProf.setOnFocusChangeListener(this);
        etWeb.setOnFocusChangeListener(this);
        btn.setOnClickListener(view -> {
            pb.setVisibility(View.VISIBLE);
            setEnabled(false);
            updateProfile();
        });

        usersViewModel = new ViewModelProvider(this).get(SearchUsersViewModel.class);

    }

    @Override
    protected void onStart() {
        super.onStart();

        documentReference.get()
                .addOnCompleteListener(task -> {
                    if (task.getResult().exists()) {
                        String nameResult = task.getResult().getString("name");
                        String bioResult = task.getResult().getString("bio");
                        String profResult = task.getResult().getString("profession");
                        String emailResult = firebaseUser.getEmail();
                        String websiteResult = task.getResult().getString("web");
                        String companyResult = task.getResult().getString("company");
                        String phoneResult = task.getResult().getString("phone");
                        etName.setText(nameResult);
                        etBio.setText(bioResult);
                        etProf.setText(profResult);
                        etEmail.setText(emailResult);
                        etWeb.setText(websiteResult);
                        etCompany.setText(companyResult);
                        etPhone.setText(phoneResult);

                        //sets old email for re-authenticate.
                        oldEmail = etEmail.getText().toString();
                    } else {
                        Toast.makeText(UpdateProfile.this, "No Profile- Redirect to Login", Toast.LENGTH_SHORT).show();
                        sendUserToLoginActivity();
                    }
                });
    }

    private void updateProfile() {
        String name = etName.getText().toString();
        String bio = etBio.getText().toString();
        String prof = etProf.getText().toString();
        String web = etWeb.getText().toString();
        String email = etEmail.getText().toString();
        String company = etCompany.getText().toString();


        if (!checkMandatoryFields()) {
            pb.setVisibility(View.GONE);
            return;
        }

        final DocumentReference sDoc = db.collection("user").document(currentUID);
        db.runTransaction((Transaction.Function<Void>) transaction -> {

            transaction.update(sDoc, "name", name);
            transaction.update(sDoc, "bio", bio);
            transaction.update(sDoc, "email", email);
            transaction.update(sDoc, "web", web);
            transaction.update(sDoc, "profession", prof);
            transaction.update(sDoc, "company", company);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> Toast.makeText(UpdateProfile.this, "הפרטים שונו בהצלחה", Toast.LENGTH_SHORT).show());

            return null;

            // Success
        }).addOnSuccessListener(aVoid -> {
            reAuthenticate(oldEmail, email); // do same with auth user details.
        })
                //Failure
                .addOnFailureListener(e -> {
                    Toast.makeText(UpdateProfile.this, "נכשל העדכון, אנא בדקו את חיבור האינטרנט.", Toast.LENGTH_SHORT).show();
                    pb.setVisibility(View.GONE);
                    setEnabled(true);
                });

    }

    private void sendUserToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }


    private void setEnabled(boolean isEnabled) {
        etEmail.setEnabled(isEnabled);
        etName.setEnabled(isEnabled);
        etBio.setEnabled(isEnabled);
        etCompany.setEnabled(isEnabled);
        etProf.setEnabled(isEnabled);
        etWeb.setEnabled(isEnabled);
        btn.setEnabled(isEnabled);

    }


    @Override
    public void onBackPressed() {
        if (isTouched) {
            TouchedDialogFragment touchedDialogFragment = new TouchedDialogFragment().newInstance();
            touchedDialogFragment.show(getSupportFragmentManager(), "onTouchedDialog");
        } else {
            super.onBackPressed();
        }
        isTouched = false;
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        isTouched = true;
    }

    private boolean regex(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            return true;
        } else
            etEmail.setError("נא להקליד מייל אמיתי");
        System.out.println(email + " : " + matcher.matches());
        setEnabled(true);
        return false;
    }

    private boolean checkMandatoryFields() {
        if (!regex(etEmail.getText().toString())) {
            return false;
        }
        if (etName.getText().toString().equals("") || etName.getText().toString() == null) {
            etName.setError("נא למלא שם פרטי");
            return false;
        }
        return true;
    }

    private void reAuthenticate(String email, String newEmail) {
        //final String password = getIntent().getExtras().getString("mPass");
        String password = getSharedPreferences("u_password",MODE_PRIVATE).getString("u_password",null);

        //  Early exit.
        if (password == null) {
            pb.setVisibility(View.GONE);
            setEnabled(true);
            return;
        }
        // Get auth credentials from the user for re-authentication
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password); // Current Login Credentials \\
        // Prompt the user to re-provide their sign-in credentials
        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    //Now change your email address \\
                    //----------------Code for Changing Email Address----------\\
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updateEmail(newEmail)
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    pb.setVisibility(View.GONE);
                                    getIntent().getExtras().remove("mPass"); // remove password from extras, no need to use it again .
                                    Toast.makeText(UpdateProfile.this, " עריכת המשתמש הסתיימה בהצלחה", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(UpdateProfile.this, MainActivity.class));
                                    UpdateProfile.this.finish();
                                    Log.d("UpdateProfileActivity", "User email address updated.");
                                }else {
                                    pb.setVisibility(View.GONE);
                                    setEnabled(true);
                                    Toast.makeText(UpdateProfile.this, "נוצרה בעיה, אנא נסו שנית", Toast.LENGTH_SHORT).show();
                                    Log.d("UpdateProfileActivity", "User email address update FAILED.");

                                }
                            });
                    //----------------------------------------------------------\\
                });


    }

    private void findViewsById() {
        etName = findViewById(R.id.edit_profile_name_et);
        etWeb = findViewById(R.id.edit_profile_website_et);
        etProf = findViewById(R.id.edit_profile_prof_et);
        etCompany = findViewById(R.id.edit_profile_company_et);
        etBio = findViewById(R.id.edit_profile_bio_et);
        etEmail = findViewById(R.id.edit_profile_email_et);
        etPhone = findViewById(R.id.edit_profile_phone_et);
        btn = findViewById(R.id.edit_profile_btn);
        pb = findViewById(R.id.update_profile_pb);
    }
}
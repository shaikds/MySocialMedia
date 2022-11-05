package com.shaikds.togather.view.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.shaikds.togather.view.fragment.FragmentStartViewPager;
import com.shaikds.togather.viewmodel.AuthViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateProfile extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 22;
    EditText etWeb, etName, etProf, etCompany, etBio, etEmail, etPhone;
    Button saveProfile;
    ProgressBar pb;
    ImageView profilePic;
    Uri imageUri = null;
    StorageReference storageReference;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    AuthViewModel authViewModel;
    User user;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUID;

    //TODO : User name MUST to be UNIQUE.

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        user = new User();

        //view model
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        //password from register activity --> set password in user first.
        final Bundle extras = getIntent().getExtras();
        SharedPreferences sp = getSharedPreferences("u_password",MODE_PRIVATE);
        final String pass = sp.getString("u_password","");
        if (pass.equals("")){
            sendToLogin();
            Toast.makeText(this, "המשתמש נותק, אנא התחברו שוב", Toast.LENGTH_SHORT).show();
            return;
        }
            user.setPassword(pass);
            sp.edit().remove("u_password").apply();

        profilePic = findViewById(R.id.create_profile_iv);
        etName = findViewById(R.id.create_profile_name_et);
        etBio = findViewById(R.id.create_profile_bio_et);
        etCompany = findViewById(R.id.create_profile_company_et);
        etEmail = findViewById(R.id.create_profile_email_et);
        etProf = findViewById(R.id.create_profile_prof_et);
        etWeb = findViewById(R.id.create_profile_website_et);
        etPhone = findViewById(R.id.create_profile_phone_et);
        saveProfile = findViewById(R.id.create_profile_btn);
        pb = findViewById(R.id.create_profile_pb);

        Picasso.get().load(R.drawable.ic_baseline_person_24).placeholder(R.drawable.ic_baseline_person_24).into(profilePic);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUID = user.getUid();

        //Set the mail user signed up with, and freeze access -mail is constant


        try {
            etPhone.setEnabled(false);
            etPhone.setText(user.getEmail().substring(0, 10)); // to show only phone number .

        } catch (Exception e) {
            Toast.makeText(this, "אין טלפון נייד למשתמש", Toast.LENGTH_SHORT).show();
        }

        documentReference = db.collection("user").document(currentUID);
        storageReference = FirebaseStorage.getInstance().getReference("Profile Images");

        // upload to DB all the data.
        saveProfile.setOnClickListener(v -> {
            startUploadingWihtImage();

        });

        //select image
        profilePic.setOnClickListener(v -> {
            selectImage();
        });

    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == PICK_IMAGE || resultCode == RESULT_OK || data != null || data.getData() != null) {
                imageUri = data.getData();
                Picasso.get().load(imageUri).into(profilePic);
            }
        } catch (Exception e) {
            Log.d("CreateProfileActivity", "onActivityResult: " + e.getMessage());
        }
    }

    //upload data when btn clicked .
    private void uploadData() {
        final String name = etName.getText().toString();
        final String prof = etProf.getText().toString();
        final String company = etCompany.getText().toString();
        final String bio = etBio.getText().toString();
        final String email = etEmail.getText().toString();
        final String web = etWeb.getText().toString();
        final String phone = etPhone.getText().toString();

        user.setName(name);
        user.setProf(prof);
        user.setUid(currentUID);
        user.setPhone(phone);
        user.setGroupLikes(new ArrayList<>());
        pb.setVisibility(View.VISIBLE); // progress bar.
        Map<String, String> profile = new HashMap<>();
        profile.put("name", name);
        profile.put("profession", prof);
        profile.put("company", company);
        profile.put("bio", bio);
        profile.put("email", email);
        profile.put("web", web);
        profile.put("uid", currentUID);
        profile.put("phone", phone);
        profile.put("groupLikes", null);
        profile.put("privacy", "public");
        profile.put("password", user.getPassword());
        try {
            profile.put("url", imageUri.toString());
            user.setUrl(imageUri.toString());
        } catch (NullPointerException e) {
            profile.put("url", null);
            pb.setVisibility(View.GONE);
            setEnabled(true);
        }


        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        firebaseUser.updateProfile(profileUpdates);
        reAuthenticate(phone + '@' + phone + '.' + phone, email); // do same with auth user details.

        documentReference.set(profile).addOnSuccessListener(aVoid -> {
            pb.setVisibility(View.INVISIBLE);
            Toast.makeText(CreateProfile.this, "הפרופיל נוצר בהצלחה", Toast.LENGTH_SHORT).show();

            Handler handler = new Handler();
            handler.postDelayed(this::sendToStartScreens, 2000);
        });

        pb.setVisibility(View.GONE);
    }

    private void sendToStartScreens() {
        saveProfile.setVisibility(View.GONE);

        FragmentStartViewPager fragment = new FragmentStartViewPager();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.create_activity_container, fragment);
        transaction.commit();
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

    // UploadImage method
    private void startUploadingWihtImage() {
        if (checkMandatoryFields()) {
            setEnabled(false);
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("העלאת התמונה...");
            progressDialog.show();
            if (imageUri != null) {
                // Defining the child of storageReference
                StorageReference ref = FirebaseStorage.getInstance().getReference()
                        .child("Profile Images/").child((user.getUid() + '/') + UUID.randomUUID().toString());

                // adding listeners on upload
                // or failure of image
                // Progress Listener for loading
                // percentage on the dialog box
                ref.putFile(imageUri)
                        .addOnCompleteListener(
                                taskSnapshot -> {
                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    Toast.makeText(getApplicationContext(), "התמונה עלתה בהצלחה!!", Toast.LENGTH_SHORT).show();
                                    final Task downloadUrlTask = ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                        imageUri = uri;
                                        user.setUrl(uri.toString());
                                        pb.setVisibility(View.VISIBLE);
                                        uploadData();

                                    });
                                }).addOnProgressListener(
                        taskSnapshot -> {
                            double progress
                                    = (100.0
                                    * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(
                                    "הועלה "
                                            + (int) progress + "%");
                        });
                ;

            } else {
                user.setUrl("");
                uploadData(); // just upload the data to firestore
                progressDialog.dismiss(); // dismiss the dialog

            }
        }

    }

    // Select Image method
    private void selectImage() {
        // self permission check first.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
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

    private boolean regex(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            return true;
        } else
            etEmail.setError("נא להקליד מייל אמיתי");
        System.out.println(email + " : " + matcher.matches());
        return false;
    }

    private void setEnabled(boolean isEnabled) {
        etEmail.setEnabled(isEnabled);
        etName.setEnabled(isEnabled);
        etBio.setEnabled(isEnabled);
        etCompany.setEnabled(isEnabled);
        etProf.setEnabled(isEnabled);
        etWeb.setEnabled(isEnabled);
        profilePic.setEnabled(isEnabled);
    }

    private void reAuthenticate(String email, String newEmail) {
        final String password = user.getPassword();
        //  Early exit.
        if (password == null) {
            pb.setVisibility(View.GONE);
            setEnabled(true);
            return;
        }
        AuthCredential credential = EmailAuthProvider
                .getCredential(email, password); // Current Login Credentials \\
        // Prompt the user to re-provide their sign-in credentials
        firebaseUser.reauthenticate(credential);
        firebaseUser.updateEmail(newEmail);
    }

    private void sendToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        authViewModel.signOut();
        finish();
    }
}

package com.shaikds.togather.view.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.R;
import com.shaikds.togather.model.User;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ImageActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 22;
    ImageView ivProfile;
    TextView textView;
    private String currentUid;
    Button btnEdit, btnDelete, btnSave, btnBack;
    DocumentReference documentReference;
    private Uri selectedUri;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser user;
    User specificUser;
    SearchUsersViewModel usersViewModel;
    boolean isTouched = false;

    @Override
    protected void onStart() {
        super.onStart();
        specificUser = (User) getIntent().getSerializableExtra("user");


        user = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = user.getUid();

        documentReference = db.collection("user").document(currentUid);
        documentReference.get().addOnCompleteListener(task -> {
            if (!isTouched) {
                if (task.getResult().exists()) {
                    String name = task.getResult().getString("name");
                    String url = task.getResult().getString("url");
                    if (url == null) {
                        Picasso.get().load(R.drawable.ic_baseline_person_24).into(ivProfile);
                    } else {
                        Picasso.get().load(url).into(ivProfile);
                        textView.setText(name);
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        btnDelete = findViewById(R.id.image_delete_btn);
        btnEdit = findViewById(R.id.image_edit_btn);
        ivProfile = findViewById(R.id.image_iv_expand);
        textView = findViewById(R.id.image_tv_name);
        btnSave = findViewById(R.id.profile_img_edit_btn_ok);
        btnBack = findViewById(R.id.activity_image_btn_back);
        usersViewModel = new ViewModelProvider(this).get(SearchUsersViewModel.class);

        btnEdit.setOnClickListener(view -> {
            //go to change photo and upload it
            selectImage();
        });
        btnDelete.setOnClickListener(view -> {
            Picasso.get().load(R.drawable.ic_baseline_person_24).into(ivProfile);
            specificUser.setUrl(null);
            selectedUri = null;
        });

        btnSave.setOnClickListener(view -> {
            uploadImage();
            startActivity(new Intent(ImageActivity.this, MainActivity.class));
            ImageActivity.this.finish();
        });

        btnBack.setOnClickListener(view -> {
            onBackPressed();
            ImageActivity.this.finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            isTouched = true; // for onStart wont do again and again.
            if (requestCode == PICK_IMAGE_REQUEST || resultCode == RESULT_OK || data != null || data.getData() != null) {
                selectedUri = data.getData();
                Picasso.get().load(selectedUri).into(ivProfile);
            }
        } catch (Exception e) {
            Log.d("ImageActivity", "onActivityResult: לא נבחרה תמונה.");
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


    // UploadImage method
    private void uploadImage() {
        if (selectedUri != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("העלאת התמונה...");
            progressDialog.show();
            // Defining the child of storageReference
            StorageReference ref = FirebaseStorage.getInstance().getReference()
                    .child("Profile Images/").child((user.getUid() + '/') + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(selectedUri)
                    .addOnCompleteListener(
                            taskSnapshot -> {
                                // Image uploaded successfully
                                // Dismiss dialog
                                Toast.makeText(getApplicationContext(), "התמונה עלתה בהצלחה!!", Toast.LENGTH_SHORT).show();
                                final Task downloadUrlTask = ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                    specificUser.setUrl(uri.toString());
                                    usersViewModel.changeUserImg(specificUser);
                                });
                            })
                    .addOnProgressListener(
                            taskSnapshot -> {
                                double progress
                                        = (100.0
                                        * taskSnapshot.getBytesTransferred()
                                        / taskSnapshot.getTotalByteCount());
                                progressDialog.setMessage(
                                        "הועלה "
                                                + (int) progress + "%");
                            });
        } else {
            usersViewModel.changeUserImg(specificUser);
        }
    }
}
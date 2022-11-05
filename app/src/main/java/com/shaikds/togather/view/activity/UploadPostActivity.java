package com.shaikds.togather.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.shaikds.togather.model.User;
import com.shaikds.togather.repository.IsraelLocationsRepo;
import com.shaikds.togather.R;
import com.shaikds.togather.model.Post;
import com.shaikds.togather.viewmodel.CategoriesViewModel;
import com.shaikds.togather.viewmodel.SearchUsersViewModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 22;
    ImageView iv;
    ProgressBar progressBar;
    private Uri selectedUri;
    TextView tvOriginalPrice, tvMinPeople, tvMaxPeople,tvTitleDate;
    EditText etDescription, etOriginalPrice, etGroupPrice, etMaxUsers, etMinUsers, etTitle;
    Button btnChooseFile, btnUpload;
    RadioGroup rg;
    RadioButton rb;
    TextView tvDatePick, tvCategory;
    DatePickerDialog.OnDateSetListener datePickerDialog;
    String url, name;
    StorageReference imgReference;
    @ServerTimestamp
    String savedDate;
    String title;
    String currentUID;
    FirebaseFirestore firestore;
    String type, endTime;
    List<String> categories;
    User specificUser;
    CategoriesViewModel categoriesViewModel;
    SearchUsersViewModel usersViewModel;
    private Post post;
    FirebaseUser user;
    Calendar cdate = Calendar.getInstance();
    SimpleDateFormat currentDate;
    private TextView tvLocation;

    public UploadPostActivity() {
        currentDate = new SimpleDateFormat("dd/MM/yyyy");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        title = getIntent().getExtras().get("title").toString();
        setTitle(title);
        findViews();
        imgReference = FirebaseStorage.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        post = new Post();
        IsraelLocationsRepo locationsRepo = new IsraelLocationsRepo();
        List<String> locationList = locationsRepo.readFromFile(UploadPostActivity.this);
        String[] locationString = locationsRepo.castToString(locationList);

        if (!title.equals("Create Group")) { // Power group
            showPowerGroupFieldsOnly();
        }

        categories = new ArrayList<>();
        //View Model Of Categories
        categoriesViewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        categoriesViewModel.gettAllCategoriesName().observe(this, strings -> {
            categories.addAll(strings);
        });

        //View  Model of Users
        usersViewModel = new ViewModelProvider(this).get(SearchUsersViewModel.class);
        specificUser = usersViewModel.getSpecificUser(currentUID);


        //On CLick Listeners

        // Choose Location Dialog.
        tvLocation.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadPostActivity.this);
            builder.setTitle("מיקום");
            builder.setIcon(R.drawable.ic_baseline_add_location_24);
            builder.setItems(locationString, (dialog, which) -> UploadPostActivity.this.checkLocationClicked(which, locationList));
            builder.show();
        });

        // Choose Category Dialog.
        tvCategory.setOnClickListener(v -> {
            String[] categoryNames = new String[categories.size()];
            for (int i = 0; i < categories.size(); i++) {
                categoryNames[i] = categories.get(i);

            }
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadPostActivity.this);
            builder.setTitle("בחר קטגוריה");
            builder.setItems(categoryNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    UploadPostActivity.this.checkCategoryClicked(which, categories);
                }
            });
            builder.show();

        });

        // Create Post
        btnUpload.setOnClickListener(view -> {
            if (getTitle().equals("Create Group")) {
                if (checkGroupFields()) {
                    uploadImage();

                } else { // Not All Fields Are Full
                    Toast.makeText(UploadPostActivity.this, "נא למלא את כל השדות!", Toast.LENGTH_SHORT).show();
                }
            } else { // power group
                if (checkPowerGroupFields()) {
                    uploadImage();
                } else { // Not All Fields Are Full
                    Toast.makeText(UploadPostActivity.this, "נא למלא את כל השדות!", Toast.LENGTH_SHORT).show();
                }
            }

            progressBar.setVisibility(View.INVISIBLE);
        });
        // Photo Choose
        btnChooseFile.setOnClickListener(view ->
                selectImage());
        //time picker dialog
        tvDatePick.setOnClickListener(view -> {
            showDateDialog();
        });

        datePickerDialog = (datePicker, year, month, day) -> {
            month = month + 1;

            if (month < 10) {
                endTime = day + "/0" + month + "/" + year;
            } else if (day < 10) {
                endTime = "0" + day + "/" + month + "/" + year;
            } else {
                endTime = day + "/" + month + "/" + year;
            }
            tvDatePick.setText(endTime);
        };


    }

    private void showDateDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                datePickerDialog,
                year, month, day);
        dialog.setTitle("רק תאריך סיום ואתם מתחילים לעשות כסף.");
        dialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUID = user.getUid();
        DocumentReference documentReference = firestore.collection("user").document(currentUID);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.getResult().exists()) {
                name = task.getResult().getString("name");
                url = task.getResult().getString("url");

            } else {
                Toast.makeText(UploadPostActivity.this, "קיימת בעיה ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void createPost() {
        String currentUID = user.getUid();
        savedDate = currentDate.format(cdate.getTime());


        if (getTitle().equals("Create Group")) { // regular group with manager.
            String desc = etDescription.getText().toString();
            String minPeople = (etMinUsers.getText().toString());
            String maxPeople = (etMaxUsers.getText().toString());
            String originalPrice = etOriginalPrice.getText().toString();
            String groupPrice = etGroupPrice.getText().toString();
            String title = etTitle.getText().toString();
            String ctgryResult = tvCategory.getText().toString();
            String location = tvLocation.getText().toString();
            progressBar.setVisibility(View.VISIBLE);

            //for image type.
            if (type.equals("iv")) { // Image Type - Create Group
                post.setDescription(desc);
                post.setTitle(title);
                checkShipmentMethod(); // puts shipment method via radio group .
                //post.setPostImageUri(downloadUri.toString());
                post.setStartTime(savedDate);
                post.setEndTime(endTime);
                post.setMinPeople(minPeople);
                post.setMaxPeople(maxPeople);
                post.setOriginalPrice(originalPrice);
                post.setGroupPrice(groupPrice);
                post.setCategory(ctgryResult);
                post.setUid(currentUID);
                post.addUser(currentUID);
                post.setLocation(location);
                post.setUsersCount(1);
                post.setEmail(user.getEmail());
                post.setGroupStarted(false);
                post.setGroupManager(user.getDisplayName());
                post.setType("iv");//image group and not video group

                firestore.collection("allgroups").add(post).addOnSuccessListener(documentReference -> Toast.makeText(UploadPostActivity.this, "הפוסט עלה בהצלחה", Toast.LENGTH_SHORT)
                        .show()).addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "נמצאה בעיה ביצירת הקבוצה", Toast.LENGTH_SHORT).show());

                Intent intent = new Intent(UploadPostActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                UploadPostActivity.this.startActivity(intent);
                UploadPostActivity.this.finish();

            } else { // Video Type
                //
            }

        } else { // Create Power Group //
            String groupPrice = etGroupPrice.getText().toString();
            String title = etTitle.getText().toString();
            String ctgryResult = tvCategory.getText().toString();
            String description = etDescription.getText().toString();
            String location = tvLocation.getText().toString();
            Calendar cdate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
            final String savedDate = currentDate.format(cdate.getTime());

            progressBar.setVisibility(View.VISIBLE);
            //for image
            if (type.equals("iv")) { // Image Type - Power Group
                post.setTitle(title);
                post.setShipmentMethod(getString(R.string.shipment_method_all));
                post.setCategory(ctgryResult);
                post.setStartTime(savedDate);
                post.setDescription(description);
                post.setGroupPrice(groupPrice);
                post.setEndTime("");
                post.setGroupStarted(false);
                post.setLocation(location);
                post.setUid(null);  // no manager still.
                post.addUser(currentUID);
                post.setUsersCount(1);
                post.setType("iv");

                firestore.collection("allgroups").add(post).addOnSuccessListener(documentReference -> {
                    Toast.makeText(UploadPostActivity.this, "הפוסט עלה בהצלחה", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    //Intent
                    Intent intent = new Intent(UploadPostActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    UploadPostActivity.this.startActivity(intent);
                    UploadPostActivity.this.finish();
                }).addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "נמצאה בעיה ביצירת הקבוצה", Toast.LENGTH_SHORT).show());

            } else { // Video Type
                Toast.makeText(UploadPostActivity.this, "סרטון", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //Checking which Category clicked and writes it to tvCategory.
    private void checkCategoryClicked(int which, List<String> categories) {
        String categoryName = categories.get(which);
        tvCategory.setText(categoryName);
    }

    //Checking which Location clicked and writes it to tvCategory.
    private void checkLocationClicked(int which, List<String> categories) {
        String categoryName = categories.get(which);
        tvLocation.setText(categoryName);
    }

    private void findViews() {
        rg = findViewById(R.id.upload_post_rg);
        iv = findViewById(R.id.iv_post);
        etTitle = findViewById(R.id.et_title_post);
        etGroupPrice = findViewById(R.id.et_group_price_post);
        etDescription = findViewById(R.id.et_desc_post);
        tvLocation = findViewById(R.id.tv_location_post);
        tvCategory = findViewById(R.id.tv_category_post);
        btnChooseFile = findViewById(R.id.btn_choose_file_post);
        btnUpload = findViewById(R.id.btn_upload_post);
        progressBar = findViewById(R.id.pb_post);
        tvDatePick = findViewById(R.id.et_end_date_post_tv);
        etOriginalPrice = findViewById(R.id.et_original_price_post);
        etMaxUsers = findViewById(R.id.et_max_people_post);
        etMinUsers = findViewById(R.id.et_min_people_post);

        //SIDE TITLES ONLY
        tvMaxPeople = findViewById(R.id.upload_post_tv_max_people);
        tvMinPeople = findViewById(R.id.upload_post_tv_min_people);
        tvOriginalPrice = findViewById(R.id.upload_post_tv_original_price);
        tvTitleDate = findViewById(R.id.upload_post_deadline_tv);
    }

    // check prices and min max users.
    private boolean checkGroupFields() {
        boolean b = false;
        try {
            final int groupPrice = Integer.parseInt(etGroupPrice.getText().toString());
            final int originalPrice = Integer.parseInt(etOriginalPrice.getText().toString());
            final int minUsers = Integer.parseInt(etMinUsers.getText().toString());
            final int maxUsers = Integer.parseInt(etMaxUsers.getText().toString());

            if (groupPrice >= originalPrice) {
                etGroupPrice.setError("המחיר גדול ממחיר רגיל");
                b = false;
            } else if (minUsers > maxUsers) {
                etMinUsers.setError("נא לתקן");
                b = false;
            } else if (selectedUri == null) {
                Toast.makeText(this, "תמונה לא נבחרה", Toast.LENGTH_SHORT).show();
                b = false;
            } else if (etTitle.getText().length() > 30) {
                etTitle.setError("עד 30 תווים");
            } else {
                b = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return b;
        }

        return b;


    }


    // check prices and min max users.
    private boolean checkPowerGroupFields() {
        boolean b = false;
        try {
            int groupPrice = Integer.parseInt(etGroupPrice.getText().toString());
            String title = etTitle.getText().toString();
            String description = etDescription.getText().toString();
            String category = tvCategory.getText().toString();


            if (title == null && title.length() <= 6) {
                etTitle.setError("בעיה בכותרת");
                return false;
            } else if (groupPrice <= 0) {
                etGroupPrice.setError("נא לתקן");
                b = false;
            } else if (description == null && description.length() <= 6) {
                etDescription.setError("6 תווים לפחות");
                b = false;
            } else if (category == null) {
                tvCategory.setError("לא בחרת קטגוריה");
                b = false;
            } else if (selectedUri == null) {
                Toast.makeText(this, "תמונה לא נבחרה", Toast.LENGTH_SHORT).show();
                b = false;
            } else {
                b = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return b;
        }

        return b;


    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        this.finish();
        super.onBackPressed();
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

    // Override onActivityResult method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            selectedUri = data.getData();
            type = "iv";
            try {
                Picasso.get().load(selectedUri).into(iv);
                iv.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                // Log the exception
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "לא נבחרה תמונה", Toast.LENGTH_SHORT).show();
        }
    }

    // UploadImage method
    private void uploadImage() {
        if (selectedUri != null) {
            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("העלאת התמונה...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = imgReference.child("groups/" + UUID.randomUUID().toString());


            // adding listeners on upload
            // or failure of image
            // Progress Listener for loading
            // percentage on the dialog box
            ref.putFile(selectedUri)
                    .addOnCompleteListener(
                            taskSnapshot -> {
                                // Image uploaded successfully
                                // Dismiss dialog
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "התמונה עלתה בהצלחה!!", Toast.LENGTH_SHORT).show();
                                final Task downloadUrlTask = ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                    post.setPostImageUri(uri.toString());
                                    createPost();

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
        }
    }

    private void checkShipmentMethod() {
        rb = findViewById(rg.getCheckedRadioButtonId());
        final String rbResult = rb.getText().toString();
        post.setShipmentMethod(rbResult);
    }


    private void showPowerGroupFieldsOnly() {
        //FIELDS
        tvDatePick.setVisibility(View.INVISIBLE);
        etOriginalPrice.setVisibility(View.INVISIBLE);
        etMaxUsers.setVisibility(View.INVISIBLE);
        etMinUsers.setVisibility(View.INVISIBLE);
        tvDatePick.setVisibility(View.INVISIBLE);
        rg.setVisibility(View.INVISIBLE);
        //SIDE TITLES
        tvTitleDate.setVisibility(View.INVISIBLE);
        tvOriginalPrice.setVisibility(View.INVISIBLE);
        tvMinPeople.setVisibility(View.INVISIBLE);
        tvMaxPeople.setVisibility(View.INVISIBLE);
    }
}




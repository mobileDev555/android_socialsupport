package com.my.socialstress;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.my.socialstress.utils.Global;
import com.my.socialstress.utils.SocialItem;
import com.my.socialstress.utils.SocialItemAdapter;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    int val;

    private EditText username_edit, email_edit, password_edit, password_conf_edit;
    private Button register_btn;
    private ImageView pass_hide, pass_show, pass_hide_conf, pass_show_conf, setupImage;
    private TextView go_login;
    private Spinner social_spinner;

    ArrayList<SocialItem> social_list;
    String userId;
    String username, email, pass, pass_conf;
    private Uri mainImageURI = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.screen_register);

        Global.pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = Global.pref.edit();

        initFirebase();
        initUI();
        initSocialSpinner();
    }

    private void initSocialSpinner() {
        final String[] select_qualification = {
            "Social support Type", "Instrumental support", "Informational support", "Emotional support", "Appraisal support", "Other"
        };

        social_list = new ArrayList<>();

        for (int i = 0; i < select_qualification.length; i++) {
            SocialItem item = new SocialItem();
            item.setTitle(select_qualification[i]);
            item.setSelected(false);
            social_list.add(item);
        }
        SocialItemAdapter myAdapter = new SocialItemAdapter(RegisterActivity.this, 0, social_list);
        social_spinner.setAdapter(myAdapter);
    }

    private void initFirebase() {
        progressDialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    private void initUI() {
        setupImage = findViewById(R.id.setup_image);
        setupImage.setOnClickListener(this);
        username_edit = findViewById(R.id.username_edit);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);
        password_conf_edit = findViewById(R.id.password_edit_conf);
        social_spinner = findViewById(R.id.social_spinner);

        go_login = findViewById(R.id.go_login);
        go_login.setOnClickListener(this);

        register_btn = findViewById(R.id.register_but);
        register_btn.setOnClickListener(this);
        pass_hide = findViewById(R.id.pass_hide);
        pass_hide.setOnClickListener(this);
        pass_show = findViewById(R.id.pass_show);
        pass_show.setOnClickListener(this);

        pass_hide_conf = findViewById(R.id.pass_hide_conf);
        pass_hide_conf.setOnClickListener(this);
        pass_show_conf = findViewById(R.id.pass_show_conf);
        pass_show_conf.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.register_but:
                registerProfile();
                break;
            case R.id.go_login:
                finish();
                break;
            case R.id.setup_image:
                setupProfilePhoto();
                break;
            case R.id.pass_hide:
                password_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                pass_show.setVisibility(View.VISIBLE);
                pass_hide.setVisibility(View.GONE);
                break;
            case R.id.pass_show:
                password_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                pass_hide.setVisibility(View.VISIBLE);
                pass_show.setVisibility(View.GONE);
                break;
            case R.id.pass_hide_conf:
                password_conf_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                pass_show_conf.setVisibility(View.VISIBLE);
                pass_hide_conf.setVisibility(View.GONE);
                break;
            case R.id.pass_show_conf:
                password_conf_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                pass_hide_conf.setVisibility(View.VISIBLE);
                pass_show_conf.setVisibility(View.GONE);
                break;
        }
    }

    private void setupProfilePhoto() {
        // Check permissions for Image Upload
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                openImagePicker();
            }
        } else {
            openImagePicker();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(RegisterActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            99);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            99);
                }
                return;
            }
        }
    }
    private void openImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(RegisterActivity.this);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Crop Image Result handler
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                setupImage.setImageURI(mainImageURI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    private void registerProfile() {
        username = username_edit.getText().toString().trim();
        email = email_edit.getText().toString().trim();
        pass = password_edit.getText().toString().trim();
        pass_conf = password_conf_edit.getText().toString().trim();

        if(username.equals("") || email.equals("") || pass.equals("")) {
            Global.showAlertWithTitle(RegisterActivity.this, "Warning", "Please fill all field");
            return;
        }
        if(!pass.equals(pass_conf)) {
            Global.showAlertWithTitle(RegisterActivity.this, "Warning", "Please enter password correctly");
            return;
        }

        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    storeProfileImgFirebase();
                } else {
                    hideProgressDialog();
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void storeProfileImgFirebase() {
        userId = mAuth.getCurrentUser().getUid();
        if(mainImageURI == null) {
            hideProgressDialog();
            deleteUserFromFirebaseAuth();
            return;
        }
        final StorageReference imagePath = storageReference.child("profile_images").child(userId + ".jpg");
        UploadTask uploadTask = imagePath.putFile(mainImageURI);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return imagePath.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    storeDataFirestore(task);
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this, "Image Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    hideProgressDialog();
                }
            }
        });
    }

    private void deleteUserFromFirebaseAuth() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(email_edit.getText().toString().trim(), password_edit.getText().toString().trim());

        // Prompt the user to re-provide their sign-in credentials
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Please select profile photo", Toast.LENGTH_LONG).show();
                                            Log.d("=========firebase auth", "User account deleted.");
                                        }
                                    }
                                });

                    }
                });
    }

    private void storeDataFirestore(Task<Uri> task) {
        Uri downloadUri;
        if (task != null) {
            downloadUri = task.getResult();
        } else {
            downloadUri = mainImageURI;
        }

        String[] spin_str = new String[social_list.size()];
        for(int k=0; k<social_list.size(); k++) {
            if(social_list.get(k).isSelected()) spin_str[k] = "1";
            else spin_str[k] = "0";
        }
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userid", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("password", pass);
        userMap.put("socialtype", Arrays.toString(spin_str));
        userMap.put("photo", downloadUri.toString());
        userMap.put("friendList", "");
        userMap.put("socialTies", "");
        userMap.put("pre_mood", "0,0,0,0,0,0,0");
        userMap.put("cur_mood", "0,0,0,0,0,0,0");

        db.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "The Account are registered successfully", Toast.LENGTH_SHORT).show();
                    Intent mainIntent = new Intent(RegisterActivity.this, TestActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(RegisterActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}


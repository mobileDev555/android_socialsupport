package com.my.socialstress;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.socialstress.utils.Global;

import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    int val;

    private EditText email_edit, password_edit;
    private Button login_btn, register_btn;
    private ImageView pass_hide, pass_show;
    private CheckBox remember_chk;
    private boolean rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.screen_login);

        Global.pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = Global.pref.edit();

        initFirebase();
        initUI();
    }
    private void initFirebase() {
        progressDialog = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    private void initUI() {
        remember_chk = findViewById(R.id.remember_chk);
        remember_chk.setOnClickListener(this);
        email_edit = findViewById(R.id.email_edit);
        password_edit = findViewById(R.id.password_edit);
        login_btn = findViewById(R.id.login_but);
        login_btn.setOnClickListener(this);
        register_btn = findViewById(R.id.register_but);
        register_btn.setOnClickListener(this);
        pass_hide = findViewById(R.id.pass_hide);
        pass_hide.setOnClickListener(this);
        pass_show = findViewById(R.id.pass_show);
        pass_show.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.login_but:
                loginProfile();
                break;
            case R.id.register_but:
                Intent intent_res = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent_res);
                break;
            case R.id.remember_chk:
                rememberMe = !rememberMe;
                remember_chk.setChecked(rememberMe);
                break;
            case R.id.pass_hide:
                password_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                pass_show.setVisibility(View.VISIBLE);
                pass_hide.setVisibility(View.GONE);
                break;
            case R.id.pass_show: {
                password_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
                pass_hide.setVisibility(View.VISIBLE);
                pass_show.setVisibility(View.GONE);
                break;
            }

        }
    }

    private void loginProfile() {
        final String email = email_edit.getText().toString();
        final String password = password_edit.getText().toString();

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            showProgressDialog();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        if (rememberMe) {
                            editor.putString(Global.EMAIL, email);
                            editor.putString(Global.PASSWORD, password);
                            editor.commit();
                        }
                        navigateToMain();
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            Toast.makeText(LoginActivity.this, "Login Fields must not be empty", Toast.LENGTH_SHORT).show();
        }
    }
    private void navigateToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

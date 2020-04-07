package com.my.socialstress;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.my.socialstress.utils.Global;

import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class MainActivity extends BaseActivity implements SmileRating.OnSmileySelectionListener, SmileRating.OnRatingSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private SmileRating mSmileRating;

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private LinearLayout linear_friend;
    private ImageView search_img, friend_img;
    private EditText search_edit;
    private TextView mood_txt, online_txt, dass_txt, friend_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = Global.pref.edit();

        initEmoji();
        initFirebase();
        initUI();
    }

    private void initUI() {
        search_img = findViewById(R.id.search_img);
        search_img.setOnClickListener(this);
        search_edit = findViewById(R.id.search_edit);
        mood_txt = findViewById(R.id.mood_txt);
        mood_txt.setOnClickListener(this);
        online_txt = findViewById(R.id.online_txt);
        online_txt.setOnClickListener(this);
        dass_txt = findViewById(R.id.dass_txt);
        dass_txt.setOnClickListener(this);

        linear_friend = findViewById(R.id.linear_friend);
        friend_img = findViewById(R.id.friend_img);
        friend_txt = findViewById(R.id.friend_txt);
    }
    private void initFirebase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    private void initEmoji() {
        mSmileRating = (SmileRating) findViewById(R.id.ratingView);
        mSmileRating.setOnSmileySelectionListener(this);
        mSmileRating.setOnRatingSelectedListener(this);
//        Typeface typeface = Typeface.createFromAsset(getAssets(), "MetalMacabre.ttf");
//        mSmileRating.setTypeface(typeface);
    }
    @Override
    public void onSmileySelected(@BaseRating.Smiley int smiley, boolean reselected) {
        switch (smiley) {
            case SmileRating.BAD:
                Log.i(TAG, "Bad");
                break;
            case SmileRating.GOOD:
                Log.i(TAG, "Good");
                break;
            case SmileRating.GREAT:
                Log.i(TAG, "Great");
                break;
            case SmileRating.OKAY:
                Log.i(TAG, "Okay");
                break;
            case SmileRating.TERRIBLE:
                Log.i(TAG, "Terrible");
                break;
            case SmileRating.NONE:
                Log.i(TAG, "None");
                break;
        }
    }

    @Override
    public void onRatingSelected(int level, boolean reselected) {
        Log.i(TAG, "Rated as: " + level + " - " + reselected);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.search_img:
                searchFriend();
                break;
            case R.id.mood_txt:
                Intent intent_mood = new Intent(MainActivity.this, MoodActivity.class);
                startActivity(intent_mood);
                break;
            case R.id.online_txt:
                Intent intent_online = new Intent(MainActivity.this, OnlineActivity.class);
                startActivity(intent_online);
                break;
            case R.id.dass_txt:
                Intent intent_dass = new Intent(MainActivity.this, DassActivity.class);
                startActivity(intent_dass);
                break;
        }
    }

    private void searchFriend() {
        if (search_edit.getText().toString().trim().equals("")) {
            Global.showAlert(MainActivity.this, "Please enter search key");
        } else {
            isCheckExistData();
        }
    }
    private void isCheckExistData() {
        linear_friend.setVisibility(View.GONE);
        showProgressDialog();
        Query usersQuery = db.collection("Users")
                .whereEqualTo("email", search_edit.getText().toString());
        usersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                hideProgressDialog();
                if (queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String username = "";
                        String photo = "";
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                username = doc.getDocument().getString("username");
                                photo = doc.getDocument().getString("photo");
                                Uri p_uri = Uri.parse(photo);
                                linear_friend.setVisibility(View.VISIBLE);
                                friend_txt.setText(username);

                                RequestOptions placeholderRequest = new RequestOptions();
                                placeholderRequest.placeholder(R.drawable.avatar_user);
                                Glide.with(MainActivity.this).setDefaultRequestOptions(placeholderRequest).load(photo).into(friend_img);
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No search result", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        search_edit.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            Global.pref.edit().clear().commit();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

package com.my.socialstress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class MainActivity extends BaseActivity implements SmileRating.OnSmileySelectionListener, SmileRating.OnRatingSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private SmileRating mSmileRating;

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private LinearLayout linear_friend;
    private ImageView search_img, friend_img, pre_img, cur_img, cancel_img;
    private EditText search_edit;
    private TextView mood_txt, online_txt, dass_txt, friend_txt, chart_title_txt;

    private BarChart chart;
    private String dayOfTheWeek;
    private int day_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.pref = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = Global.pref.edit();

        initFirebase();
        initUI();
        initEmoji();
    }

    private void initUI() {
        chart = findViewById(R.id.barChart);
        search_img = findViewById(R.id.search_img);
        search_img.setOnClickListener(this);
        pre_img = findViewById(R.id.pre_img);
        pre_img.setOnClickListener(this);
        cur_img = findViewById(R.id.cur_img);
        cur_img.setOnClickListener(this);
        search_edit = findViewById(R.id.search_edit);
        mood_txt = findViewById(R.id.mood_txt);
        mood_txt.setOnClickListener(this);
        online_txt = findViewById(R.id.online_txt);
        online_txt.setOnClickListener(this);
        dass_txt = findViewById(R.id.dass_txt);
        dass_txt.setOnClickListener(this);
        cancel_img = findViewById(R.id.cancel_img);
        cancel_img.setOnClickListener(this);
        chart_title_txt = findViewById(R.id.chart_title_txt);

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
        SimpleDateFormat sdf = new SimpleDateFormat("EE");
        Date d = new Date();
        dayOfTheWeek = sdf.format(d);
        switch (dayOfTheWeek) {
            case "Sun":
                day_num = 0;
                break;
            case "Mon":
                day_num = 1;
                break;
            case "Tue":
                day_num = 2;
                break;
            case "Wed":
                day_num = 3;
                break;
            case "Thu":
                day_num = 4;
                break;
            case "Fri":
                day_num = 5;
                break;
            case "Sat":
                day_num = 6;
                break;
        }

        mSmileRating = (SmileRating) findViewById(R.id.ratingView);
        mSmileRating.setOnSmileySelectionListener(this);
        mSmileRating.setOnRatingSelectedListener(this);

        int emoji_val = Global.pref.getInt(Global.EMOJI, 0);
        int day_val = Global.pref.getInt(Global.DAYWEEK, 0);
        if(day_val == day_num) {
            if(emoji_val == 1) mSmileRating.setSelectedSmile(BaseRating.TERRIBLE);
            else if(emoji_val == 2) mSmileRating.setSelectedSmile(BaseRating.BAD);
            else if(emoji_val == 3) mSmileRating.setSelectedSmile(BaseRating.OKAY);
            else if(emoji_val == 4) mSmileRating.setSelectedSmile(BaseRating.GOOD);
            else if(emoji_val == 5) mSmileRating.setSelectedSmile(BaseRating.GREAT);
        }
        getEmojiFirebase(0);
    }

    private void getEmojiFirebase(final int flag) {
            String currentUserId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        String pre_mood = task.getResult().getString("pre_mood");
                        String cur_mood = task.getResult().getString("cur_mood");

                        if(flag == 0) {
                            initBarChart(cur_mood);
                        } else if(flag == 1) {
                            initBarChart(pre_mood);
                        }
                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
    private void saveEmojiFirebase(final int moodVal) {
        editor.putInt(Global.EMOJI, moodVal);
        editor.putInt(Global.DAYWEEK, day_num);
        editor.commit();

        showProgressDialog();
        final String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    String pre_mood = task.getResult().getString("pre_mood");
                    String cur_mood = task.getResult().getString("cur_mood");//"2,3,2,0,0,0,0"

                    String mood = "";
                    String[] c_m_arr = cur_mood.split(",");
                    for(int i=0; i<c_m_arr.length; i++) {
                        if(i == day_num) {
                            if(mood.equals("")) mood = String.valueOf(moodVal);
                            else mood = mood +","+String.valueOf(moodVal);
                        } else {
                            if(mood.equals("")) mood = c_m_arr[i];
                            else mood = mood +","+c_m_arr[i];
                        }
                    }

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("cur_mood", mood);
                    db.collection("Users").document(currentUserId).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                getEmojiFirebase(0);
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    public void onSmileySelected(@BaseRating.Smiley int smiley, boolean reselected) {
        switch (smiley) {
            case SmileRating.TERRIBLE:
                Log.i(TAG, "Terrible");
                saveEmojiFirebase(1);
                break;
            case SmileRating.BAD:
                Log.i(TAG, "Bad");
                saveEmojiFirebase(2);
                break;
            case SmileRating.OKAY:
                Log.i(TAG, "Okay");
                saveEmojiFirebase(3);
                break;
            case SmileRating.GOOD:
                Log.i(TAG, "Good");
                saveEmojiFirebase(4);
                break;
            case SmileRating.GREAT:
                Log.i(TAG, "Great");
                saveEmojiFirebase(5);
                break;
            case SmileRating.NONE:
                Log.i(TAG, "None");
                saveEmojiFirebase(0);
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
            case R.id.pre_img:
                getEmojiFirebase(1);
                chart_title_txt.setText("Daily Checkins\n(preview week)");
                break;
            case R.id.cur_img:
                getEmojiFirebase(0);
                chart_title_txt.setText("Daily Checkins\n(current week)");
                break;
            case R.id.cancel_img:
                linear_friend.setVisibility(View.GONE);
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
                .whereEqualTo("email", search_edit.getText().toString().trim());
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


    private void initBarChart(String cur_mood) {
        if(cur_mood == null) return;
        String[] cur_mood_arr = cur_mood.split(",");
        int groupCount = cur_mood_arr.length;

        final String[] date_day = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};


        chart.setDescription(null);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        ArrayList<BarEntry> entries = new ArrayList<>();
        for(int i=0; i<cur_mood_arr.length; i++) {
            int index = i+1;
            entries.add(new BarEntry(i, Integer.parseInt(cur_mood_arr[i])));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Mood");
        barDataSet.setBarBorderWidth(0f);
        barDataSet.setStackLabels(date_day);
        barDataSet.setColor(getResources().getColor(R.color.colorYellow));
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.7f);
        chart.setData(barData);
        chart.setFitBars(true);
        chart.animateY(1500);
        chart.invalidate();

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(true);
        l.setYOffset(12f);
        l.setXOffset(0f);
        l.setYEntrySpace(0f);
        l.setTextSize(10f);

        //X-axis
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(groupCount);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(date_day));

        //Y-axis
        chart.getAxisRight().setEnabled(false);
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

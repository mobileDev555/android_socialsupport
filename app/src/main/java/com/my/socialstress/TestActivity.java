package com.my.socialstress;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.socialstress.utils.Global;
import com.my.socialstress.utils.QuizDbHelper;
import com.my.socialstress.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.my.socialstress.utils.Global.PREF_NAME;
import static com.my.socialstress.utils.Global.editor;

public class TestActivity extends BaseActivity implements View.OnClickListener {

    private View mainView;
    private Button continue_btn;
    private TextView q_txt, q_count_txt;
    private RadioGroup rbGroup;
    private RadioButton rb1, rb2, rb3, rb4, rb5;

    private int score;
    boolean answered = false;
    private ArrayList<String> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private String currentQuestion;
    private String json;

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    QuizDbHelper quizDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.screen_test);

        mainView = findViewById(R.id.mainView);
        Global.hideSystemUI(mainView);

        initFirebase();
        initJSONData();
        initUI();
        showNextQuestion();
    }
    private void initFirebase() {
        quizDbHelper = new QuizDbHelper(TestActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    private void initJSONData() {
        questionList = new ArrayList<>();
        json = Utils.inputStreamToString(getResources().openRawResource(R.raw.json_test));
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("QA");

            questionCountTotal = jsonArray.length();
            for(int i=0; i<questionCountTotal; i++) {
                JSONObject q_obj = jsonArray.getJSONObject(i);
                String quiz = q_obj.getString("question");
                questionList.add(quiz);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        continue_btn = findViewById(R.id.continue_but);
        continue_btn.setOnClickListener(this);
        q_txt = findViewById(R.id.q_txt);
        q_count_txt = findViewById(R.id.q_count_txt);

        rbGroup = (RadioGroup) findViewById(R.id.radio_group);
        rb1 = (RadioButton) findViewById(R.id.radio_button1);
        rb2 = (RadioButton) findViewById(R.id.radio_button2);
        rb3 = (RadioButton) findViewById(R.id.radio_button3);
        rb4 = (RadioButton) findViewById(R.id.radio_button4);
        rb5 = (RadioButton) findViewById(R.id.radio_button5);
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.continue_but:
                continueQuiz();
                break;
        }
    }


    private void continueQuiz() {
        if (!answered) {
            if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked() || rb5.isChecked()) {
                checkAnswer();
            } else {
                Toast.makeText(TestActivity.this, "Please select answer", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkAnswer() {
        answered = true;

        RadioButton rbSelected = findViewById(rbGroup.getCheckedRadioButtonId());
        int selected_val = rbGroup.indexOfChild(rbSelected);
        score = score + selected_val;

        showNextQuestion();
    }
    private void showNextQuestion() {
        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);
            int show_counter = questionCounter + 1;
            q_count_txt.setText("Questions:" + show_counter+"/"+questionCountTotal);
            q_txt.setText("Q."+show_counter+":"+currentQuestion);

            questionCounter++;
            answered = false;
            continue_btn.setText("Confirm");
        } else {
            confirmData();
        }
    }
    private void confirmData() {
        final int val = score;

        final String userId = mAuth.getCurrentUser().getUid();

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final String dateToStr = format.format(today);

        quizDbHelper.insertTest(userId, score, dateToStr);

        String msg = "";
        if(val > 12) {
            msg = "High personality score\n" +
                    "\n" +
                    "  1. Open-Mindedness\n" +
                    "   “High scorers tend to be original, creative, curious, complex”\n" +
                    "        \n" +
                    "  2. Conscientiousness\n" +
                    "  “High scorers tend to be reliable, well-organized, self-disciplined, and careful”\n" +
                    "        \n" +
                    "  3. Extraversion\n" +
                    "   “High scorers tend to be sociable, friendly, fun loving, talkative;”\n" +
                    "        \n" +
                    "  4. Agreeableness\n" +
                    "   “High scorers tend to be good natured, sympathetic, forgiving, and courteous”\n" +
                    "        \n" +
                    "  5. Negative Emotionality\n" +
                    "   “High scorers tend to be nervous, high-strung, insecure, worrying”";
        } else {
            msg = "Low personality score\n" +
                    "\n" +
                    "       1. Open-Mindedness\n" +
                    "        “Low scorers tend to be conventional, down to earth, narrow interests, uncreative.”\n" +
                    "        \n" +
                    "      2.  Conscientiousness \n" +
                    "       “Low scorers tend to be disorganized, undependable, and negligent.”\n" +
                    "        \n" +
                    "       3. Extraversion\n" +
                    "        “Low scorers tend to be introverted, reserved, inhibited, and quiet.”\n" +
                    "        \n" +
                    "        4. Agreeableness\n" +
                    "        “Low scorers tend to be critical, rude, harsh, and callous.”\n" +
                    "        \n" +
                    "        5. Negative Emotionality\n" +
                    "        “Low scorers tend to be calm, relaxed, secure, and hardy.”";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(TestActivity.this);
        builder.setTitle("Message")
                .setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePersonalScore(val);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void savePersonalScore(int val) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("p_score", val);

        db.collection("Users").document(mAuth.getCurrentUser().getUid()).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(TestActivity.this, "Added", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(TestActivity.this, MainActivity.class));
                    finish();
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(TestActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

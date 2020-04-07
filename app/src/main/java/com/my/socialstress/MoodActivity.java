package com.my.socialstress;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.socialstress.utils.Global;
import com.my.socialstress.utils.Question;
import com.my.socialstress.utils.QuizDbHelper;
import com.my.socialstress.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.my.socialstress.utils.Global.editor;

public class MoodActivity extends BaseActivity implements View.OnClickListener {
    private View mainView;
    private Button continue_btn;
    private TextView q_txt, q_count_txt;
    private RadioGroup rbGroup;
    private RadioButton rb1, rb2, rb3, rb4, rb5;

    private int score, count;
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
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e){}
        setContentView(R.layout.screen_mood);

        initFirebase();
        initJSONData();
        initUI();
        showNextQuestion();
    }
    private void initFirebase() {
        quizDbHelper = new QuizDbHelper(MoodActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    private void initJSONData() {
        questionList = new ArrayList<>();
        json = Utils.inputStreamToString(getResources().openRawResource(R.raw.json_mood));
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
                Toast.makeText(MoodActivity.this, "Please select answer", Toast.LENGTH_LONG).show();
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
        int val = score;
        final String userId = mAuth.getCurrentUser().getUid();

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final String dateToStr = format.format(today);

        int c  = quizDbHelper.numberOfRows(QuizDbHelper.TABLE_MOOD);
        Log.e("===========data", ""+c);
        count++;

        if(quizDbHelper.insertMood(userId, score, dateToStr, count)) {
            if (val > 12) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MoodActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Message")
                        .setMessage("Your mood is not normal DASS test is required");
                final AlertDialog dialog = builder.create();
                dialog.show();

                autoHideDialog(dialog, 5000);
            } else {
                startActivity(new Intent(MoodActivity.this, MainActivity.class));
                finish();
            }
        }
    }

    private void autoHideDialog(final AlertDialog dialog, int i) {
        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        };
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.removeCallbacks(runnable);
                startActivity(new Intent(MoodActivity.this, DassActivity.class));
                finish();
            }
        });
        handler.postDelayed(runnable, i);
    }
}

//        showProgressDialog();
//        Query usersQuery = db.collection("Moods")
//                .whereEqualTo("userId", userId)
//                .whereEqualTo("mood_date", dateToStr);
//        usersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
//                if (queryDocumentSnapshots != null) {
//                    if (!queryDocumentSnapshots.isEmpty()) {
//                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
//                            if (doc.getType() == DocumentChange.Type.ADDED) {
//                                double mood_count = doc.getDocument().getDouble("mood_count");
//                                String mood_id = doc.getDocument().getId();
//                                String mood_score = doc.getDocument().getString("mood_score");
//                                Log.e("=======mood id", mood_id);
//                                if(mood_count > 2) {
//                                    Global.showAlert(MoodActivity.this, "You already saved mood state 3 times today");
//                                    return;
//                                } else {
//                                    final int c = (int)mood_count + 1;
//                                    String m_s = mood_score + ","+score;
//                                    Map<String, Object> userMap = new HashMap<>();
//                                    userMap.put("userId", userId);
//                                    userMap.put("mood_score", score);
//                                    userMap.put("mood_date", dateToStr);
//                                    userMap.put("mood_count", c);
//
//                                    db.collection("Moods").document(mood_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Toast.makeText(MoodActivity.this, "Saved score successfully", Toast.LENGTH_SHORT).show();
//                                                editor.putInt(Global.COUNT, c);
//                                                editor.commit();
//                                            } else {
//                                                String errorMessage = task.getException().getMessage();
//                                                Toast.makeText(MoodActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
//                                            }
//                                        }
//                                    });
//                                }
//                            }
//                        }
//                    } else {
//                    }
//                }
//            }
//        });

//        db.collection("Moods").add(userMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentReference> task) {
//                if (task.isSuccessful()) {
//                    Toast.makeText(MoodActivity.this, "Post added successfully", Toast.LENGTH_LONG).show();
//                } else {
//                    String errorMessage = task.getException().getMessage();
//                }
//            }
//        });
//        db.collection("Users").document(userId).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                hideProgressDialog();
//                if (task.isSuccessful()) {
//                    Toast.makeText(MoodActivity.this, "successfully", Toast.LENGTH_SHORT).show();
//                } else {
//                    String errorMessage = task.getException().getMessage();
//                    Toast.makeText(MoodActivity.this, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
//                }
//            }
//        });

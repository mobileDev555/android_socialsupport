package com.my.socialstress;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.my.socialstress.utils.Global;
import com.my.socialstress.utils.MyFriendRecyclerAdapter;
import com.my.socialstress.utils.MyRecyclerAdapter;
import com.my.socialstress.utils.QuizDbHelper;
import com.my.socialstress.utils.User;
import com.my.socialstress.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DassActivity extends BaseActivity implements View.OnClickListener {
    private View mainView;
    private Button continue_btn;
    private TextView q_txt, q_count_txt;
    private RadioGroup rbGroup;
    private RadioButton rb1, rb2, rb3, rb4, rb5;
    private ImageView back_img;

    private int score;
    private boolean answered = false;
    private ArrayList<String> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private String currentQuestion;
    private String json;

    List<String> name_arr = new ArrayList<>();
    List<String> image_arr = new ArrayList<>();;
    List<String> email_arr = new ArrayList<>();;
    int index = 0;

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
        setContentView(R.layout.screen_dass);

//        mainView = findViewById(R.id.mainView);
//        Global.hideSystemUI(mainView);

        initFirebase();
        initJSONData();
        initUI();
        showNextQuestion();
    }
    private void initFirebase() {
        quizDbHelper = new QuizDbHelper(DassActivity.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initJSONData() {
        questionList = new ArrayList<>();
        json = Utils.inputStreamToString(getResources().openRawResource(R.raw.json_dass));
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
        back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(this);
        q_txt = findViewById(R.id.q_txt);
        q_count_txt = findViewById(R.id.q_count_txt);

        rbGroup = (RadioGroup) findViewById(R.id.radio_group);
        rb1 = (RadioButton) findViewById(R.id.radio_button1);
        rb2 = (RadioButton) findViewById(R.id.radio_button2);
        rb3 = (RadioButton) findViewById(R.id.radio_button3);
        rb4 = (RadioButton) findViewById(R.id.radio_button4);
        rb5 = (RadioButton) findViewById(R.id.radio_button5);
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
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.continue_but:
                continueQuiz();
                break;
            case R.id.back_img:
                finish();
                break;
        }
    }
    private void continueQuiz() {
        if (!answered) {
            if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked() || rb5.isChecked()) {
                checkAnswer();
            } else {
                Toast.makeText(DassActivity.this, "Please select answer", Toast.LENGTH_LONG).show();
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
    private void confirmData() {
        int val = score;

        final String userId = mAuth.getCurrentUser().getUid();

        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final String dateToStr = format.format(today);

        quizDbHelper.insertDass(userId, score, dateToStr);

        if(val >= 0 && val < 15) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DassActivity.this);
            builder.setCancelable(false);
            builder.setTitle("Normal")
                    .setMessage("Your current stress level is normal");
            AlertDialog dialog = builder.create();
            dialog.show();

            autoHideDialog(dialog, 5000, 1);

        } else if(val >= 15 && val < 19) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DassActivity.this);
            builder.setCancelable(false);
            builder.setTitle("Mild")
                    .setMessage("Your current stress level is Mild");
            AlertDialog dialog = builder.create();
            dialog.show();

            autoHideDialog(dialog, 5000, 1);

        } else if(val >= 19 && val < 26) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DassActivity.this);
            builder.setCancelable(false);
            builder.setTitle("Moderate")
                    .setMessage("Your current stress level is High");
            AlertDialog dialog = builder.create();
            dialog.show();

            autoHideDialog(dialog, 5000, 2);

        } else if(val >= 26 && val < 34) {
            AlertDialog.Builder builder = new AlertDialog.Builder(DassActivity.this);
            builder.setCancelable(false);
            builder.setTitle("Sever")
                    .setMessage("Your current stress level is Serve");
            AlertDialog dialog = builder.create();
            dialog.show();

            autoHideDialog(dialog, 5000, 2);

        } else if(val >= 34){
            startActivity(new Intent(DassActivity.this, OnlineActivity.class));
            finish();
        }
    }

    private void autoHideDialog(final AlertDialog dialog, int i, final int flag) {
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
                if(flag == 1) {
                    startActivity(new Intent(DassActivity.this, MainActivity.class));
                    finish();
                } else if(flag == 2) {
                    showMachingSearchDialog();
                }
            }
        });
        handler.postDelayed(runnable, i);
    }

    private void showMachingSearchDialog() {
        final Dialog dialog = new Dialog(DassActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.match_input_dialog);

        final CheckBox support_chk1 = (CheckBox) dialog.findViewById(R.id.support_chk1);
        final CheckBox support_chk2 = (CheckBox) dialog.findViewById(R.id.support_chk2);
        final CheckBox support_chk3 = (CheckBox) dialog.findViewById(R.id.support_chk3);
        final CheckBox support_chk4 = (CheckBox) dialog.findViewById(R.id.support_chk4);

        final RadioButton radio_weak = (RadioButton) dialog.findViewById(R.id.radio_weak);
        final RadioButton radio_strong = (RadioButton) dialog.findViewById(R.id.radio_strong);

        Button confirm_btn = (Button) dialog.findViewById(R.id.confirm_btn);
        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                String chk_str = "";
                if(support_chk1.isChecked()) {
                    if(chk_str.equals("")) chk_str = ""+1;
                    else chk_str = chk_str +", "+1;
                } else {
                    if(chk_str.equals("")) chk_str = ""+0;
                    else chk_str = chk_str +", "+0;
                }

                if(support_chk2.isChecked()) {
                    if(chk_str.equals("")) chk_str = ""+1;
                    else chk_str = chk_str +", "+1;
                } else {
                    if(chk_str.equals("")) chk_str = ""+0;
                    else chk_str = chk_str +", "+0;
                }

                if(support_chk3.isChecked()) {
                    if(chk_str.equals("")) chk_str = ""+1;
                    else chk_str = chk_str +", "+1;
                } else {
                    if(chk_str.equals("")) chk_str = ""+0;
                    else chk_str = chk_str +", "+0;
                }

                if(support_chk4.isChecked()) {
                    if(chk_str.equals("")) chk_str = ""+1;
                    else chk_str = chk_str +", "+1;
                } else {
                    if(chk_str.equals("")) chk_str = ""+0;
                    else chk_str = chk_str +", "+0;
                }
                chk_str = "[0, "+chk_str+", 0]";

                String radio_str = "";
                if(radio_strong.isChecked()) radio_str = (String) radio_strong.getText();
                else if(radio_weak.isChecked()) radio_str = (String) radio_weak.getText();
                searchMatchingFriend(chk_str, radio_str);
            }
        });

        dialog.show();
    }

    private void searchMatchingFriend(final String chk_str, final String radio_str) {
        index = 0;
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        final String mineFriendList = task.getResult().getString("friendList");
                        final String socialTies = task.getResult().getString("socialTies");

                        if (mineFriendList.equals("")) return;

                        final String[] f_arr = mineFriendList.split(",");
                        String[] t_arr = socialTies.split(",");
                        final boolean[] isExist = {false};
                        for(int i=0; i< f_arr.length; i++) {
                            final String userId = f_arr[i].trim();
                            final String ties = t_arr[i].trim();

                            if(ties.equals(radio_str)) {
                                db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().exists()) {
                                                String name = task.getResult().getString("username");
                                                String image = task.getResult().getString("photo");
                                                String email = task.getResult().getString("email");
                                                String socialtype = task.getResult().getString("socialtype");
                                                String password = task.getResult().getString("password");

                                                if (chk_str.equals(socialtype)) {
                                                    Log.e("====friends", chk_str+"||"+socialtype);
                                                    isExist[0] = true;
                                                    name_arr.add(name);
                                                    image_arr.add(image);
                                                    email_arr.add(email);
                                                }

                                            }
                                        }
                                    }
                                });
                            }
                        }
                        final Handler handler = new Handler();
                        final Runnable r = new Runnable() {
                            public void run() {
                                if(isExist[0]) {
                                    int num = name_arr.size();
                                    Random r = new Random();
                                    int num_index = r.nextInt(num);
                                    showFriendDialog(name_arr.get(num_index), image_arr.get(num_index), email_arr.get(num_index));
                                    return;
                                } else {
                                    finish();
                                }
                                handler.postDelayed(this, 1000);
                            }
                        };
                        handler.postDelayed(r, 1000);
                    }
                }
            }
        });
    }

    private void showFriendDialog(String username, String photo, final String email) {
        final Dialog dialog = new Dialog(DassActivity.this);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.match_friend_dialog);
        Window window = dialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView username_txt = (TextView) dialog.findViewById(R.id.username);
        username_txt.setText(username);
        ImageView user_img = (ImageView) dialog.findViewById(R.id.user_img);
        final EditText msg_edit = (EditText) dialog.findViewById(R.id.msg_edit);

        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.avatar_user);
        Glide.with(DassActivity.this).setDefaultRequestOptions(placeholderRequest).load(photo).into(user_img);

        Button send_btn = (Button) dialog.findViewById(R.id.send_but);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                String msg = msg_edit.getText().toString();
                requireHelp(msg, email);
            }
        });
        Button close_btn = (Button) dialog.findViewById(R.id.close_but);
        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    private void requireHelp(String msg, String email) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
        sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
        sendIntent.putExtra(Intent.EXTRA_TITLE, "Need Help");
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Need Help");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Send message..."));
        finish();
    }
}

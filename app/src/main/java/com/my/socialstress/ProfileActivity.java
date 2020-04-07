package com.my.socialstress;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.my.socialstress.utils.MyRecyclerAdapter;
import com.my.socialstress.utils.QuizDbHelper;
import com.my.socialstress.utils.SocialItem;
import com.my.socialstress.utils.SocialItemAdapter;
import com.my.socialstress.utils.User;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    private View mainView;
    private TextView username_txt, useremail_txt, msg_txt;
    private ImageView user_img, back_img;
    private Spinner social_spinner;
    private RecyclerView friend_list_view;
    private LinearLayout linear_friend_list;
    private Button view_friend_btn, add_friend_btn;

    private static MyRecyclerAdapter adapter;
    List<User> userList;
    ArrayList<SocialItem> social_list;

    public StorageReference storageReference;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    String mineFriendList;
    QuizDbHelper quizDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            this.getSupportActionBar().hide();
        } catch (NullPointerException e){}
        setContentView(R.layout.screen_profile);

        initFirebase();
        initUI();
        initProfile();
        loadFriends();
    }

    private void initUI() {
        user_img = findViewById(R.id.user_img);
        back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(this);
        username_txt = findViewById(R.id.username_txt);
        useremail_txt = findViewById(R.id.useremail_txt);
        social_spinner = findViewById(R.id.social_spinner);
        msg_txt = findViewById(R.id.msg_txt);
        friend_list_view = findViewById(R.id.friend_list_view);
        linear_friend_list = findViewById(R.id.linear_friend_list);
        view_friend_btn = findViewById(R.id.view_friend_btn);
        view_friend_btn.setOnClickListener(this);
        add_friend_btn = findViewById(R.id.add_friend_btn);
        add_friend_btn.setOnClickListener(this);

        userList= new ArrayList<>();
        adapter = new MyRecyclerAdapter(userList, null);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        friend_list_view.setLayoutManager(layoutManager);
        friend_list_view.setAdapter(adapter);
    }

    private void initProfile() {
        showProgressDialog();
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                hideProgressDialog();
                if (task.isSuccessful()) {
                    String userName = task.getResult().getString("username");
                    String userImage = task.getResult().getString("photo");
                    String userEmail = task.getResult().getString("email");
                    String userSocial = task.getResult().getString("socialtype");//[0,0,1,1,1,0]

                    username_txt.setText(userName);
                    useremail_txt.setText(userEmail);
                    initSocialSpinner(userSocial);
                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.avatar_user);
                    Glide.with(ProfileActivity.this).setDefaultRequestOptions(placeholderRequest).load(userImage).into(user_img);
                } else {
                    String errorMessage = task.getException().getMessage();
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public  void loadFriends() {
        userList.clear();
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        mineFriendList = task.getResult().getString("friendList");
                        final String socialTies = task.getResult().getString("socialTies");

                        if (mineFriendList.equals("")) return;

                        final String[] f_arr = mineFriendList.split(",");
                        String[] t_arr = socialTies.split(",");
                        for(int i=0; i< f_arr.length; i++) {
                            final String userId = f_arr[i].trim();
                            final String ties = t_arr[i].trim();
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

                                            User user = new User(userId, image, name, socialtype, email, password, userId.trim(), ties.trim());
                                            userList.add(user);

                                            adapter = new MyRecyclerAdapter(userList, null);
                                            friend_list_view.setAdapter(adapter);

                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
//                        Query usersQuery = db.collection("Users");
//                        usersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
//                            @Override
//                            public void onEvent(QuerySnapshot queryDocumentSnapshots, final FirebaseFirestoreException e) {
//                                if (queryDocumentSnapshots != null) {
//                                    if (!queryDocumentSnapshots.isEmpty()) {
//                                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()) {
//                                            if (doc.getType() == DocumentChange.Type.ADDED) {
//                                                final String userId = doc.getDocument().getId();
//                                                if(userId.equals(mAuth.getCurrentUser().getUid())) continue;
//
//                                                db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                                        if (task.isSuccessful()) {
//                                                            if (task.getResult().exists()) {
//                                                                String name = task.getResult().getString("username");
//                                                                String image = task.getResult().getString("photo");
//                                                                String email = task.getResult().getString("email");
//                                                                String socialtype = task.getResult().getString("socialtype");
//                                                                String password = task.getResult().getString("password");
//                                                                String friendList = task.getResult().getString("friendList");
//                                                                String socialTies = task.getResult().getString("socialTies");
//Log.e("===========ties", socialTies);
//                                                                if(mineFriendList.contains(userId)) {
//                                                                    User user = new User(userId, image, name, socialtype, email, password, friendList, socialTies);
//                                                                    userList.add(user);
//                                                                }
//
//                                                                adapter= new MyRecyclerAdapter(userList, null);
//                                                                friend_list_view.setAdapter(adapter);
//
//                                                                adapter.notifyDataSetChanged();
//                                                            }
//                                                        }
//                                                    }
//                                                });
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        });
                    }
                }
            }
        });
    }

    private void initFirebase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.add_friend_btn:
                addFriends();
                break;
            case R.id.view_friend_btn:
                viewFriends();
                break;
            case R.id.back_img:
                finish();
                break;
        }

    }

    private void addFriends() {
        Intent i = new Intent(ProfileActivity.this, AddFriendActivity.class);
        startActivity(i);
        finish();
    }

    boolean isShowFriends = false;
    private void viewFriends() {
        if(isShowFriends) {
            linear_friend_list.setVisibility(View.GONE);
            view_friend_btn.setText("Show Friends");
            isShowFriends = false;
        } else {
            linear_friend_list.setVisibility(View.VISIBLE);
            view_friend_btn.setText("Hide Friends");
            isShowFriends = true;
        }
    }

    private void initSocialSpinner(String userSocial) {
        final String[] select_qualification = {
                "Support Type", "Instrumental support", "Informational support", "Emotional support", "Appraisal support", "Other"
        };
        userSocial = userSocial.substring(1, userSocial.length() - 1);
        String[] arr = userSocial.split(",");

        social_list = new ArrayList<>();
        for (int i = 0; i < select_qualification.length; i++) {
            SocialItem item = new SocialItem();
            item.setTitle(select_qualification[i]);
            if (arr[i].trim().equals("1")) {
                item.setSelected(true);
            } else {
                item.setSelected(false);
            }
            social_list.add(item);
        }

        SocialItemAdapter myAdapter = new SocialItemAdapter(ProfileActivity.this, 0, social_list, true);
        social_spinner.setAdapter(myAdapter);
    }
}

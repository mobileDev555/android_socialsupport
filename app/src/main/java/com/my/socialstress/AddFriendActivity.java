package com.my.socialstress;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.my.socialstress.utils.MyFriendRecyclerAdapter;
import com.my.socialstress.utils.MyRecyclerAdapter;
import com.my.socialstress.utils.QuizDbHelper;
import com.my.socialstress.utils.SocialItem;
import com.my.socialstress.utils.SocialItemAdapter;
import com.my.socialstress.utils.User;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends BaseActivity implements View.OnClickListener {

    private View mainView;
    private RecyclerView friend_list_view;
    private ImageView back_img;

    private static MyFriendRecyclerAdapter adapter;
    List<User> userList;
    ArrayList<SocialItem> social_list;
    String mineFriendList;

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
        setContentView(R.layout.screen_add_friend);

        initFirebase();
        initUI();
        loadUsers();
    }

    private void initUI() {
        back_img = findViewById(R.id.back_img);
        back_img.setOnClickListener(this);
        friend_list_view = findViewById(R.id.friend_list_view);

        userList= new ArrayList<>();
        adapter = new MyFriendRecyclerAdapter(userList, AddFriendActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        friend_list_view.setLayoutManager(layoutManager);
        friend_list_view.setAdapter(adapter);
    }

    public void loadUsers() {
        userList.clear();
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        mineFriendList = task.getResult().getString("friendList");

                        Query usersQuery = db.collection("Users");
                        usersQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot queryDocumentSnapshots, final FirebaseFirestoreException e) {
                                if (queryDocumentSnapshots != null) {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()) {
                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                final String userId = doc.getDocument().getId();
                                                if(userId.equals(mAuth.getCurrentUser().getUid())) continue;

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
                                                                String friendList = task.getResult().getString("friendList");
                                                                String socialTies = task.getResult().getString("socialTies");

                                                                if (mineFriendList.equals("") || !mineFriendList.contains(userId)) {
                                                                    User user = new User(userId, image, name, socialtype, email, password, friendList, socialTies);
                                                                    userList.add(user);
                                                                }

                                                                adapter= new MyFriendRecyclerAdapter(userList, AddFriendActivity.this);
                                                                friend_list_view.setAdapter(adapter);

                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            }
                        });
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
            case R.id.back_img:
                startActivity(new Intent(AddFriendActivity.this, ProfileActivity.class));
                finish();
                break;
        }

    }

}


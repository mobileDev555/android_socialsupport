package com.my.socialstress.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.my.socialstress.ProfileActivity;
import com.my.socialstress.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MyRecyclerAdapter  extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    public List<User> userList;
    public List<User> userListFiltered;

    public Context context;
    public MyRecyclerAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public User profileItem;
    private boolean isClicked = false;

    public MyRecyclerAdapter(List<User> userList, List<User> userList1) {
        this.userList = userList;
        this.userListFiltered = userList;
        this.adapter = this;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final String currentUserId = mAuth.getCurrentUser().getUid();

        User userInfo = new User();
        final String userId = userList.get(position).getUser_id();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();
        String userImage = userList.get(position).getImage();
        String userSocial = userList.get(position).getSocial();
        String userTies = userList.get(position).getTies();
        String friendList = userList.get(position).getFriendList();
        Log.e("=========", userTies);
        String social_str = "";
        if(userSocial == null) social_str = "";
        else {
            userSocial = userSocial.substring(1, userSocial.length() - 1);
            String[] arr = userSocial.split(",");
            String[] select_qualification = {
                    "Social support Type", "Instrumental support", "Informational support", "Emotional support", "Appraisal support", "Other"
            };
            for (int i = 0; i < arr.length; i++) {
                if (arr[i].trim().equals("1")) {
                    if(social_str.equals("")) social_str = select_qualification[i];
                    else social_str = social_str + "," + select_qualification[i];
                }
            }
        }


        holder.username_txt.setText(userName);
        holder.usersocial_txt.setText(social_str);

        if(userTies.equals("Weak")) holder.weak_radio.setChecked(true);
        else if(userTies.equals("Strong")) holder.strong_radio.setChecked(true);

        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.avatar_user);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(userImage).into(holder.user_img);

        holder.remove_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userList.size()!=0){
                    userList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,userList.size());
                    addAndRemoveFriendToFirebase(userId);
                }
            }
        });
    }

    private void addAndRemoveFriendToFirebase(final String userId) {
        final String currentUserId = mAuth.getCurrentUser().getUid();
        db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String friendList = task.getResult().getString("friendList");
                        String socialTies = task.getResult().getString("socialTies");

                            Map<String, Object> userMap = new HashMap<>();
                            if(friendList.contains(userId)) {
                                String f = "";
                                String s = "";
                                String[] f_arr = friendList.split(",");
                                String[] s_arr = socialTies.split(",");
                                for(int i=0; i<f_arr.length; i++) {
                                    if(f_arr[i].trim().equals(userId)) continue;
                                    if(f.equals("")) {
                                        f = f_arr[i];
                                        s = s_arr[i];
                                    } else {
                                        f = f +", "+f_arr[i];
                                        s = s +", "+s_arr[i];
                                    }
                                }
                                userMap.put("friendList", f);
                                userMap.put("socialTies", s);
                            }
                            db.collection("Users").document(currentUserId).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Removed", Toast.LENGTH_LONG).show();
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(context, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView username_txt, usersocial_txt;
        private ImageView user_img;
        private Button remove_btn;
        private RadioButton weak_radio, strong_radio;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            user_img = mView.findViewById(R.id.user_img);
            username_txt = mView.findViewById(R.id.username_txt);
            usersocial_txt = mView.findViewById(R.id.usersocial_txt);
            weak_radio = mView.findViewById(R.id.radio_weak);
            strong_radio = mView.findViewById(R.id.radio_strong);
            remove_btn = mView.findViewById(R.id.remove_btn);
        }
    }
}

package com.my.socialstress.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.my.socialstress.AddFriendActivity;
import com.my.socialstress.R;
import com.my.socialstress.RegisterActivity;
import com.my.socialstress.TestActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyFriendRecyclerAdapter extends RecyclerView.Adapter<MyFriendRecyclerAdapter.ViewHolder> {

    public List<User> userList;
    public List<User> userListFiltered;

    public Context context;
    public MyFriendRecyclerAdapter adapter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public User profileItem;
    private boolean isClicked = false;
    String currentUserId;

    public MyFriendRecyclerAdapter(List<User> userList, List<User> userList1) {
        this.userList = userList;
        this.userListFiltered = userList;
        this.adapter = this;
    }

    public MyFriendRecyclerAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.userListFiltered = userList;
        this.adapter = this;
        this.context = context;
    }

    @NonNull
    @Override
    public MyFriendRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_add_item, parent, false);
        context = parent.getContext();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        return new MyFriendRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyFriendRecyclerAdapter.ViewHolder holder, final int position) {

        final String currentUserId = mAuth.getCurrentUser().getUid();

        User userInfo = new User();
        final String userId = userList.get(position).getUser_id();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userSocial = userList.get(position).getSocial();

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
                    if (social_str.equals("")) social_str = select_qualification[i];
                    else social_str = social_str + ", " + select_qualification[i];
                }
            }
        }

        holder.username_txt.setText(userName);
        holder.usersocial_txt.setText(social_str);
        RequestOptions placeholderRequest = new RequestOptions();
        placeholderRequest.placeholder(R.drawable.avatar_user);
        Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(userImage).into(holder.user_img);

        holder.add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = holder.radioGroup.getCheckedRadioButtonId();
                String select_radio = "";
                if(holder.radio_weak.isChecked()) select_radio = (String) holder.radio_weak.getText();
                if(holder.radio_strong.isChecked()) select_radio = (String) holder.radio_strong.getText();

                addAndRemoveFriendToFirebase(userId, select_radio, holder.add_btn);
            }
        });
    }

    private void addAndRemoveFriendToFirebase(final String userId, final String radio_str, final Button add_btn) {
        db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String friendList = task.getResult().getString("friendList");
                        String socialTies = task.getResult().getString("socialTies");

                        if(add_btn.getText().toString().equals("Add")) {
                            Map<String, Object> userMap = new HashMap<>();
                            if(friendList.equals("")) {
                                userMap.put("friendList", userId);
                                userMap.put("socialTies", radio_str);
                            } else if(!friendList.contains(userId)) {
                                String f = friendList +", "+userId;
                                String s = socialTies +", "+radio_str;
                                userMap.put("friendList", f);
                                userMap.put("socialTies", s);
                            }
                            db.collection("Users").document(currentUserId).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "Added", Toast.LENGTH_LONG).show();
                                        add_btn.setText("Remove");
                                        add_btn.setBackgroundResource(R.drawable.default_round_yellow);
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(context, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Map<String, Object> userMap = new HashMap<>();
                            if(friendList.contains(userId)) {
                                String f = "";
                                String s = "";
                                String[] f_arr = friendList.split(",");
                                String[] s_arr = socialTies.split(",");
                                for(int i=0; i<f_arr.length; i++) {
                                    if(f_arr[i].equals(userId)) continue;
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
                                        add_btn.setText("Add");
                                        add_btn.setBackgroundResource(R.drawable.default_round);
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(context, "Firestore Error: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
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
        private RadioGroup radioGroup;
        private RadioButton radio_weak, radio_strong;
        private Button add_btn;
        private ImageView user_img;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            user_img = mView.findViewById(R.id.user_img);
            username_txt = mView.findViewById(R.id.username_txt);
            usersocial_txt = mView.findViewById(R.id.usersocial_txt);
            radioGroup = mView.findViewById(R.id.radio_group);
            radio_weak = mView.findViewById(R.id.radio_weak);
            radio_strong = mView.findViewById(R.id.radio_strong);
            add_btn = mView.findViewById(R.id.add_btn);
        }
    }
}


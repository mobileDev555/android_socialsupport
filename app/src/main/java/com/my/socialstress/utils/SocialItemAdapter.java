package com.my.socialstress.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.my.socialstress.ProfileActivity;
import com.my.socialstress.R;
import com.my.socialstress.RegisterActivity;

import java.util.ArrayList;
import java.util.List;

import static com.my.socialstress.utils.Global.editor;

public class SocialItemAdapter extends ArrayAdapter<SocialItem> {
    private Context mContext;
    private ArrayList<SocialItem> listState;
    private SocialItemAdapter myAdapter;
    private boolean isFromView = false;
    private boolean isDisable = false;

    public SocialItemAdapter(Context context, int i, ArrayList<SocialItem> social_list) {
        super(context, i, social_list);
        this.mContext = context;
        this.listState = social_list;
        this.myAdapter = this;
    }

    public SocialItemAdapter(Context context, int i, ArrayList<SocialItem> social_list, boolean b) {
        super(context, i, social_list);
        this.mContext = context;
        this.listState = social_list;
        this.myAdapter = this;
        this.isDisable = b;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
    @Override
    public int getCount() {
        return listState.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.social_spinner_item, null);
        TextView textView = (TextView) v.findViewById(R.id.social_txt);
        CheckBox checkBox = (CheckBox) v.findViewById(R.id.social_chk);
        textView.setText(listState.get(position).getTitle());
        isFromView = true;
        checkBox.setChecked(listState.get(position).isSelected());
        isFromView = false;

        if(isDisable) checkBox.setEnabled(false);

        if ((position == 0)) {
            checkBox.setVisibility(View.INVISIBLE);
        } else {
            checkBox.setVisibility(View.VISIBLE);
        }
        checkBox.setTag(position);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int getPosition = (Integer) buttonView.getTag();
                Log.e("=========", ""+getPosition);
                    listState.get(getPosition).setSelected(isChecked);
            }
        });
        return v;

    }

//    private class ViewHolder {
//        private TextView mTextView;
//        private CheckBox mCheckBox;
//        ViewHolder() {
//            mTextView =
//        }
//    }
}

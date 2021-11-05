package com.team11.ditto.follow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.team11.ditto.R;
import com.team11.ditto.profile_details.User;

import java.util.ArrayList;

public class FollowRequestList extends ArrayAdapter {
    private ArrayList<User> users;
    private Context context;

    public FollowRequestList(Context context, ArrayList<User> users) {
        super(context,0,users);
        this.users = users;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        //return super.getView(position, convertView, parent);

        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.follow_request_content, parent,false);
        }

        User user = users.get(position);

        TextView username = view.findViewById(R.id.fr_user_name);
        ImageView userphoto = view.findViewById(R.id.fr_user_photo);

        username.setText(user.getUsername());
        userphoto.setImageResource(R.drawable.bwayne);

        return view;
    }

    public void add(User user){
        users.add(user);
    }

}

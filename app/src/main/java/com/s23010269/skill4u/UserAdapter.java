package com.s23010269.skill4u;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import java.util.*;

public class UserAdapter extends ArrayAdapter<UserModel> {

    private final Context context;
    private final List<UserModel> users;
    private final String currentUserId;
    private final String mode; // "friends", "add", "requests"

    public UserAdapter(Context context, List<UserModel> users, String currentUserId, String mode) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.currentUserId = currentUserId;
        this.mode = mode;
    }

    @NonNull
    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.user_card, parent, false);

        ImageView profileIcon = convertView.findViewById(R.id.profile_icon);
        TextView nameText = convertView.findViewById(R.id.user_name);
        Button actionBtn = convertView.findViewById(R.id.action_button);
        ImageView menuDots = convertView.findViewById(R.id.menu_dots);

        UserModel user = users.get(pos);
        nameText.setText(user.getName());

        // Static profile image
        profileIcon.setImageResource(R.drawable.profile);

        // Profile click
        if (mode.equals("friends")) {
            profileIcon.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("uid", user.getUid());
                context.startActivity(intent);
            });
        }

        // Action buttons
        if (mode.equals("add")) {
            actionBtn.setVisibility(View.VISIBLE);
            actionBtn.setText("Request");
            actionBtn.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("FriendRequests")
                        .child(user.getUid()).child(currentUserId).setValue("pending");
                Toast.makeText(context, "Request Sent", Toast.LENGTH_SHORT).show();
            });
            menuDots.setVisibility(View.GONE);
        } else if (mode.equals("requests")) {
            actionBtn.setVisibility(View.GONE);
            LinearLayout buttonRow = convertView.findViewById(R.id.request_button_row);
            buttonRow.setVisibility(View.VISIBLE);

            Button acceptBtn = convertView.findViewById(R.id.accept_button);
            Button declineBtn = convertView.findViewById(R.id.decline_button);

            acceptBtn.setOnClickListener(v -> {
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child("Friends").child(currentUserId).child(user.getUid()).setValue(true);
                db.child("Friends").child(user.getUid()).child(currentUserId).setValue(true);
                db.child("FriendRequests").child(currentUserId).child(user.getUid()).removeValue();
                Toast.makeText(context, "Friend Added", Toast.LENGTH_SHORT).show();
            });

            declineBtn.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("FriendRequests")
                        .child(currentUserId).child(user.getUid()).removeValue();
                Toast.makeText(context, "Request Declined", Toast.LENGTH_SHORT).show();
            });

            menuDots.setVisibility(View.GONE);
        } else {
            actionBtn.setVisibility(View.GONE);
            menuDots.setVisibility(View.VISIBLE);
            menuDots.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Remove Friend")
                        .setMessage("Do you want to remove this friend?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                            db.child("Friends").child(currentUserId).child(user.getUid()).removeValue();
                            db.child("Friends").child(user.getUid()).child(currentUserId).removeValue();
                            Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                        }).setNegativeButton("No", null).show();
            });
        }

        return convertView;
    }
}

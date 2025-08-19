package com.s23010269.skill4u;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<Post> posts;
    private String currentUsername;
    private OnPostOptionsClickListener listener;

    public interface OnPostOptionsClickListener {
        void onEditClicked(Post post);
        void onDeleteClicked(Post post);
    }

    public void setOnPostOptionsClickListener(OnPostOptionsClickListener listener) {
        this.listener = listener;
    }

    public PostAdapter(ArrayList<Post> posts, String currentUsername) {
        this.posts = posts;
        this.currentUsername = currentUsername;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);

        holder.txtUser.setText(post.getUsername());
        holder.txtText.setText(post.getText());

        String time = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault())
                .format(new Date(post.getTimestamp()));
        holder.txtTime.setText(time);

        // Show options only for current user's posts
        if (post.getUsername().equals(currentUsername)) {
            holder.imgOptions.setVisibility(View.VISIBLE);
        } else {
            holder.imgOptions.setVisibility(View.GONE);
        }

        // Three-dot menu click
        holder.imgOptions.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.post_options_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (listener != null) {
                    if (item.getItemId() == R.id.menu_edit) {
                        listener.onEditClicked(post);
                        return true;
                    } else if (item.getItemId() == R.id.menu_delete) {
                        listener.onDeleteClicked(post);
                        return true;
                    }
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView txtUser, txtText, txtTime;
        ImageView imgOptions;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUser = itemView.findViewById(R.id.txt_post_user);
            txtText = itemView.findViewById(R.id.txt_post_text);
            txtTime = itemView.findViewById(R.id.txt_post_time);
            imgOptions = itemView.findViewById(R.id.img_post_options);
        }
    }
}

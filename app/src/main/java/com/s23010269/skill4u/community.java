package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;

public class community extends AppCompatActivity {

    // UI components
    private ImageView addPostBtn, openmenu;
    private CardView addPostCard;
    private EditText inputPost;
    private Button btnCancel, btnSubmit;
    private RecyclerView recyclerPosts;
    private PostAdapter postAdapter;
    private RadioGroup segmentGroup;

    // Data lists
    private ArrayList<Post> allPosts = new ArrayList<>();      // all posts from database
    private ArrayList<Post> filteredPosts = new ArrayList<>(); // posts filtered for current view
    private DatabaseReference postsRef;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        addPostBtn = findViewById(R.id.add_a_post);
        openmenu = findViewById(R.id.openmenu);
        addPostCard = findViewById(R.id.add_post_card);
        inputPost = findViewById(R.id.input_post);
        btnCancel = findViewById(R.id.btn_cancel_post);
        btnSubmit = findViewById(R.id.btn_submit_post);
        recyclerPosts = findViewById(R.id.recycler_posts);
        segmentGroup = findViewById(R.id.segmentGroup);

        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        // Get username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", "Unknown User");


        postAdapter = new PostAdapter(filteredPosts, currentUsername);
        recyclerPosts.setAdapter(postAdapter);

        postsRef = FirebaseDatabase.getInstance().getReference("Posts");

        // Handle post (edit/delete)
        postAdapter.setOnPostOptionsClickListener(new PostAdapter.OnPostOptionsClickListener() {
            @Override
            public void onEditClicked(Post post) {
                showEditDialog(post);  // open dialog to edit post
            }

            @Override
            public void onDeleteClicked(Post post) {
                confirmDelete(post);   // confirm before deleting post
            }
        });

        // Show "add post" card when "+ image" is clicked
        addPostBtn.setOnClickListener(v -> addPostCard.setVisibility(View.VISIBLE));

        // Cancel new post creation
        btnCancel.setOnClickListener(v -> {
            inputPost.setText("");   // clear input
            hideKeyboard();          // hide keyboard
            addPostCard.setVisibility(View.GONE); // hide card
        });

        // Submit new post to Firebase
        btnSubmit.setOnClickListener(v -> submitPost());

        // Open menu activity
        openmenu.setOnClickListener(v -> startActivity(new Intent(community.this, menu.class)));

        // Handle segmented toggle changes
        segmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.mypost) {
                showMyPosts();         // show only user's posts
            } else if (checkedId == R.id.publicpost) {
                showCommunityFeed();   // show all posts
            }
        });

        // Load posts from Firebase database
        loadPosts();
    }

    // Submit post to Firebase
    private void submitPost() {
        String text = inputPost.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Write something first!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false); // prevent multiple submissions

        String postId = postsRef.push().getKey();  // generate unique ID
        long timestamp = System.currentTimeMillis();

        Post post = new Post(postId, currentUsername, text, timestamp);

        // Add post to Firebase
        postsRef.child(postId).setValue(post)
                .addOnCompleteListener(task -> {
                    btnSubmit.setEnabled(true);
                    if (task.isSuccessful()) {
                        inputPost.setText(""); // clear input
                        hideKeyboard();
                        addPostCard.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, "Failed to post: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Show dialog to edit a post
    private void showEditDialog(Post post) {
        EditText editText = new EditText(this);
        editText.setText(post.getText());

        new AlertDialog.Builder(this)
                .setTitle("Edit Post")
                .setView(editText)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = editText.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        postsRef.child(post.getPostId()).child("text").setValue(newText); // update Firebase
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Show confirmation dialog before deleting a post
    private void confirmDelete(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    postsRef.child(post.getPostId()).removeValue(); // remove from Firebase
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Load posts from Firebase and update lists
    private void loadPosts() {
        postsRef.orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Post post = snapshot.getValue(Post.class);
                if (post != null) {
                    allPosts.add(0, post); // newest posts first

                    int checkedId = segmentGroup.getCheckedRadioButtonId();
                    // Add to filtered list depending on toggle selection
                    if ((checkedId == R.id.mypost && post.getUsername().equals(currentUsername))
                            || checkedId == R.id.publicpost) {
                        filteredPosts.add(0, post);
                        postAdapter.notifyItemInserted(0);
                        recyclerPosts.scrollToPosition(0);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                Post updatedPost = snapshot.getValue(Post.class);
                if (updatedPost != null) {
                    // Update post in allPosts
                    for (int i = 0; i < allPosts.size(); i++) {
                        if (allPosts.get(i).getPostId().equals(updatedPost.getPostId())) {
                            allPosts.set(i, updatedPost);
                            break;
                        }
                    }
                    // Update post in filteredPosts
                    for (int i = 0; i < filteredPosts.size(); i++) {
                        if (filteredPosts.get(i).getPostId().equals(updatedPost.getPostId())) {
                            filteredPosts.set(i, updatedPost);
                            postAdapter.notifyItemChanged(i);
                            return;
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Post removedPost = snapshot.getValue(Post.class);
                if (removedPost != null) {
                    // Remove from allPosts
                    for (int i = 0; i < allPosts.size(); i++) {
                        if (allPosts.get(i).getPostId().equals(removedPost.getPostId())) {
                            allPosts.remove(i);
                            break;
                        }
                    }
                    // Remove from filteredPosts
                    for (int i = 0; i < filteredPosts.size(); i++) {
                        if (filteredPosts.get(i).getPostId().equals(removedPost.getPostId())) {
                            filteredPosts.remove(i);
                            postAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(community.this, "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter and show only user's posts
    private void showMyPosts() {
        filteredPosts.clear();
        for (Post p : allPosts) {
            if (p.getUsername().equals(currentUsername)) {
                filteredPosts.add(p);
            }
        }
        postAdapter.notifyDataSetChanged();
        recyclerPosts.scrollToPosition(0);
    }

    // community feed
    private void showCommunityFeed() {
        filteredPosts.clear();
        filteredPosts.addAll(allPosts);
        postAdapter.notifyDataSetChanged();
        recyclerPosts.scrollToPosition(0);
    }

    // Hide keyboard utility
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(inputPost.getWindowToken(), 0);
        }
    }
}

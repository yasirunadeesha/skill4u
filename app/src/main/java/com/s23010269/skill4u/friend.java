package com.s23010269.skill4u;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class friend extends AppCompatActivity {

    // UI Components
    private RadioGroup friendsSegmentGroup;
    private RadioButton rbMyFriends, rbAddFriends, rbRequests;
    private TextView usernameDisplay;
    private LinearLayout myFriendsContainer, addFriendsContainer, requestContainer; // Containers for each tab
    private ListView myFriendsList, addFriendsList, requestList; // Lists for displaying friends, addable friends, and requests
    private ImageView openmenu;
    private DatabaseReference usersRef, requestsRef, friendsRef;
    private String currentUsername;
    private ArrayAdapter<String> myFriendsAdapter, addFriendsAdapter, requestsAdapter;
    private ArrayList<String> myFriends = new ArrayList<>();
    private ArrayList<String> addFriends = new ArrayList<>();
    private ArrayList<String> friendRequests = new ArrayList<>();


    private Map<String, String> userIdMap = new HashMap<>(); // name -> uid

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        openmenu = findViewById(R.id.openmenu);
        usernameDisplay = findViewById(R.id.username_display);

        friendsSegmentGroup = findViewById(R.id.friendsSegmentGroup);
        rbMyFriends = findViewById(R.id.rb_my_friends);
        rbAddFriends = findViewById(R.id.rb_add_friends);
        rbRequests = findViewById(R.id.rb_friend_requests);

        myFriendsContainer = findViewById(R.id.my_friends_container);
        addFriendsContainer = findViewById(R.id.add_friends_container);
        requestContainer = findViewById(R.id.friend_request_container);

        myFriendsList = findViewById(R.id.friend_details);
        addFriendsList = findViewById(R.id.list_add_friends);
        requestList = findViewById(R.id.list_requests);

        // Get current username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", null);
        if (getIntent().hasExtra("USERNAME")) {
            currentUsername = getIntent().getStringExtra("USERNAME");
        }

        // If user is not logged in, redirect to sign-in
        if (currentUsername == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, signin.class));
            finish();
            return;
        }

        // Display username
        usernameDisplay.setText(currentUsername);

        // Initialize Firebase references
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");       // Stores all users
        requestsRef = rootRef.child("Requests"); // Stores friend requests
        friendsRef = rootRef.child("Friends");   // Stores friendships

        // Open menu on click
        openmenu.setOnClickListener(v -> startActivity(new Intent(friend.this, menu.class)));

        // Setup tabs and default view
        setupSegmentTabs();

        // Load all users from Firebase and initialize lists
        loadAllUsers();
    }

    // Setup tab buttons and switch visibility of containers
    private void setupSegmentTabs() {
        // Default: show My Friends tab
        showSection(myFriendsContainer);
        rbMyFriends.setChecked(true);

        // Listen for tab changes
        friendsSegmentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_my_friends) {
                showSection(myFriendsContainer);
            } else if (checkedId == R.id.rb_add_friends) {
                showSection(addFriendsContainer);
            } else if (checkedId == R.id.rb_friend_requests) {
                showSection(requestContainer);
            }
        });
    }

    // Show only the selected section, hide the others
    private void showSection(LinearLayout visible) {
        myFriendsContainer.setVisibility(View.GONE);
        addFriendsContainer.setVisibility(View.GONE);
        requestContainer.setVisibility(View.GONE);
        visible.setVisibility(View.VISIBLE);
    }

    // Load all users from Firebase (except current user)
    private void loadAllUsers() {
        userIdMap.clear();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    String name = userSnapshot.child("name").getValue(String.class);
                    // Exclude current user
                    if (username != null && name != null && !username.equals(currentUsername)) {
                        userIdMap.put(name, username);
                    }
                }

                // After loading users, populate the three lists
                loadMyFriends();
                loadRequests();
                loadAddableFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Load current user's friends
    private void loadMyFriends() {
        myFriends.clear();
        friendsRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendUsername = friendSnapshot.getKey();
                    // Retrieve friend's display name
                    usersRef.child(friendUsername).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String name = snap.getValue(String.class);
                                    if (name != null) {
                                        myFriends.add(name);
                                        // Update adapter each time a friend is loaded
                                        myFriendsAdapter = new ArrayAdapter<>(friend.this, R.layout.item_friend, R.id.text_friend_name, myFriends);
                                        myFriendsList.setAdapter(myFriendsAdapter);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Load users that can be added as friends
    private void loadAddableFriends() {
        addFriends.clear();
        for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
            String name = entry.getKey();
            String uid = entry.getValue();

            // Check if user is already a friend
            friendsRef.child(currentUsername).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                    if (!snapshot1.exists()) { // Not a friend
                        // Check if friend request has already been sent
                        requestsRef.child(uid).child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                if (!snapshot2.exists()) { // No request exists
                                    addFriends.add(name);
                                    addFriendsAdapter = new ArrayAdapter<>(friend.this, R.layout.item_friend, R.id.text_friend_name, addFriends);
                                    addFriendsList.setAdapter(addFriendsAdapter);

                                    // Handle click to send friend request
                                    addFriendsList.setOnItemClickListener((adapterView, view, i, l) -> {
                                        String selectedName = addFriends.get(i);
                                        String selectedUid = userIdMap.get(selectedName);
                                        requestsRef.child(selectedUid).child(currentUsername).setValue("request");
                                        Toast.makeText(friend.this, "Request sent to " + selectedName, Toast.LENGTH_SHORT).show();
                                        addFriends.remove(i);
                                        addFriendsAdapter.notifyDataSetChanged();
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    // Load incoming friend requests
    private void loadRequests() {
        friendRequests.clear();
        requestsRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reqSnap : snapshot.getChildren()) {
                    String uid = reqSnap.getKey();
                    // Get the name of the requester
                    usersRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            String name = snap.getValue(String.class);
                            if (name != null) {
                                friendRequests.add(name);
                                requestsAdapter = new ArrayAdapter<>(friend.this, R.layout.item_friend, R.id.text_friend_name, friendRequests);
                                requestList.setAdapter(requestsAdapter);

                                // Accept request on click
                                requestList.setOnItemClickListener((adapterView, view, i, l) -> {
                                    String requesterName = friendRequests.get(i);
                                    String requesterUid = userIdMap.get(requesterName);

                                    // Add to friends in both directions
                                    friendsRef.child(currentUsername).child(requesterUid).setValue(true);
                                    friendsRef.child(requesterUid).child(currentUsername).setValue(true);
                                    requestsRef.child(currentUsername).child(requesterUid).removeValue();

                                    Toast.makeText(friend.this, "Friend request from " + requesterName + " accepted", Toast.LENGTH_SHORT).show();
                                    friendRequests.remove(i);
                                    requestsAdapter.notifyDataSetChanged();
                                });

                                // Decline request on long click
                                requestList.setOnItemLongClickListener((adapterView, view, i, l) -> {
                                    String requesterName = friendRequests.get(i);
                                    String requesterUid = userIdMap.get(requesterName);
                                    requestsRef.child(currentUsername).child(requesterUid).removeValue();
                                    Toast.makeText(friend.this, "Request from " + requesterName + " declined", Toast.LENGTH_SHORT).show();
                                    friendRequests.remove(i);
                                    requestsAdapter.notifyDataSetChanged();
                                    return true;
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

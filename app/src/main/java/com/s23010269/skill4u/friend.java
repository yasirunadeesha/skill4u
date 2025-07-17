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

    private TextView myFriendsTab, addFriendsTab, requestsTab, usernameDisplay;
    private LinearLayout myFriendsContainer, addFriendsContainer, requestContainer;
    private ListView myFriendsList, addFriendsList, requestList;
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
        myFriendsTab = findViewById(R.id.your_friends);
        addFriendsTab = findViewById(R.id.add_friends);
        requestsTab = findViewById(R.id.friend_request);
        usernameDisplay = findViewById(R.id.username_display);

        myFriendsContainer = findViewById(R.id.my_friends_container);
        addFriendsContainer = findViewById(R.id.add_friends_container);
        requestContainer = findViewById(R.id.friend_request_container);

        myFriendsList = findViewById(R.id.friend_details);
        addFriendsList = findViewById(R.id.list_add_friends);
        requestList = findViewById(R.id.list_requests);

        // Load username
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("USERNAME", null);
        if (getIntent().hasExtra("USERNAME")) {
            currentUsername = getIntent().getStringExtra("USERNAME");
        }

        if (currentUsername == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, signin.class));
            finish();
            return;
        }

        usernameDisplay.setText(currentUsername);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");
        requestsRef = rootRef.child("Requests");
        friendsRef = rootRef.child("Friends");

        openmenu.setOnClickListener(v -> startActivity(new Intent(friend.this, menu.class)));

        setupTabs();
        loadAllUsers();
    }

    private void setupTabs() {
        myFriendsTab.setOnClickListener(v -> showSection(myFriendsContainer));
        addFriendsTab.setOnClickListener(v -> showSection(addFriendsContainer));
        requestsTab.setOnClickListener(v -> showSection(requestContainer));
    }

    private void showSection(LinearLayout visible) {
        myFriendsContainer.setVisibility(View.GONE);
        addFriendsContainer.setVisibility(View.GONE);
        requestContainer.setVisibility(View.GONE);
        visible.setVisibility(View.VISIBLE);
    }

    private void loadAllUsers() {
        userIdMap.clear();
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String username = userSnapshot.getKey();
                    String name = userSnapshot.child("name").getValue(String.class);
                    if (username != null && name != null && !username.equals(currentUsername)) {
                        userIdMap.put(name, username);
                    }
                }

                loadMyFriends();
                loadRequests();
                loadAddableFriends();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadMyFriends() {
        myFriends.clear();
        friendsRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot friendSnapshot : snapshot.getChildren()) {
                    String friendUsername = friendSnapshot.getKey();
                    usersRef.child(friendUsername).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    String name = snap.getValue(String.class);
                                    if (name != null) {
                                        myFriends.add(name);
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

    private void loadAddableFriends() {
        addFriends.clear();
        for (Map.Entry<String, String> entry : userIdMap.entrySet()) {
            String name = entry.getKey();
            String uid = entry.getValue();

            friendsRef.child(currentUsername).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                    if (!snapshot1.exists()) {
                        requestsRef.child(uid).child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                if (!snapshot2.exists()) {
                                    addFriends.add(name);
                                    addFriendsAdapter = new ArrayAdapter<>(friend.this, R.layout.item_friend, R.id.text_friend_name, addFriends);
                                    addFriendsList.setAdapter(addFriendsAdapter);

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

    private void loadRequests() {
        friendRequests.clear();
        requestsRef.child(currentUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot reqSnap : snapshot.getChildren()) {
                    String uid = reqSnap.getKey();
                    usersRef.child(uid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            String name = snap.getValue(String.class);
                            if (name != null) {
                                friendRequests.add(name);
                                requestsAdapter = new ArrayAdapter<>(friend.this, R.layout.item_friend, R.id.text_friend_name, friendRequests);
                                requestList.setAdapter(requestsAdapter);

                                requestList.setOnItemClickListener((adapterView, view, i, l) -> {
                                    String requesterName = friendRequests.get(i);
                                    String requesterUid = userIdMap.get(requesterName);

                                    friendsRef.child(currentUsername).child(requesterUid).setValue(true);
                                    friendsRef.child(requesterUid).child(currentUsername).setValue(true);
                                    requestsRef.child(currentUsername).child(requesterUid).removeValue();

                                    Toast.makeText(friend.this, "Friend request from " + requesterName + " accepted", Toast.LENGTH_SHORT).show();
                                    friendRequests.remove(i);
                                    requestsAdapter.notifyDataSetChanged();
                                });

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

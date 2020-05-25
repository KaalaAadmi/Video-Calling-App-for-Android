package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private String recieverUserID = "", recieverUserImage = "", recieverUserName = "";
    private ImageView backgroundProfileView;
    private TextView nameProfile;
    private Button addFriend;
    private Button cancelFriend;

    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState ="new";
    private DatabaseReference friendRequestReference, contactsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts");

        recieverUserID = getIntent().getExtras().get("visit_user_id").toString();
        recieverUserImage = getIntent().getExtras().get("profile_image").toString();
        recieverUserName = getIntent().getExtras().get("profile_name").toString();

        backgroundProfileView = findViewById(R.id.background_profile_view);
        nameProfile = findViewById(R.id.name_profile);
        addFriend = findViewById(R.id.add_friend);
        cancelFriend = findViewById(R.id.decline_friend_request);

        Picasso.get().load(recieverUserImage).into(backgroundProfileView);
        nameProfile.setText(recieverUserName);

        manageClickEvent();
    }

    private void manageClickEvent() {

        friendRequestReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(recieverUserID))
                {
                    String requestType = dataSnapshot.child(recieverUserID).child("request_type").getValue().toString();
                    if(requestType.equals("sent"))
                    {
                        currentState = "request_sent";
                        addFriend.setText("Withdraw");
                    }
                    else if(requestType.equals("received"))
                    {
                        currentState = "request_received";
                        addFriend.setText("Accept Friend Request");
                        cancelFriend.setVisibility(View.VISIBLE);
                        cancelFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest();
                            }
                        });
                    }
                    else
                    {
                        contactsReference.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(recieverUserID))
                                {
                                    currentState = "friends";
                                    addFriend.setText("Delete Contact");
                                }
                                else
                                {
                                    currentState = "new";
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(senderUserId.equals(recieverUserID))
        {
            addFriend.setVisibility(View.GONE);
        }
        else
        {
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentState.equals("new"))
                    {
                        sendFriendRequest();
                    }
                    if(currentState.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                    if(currentState.equals("request_received"))
                    {
                        acceptFriendRequest();
                    }
                    if(currentState.equals("request_sent"))
                    {
                        cancelFriendRequest();
                    }
                }
            });
        }
    }

    private void acceptFriendRequest() {
        contactsReference.child(senderUserId).child(recieverUserID).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    contactsReference.child(recieverUserID).child(senderUserId).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                friendRequestReference.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            friendRequestReference.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        currentState = "friends";
                                                        addFriend.setText("Delete Contact");
                                                        cancelFriend.setVisibility(View.GONE);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelFriendRequest() {
        friendRequestReference.child(senderUserId).child(recieverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    friendRequestReference.child(recieverUserID).child(senderUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                currentState = "new";
                                addFriend.setText("Add Friend");
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendFriendRequest() {
        friendRequestReference.child(senderUserId).child(recieverUserID).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    friendRequestReference.child(recieverUserID).child(senderUserId).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                currentState = "request_sent";
                                addFriend.setText("Withdraw");
                                Toast.makeText(ProfileActivity.this, "Friend Request Sent", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
                }
        });
    }
}

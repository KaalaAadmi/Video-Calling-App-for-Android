package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button saveBtn;
    private EditText usernameET;
    private EditText userBioET;
    private ImageView profileImageView;
    private static int galleryPick = 1;
    private Uri imageUri;
    private StorageReference userProfileImageReference;
    private String downloadUrl;
    private DatabaseReference userReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        saveBtn = findViewById(R.id.save_settings_button);
        usernameET = findViewById(R.id.username_settings);
        userBioET = findViewById(R.id.bio_settings);
        profileImageView = findViewById(R.id.settings_profile_pic);

        progressDialog = new ProgressDialog(this);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);

            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        retrieveUserInfo();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPick && resultCode == RESULT_OK && data != null)
        {
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void saveUserData() {
        final String getUsername = usernameET.getText().toString();
        final String getUserStatus = userBioET.getText().toString();

        if(imageUri == null)
        {
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("Image"))
                    {
                        saveInfoOnlyWithoutImage();
                    }
                    else
                    {
                        Toast.makeText(SettingsActivity.this, "Please Select Image First", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else if(getUsername.equals(""))
        {
            Toast.makeText(SettingsActivity.this, "Username is Mandatory", Toast.LENGTH_LONG).show();
        }
        else if(getUserStatus.equals(""))
        {
//            getUserStatus = "Available";
        }
        else
        {
            final StorageReference filePath = userProfileImageReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask = filePath.putFile(imageUri);
            uploadTask.continueWith(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    downloadUrl = filePath.getDownloadUrl().toString();
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Task<Uri>>() {
                @Override
                public void onComplete(@NonNull Task<Task<Uri>> task) {
                    if(task.isSuccessful())
                    {
                        downloadUrl = task.getResult().toString();
                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("Name", getUsername);
                        profileMap.put("Status", getUserStatus);
                        profileMap.put("Image", downloadUrl);

                        userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                                    startActivity(intent);
                                    finish();

                                    Toast.makeText(SettingsActivity.this, "Profile has been updated", Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    Toast.makeText(SettingsActivity.this, "Error: "+task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoOnlyWithoutImage() {
        final String getUsername = usernameET.getText().toString();
        final String getUserStatus = userBioET.getText().toString();

        if(getUsername.equals(""))
        {
            Toast.makeText(SettingsActivity.this, "Username is Mandatory", Toast.LENGTH_LONG).show();
        }
        else if(getUserStatus.equals(""))
        {
//            getUserStatus = "Available";
        }
        else
        {
            progressDialog.setTitle("Account Settings");
            progressDialog.setMessage("Please Wait");
            progressDialog.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("Name", getUsername);
            profileMap.put("Status", getUserStatus);

            userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Intent intent = new Intent(SettingsActivity.this, ContactsActivity.class);
                        startActivity(intent);
                        finish();
                        progressDialog.dismiss();

                        Toast.makeText(SettingsActivity.this, "Profile has been updated", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(SettingsActivity.this, "Error: "+task.getException(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void retrieveUserInfo()
    {
        userReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String imageDb = dataSnapshot.child("Image").getValue().toString();
                    String nameDb = dataSnapshot.child("Name").getValue().toString();
                    String bioDb = dataSnapshot.child("Status").getValue().toString();
                    usernameET.setText(nameDb);
                    userBioET.setText(bioDb);
                    Picasso.get().load(imageDb).placeholder(R.drawable.profile_image).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
//import android.se.omapi.Session;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_Key = "46761682";
    private static String SESSION_ID = "1_MX40Njc2MTY4Mn5-MTU5MDQzNzU3MDI3M342QXZXUWFJUWZMemdmblk5UG05WDJSKzh-fg";
    private static String TOKEN ="T1==cGFydG5lcl9pZD00Njc2MTY4MiZzaWc9NjMzMjJlNGJmYjE2NDkyZjU4N2U3MTYyOGQzNzk5ZTk5Yzg1NGVmNDpzZXNzaW9uX2lkPTFfTVg0ME5qYzJNVFk0TW41LU1UVTVNRFF6TnpVM01ESTNNMzQyUVhaWFVXRkpVV1pNZW1kbWJsazVVRzA1V0RKU0t6aC1mZyZjcmVhdGVfdGltZT0xNTkwNDM3NjE1Jm5vbmNlPTAuNzU2ODQ3MDU3OTEyNzA2OSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkzMDI5NjEzJmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVideoChatBtn;
    private DatabaseReference usersReference;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            usersReference.child(userID).child("Ringing").removeValue();

                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            usersReference.child(userID).child("Calling").removeValue();

                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }
                        else
                        {
                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }
                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegisterActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoChatActivity.this);
    }
    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String perms[] = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this, perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //Initialize and connect to the session
            mSession = new Session.Builder(this, API_Key, SESSION_ID).build();
            mSession.setSessionListener(VideoChatActivity.this);
            mSession.connect(TOKEN);
        }
        else
        {
            EasyPermissions.requestPermissions(this, "Hey, this app needs Mic and Camera Permission, Please Allow.", RC_VIDEO_APP_PERM);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //Publishing a Stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish((mPublisher));
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Stream Disconnected");
    }

    // Subscribing to the streams which have been published already
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if(mSubscriber != null)
        {
            mSubscriber = null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG, "Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

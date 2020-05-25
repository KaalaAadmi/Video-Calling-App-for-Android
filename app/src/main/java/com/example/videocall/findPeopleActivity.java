package com.example.videocall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class findPeopleActivity extends AppCompatActivity {

    private RecyclerView findFriendList;
    private EditText searchET;
    private String str="";
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        searchET = (EditText) findViewById(R.id.search_user_text);
        findFriendList = (RecyclerView) findViewById(R.id.find_friends_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchET.getText().toString().equals(""))
                {
                    Toast.makeText(findPeopleActivity.this, "Please write a name", Toast.LENGTH_LONG).show();
                }
                else
                {
                    str = s.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = null;
        if(str.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userReference, Contacts.class).build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userReference.orderByChild("Name").startAt(str).endAt(str + "\uf8ff"), Contacts.class).build();
        }

        FirebaseRecyclerAdapter<Contacts, FindFreindsViewHolder> firebaseRecyclerAdapter  = new FirebaseRecyclerAdapter<Contacts, FindFreindsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFreindsViewHolder holder, final int position, @NonNull final Contacts contact) {
                holder.userNameTxt.setText(contact.getName());
                Picasso.get().load(contact.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String visit_user_id = getRef(position).getKey();

                        Intent intent = new Intent(findPeopleActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_user_id", visit_user_id);
                        intent.putExtra("profile_image", contact.getImage());
                        intent.putExtra("profile_name", contact.getName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFreindsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                FindFreindsViewHolder viewHolder = new FindFreindsViewHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFreindsViewHolder extends RecyclerView.ViewHolder {

        TextView userNameTxt;
        Button videoCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFreindsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userNameTxt = itemView.findViewById(R.id.name_contact);
            videoCallBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.card_view1);

            videoCallBtn.setVisibility(View.GONE);

        }

    }
}

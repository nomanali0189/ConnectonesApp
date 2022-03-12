package com.example.connectones.Activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.connectones.Adapters.TopStatusAdapter;
import com.example.connectones.Models.Status;
import com.example.connectones.Models.UserStatus;
import com.example.connectones.R;
import com.example.connectones.Models.User;
import com.example.connectones.Adapters.UsersAdapter;
import com.example.connectones.databinding.ActivityDashboardBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.todkars.shimmer.ShimmerRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class dashboard extends AppCompatActivity {

    ActivityDashboardBinding binding;
    FirebaseDatabase database;
    ArrayList<User> users;
    UsersAdapter usersAdapter;
    TopStatusAdapter statusAdapter;
    ArrayList<UserStatus> userStatuses;
    Uri imageUri;
    NavigationBarView imageView;
    ProgressDialog dailog;
    private ShimmerRecyclerView mShimmerRecyclerView ,mShimmerRecyclerViewStatus;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDashboardBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) //(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {


                // For Background Image

//                String backgroundImage = mFirebaseRemoteConfig.getString("backgroundImage");
//                Glide.with(dashboard.this)
//                        .load(backgroundImage)
//                        .into(binding.backgroundImage);

                String toolbarColor = mFirebaseRemoteConfig.getString("toolbarColor");
                String toolbarImage = mFirebaseRemoteConfig.getString("toolbarImage");


                //  For Toolbar Color Change


//                getSupportActionBar()
//                        .setBackgroundDrawable(new ColorDrawable(Color.parseColor(toolbarColor)));


                //  For Toolbar Image


//                Glide.with(dashboard.this)
//                        .load(toolbarImage)
//                        .into(new CustomTarget<Drawable>() {
//                            @Override
//                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                                getSupportActionBar()
//                                        .setBackgroundDrawable(resource);
//                            }
//
//                            @Override
//                            public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                            }
//                        });

            }
        });

        database.getReference().child("users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("token",token);
                        database.getReference()
                                .child("users")
                                .child(FirebaseAuth.getInstance().getUid())
                                .updateChildren(map);
                    }
                });

        dailog = new ProgressDialog(this);
        dailog.setMessage("Uploading Image....");
        dailog.setCancelable(false);


        usersAdapter = new UsersAdapter(this, users);
        statusAdapter = new TopStatusAdapter(this, userStatuses);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);

        binding.statuLlist.setLayoutManager(layoutManager);
        binding.statuLlist.setAdapter(statusAdapter);


        binding.recyclerView.setAdapter(usersAdapter);

        mShimmerRecyclerView = findViewById(R.id.recyclerView);
        mShimmerRecyclerViewStatus = findViewById(R.id.statuLlist);

        mShimmerRecyclerView.setLayoutManager(new LinearLayoutManager(this),
                R.layout.demo_layout);

        mShimmerRecyclerViewStatus.setLayoutManager(new LinearLayoutManager(this),
                R.layout.demo_status);

        mShimmerRecyclerView.showShimmer();


        imageView = findViewById(R.id.bottomNavigationView);

        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                for(DataSnapshot snapshot1: snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);
                    if (!user.getUid().equals(FirebaseAuth.getInstance().getUid()))
                            users.add(user);


                }
                mShimmerRecyclerView.hideShimmer();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot storySnapshot : snapshot.getChildren()){
                        UserStatus status = new UserStatus();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()){
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null){
                            dailog.show();
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            Date date= new Date();
                            StorageReference reference = storage.getReference().child("status").child(date.getTime() +  "");
                            reference.putFile(result).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()){
                                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                UserStatus userStatus = new UserStatus();
                                                userStatus.setName(user.getName());
                                                userStatus.setProfileImage(user.getProfileImage());
                                                userStatus.setLastUpdated(date.getTime());

                                                HashMap<String, Object> obj = new HashMap<>();
                                                obj.put("name",userStatus.getName());
                                                obj.put("profileImage",userStatus.getProfileImage());
                                                obj.put("lastUpdated",userStatus.getLastUpdated());

                                                String imageUrl = uri.toString();
                                                Status status = new Status(imageUrl,userStatus.getLastUpdated());

                                                database.getReference()
                                                        .child("stories")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .updateChildren(obj);

                                                database.getReference()
                                                        .child("stories")
                                                        .child(FirebaseAuth.getInstance().getUid())
                                                        .child("statuses")
                                                        .push()
                                                        .setValue(status);

                                                dailog.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                }
        );

       binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
           @Override
           public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               switch (item.getItemId()) {
                   case R.id.status:
                       Intent intent = new Intent();
                       intent.setAction(Intent.ACTION_GET_CONTENT);
                        mGetContent.launch("image/*");
                       break;
               }
               return false;
           }
       });
    }


    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentId).setValue("Offline");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group:
                startActivity(new Intent(dashboard.this,GroupChatActivity.class));
                break;
            case R.id.search:
                Toast.makeText(this,"Searched Clicked",Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Intent intent = new Intent(dashboard.this,SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);

        return super.onCreateOptionsMenu(menu);
    }
}
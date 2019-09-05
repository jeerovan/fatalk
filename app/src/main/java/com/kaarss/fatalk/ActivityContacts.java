package com.kaarss.fatalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityContacts extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = ActivityContacts.class.getSimpleName();

    private static final Handler handler = new Handler(Looper.getMainLooper());

    CircleImageView _profileImage;
    LinearLayout _userDetail;
    TextView _userName;
    TextView _userBio;

    public static View.OnClickListener listItemClickListener;
    private AdapterContacts adapterContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        _profileImage = findViewById(R.id.profile_image);
        _userDetail = findViewById(R.id.user_detail);
        _userName = findViewById(R.id.user);
        _userBio = findViewById(R.id.bio);

        _profileImage.setOnClickListener(this);
        _userDetail.setOnClickListener(this);

        listItemClickListener = new ActivityContacts.ListItemClickListener();
        final RecyclerView recyclerView = findViewById(R.id.contacts);
        adapterContacts = new AdapterContacts(this);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapterContacts);
        recyclerView.setItemAnimator(null);
        App.daoChatMessage.getChatProfiles().observe(this,(profiles) -> {
            adapterContacts.setUsers(profiles);
        });
    }
    @Override
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Setting Here, As These Might Have Changed
        String userId = AppPreferences.getString(Keys.userId,"");
        _userName.setText(AppPreferences.getString(Keys.userName,""));
        _profileImage.setImageBitmap(App.getUserImage(userId, AppPreferences.getInt(Keys.userGender,0)));
        _userBio.setText(AppPreferences.getString(Keys.userBio,""));
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        listItemClickListener = null;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.user_detail:
            case R.id.profile_image:
                startActivity(new Intent(this, ActivityEditProfile.class));
                break;
        }
    }
    private void showProfile(String userId){
        Intent profileIntent = new Intent(App.applicationContext, ActivityViewProfile.class);
        profileIntent.putExtra(Keys.userId,userId);
        startActivity(profileIntent);
    }
    private class ListItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String[] data = v.getContentDescription().toString().split("\\|");
            String userId = data[0];
            String userName = data[1];
            switch (v.getId()){
                case R.id.profile_image:
                    showProfile(userId);
                    break;
                case R.id.user_detail:
                    AppLog.d(TAG,"Clicked -> "+userId+"|"+userName);
                    Intent intent = new Intent(App.applicationContext, ActivityChat.class);
                    intent.putExtra(Keys.userId,userId);
                    intent.putExtra(Keys.userName,userName);
                    startActivity(intent);
                    break;
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleImageDownloaded(EventDownloadResult event){
        String userId = AppPreferences.getString(Keys.userId,"");
        if(event.downloadType == Keys.mediaTypeUser) {
            if (event.downloadId.equals(userId)) {
                _profileImage.setImageBitmap(App.getUserImage(userId, AppPreferences.getInt(Keys.userGender, 0)));
            } else {
                adapterContacts.updateImage(event.downloadId);
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(JSONObject message) throws JSONException {
        int messageType = message.getInt(Keys.messageType);
        switch (messageType) {
            case 12:
                int rs12 = message.getInt(Keys.responseStatus);
                if(rs12 == 0){
                    String error = message.getString(Keys.responseError);
                    Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}

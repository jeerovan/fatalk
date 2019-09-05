package com.kaarss.fatalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityViewProfile extends AppCompatActivity implements DbResponse{
    private static final String TAG = ActivityViewProfile.class.getSimpleName();

    String userId;
    String userName;

    TextView _country;
    CircleImageView _profileImage;
    TextView _username;
    ImageView _female;
    ImageView _male;
    TextView _age;
    TextView _bio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        _country = findViewById(R.id.country);
        _profileImage = findViewById(R.id.profile_image);
        _username = findViewById(R.id.username);
        _female = findViewById(R.id.female);
        _male = findViewById(R.id.male);
        _age = findViewById(R.id.age);
        _bio = findViewById(R.id.bio);

        final Intent intent = getIntent();
        userId = intent.getStringExtra(Keys.userId);
        AppLog.d(TAG,"Show Profile For User Id : "+userId);
        getProfile(userId);

    }
    private void getProfile(String userId){
        App.getProfile getProfile = new App.getProfile();
        getProfile.delegate = this;
        getProfile.execute(userId);
    }
    @Override
    public void dbQueryResult(String type,Object data){
        if(type.equals(Keys.dbProfile)){
            UserProfile profile = (UserProfile) data;
            userName = profile.getUserName();
            _country.setText(profile.getCountry());
            _profileImage.setImageBitmap(App.getUserImage(profile.getUserId(),profile.getUserGender()));
            _username.setText(profile.getUserName().trim().replaceAll(" +"," "));
            if(profile.getUserGender() == 1){
                _female.setVisibility(View.GONE);
            } else {
                _male.setVisibility(View.GONE);
            }
            _age.setText(profile.getUserAge()+" Yr");
            String bio = profile.getBio().trim().replaceAll(" +"," ");
            if(bio.isEmpty()){
                _bio.setText("Couldn't Fetch Bio. Please Try Again.");
            } else {
                _bio.setText(bio);
            }
        }
    }
}

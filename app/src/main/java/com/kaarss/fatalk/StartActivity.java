package com.kaarss.fatalk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity implements SharedPreferences
        .OnSharedPreferenceChangeListener {
    private static final String TAG = StartActivity.class.getSimpleName();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        String fcmId = AppPreferences.getString(Keys.fcmId,"");
        if(!fcmId.isEmpty()){
            handler.postDelayed(haveFcmId,1000);
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        App.sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop(){
        App.sharedPref.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Keys.fcmId)){
            handler.postDelayed(haveFcmId,1000);
        }
    }
    private Runnable haveFcmId = this::haveFcmId;
    private void haveFcmId(){
        //--- fetch settings as soon as we have fcm id ----
        Request.getAppData();
        App.removeNonChatUsers();
        boolean termsAccepted = AppPreferences.getBoolean(Keys.termsAccepted,false);
        String userId = AppPreferences.getString(Keys.userId,"");
        int userState = AppPreferences.getInt(Keys.userState,0);
        if(!termsAccepted){
            startActivity(new Intent(this,TermsActivity.class));
        } else if(userId.isEmpty() || userState == 0) {
            startActivity(new Intent(this, SignInUpActivity.class));
        } else if(userState == 1){
            startActivity(new Intent(this, SecurityQuestionsActivity.class));
        } else if(userState == 2){
            startActivity(new Intent(this, SetProfileActivity.class));
        } else {
            startActivity(new Intent(this, ContactsActivity.class));
        }
        finish();
    }
}

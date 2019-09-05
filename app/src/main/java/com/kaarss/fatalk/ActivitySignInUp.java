package com.kaarss.fatalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

public class ActivitySignInUp extends AppCompatActivity implements
        View.OnClickListener{

    private static final String TAG = ActivitySignInUp.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private int PICK_COUNTRY_CODE = 101;
    private String countryName = "USA";

    private TextInputEditText _username;
    private TextInputEditText _password;
    private TextView _country;
    private Button _submit;
    private TextView _forgot;
    private TextView _signLink;
    private FrameLayout _progress;

    private Boolean stateIsSignIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_up);

        _username = findViewById(R.id.input_username);
        _password = findViewById(R.id.input_password);
        _country = findViewById(R.id.country);
        _submit = findViewById(R.id.submit);
        _forgot = findViewById(R.id.forgot);
        _signLink = findViewById(R.id.link_sign);
        _progress = findViewById(R.id.progress);

        _country.setOnClickListener(this);
        _submit.setOnClickListener(this);
        _forgot.setOnClickListener(this);
        _signLink.setOnClickListener(this);

        //Current State Is Sign In
        _country.setVisibility(View.GONE);
        _progress.setVisibility(View.GONE);
    }

    @Override
    protected void onStart(){
        super.onStart();
        hideProgress();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop(){
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    private void showProgress(){
        handler.postDelayed(scheduleCancelRequest,AppPreferences.getGeneralRequestTimeoutMillis());
        _progress.setVisibility(View.VISIBLE);
        _submit.setEnabled(false);
    }
    private void hideProgress(){
        handler.removeCallbacks(scheduleCancelRequest);
        _progress.setVisibility(View.GONE);
        _submit.setEnabled(true);
    }
    private Runnable scheduleCancelRequest = new Runnable() {
        @Override
        public void run() {
            hideProgress();
            Toast.makeText(App.applicationContext,"Please Try Again",Toast.LENGTH_LONG).show();
        }
    };
    private void changeSignState(){
        if(stateIsSignIn){
            _country.setVisibility(View.VISIBLE);
            _submit.setText("SignUp");
            _forgot.setVisibility(View.GONE);
            _signLink.setText("SignIn");
        } else {
            _country.setVisibility(View.GONE);
            _submit.setText("SignIn");
            _forgot.setVisibility(View.VISIBLE);
            _signLink.setText("SignUp");
        }
        stateIsSignIn = !stateIsSignIn;
        if(!stateIsSignIn)startActivityForResult(new Intent(this, ActivityCountryCode.class), PICK_COUNTRY_CODE);
    }

    private boolean checkUsernamePassword(){
        boolean valid = false;
        String userName = _username.getText().toString();
        int userNameLength = userName.length();
        String passWord = _password.getText().toString();
        int passwordLength = passWord.length();
        if(userName.isEmpty()){
            _username.setError("Please Enter Your Username");
        } else if(userNameLength < 6 || userNameLength > 20){
            _username.setError("Min 6 And Max 20 Characters");
        } else if(passwordLength < 8 || passwordLength > 15){
            _password.setError("Min 8 And Max 15 Characters");
        } else {
            valid = true;
        }
        return valid;
    }
    private void handleSubmit(){
        boolean userNamePasswordValid = checkUsernamePassword();
        if(!userNamePasswordValid)return;
        //Send Sign In/Up Request, Show The Progress Bar And Update State As Per Response
        showProgress();
        String username = _username.getText().toString().trim().replaceAll(" +"," ");
        if(stateIsSignIn){
            Request.signIn(username, _password.getText().toString());
        } else {
            Request.signUp(username, _password.getText().toString(), countryName);
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.link_sign:
                changeSignState();
                break;
            case R.id.submit:
                handleSubmit();
                break;
            case R.id.country:
                startActivityForResult(new Intent(this, ActivityCountryCode.class), PICK_COUNTRY_CODE);
                break;
            case R.id.forgot:
                if(_username.getText().toString().isEmpty()){
                    _username.setError("Enter Username");
                } else {
                    AppPreferences.setString(Keys.userName,_username.getText().toString());
                    AppPreferences.setString(Keys.securityState,Keys.verifyingSecurity);
                    startActivity(new Intent(this, ActivitySecurityQuestions.class));
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_COUNTRY_CODE) {
            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra("position", 0);
                countryName = AppPreferences.CountriesName.get(position);
                _country.setText(countryName);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(JSONObject message) throws JSONException {
        int messageType = message.getInt(Keys.messageType);
        switch (messageType){
            case 1:
                hideProgress();
                int rsOne = message.getInt(Keys.responseStatus);
                if(rsOne == 1){
                    AppPreferences.setString(Keys.userName,_username.getText().toString());
                    AppPreferences.setString(Keys.country, countryName);
                    startActivity(new Intent(this, ActivitySecurityQuestions.class));
                    finish();
                }
                else if(rsOne == 0){
                    String errorOne = message.getString(Keys.responseError);
                    String errorField = message.getString(Keys.errorField);
                    if(errorField.equals(Keys.userName)){
                        _username.setError(errorOne);
                    } else {
                        Toast.makeText(this, errorOne, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case 2:
                hideProgress();
                int rsTwo = message.getInt(Keys.responseStatus);
                if(rsTwo == 1){
                    int userState = message.getInt(Keys.userState);
                    if(userState == 1){
                        startActivity(new Intent(this, ActivitySecurityQuestions.class));
                    } else if(userState == 2){
                        startActivity(new Intent(this, ActivitySetProfile.class));
                    } else {
                        startActivity(new Intent(this, ActivityContacts.class));
                    }
                    finish();
                } else if(rsTwo == 0){
                    String errorTwo = message.getString(Keys.responseError);
                    String errorField = message.getString(Keys.errorField);
                    if(errorField.equals(Keys.userName)){
                        _username.setError(errorTwo);
                    } else if(errorField.equals(Keys.passWord)){
                        _password.setError(errorTwo);
                    } else {
                        Toast.makeText(this, errorTwo, Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}

package com.kaarss.fatalk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
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

/*  states _at : question
    one, two , three, four
 */
public class SecurityQuestionsActivity extends AppCompatActivity {

    private static final String TAG = SecurityQuestionsActivity.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private TextView _question;
    private FrameLayout _progress;
    private TextInputEditText _answer;
    private Button _submit;

    private String _at;
    private String _state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_questions);
        _question = findViewById(R.id.question);
        _answer = findViewById(R.id.answer);
        _progress = findViewById(R.id.progress);
        _submit = findViewById(R.id.submit);
        _submit.setText("Next");
        _submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submit();
            }
        });

        //get state -- setting | verifying
        _state = AppPreferences.getString(Keys.securityState,"");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recoverState();
        setQuestion();
        _submit.setEnabled(true);
        _progress.setVisibility(View.GONE);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
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
    public void recoverState(){
        _at = AppPreferences.getString(Keys.atAnswer,"");
        _at = _at.equals("") ? Keys.answerOne : _at;
    }

    public void setQuestion() {
        // If Verifying, Answers wont be available. Also we must save these answers.
        _answer.setText(AppPreferences.getString(_at,""));
        if (_at.equals(Keys.answerOne)){
            _question.setText(Questions.one);
        } else if(_at.equals(Keys.answerTwo)) {
            _question.setText(Questions.two);
        } else if(_at.equals(Keys.answerThree)) {
            _question.setText(Questions.three);
        } else if(_at.equals(Keys.answerFour)) {
            _question.setText(Questions.four);
            _submit.setText("Submit");
        }
    }

    public void submit() {
        if (!validate()) {
            return;
        }
        String answer = _answer.getText().toString();
        AppPreferences.setString(_at,answer);
        if(_at.equals(Keys.answerOne)){
            _at = Keys.answerTwo;
        } else if(_at.equals(Keys.answerTwo)){
            _at = Keys.answerThree;
        } else if(_at.equals(Keys.answerThree)){
            _at = Keys.answerFour;
        } else if(_at.equals(Keys.answerFour)){
            if(_state.equals(Keys.settingSecurity)){
                Request.setSecurity();
                AppPreferences.setInt(Keys.userState,2);
                startActivity(new Intent(this, SetProfileActivity.class));
                finish();
            } else if(_state.equals(Keys.verifyingSecurity)){
                Request.verifySecurity();
                showProgress();
            }
        }
        AppPreferences.setString(Keys.atAnswer,_at);
        setQuestion();
    }
    public boolean validate() {
        boolean valid = true;
        String answer = _answer.getText().toString();
        if (answer.isEmpty()) {
            _answer.setError("enter an answer");
            valid = false;
        } else {
            _answer.setError(null);
        }
        return valid;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(JSONObject message) throws JSONException {
        int messageType = message.getInt(Keys.messageType);
        switch (messageType) {
            case 5:
                hideProgress();
                int rsFive = message.getInt(Keys.responseStatus);
                if(rsFive == 1){
                    int userState = message.getInt(Keys.userState);
                    if(userState == 2){
                        startActivity(new Intent(this,SetProfileActivity.class));
                    } else {
                        startActivity(new Intent(this, ContactsActivity.class));
                    }
                    finish();
                } else {
                    String error = message.getString(Keys.responseError);
                    AppPreferences.unsetUser();
                    Toast.makeText(this,error,Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this,SignInUpActivity.class));
                    finish();
                }
                break;
        }
    }
}

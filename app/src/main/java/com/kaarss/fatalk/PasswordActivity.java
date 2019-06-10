package com.kaarss.fatalk;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class PasswordActivity extends AppCompatActivity {
    private static final String TAG = PasswordActivity.class.getSimpleName();

    TextInputEditText _password;
    Button _submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        _password = findViewById(R.id.password);
        _submit = findViewById(R.id.submit);

        _submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleChange();
            }
        });
    }
    private void handleChange(){
        String password = _password.getText().toString();
        int passwordLength = password.length();
        if(password.isEmpty()){
            _password.setError("Enter Password");
        } else if(passwordLength < 8 || passwordLength > 15){
            _password.setError("Min 8 And Max 15 Characters");
        } else {
            Request.changePassword(password);
            finish();
        }
    }
}

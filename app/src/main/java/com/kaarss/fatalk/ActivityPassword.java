package com.kaarss.fatalk;

import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ActivityPassword extends AppCompatActivity {
    private static final String TAG = ActivityPassword.class.getSimpleName();

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

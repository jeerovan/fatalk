package com.kaarss.fatalk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityEditBio extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = ActivityEditBio.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    EditText _bio;
    TextView _count;
    Button _submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bio);

        _bio = findViewById(R.id.bio);
        _count = findViewById(R.id.count);
        _submit = findViewById(R.id.submit);

        _bio.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                _count.setText(s.length()+" Characters");
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        _submit.setOnClickListener(this);
    }
    @Override protected void onStart() {
        super.onStart();
        _bio.setText(AppPreferences.getString(Keys.userBio, ""));
    }
    @Override public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                String bio = _bio.getText().toString().trim().replaceAll(" +"," ");
                if(bio.isEmpty()){
                    _bio.setError("Write Something About You");
                } else if(bio.length() > 1000){
                    _bio.setError("Maximum 1000 Characters");
                } else {
                    Request.updateBio(bio);
                    AppPreferences.setString(Keys.userBio,bio);
                    finish();
                }
                break;
        }
    }
}

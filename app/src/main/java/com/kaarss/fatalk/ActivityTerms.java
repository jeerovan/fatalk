package com.kaarss.fatalk;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ActivityTerms extends AppCompatActivity {

    Button _iAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        final TextView _terms = findViewById(R.id.terms);
        _terms.setMovementMethod(LinkMovementMethod.getInstance());

        _iAgree = findViewById(R.id.iagree);
        _iAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppPreferences.setBoolean(Keys.termsAccepted,true);
                String userId = AppPreferences.getString(Keys.userId,"");
                int userState = AppPreferences.getInt(Keys.userState,0);
                if(userId.isEmpty() || userState == 0) {
                    startActivity(new Intent(App.applicationContext, ActivitySignInUp.class));
                } else if(userState == 1){
                    startActivity(new Intent(App.applicationContext, ActivitySecurityQuestions.class));
                } else if(userState == 2){
                    startActivity(new Intent(App.applicationContext, ActivitySetProfile.class));
                } else {
                    startActivity(new Intent(App.applicationContext, ActivityContacts.class));
                }
                finish();
            }
        });
    }
}

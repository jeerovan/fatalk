package com.kaarss.fatalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityEditProfile extends ActivityBase implements View.OnClickListener {
    private static final String TAG = ActivityEditProfile.class.getCanonicalName();

    TextView _country;
    TextView _username;
    TextView _age;
    ImageView _male;
    ImageView _female;
    CircleImageView _profileImage;
    CircularProgressIndicator _imageProgress;
    TextView _bio;
    TextView _changeBio;
    Button _security;
    Button _password;
    Button _logout;
    JSONObject s3Params;
    Bitmap imageBitmap;

    private int PICK_IMAGE_REQUEST = 101;
    private String userId = AppPreferences.getString(Keys.userId,"");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.initialize(true,App.READ_WRITE_PERMISSIONS);
        setContentView(R.layout.activity_edit_profile);

        _country = findViewById(R.id.country);
        _username = findViewById(R.id.username);
        _male = findViewById(R.id.male);
        _female = findViewById(R.id.female);
        _age = findViewById(R.id.age);
        _profileImage = findViewById(R.id.profile_image);
        _imageProgress = findViewById(R.id.image_progress);
        _bio = findViewById(R.id.bio);
        _changeBio = findViewById(R.id.change_bio);
        _security = findViewById(R.id.security);
        _password = findViewById(R.id.password);
        _logout = findViewById(R.id.logout);

        _imageProgress.setMaxProgress(100);
        _imageProgress.setVisibility(View.INVISIBLE);

        _profileImage.setOnClickListener(this);
        _changeBio.setOnClickListener(this);
        _security.setOnClickListener(this);
        _password.setOnClickListener(this);
        _logout.setOnClickListener(this);

        // Set Properties
        _username.setText(AppPreferences.getString(Keys.userName,""));
        _country.setText(AppPreferences.getString(Keys.country,""));
        if(AppPreferences.getInt(Keys.userGender,0) == 0){
            _male.setVisibility(View.GONE);
        } else {
            _female.setVisibility(View.GONE);
        }
        _age.setText(AppPreferences.getInt(Keys.userAge,0)+ " Yr");
        _profileImage.setImageBitmap(App.getUserImage(AppPreferences.getString(Keys.userId,""),
                AppPreferences.getInt(Keys.userGender,0)));
    }
    @Override
    protected void onStart(){
        super.onStart();
        Request.getS3ParamsForDp();
        _bio.setText(AppPreferences.getString(Keys.userBio,""));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_bio:
                startActivity(new Intent(this, ActivityEditBio.class));
                break;
            case R.id.security:
                AppPreferences.setString(Keys.securityState,Keys.settingSecurity);
                startActivity(new Intent(this, ActivitySecurityQuestions.class));
                break;
            case R.id.password:
                startActivity(new Intent(this, ActivityPassword.class));
                break;
            case R.id.logout:
                App.clearDatabase();
                AppPreferences.unsetUser();
                startActivity(new Intent(this, ActivitySignInUp.class));
                finish();
                break;
            case R.id.profile_image:
                if(s3Params != null){
                    chooseImage();
                } else {
                    Request.getS3ParamsForDp();
                    Toast.makeText(this,"Not Ready Yet.",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            imageBitmap = FileUtils.decodeSampledBitmapFromUri(this,data.getData(),300,300);
            if(imageBitmap != null) {
                _profileImage.setImageBitmap(imageBitmap);
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bytearrayoutputstream);
                try {
                    new FileUpload(userId,Keys.mediaTypeUser,bytearrayoutputstream.toByteArray(),s3Params).start();
                    AppLog.d(TAG,"Uploading User Image:"+userId);
                    _imageProgress.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(JSONObject message) throws JSONException {
        int messageType = message.getInt(Keys.messageType);
        switch (messageType) {
            case 6:
                int rsSix = message.getInt(Keys.responseStatus);
                if(rsSix == 1){
                    s3Params = message;
                }
                break;
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventUploadProgress progress){
        String uploadId = progress.uploadId;
        int uploadType = progress.uploadType;
        int percent = progress.percent;
        if(uploadId.equals(userId) && uploadType == Keys.mediaTypeUser){
            _imageProgress.setCurrentProgress(percent);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEvent(EventUploadResult result){
        String uploadId = result.uploadId;
        int uploadType = result.uploadType;
        boolean success = result.success;
        if(uploadId.equals(userId) && uploadType == Keys.mediaTypeUser){
            if(success) {
                AppPreferences.saveImage(imageBitmap, userId);
                AppPreferences.increaseDpVersion();
                Request.updateDpVersion();
            }
            _imageProgress.setVisibility(View.INVISIBLE);
        }
    }
}

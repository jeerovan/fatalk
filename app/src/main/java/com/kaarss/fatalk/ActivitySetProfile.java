package com.kaarss.fatalk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
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

public class ActivitySetProfile extends ActivityBase implements View.OnClickListener {

    private static final String TAG = ActivitySetProfile.class.getSimpleName();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    RadioButton _genderm;
    RadioButton _genderf;
    CircleImageView _profileImage;
    CircularProgressIndicator _imageProgress;
    TextInputEditText _age;
    EditText _bio;
    TextView _count;
    Button _submit;
    JSONObject s3Params;
    Bitmap imageBitmap;

    private int _gender = 1;
    private int PICK_IMAGE_REQUEST = 201;
    int dummyImageId = 0;

    String userId = AppPreferences.getString(Keys.userId,"");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.initialize(true,App.READ_WRITE_PERMISSIONS);
        setContentView(R.layout.activity_set_profile);

        _genderm = findViewById(R.id.radiom);
        _genderf = findViewById(R.id.radiof);
        _profileImage = findViewById(R.id.profile_image);
        _imageProgress = findViewById(R.id.image_progress);
        _age = findViewById(R.id.age);
        _bio = findViewById(R.id.bio);
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
        _count = findViewById(R.id.count);
        _submit = findViewById(R.id.submit);

        _genderm.setOnClickListener(this);
        _genderf.setOnClickListener(this);

        _profileImage.setOnClickListener(this);
        _submit.setOnClickListener(this);
        _imageProgress.setVisibility(View.INVISIBLE);
        _imageProgress.setMaxProgress(100);

        // SET GENDER
        setGender(AppPreferences.getInt(Keys.userGender,0));
        // SET AGE
        int savedAge = AppPreferences.getInt(Keys.userAge,0);
        if(savedAge > 0){
            _age.setText(savedAge+"");
        }
        // SET IMAGE
        setImage(AppPreferences.getInt(Keys.userGender,0));
    }

    @Override
    protected void onStart(){
        super.onStart();
        Request.getS3ParamsForDp();
        // ---- Now We Have User Id, Fetch Active Contacts ---
        Request.getAppData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.profile_image:
                if(s3Params != null){
                    chooseImage();
                } else {
                    Request.getS3ParamsForDp();
                    Toast.makeText(this,"Not Ready Yet.",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.submit:
                String ageText = _age.getText().toString();
                String bio = _bio.getText().toString().trim().replaceAll(" +"," ");
                if(ageText.isEmpty()){
                    _age.setError("Enter Age");
                    return;
                } else if(bio.isEmpty()){
                    _bio.setError("Please Write A Few Words");
                    return;
                } else {
                    int age = 0;
                    try{
                        age = Integer.parseInt(ageText);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    if(age < 15 || age > 90) {
                        _age.setError("Enter Valid Age");
                        return;
                    }
                    if (bio.length() > 1000) {
                        _bio.setError("That's More Than 1000 Characters");
                        return;
                    }
                    Request.updateAgeGenderBio(age, _gender, bio);
                    AppPreferences.setInt(Keys.userGender,_gender);
                    AppPreferences.setInt(Keys.userAge,age);
                    AppPreferences.setString(Keys.userBio,bio);
                    AppPreferences.setInt(Keys.userState,3); // profile complete
                    if (dummyImageId > 0) {
                        AppPreferences.setInt(userId + "_dummy_image_id", dummyImageId);
                    }
                    startActivity(new Intent(this, ActivityContacts.class));
                    finish();
                }
                break;
            case R.id.radiom:
                setImage(1);
                setGender(1);
                break;
            case R.id.radiof:
                setImage(0);
                setGender(0);
                break;
        }
    }

    private void setImage(int gender){
        int dpVersion = AppPreferences.getInt(Keys.dpVersion,0);
        if(dpVersion > 0){
            _profileImage.setImageBitmap(App.getUserImage(userId,gender));
        } else {
            dummyImageId = AppPreferences.getInt(userId +"_dummy_image_id",0);
            if(dummyImageId == 0){
                dummyImageId = AppPreferences.getRandomInt(6);
             }
            _profileImage.setImageResource(AppPreferences.getDummyDrawable(dummyImageId, gender));
        }
    }

    private void setGender(int gender){
        _gender = gender;
        _genderf.setChecked(gender == 0);
        _genderm.setChecked(gender == 1);
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
                && data != null && data.getData() != null ) {
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
    public void handleImageDownloaded(EventDownloadResult event){
        if(event.downloadType == Keys.mediaTypeUser && event.downloadId.equals(userId)){
            _profileImage.setImageBitmap(App.getUserImage(userId,_gender));
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

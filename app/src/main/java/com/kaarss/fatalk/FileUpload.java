package com.kaarss.fatalk;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FileUpload implements ProgressRequestBody.UploadCallbacks, Callback<ResponseBody> {

    private static final String TAG = FileUpload.class.getSimpleName();

    private String uploadId;
    private int uploadType;
    private byte[] bytes;
    private JSONObject uploadParams;

    FileUpload(String uploadId, int uploadType, byte[] bytes, JSONObject uploadParams){
        this.uploadId = uploadId;
        this.uploadType = uploadType;
        this.bytes = bytes;
        this.uploadParams = uploadParams;
    }

    public void start() throws JSONException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(uploadParams.getString(Keys.s3Url))
                .build();
        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        String bucket = uploadParams.getString(Keys.s3Bucket);
        RequestBody requestKey =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Key));
        RequestBody requestAcl =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Acl));
        RequestBody requestMeta =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3MetaUuid));
        RequestBody requestBucket =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Bucket));
        RequestBody requestPolicy =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Policy));
        RequestBody requestSignature =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Signature));
        RequestBody requestCredential =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Credential));
        RequestBody requestAlgo =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Algorithm));
        RequestBody requestDate =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), uploadParams.getString(Keys.s3Date));

        ProgressRequestBody requestFile = new ProgressRequestBody(bytes, this, uploadId,uploadType);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

        Call<ResponseBody> request = retrofitInterface.uploadImage(bucket,
                requestKey,
                requestAcl,
                requestMeta,
                requestBucket,
                requestPolicy,
                requestSignature,
                requestCredential,
                requestAlgo,
                requestDate,
                body);
        request.enqueue(this);
    }

    @Override
    public void onProgressUpdate(String uploadId, int uploadType, int percentage) {
        EventBus.getDefault().post(new EventUploadProgress(uploadId,uploadType,percentage));
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if(response.isSuccessful()) {
            String  data = response.toString();
            AppLog.d(TAG,"upload response:"+data);
            if(uploadType == Keys.mediaTypeFile){
                App.setMediaUploaded(uploadId);
                App.sendMediaMessage(uploadId);
            }
            EventBus.getDefault().post(new EventUploadResult(uploadId,uploadType,true));
        } else {
            if(uploadType == Keys.mediaTypeFile){
                App.setMediaUploadFailed(uploadId);
            }
            EventBus.getDefault().post(new EventUploadResult(uploadId,uploadType,false));
            AppLog.d(TAG,response.toString());
            try {
                AppLog.d(TAG,"response error:"+response.body());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        App.setMediaUploadFailed(uploadId);
    }
}

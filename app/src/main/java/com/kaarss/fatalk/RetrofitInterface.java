package com.kaarss.fatalk;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RetrofitInterface {
    @Multipart
    @POST("{bucket}")
    Call<ResponseBody> uploadImage(@Path("bucket") String bucket,
                                   @Part("key") RequestBody key,
                                   @Part("acl") RequestBody acl,
                                   @Part("x-amz-meta-uuid") RequestBody meta,
                                   @Part("bucket") RequestBody requestBucket,
                                   @Part("policy") RequestBody policy,
                                   @Part("x-amz-signature") RequestBody signature,
                                   @Part("x-amz-credential") RequestBody credential,
                                   @Part("x-amz-algorithm") RequestBody algo,
                                   @Part("x-amz-date") RequestBody date,
                                   @Part MultipartBody.Part image);
}
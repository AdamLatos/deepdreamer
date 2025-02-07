package com.example.deepdreamer;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    @Multipart
    @POST("/images/upload")
    Call<MyResponse> uploadImage(@Part MultipartBody.Part image);
}
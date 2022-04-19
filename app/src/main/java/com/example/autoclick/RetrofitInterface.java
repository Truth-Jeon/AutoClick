package com.example.autoclick;

import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {

//    @POST("/login")
//    Call<LoginActivity> executeLogin(@Body HashMap<String, String> map);

    @POST("/register")
    Call<Void> executeRegister(@Body HashMap<String, String> map);

    @Multipart
    @POST("/single")
    Call<String> singleImage(@Part MultipartBody.Part file);

    @Multipart
    @POST("/multiple")
    Call<ResponseBody> multiImage(@Part MultipartBody.Part image, @Part("images") RequestBody name);

    @Multipart
    @POST("uploadMulti")
    Call<ResponseBody> multiImage2(@Part List<MultipartBody.Part> files);

//    @Multipart
//    @POST("/multiple")
//    Call<ResponseBody> multiImage(@Part MultipartBody.Part file1, @Part MultipartBody.Part file2, @Part MultipartBody.Part file3);

//    @Multipart
//    @POST("/multiple")
//    Call<ResponseBody> multiImage(@Part List<MultipartBody.Part> files);

}
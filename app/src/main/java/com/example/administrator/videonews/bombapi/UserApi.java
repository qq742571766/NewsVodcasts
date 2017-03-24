package com.example.administrator.videonews.bombapi;

import com.example.administrator.videonews.bombapi.entity.UserEntity;
import com.example.administrator.videonews.bombapi.result.UserResult;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserApi {
    //登录
    @GET("1/login")
    @Headers({"X-Bmob-Application-Id:623aaef127882aed89b9faa348451da3", "X-Bmob-REST-API-Key:c00104962a9b67916e8cbcb9157255de","Content-Type:application/json"})
    Call<UserResult> login(@Query("username") String username, @Query("password") String password);

    //注册
    @POST("1/users")
    @Headers({"X-Bmob-Application-Id:623aaef127882aed89b9faa348451da3", "X-Bmob-REST-API-Key:c00104962a9b67916e8cbcb9157255de","Content-Type:application/json"})
    Call<UserResult> register(@Body UserEntity userEntity);
}

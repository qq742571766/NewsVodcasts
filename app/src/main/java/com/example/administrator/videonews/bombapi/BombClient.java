package com.example.administrator.videonews.bombapi;

import com.example.administrator.videonews.bombapi.entity.UserEntity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BombClient {
    private static BombClient bombClient;

    public static BombClient getInstance(){
        if (bombClient == null){
            bombClient = new BombClient();
        }
        return bombClient;
    }

    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private UserApi userApi;
    private NewsApi newsApi;
    private Retrofit retrofit_cloud;//用于新接口
    private NewsApi newsApi_cloud;//用于新接口

    private BombClient(){
        //构建“日志拦截器”
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        //设置“日志拦截器”的拦截级别
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        okHttpClient = new OkHttpClient.Builder()
                //添加Bomb必要的头字段的拦截器
                .addInterceptor(new BombIntercepted())
                //添加日志拦截器
                .addInterceptor(httpLoggingInterceptor)
                .build();

        //让gson能够将bomb返回的时间戳自动转换为Date对象
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        retrofit = new Retrofit.Builder()
                .client(okHttpClient)//目的是使用okhttpclient身上的拦截器
                .baseUrl("https://api.bmob.cn/")
                //添加转换器
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        //构建retrofit_cloud
        retrofit_cloud = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://cloud.bmob.cn/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    //拿到UserApi
    public UserApi getUserApi(){
        if (userApi == null){
            userApi = retrofit.create(UserApi.class);
        }
        return userApi;
    }

    //拿到NewsApi
    public NewsApi getNewsApi(){
        if (newsApi == null){
            newsApi = retrofit.create(NewsApi.class);
        }
        return newsApi;
    }

    //拿到newsApi_cloud
    public NewsApi getNewsApi_cloud(){
        if (newsApi_cloud == null){
            newsApi_cloud = retrofit_cloud.create(NewsApi.class);
        }
        return newsApi_cloud;
    }



















    public Call register(String username, String password){
        //构建一个请求的请求体（根据服务器要求）
        //Gson 是一个用来生成，解析json数据的第三方库
        //生成，可以将一个类，生成为一串json格式的数据
        String json = new Gson().toJson(new UserEntity(username,password));

        RequestBody requestBody = RequestBody.create(null,json);

        Request request = new Request.Builder()
                .url("https://api.bmob.cn/1/users")
                .post(requestBody)
                .build();

        return okHttpClient.newCall(request);
    }


    public Call login(String username,String password){
        Request request = new Request.Builder()
                .get()
                .url("https://api.bmob.cn/1/login" + "?"
                        +"username=" + username + "&"
                        +"password=" + password)
                .build();

        return okHttpClient.newCall(request);
    }
}

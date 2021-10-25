package com.nxt.faceuploader;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;



public class ApiManager {
    public static final String BASE_URL = "http://ec2-54-81-185-4.compute-1.amazonaws.com:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {


        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build();
        }
        return retrofit;
    }
}
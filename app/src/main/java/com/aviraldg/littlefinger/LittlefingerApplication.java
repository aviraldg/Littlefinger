package com.aviraldg.littlefinger;

import android.app.Application;

import com.aviraldg.littlefinger.api.LittlefingerApi;
import com.facebook.drawee.backends.pipeline.Fresco;

import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LittlefingerApplication extends Application {
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://jsonblob.com/api/56dd50b6e4b01190df5300b8/")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private static LittlefingerApi api;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);
    }

    public static LittlefingerApi getApi() {
        if(api == null) {
            api = retrofit.create(LittlefingerApi.class);
        }
        return api;
    }
}

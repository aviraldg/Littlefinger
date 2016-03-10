package com.aviraldg.littlefinger.api;

import com.aviraldg.littlefinger.api.models.ApiData;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface LittlefingerApi {
    @GET("expenses")
    Call<ApiData> queryExpenses();

    @PUT("expenses")
    Call<ApiData> updateExpenses(@Body ApiData data);
}

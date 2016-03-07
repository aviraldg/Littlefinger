package com.aviraldg.littlefinger.api;

import com.aviraldg.littlefinger.api.models.ApiResponse;
import com.aviraldg.littlefinger.api.models.Expense;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LittlefingerApi {
    @GET("expenses")
    Call<ApiResponse> queryExpenses();
}

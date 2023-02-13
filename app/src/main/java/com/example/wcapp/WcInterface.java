package com.example.wcapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WcInterface {
    @GET(" ")
    Call<List<WcAPI>> getWcAPI();

}

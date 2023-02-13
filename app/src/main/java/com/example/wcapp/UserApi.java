package com.example.wcapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {


    @GET(" ")
    Call<List<User>> getUsers();
}

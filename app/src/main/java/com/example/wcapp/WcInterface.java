package com.example.wcapp;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WcInterface {
    @GET(" ")

    Call<List<WcAPI>> getWcAPI();

    @FormUrlEncoded
    @POST(" ")
    Call<List<WcAPI>> postWcAPI(@Field("longi") Double longi, @Field("lati") Double lati, @Field("descr") String descr,@Field("user") String user, @Field("note") String note, @Field("avis") String avis);


}

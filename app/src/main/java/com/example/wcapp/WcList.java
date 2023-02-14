package com.example.wcapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WcList extends AppCompatActivity {
    ListView simpleList;
    private static final String AUTH = "Basic " + Base64.encodeToString("hbella:bella7905Hb@".getBytes(), Base64.NO_WRAP);
    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                okhttp3.Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", AUTH)
                        .build();
                return chain.proceed(newRequest);
            })
            .build();


    ArrayList<String> cityList;
    ArrayAdapter<String> aa;

    List<String> wclist = new ArrayList<String>();
    String [] wclist2 = {"test"};





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wc_list);
        simpleList = (ListView)findViewById(R.id.simpleListView);

        wclist = new ArrayList<String>(Arrays.asList(wclist2));



        cityList = new ArrayList<String>();
    cityList.add("test");
        FetchWc();
        Log.d("cityList", cityList.toString());
        aa = new ArrayAdapter<String>(this,
                R.layout.activity_listview, cityList);

    }



    protected void FetchWc() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webdev.iut-orsay.fr/~nabitb1/WCapp/")
                .client(client)

                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        WcInterface wcInterface = retrofit.create(WcInterface.class);

        Call<List<WcAPI>> call = wcInterface.getWcAPI();

        call.enqueue(new Callback<List<WcAPI>>() {
            @Override
            public void onResponse(@NonNull Call<List<WcAPI>> call, @NonNull retrofit2.Response<List<WcAPI>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(WcList.this, "Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<WcAPI> wcAPI = response.body();

                for (WcAPI wc : wcAPI) {
                    String content = "";
                    content +=  wc.getDescr();
                    cityList.add(content);



                }
            }
            @Override
            public void onFailure(Call<List<WcAPI>> call, Throwable t) {
                Toast.makeText(WcList.this, "Erreur lors de la récupération : "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
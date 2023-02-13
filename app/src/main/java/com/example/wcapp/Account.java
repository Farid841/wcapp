package com.example.wcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Account extends AppCompatActivity {
    private TextView apiText;
    private Button Retour;
    private String id_user;
    private String AUTH = "Basic " + Base64.encodeToString("hbella:bella7905Hb@".getBytes(), Base64.NO_WRAP);
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    okhttp3.Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", AUTH)
                            .build();
                    return chain.proceed(newRequest);
                }
            })
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Intent intent_user = getIntent();
        id_user = intent_user.getStringExtra("id_user");


        apiText = findViewById(R.id.textView);

        Retour = findViewById(R.id.Retour);
        Retour.setOnClickListener(v -> {
            Intent intent = new Intent(Account.this, MainActivity.class);
            intent.putExtra("id_user", id_user);
            startActivity(intent);
        });

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webdev.iut-orsay.fr/~nabitb1/WCapp/user.php/")
                .client(client)

                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        UserApi userApi = retrofit.create(UserApi.class);
        Call<List<User>> call = userApi.getUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, retrofit2.Response<List<User>> response) {
                if (!response.isSuccessful()) {
                    apiText.setText("Code: " + response.code());
                    return;
                }
                List<User> users = response.body();
                for (User user : users) {
                    if(user.getId().equals(id_user)){
                        String content = "";
                        content += "User: " + user.getUser();
                        content += "Id: " + user.getId();
                        apiText.setText(user.getUser());
                    }else{
                        apiText.setText("User not found");
                    }


                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                apiText.setText(t.getMessage());
            }

        });

    }
}


package com.example.wcapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Login extends AppCompatActivity {

    TextView resultapii;
    Button Connexion;
    EditText username, password;
    ImageView logo;

    private String AUTH = "Basic " + Base64.encodeToString("hbella:bella7905Hb@".getBytes(), Base64.NO_WRAP);
    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
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
        setContentView(R.layout.activity_login);





        Connexion = findViewById(R.id.Connexion);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        Connexion.setOnClickListener(v -> {
            fetchLogin(username.getText().toString(), password.getText().toString());
            Connexion.setText("Connexion en cours");
            Connexion.setEnabled(false);

        });


    }

    protected void fetchLogin(String username, String password) {
        resultapii = findViewById(R.id.resultapii);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webdev.iut-orsay.fr/~nabitb1/WCapp/user.php/")
                .client(client)

                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        UserApi userlist = retrofit.create(UserApi.class);

        Call<List<User>> call = userlist.getUsers();
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (!response.isSuccessful()) {
                    resultapii.setText("Code: " + response.code());
                    return;
                }

                List<User> users = response.body();

                for (User user : users) {
                    if (user.getUser().equals(username) && user.getMdp().equals(password)) {


                        resultapii.setTextSize(20);
                        resultapii.setText("Bienvenue " + user.getUser()+" !");
                        resultapii.setTextColor(Color.GREEN);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                intent.putExtra("id_user", user.getId());
                                startActivity(intent);
                            }
                        }, 1000);





                    } else {
                        resultapii.setText("Utilisateur non trouvé");
                        resultapii.setTextColor(Color.RED);
                        resultapii.setTextSize(20);

                        Connexion.setText("Connexion");
                        Connexion.setEnabled(true);

                    }
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                resultapii.setText(t.getMessage());
            }
        });






    }
}
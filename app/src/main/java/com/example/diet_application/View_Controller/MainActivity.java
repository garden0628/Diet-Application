package com.example.diet_application.View_Controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.diet_application.Model.DataModel;
import com.example.diet_application.R;
import com.example.diet_application.privacyURL;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button login, signup;
    private EditText id, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = findViewById(R.id.Login);
        signup = findViewById(R.id.SignUp);

        id = findViewById(R.id.id);
        password = findViewById(R.id.password);

        signup.setOnClickListener(view -> {
            id.setText("");
            password.setText("");
            Intent intent = new Intent(MainActivity.this, SignUp.class);
            startActivity(intent);
        });

        login.setOnClickListener(view -> {
            String input_id = id.getText().toString();
            String input_password = password.getText().toString();

            DataModel data = new DataModel();
            data.setName(input_id);
            data.setPasswd(input_password);

            Gson gson = new Gson();
            String json = gson.toJson(data, DataModel.class);

            OkHttpClient client = new OkHttpClient();

            privacyURL privacy = new privacyURL();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(privacy.loginUrl).newBuilder();
            String url = urlBuilder.build().toString();
            Request req = new Request.Builder().url(url).post(RequestBody.create(MediaType.parse("application/json"), json)).build();
            client.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    final String myResponse = response.body().string();

                    String check = "";
                    try {
                        JSONObject jsonObject = new JSONObject(myResponse);
                        check = jsonObject.getString("success");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (check=="true"){
                        Intent intent = new Intent(getApplicationContext(), Properties.class);
                        intent.putExtra("name", input_id);
                        startActivity(intent);
                    }
                    else{
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Please check your id and password", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        });
    }

}
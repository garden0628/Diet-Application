package com.example.diet_application.View_Controller;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diet_application.Model.DataModel;
import com.example.diet_application.Model.PropertyModel;
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


public class Properties extends AppCompatActivity {

    private String name;
    private RadioGroup radioGroup;
    private EditText tall;
    private Button property;
    private Integer bias;
    private float calorie;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_properties);

        Intent intent = getIntent();
        name = intent.getExtras().getString("name");

        tall = findViewById(R.id.height);
        property = findViewById(R.id.property);
        radioGroup = findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.veryActive){
                    bias = 25;
                }
                else if(checkedId == R.id.Active){
                    bias = 35;
                }
                else{
                    bias = 40;
                }
            }
        });

        property.setOnClickListener(view -> {
            String input_height = tall.getText().toString();
            calorie = (float) ((Double.parseDouble(input_height)-100)*0.9*bias);

            OkHttpClient client = new OkHttpClient();

            PropertyModel property = new PropertyModel();
            property.setName(name);
            property.setRecommendation(calorie);

            Gson gson = new Gson();
            String json = gson.toJson(property, PropertyModel.class);

            privacyURL privacy = new privacyURL();
            HttpUrl.Builder urlBuilder = HttpUrl.parse(privacy.propertyUrl).newBuilder();
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

                    if (check == "true"){
                        Intent intent2 = new Intent(getApplicationContext(), MainPage.class);
                        intent2.putExtra("name", name);
                        startActivity(intent2);
                    }
                    else{
                        Properties.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Something cause error", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        });
    }

}
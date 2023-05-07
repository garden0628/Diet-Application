package com.example.diet_application.View_Controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.diet_application.Model.AddFoodModel;
import com.example.diet_application.Model.DateModel;
import com.example.diet_application.Model.GetRecommendationModel;
import com.example.diet_application.R;
import com.example.diet_application.privacyURL;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainPage extends AppCompatActivity {
    private String name, today;
    private CalendarView calendarView;
    private Button add;
    private TextView date, foodList, total;
    private EditText food;
    String foodList_text = "";
    String recommendation = "";
    Double total_calorie = Double.valueOf(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        calendarView = findViewById(R.id.calendarView);
        add = findViewById(R.id.add);
        date = findViewById(R.id.date);
        foodList = findViewById(R.id.foodlist);
        total = findViewById(R.id.total);
        food = findViewById(R.id.food);

        Intent intent2 = getIntent();
        name = intent2.getExtras().getString("name");

        privacyURL privacy = new privacyURL();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                today = String.valueOf(year) + String.valueOf(month) + String.valueOf(dayOfMonth);

                date.setText(String.format("%d / %d / %d", year, month, dayOfMonth));
                food.setText("");

                showFoodList();

                add.setOnClickListener(v -> {
                    String input_food = food.getText().toString();
                    if (input_food != ""){
                        OkHttpClient client2 = new OkHttpClient();
                        HttpUrl.Builder urlBuilder2 = HttpUrl.parse("https://api.calorieninjas.com/v1/nutrition").newBuilder();
                        urlBuilder2.addQueryParameter("query", input_food);

                        String url2 = urlBuilder2.build().toString();
                        Request req2 = new Request.Builder().url(url2).addHeader("X-Api-key", privacy.apiKey).build();

                        client2.newCall(req2).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                final String myResponse2 = response.body().string();
                                System.out.println(myResponse2);
                                Double calories = Double.valueOf(0);

                                try{
                                    JSONObject jsonObject = new JSONObject(myResponse2);
                                    JSONArray array = jsonObject.getJSONArray("items");

                                    for(int i=0; i<array.length(); i++){
                                        JSONObject itemObject = array.getJSONObject(i);
                                        calories += Double.parseDouble(itemObject.getString("calories"));
                                    }

                                    OkHttpClient client3 = new OkHttpClient();

                                    AddFoodModel model2 = new AddFoodModel();
                                    model2.setName(name);
                                    model2.setDate(today);
                                    model2.setFood(input_food);
                                    model2.setCalorie(calories);

                                    Gson gson = new Gson();
                                    String json = gson.toJson(model2, AddFoodModel.class);

                                    HttpUrl.Builder urlBuilder3 = HttpUrl.parse(privacy.addfoodInfoUrl).newBuilder();
                                    String url3 = urlBuilder3.build().toString();

                                    Request req3 = new Request.Builder().url(url3).post(RequestBody.create(MediaType.parse("application/json"), json)).build();
                                    client3.newCall(req3).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                            e.printStackTrace();
                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                            final String myResponse3 = response.body().string();

                                            String check = "";
                                            try {
                                                JSONObject jsonObject = new JSONObject(myResponse3);
                                                check = jsonObject.getString("success");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            if (check=="true"){
                                                showFoodList();
                                            }
                                            else{
                                                System.out.println("Adding food makes error!");
                                            }
                                        }
                                    });
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    public void showFoodList(){
        total_calorie = Double.valueOf(0);
        foodList_text = "";

        // Get daily recommendation calorie
        OkHttpClient client1 = new OkHttpClient();

        GetRecommendationModel model1 = new GetRecommendationModel();
        model1.setName(name);

        Gson gson = new Gson();
        String json1 = gson.toJson(model1, GetRecommendationModel.class);

        privacyURL privacy = new privacyURL();
        HttpUrl.Builder urlBuilder1 = HttpUrl.parse(privacy.getRecommendationUrl).newBuilder();
        String url1 = urlBuilder1.build().toString();

        Request req1 = new Request.Builder().url(url1).post(RequestBody.create(MediaType.parse("application/json"), json1)).build();
        client1.newCall(req1).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                recommendation = response.body().string();
            }
        });

        System.out.println(recommendation);

        // Get food daily food list and calculate calories
        OkHttpClient client = new OkHttpClient();

        DateModel model = new DateModel();
        model.setName(name);
        model.setDate(today);

        String json = gson.toJson(model, DateModel.class);

        HttpUrl.Builder urlBuilder = HttpUrl.parse(privacy.getfoodInfoUrl).newBuilder();
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

                try {
                    JSONArray jsonArray = new JSONArray(myResponse);
                    List<Object> responseList = new ArrayList<>();

                    for (int i=0; i<jsonArray.length(); i++){
                        responseList.add(jsonArray.get(i));
                    }

                    try {
                        Object secondElement = responseList.get(1);

                        if (secondElement instanceof JSONObject) {
                            JSONObject jsonObject = (JSONObject) secondElement;

                            Iterator<String> iterator = jsonObject.keys();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                Double value = jsonObject.getDouble(key);
                                String string_value = Double.toString(value);
                                foodList_text += (key + " : " + string_value + "\n");
                                total_calorie += value;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                MainPage.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        foodList.setText(foodList_text);
                        total.setText("Total Calorie \n" + Double.toString(total_calorie) + " / " + recommendation);
                        food.setText("");
                    }
                });
            }
        });
    }
}
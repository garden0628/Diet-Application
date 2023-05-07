package com.example.diet_application.View_Controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


public class SignUp extends AppCompatActivity {

    private EditText id, password, confirm;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        id = findViewById(R.id.id);
        password = findViewById(R.id.password);
        confirm = findViewById(R.id.confirm);
        submit = findViewById(R.id.submit);

        Intent intent = getIntent();

        confirm.addTextChangedListener(new TextWatcher() {
            String passwd = password.getText().toString();
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String check = confirm.getText().toString();
                if (check == passwd){
                    Toast.makeText(getApplicationContext(), "Password is correct!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Password is incorrect!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        submit.setOnClickListener(view -> {
            String passwd = password.getText().toString();
            String check = password.getText().toString();

            if (check.equals(passwd)) {
                String user_id = id.getText().toString();

                DataModel data = new DataModel();
                data.setName(user_id);
                data.setPasswd(passwd);

                OkHttpClient client = new OkHttpClient();

                Gson gson = new Gson();
                String json = gson.toJson(data, DataModel.class);

                privacyURL privacy = new privacyURL();
                HttpUrl.Builder urlBuilder = HttpUrl.parse(privacy.addUserUrl).newBuilder();
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
                            throw new RuntimeException(e);
                        }

                        if(check == "true"){
                            finish();
                        }
                        else{
                            SignUp.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Please check your id and password", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(), "Check your password and confirm password again", Toast.LENGTH_LONG).show();
            }
        });
    }
}
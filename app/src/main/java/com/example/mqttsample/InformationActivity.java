package com.example.mqttsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mqttsample.model.UserInfo;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Set;

public class InformationActivity extends AppCompatActivity {
    private Button btnSubmit;
    private EditText username;
    private EditText licensePlate;

    SharedPreferences preferenceManager;
    SharedPreferences.Editor editor;
    Gson gson;
    UserInfo userInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferenceManager.edit();
        gson = new Gson();

        btnSubmit = findViewById(R.id.activity_main_submit_button);
        username = findViewById(R.id.activity_main_username_edit_text);
        licensePlate = findViewById(R.id.activity_main_license_plate_edit_text);

        String userInfoStr = preferenceManager.getString("info", "");
        if (userInfoStr != "") {
            userInfo = gson.fromJson(userInfoStr, UserInfo.class);
            Log.e("userInfo: ", "" + userInfo);
            username.setText(userInfo.getUsername());
            licensePlate.setText(userInfo.getLicensePlate());

            Intent locationIntent = new Intent(this, LocationActivity.class);
            locationIntent.putExtra("username", userInfo.getUsername());
            locationIntent.putExtra("licensePlate", userInfo.getLicensePlate());
            startActivity(locationIntent);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (username.getText().length() > 0 && licensePlate.getText().length() > 0) {
                    Log.e("onClickSubmit", "username: " + username.getText().toString() + "licensePlate: " + licensePlate.getText().toString());
                    userInfo = new UserInfo(username.getText().toString(), licensePlate.getText().toString());
                    editor.putString("info", gson.toJson(userInfo));
                    editor.commit();
                }
            }
        });
    }
}
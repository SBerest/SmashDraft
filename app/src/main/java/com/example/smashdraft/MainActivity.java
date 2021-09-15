package com.example.smashdraft;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LogIn Activity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG,"create");

        Button joinButton = findViewById(R.id.join_button);
        Button settingButton = findViewById(R.id.settings_button);

        joinButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int clickedId = v.getId();
        if (clickedId == R.id.join_button) {
            Log.d(TAG, "Join Clicked");
            Intent intent = new Intent(this, DraftActivity.class);
            startActivity(intent);
        }
        else if(clickedId == R.id.settings_button){
            Log.d(TAG, "Settings Clicked");
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed(){

    }
}

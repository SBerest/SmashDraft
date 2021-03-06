package com.example.smashdraft;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(android.R.id.content, new SettingsFragment()).commit();}
    }

    @Override
    public void onBackPressed(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String TAG = "Hi";
        String gameMode = sharedPreferences.getString("gameMode","Draft As You Go");
        Log.d(TAG,"gameMode: "+gameMode);
        int numTeams = sharedPreferences.getInt("numTeams",4);
        Log.d(TAG,"numTeams: "+numTeams);
        int numRed = sharedPreferences.getInt("numRed",2);
        Log.d(TAG,"numRed: "+numRed);
        int numBlue = sharedPreferences.getInt("numBlue",2);
        Log.d(TAG,"numBlue: "+numBlue);
        int numGreen = sharedPreferences.getInt("numGreen",2);
        Log.d(TAG,"numGreen: "+numGreen);
        int numYellow = sharedPreferences.getInt("numYellow",2);
        Log.d(TAG,"numYellow: "+numYellow);
        int numCharacters = sharedPreferences.getInt("numCharacters",8);
        Log.d(TAG,"numCharacters: "+numCharacters);
        int numRandoms = sharedPreferences.getInt("numRandoms",2);
        Log.d(TAG,"numRandoms: "+numRandoms);
        boolean randomEnd = sharedPreferences.getBoolean("randomEnd",true);
        Log.d(TAG,"randomEnd: "+randomEnd);
        int numSkips = sharedPreferences.getInt("numSkips",1);
        Log.d(TAG,"numSkips: "+numSkips);
        boolean skipAnyTime = sharedPreferences.getBoolean("skipAnyTime",false);
        Log.d(TAG,"skipAnyTime: "+skipAnyTime);
        super.onBackPressed();
    }
}
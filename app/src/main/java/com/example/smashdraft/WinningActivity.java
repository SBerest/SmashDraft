package com.example.smashdraft;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WinningActivity extends AppCompatActivity {
    String TAG = "Winning Activity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);
        TextView text = findViewById(R.id.winningTeam);

        int team = ((ManagingApplication)getApplicationContext()).winningTeamNum;
        ArrayList<Fighter> fighters = ((ManagingApplication)getApplicationContext()).winningTeamComp;

        switch (team){
            case 0:
                text.setTextColor(Color.parseColor("#ff0000"));
                text.setText(R.string.RedTeam);
                break;
            case 1:
                text.setTextColor(Color.parseColor("#0060FF"));
                text.setText(R.string.BlueTeam);
                break;
            case 2:
                text.setTextColor(Color.parseColor("#00cc00"));
                text.setText(R.string.GreenTeam);
                break;
            case 3:
                text.setTextColor(Color.parseColor("#ffff00"));
                text.setText(R.string.YellowTeam);
                break;
            default:
                text.setTextColor(Color.parseColor("#000000"));
                break;
        }

        RecyclerView mRecyclerView = findViewById(R.id.winningRecyclerView);
        int noOfColumns = 2;
        if(fighters.size() > 30) noOfColumns = 9;
        else if(fighters.size() > 20) noOfColumns = 4;
        else if(fighters.size() > 16) noOfColumns = 3;

        mRecyclerView.setLayoutManager(new GridLayoutManager(this,noOfColumns));
        WinningRecyclerAdapter adapter = new WinningRecyclerAdapter(this, fighters, noOfColumns, this);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onBackPressed(){

    }
}

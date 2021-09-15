package com.example.smashdraft;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class WinningActivity extends AppCompatActivity {

    //TODO make a recycler view so it looks better
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        TextView text = findViewById(R.id.winningTeam);

        int team = intent.getIntExtra("team", 0);
        ArrayList<Fighter> fighters = (ArrayList<Fighter>) bundle.getSerializable("comp");

        switch (team){
            case 0:
                text.setTextColor(Color.parseColor("#0060FF"));
                text.setText(R.string.BlueTeam);
                break;
            case 1:
                text.setTextColor(Color.parseColor("#ff0000"));
                text.setText(R.string.RedTeam);
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

        ImageView char0 = findViewById(R.id.win_char_0);
        ImageView char1 = findViewById(R.id.win_char_1);
        ImageView char2 = findViewById(R.id.win_char_2);
        ImageView char3 = findViewById(R.id.win_char_3);
        ImageView char4 = findViewById(R.id.win_char_4);
        ImageView char5 = findViewById(R.id.win_char_5);
        ImageView char6 = findViewById(R.id.win_char_6);
        ImageView char7 = findViewById(R.id.win_char_7);
        ImageView rand0 = findViewById(R.id.ran_char_0);
        ImageView rand1 = findViewById(R.id.ran_char_1);

        if (fighters != null) {
            char0.setImageResource(fighters.get(0).getImageId());
            char1.setImageResource(fighters.get(1).getImageId());
            char2.setImageResource(fighters.get(2).getImageId());
            char3.setImageResource(fighters.get(3).getImageId());
            char4.setImageResource(fighters.get(4).getImageId());
            char5.setImageResource(fighters.get(5).getImageId());
            char6.setImageResource(fighters.get(6).getImageId());
            char7.setImageResource(fighters.get(7).getImageId());
            rand0.setImageResource(fighters.get(8).getImageId());
            rand1.setImageResource(fighters.get(9).getImageId());
        }

    }

    @Override
    public void onBackPressed(){

    }
}

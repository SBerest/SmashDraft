package com.example.smashdraft;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class GamePlayActivity extends AppCompatActivity implements View.OnClickListener, GamePlayRecyclerAdapter.onTeamListener{

    private static final String TAG = "GamePlayActivity";
    private static int NUMTEAMS;
    private static int NUMT0;
    private static int NUMT1;
    private static int NUMT2;
    private static int NUMT3;
    private static int NUMWINS;
    private static int NUMSKIPS;
    private static boolean RANDOMATEND;
    private static int NUMRANDOM;
    private static int NUMLOSSES = 4; //TODO Add this as a setting?

    private Button skip_button = null;
    private Button win_button = null;
    private TextView win_counter = null;
    private ImageView loss_counter = null;///

    boolean skipOn;

    private int focusTeam = 0;

    GamePlayRecyclerAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<Fighter> t0;
    ArrayList<Fighter> t1;
    ArrayList<Fighter> t2;
    ArrayList<Fighter> t3;
    ArrayList<Fighter> remainder;
    ArrayList<Team> teams = new ArrayList<>();

    //TODO implement number of skips
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"GamePlay Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        NUMTEAMS = sharedPreferences.getInt("numTeams",4);
        Log.d(TAG,"numTeams = "+NUMTEAMS);
        NUMT0 = sharedPreferences.getInt("numRed",2);
        Log.d(TAG,"numRed = "+NUMT0);
        NUMT1 = sharedPreferences.getInt("numBlue",2);
        Log.d(TAG,"numBlue = "+NUMT1);
        NUMT2 = sharedPreferences.getInt("numGreen",2);
        Log.d(TAG,"numGreen = "+NUMT2);
        NUMT3 = sharedPreferences.getInt("numYellow",2);
        Log.d(TAG,"numYellow = "+NUMT3);
        NUMWINS = sharedPreferences.getInt("numCharacters",8) + sharedPreferences.getInt("numRandoms",2);
        Log.d(TAG,"numCharacters + numRandoms = Num Wins"+sharedPreferences.getInt("numCharacters",8)+"+"+sharedPreferences.getInt("numRandoms",2)+"="+NUMWINS);
        NUMSKIPS = sharedPreferences.getInt("numSkip",0);
        Log.d(TAG,"numSkips = "+NUMSKIPS);
        skipOn = NUMSKIPS != 0;

        RANDOMATEND = sharedPreferences.getBoolean("randomEnd",true);
        Log.d(TAG,"randomAtEnd = "+RANDOMATEND);
        NUMRANDOM = sharedPreferences.getInt("numRandoms",2);
        Log.d(TAG,"numRandoms = "+NUMRANDOM);
        if(skipOn) skip_button = findViewById(R.id.skip_button);
        win_button = findViewById(R.id.win_button);

        win_counter = findViewById(R.id.number_of_wins);
        loss_counter = findViewById(R.id.losses_image);

        if(skipOn) skip_button.setOnClickListener(this);
        win_button.setOnClickListener(this);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        assert bundle != null;
        this.remainder = (ArrayList<Fighter>) bundle.getSerializable("remainders");

        this.t0 = (ArrayList<Fighter>) bundle.getSerializable("t0");
        this.t1 = (ArrayList<Fighter>) bundle.getSerializable("t1");
        this.t2 = (ArrayList<Fighter>) bundle.getSerializable("t2");
        this.t3 = (ArrayList<Fighter>) bundle.getSerializable("t3");
        //Can't have drop through because I want active fighters in the correct order
        switch(NUMTEAMS){
            case 4:
                teams.add(new Team(t3, 3, NUMT3, NUMRANDOM, RANDOMATEND));
                Log.d(TAG, t3.toString());
            case 3:
                teams.add(0, new Team(t2, 2, NUMT2, NUMRANDOM, RANDOMATEND));
                Log.d(TAG, t2.toString());
            case 2:
                teams.add(0,new Team(t0, 0, NUMT0, NUMRANDOM, RANDOMATEND));
                teams.add(1,new Team(t1,1, NUMT1, NUMRANDOM, RANDOMATEND));
                Log.d(TAG, t0.toString());
                Log.d(TAG, t1.toString());
                break;
        }

        recyclerView = findViewById(R.id.gameplay_draft_list);
        adapter = new GamePlayRecyclerAdapter(this, teams, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateLoseImage();
    }

    //TODO Undo function? with confirmation?
    @Override
    public void onBackPressed(){

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.win_button){
                Log.d(TAG,focusTeam+" Win Button");
                for(int i = 0; i < NUMTEAMS; i++) {
                    if (i != focusTeam) {
                        if (notFirst(i)) {
                            teams.get(i).incLoss();
                            if (teams.get(i).getLosses() >= NUMLOSSES) {
                                teams.get(i).setLosses(0);
                                teams.get(i).incWin();
                                updateWins(i);
                            }
                        }
                    }
                }

                teams.get(focusTeam).setLosses(0);
                teams.get(focusTeam).incWin();
                updateWins(focusTeam);
                reorder();
            }
        updateLoseImage();
    }

    private boolean notFirst(int team) {
        for(int i = 0; i < NUMTEAMS; i++){
            if(teams.get(team).getWins() < teams.get(i).getWins()){
                return true;
            }
        }
        return false;
    }

    private void reorder() {
        int team = teams.get(focusTeam).getTeam();
        ArrayList<Team> newFighters = new ArrayList<>(teams);
        Collections.sort(newFighters);
        int[] pos = compareArraylists(teams,newFighters);
        teams.clear();
        teams.addAll(newFighters);
        Log.d(TAG,teams+"");

        if(pos != null) {
            adapter.notifyItemMoved(pos[0], pos[1]);
        }
        else{
            Log.d(TAG,"No Changes");
        }
        for(int i = 0; i < NUMTEAMS; i++){
            if(teams.get(i).getTeam() == team){
                focusTeam = i;
            }
        }
    }

    //Given a before arrayList and after arrayList, find the one movement.
    private int[] compareArraylists(ArrayList<Team> before, ArrayList<Team> after){
        //Iterate till there is a difference
        int to_pos = 0;

        while(before.get(to_pos).getName(0).equals(after.get(to_pos).getName(0))){
            to_pos++;
            if(to_pos == NUMTEAMS){
                return null;
            }
        }
        Team to_find = after.get(to_pos);
        int from_pos = to_pos;

        //Now find where this new item comes from
        while(!before.get(from_pos).getName(0).equals(to_find.getName(0))){
            from_pos++;
            if(from_pos >= before.size()){
                return null;
            }
        }
        //returns from position and two position
        return new int[] {from_pos,to_pos};
    }

    private void updateLoseImage(){
        if(focusTeam != -1) {
            this.win_counter.setText(Integer.toString(teams.get(focusTeam).getWins()));
            Log.d(TAG,"UpdateLoseImage, getLosses = "+teams.get(focusTeam).getLosses());
            switch (teams.get(focusTeam).getLosses()) {
                case 0:
                    this.loss_counter.setImageResource(R.drawable.lose_0);
                    this.loss_counter.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
                    break;
                case 1:
                    this.loss_counter.setImageResource(R.drawable.lose_1);
                    this.loss_counter.setColorFilter( R.color.blue, PorterDuff.Mode.MULTIPLY );
                    break;
                case 2:
                    this.loss_counter.setImageResource(R.drawable.lose_2);
                    this.loss_counter.setColorFilter( R.color.green, PorterDuff.Mode.MULTIPLY );
                    break;
                case 3:
                    this.loss_counter.setImageResource(R.drawable.lose_3);
                    this.loss_counter.setColorFilter( R.color.yellow, PorterDuff.Mode.MULTIPLY );
                    break;
            }

        }
    }

    private void updateWins(int position) {
        if(position == focusTeam)
            win_counter.setText(teams.get(focusTeam).getWins()+"");

        if(teams.get(position).getWins() < NUMWINS) {
            teams.get(position).updatePointersFromWin();
            adapter.notifyItemChanged(position);
        }

        Log.d(TAG,teams.get(position)+":"+teams.get(position).getWins()+"/"+NUMWINS+"Team X Wins/NumWins");
        if(teams.get(position).getWins() == NUMWINS){
            Log.d(TAG,"Starting Win");

            Intent intent = new Intent(this, WinningActivity.class);
            intent.putExtra("team",teams.get(focusTeam).getTeam());
            Bundle bundle = new Bundle();
            teams.get(focusTeam).addRandoms();
            bundle.putSerializable("comp",teams.get(focusTeam).getFighters());
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }

    public void setFocusTeam(int position){
        int team = teams.get(position).getTeam();
        this.focusTeam = position;
        win_counter.setText(teams.get(position).getWins()+"");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        switch (team) {
            case(0):
                window.setStatusBarColor(Color.RED);
                this.win_counter.setTextColor(Color.parseColor("#ff0000"));
                this.win_button.setTextColor(Color.parseColor("#ff0000"));
                if(skipOn) this.skip_button.setTextColor(Color.parseColor("#ff0000"));
                break;
            case(1):
                window.setStatusBarColor(Color.BLUE);
                this.win_counter.setTextColor(Color.parseColor("#0060FF"));
                this.win_button.setTextColor(Color.parseColor("#0060FF"));
                if(skipOn) this.skip_button.setTextColor(Color.parseColor("#0060FF"));
                break;
            case(2):
                window.setStatusBarColor(Color.GREEN);
                this.win_counter.setTextColor(Color.parseColor("#00cc00"));
                this.win_button.setTextColor(Color.parseColor("#00cc00"));
                if(skipOn) this.skip_button.setTextColor(Color.parseColor("#00cc00"));
                break;
            case(3):
                window.setStatusBarColor(Color.YELLOW);
                this.win_counter.setTextColor(Color.parseColor("#ffff00"));
                this.win_button.setTextColor(Color.parseColor("#ffff00"));
                if(skipOn) this.skip_button.setTextColor(Color.parseColor("#ffff00"));
                break;
            default:
                window.setStatusBarColor(Color.BLACK);
                this.win_counter.setTextColor(Color.parseColor("#000000"));
                this.win_button.setTextColor(Color.parseColor("#000000"));
                if(skipOn) this.skip_button.setTextColor(Color.parseColor("#000000"));
                break;
        }
        updateLoseImage();
    }

    @Override
    public void onTeamClick(int position) {
        setFocusTeam(position);
    }
}

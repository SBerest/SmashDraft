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
import java.util.Arrays;
import java.util.Collections;

public class GamePlayActivity extends AppCompatActivity implements View.OnClickListener, GamePlayRecyclerAdapter.onTeamListener{

    private static final String TAG = "GamePlayActivity";
    private static String GAMEMODE;
    private static int NUMTEAMS;
    private static int NUMWINS;
    private static boolean SKIPANYTIME;

    private Button skip_button = null;
    private Button win_button = null;
    private TextView win_counter = null;
    private ImageView loss_counter = null;
    private ImageView skip0 = null;
    private ImageView skip1 = null;
    private ImageView skip2 = null;
    private ArrayList<Integer> draftOrder = new ArrayList<>();;
    boolean skipOn;

    private Team focusTeam;

    GamePlayRecyclerAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<Fighter> remainder;
    ArrayList<Team> teams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"GamePlay Create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        GAMEMODE = sharedPreferences.getString("gameMode","Draft As You Go");
        NUMTEAMS = sharedPreferences.getInt("numTeams",4);

        //Can't have drop through because I want active fighters in the correct order
        switch(NUMTEAMS){
            case 4:
                teams.add(((ManagingApplication) getApplicationContext()).team3);
            case 3:
                teams.add(0,((ManagingApplication) getApplicationContext()).team2);
            case 2:
                teams.add(0,((ManagingApplication) getApplicationContext()).team0);
                teams.add(1,((ManagingApplication) getApplicationContext()).team1);
                break;
        }
        focusTeam = teams.get(0);

        int NUMCHARS = sharedPreferences.getInt("numCharacters", 8);

        NUMWINS = sharedPreferences.getInt("numCharacters",8) + sharedPreferences.getInt("numRandoms",2);
        if(GAMEMODE.equals("Columns")){
            NUMWINS = NUMCHARS /4;
        }
        int NUMSKIPS = sharedPreferences.getInt("numSkips", 0);
        skipOn = NUMSKIPS != 0;
        SKIPANYTIME = sharedPreferences.getBoolean("skipAnyTime",false);

        if(skipOn) {
            skip_button = findViewById(R.id.skip_button);
            skip_button.setVisibility(View.VISIBLE);
        }

        skip0 = findViewById(R.id.skip0);
        skip1 = findViewById(R.id.skip1);
        skip2 = findViewById(R.id.skip2);
        switch (NUMSKIPS){
            case 2:
                skip2.setVisibility(View.GONE);
            case 1:
                skip1.setVisibility(View.GONE);
            case 0:
                skip0.setVisibility(View.GONE);
        }
        Log.d(TAG,"Visibilities "+ skip0.getVisibility() +":"+skip1.getVisibility()+":"+skip2.getVisibility());

        win_button = findViewById(R.id.win_button);

        win_counter = findViewById(R.id.number_of_wins);
        loss_counter = findViewById(R.id.losses_image);

        if(skipOn) skip_button.setOnClickListener(this);
        win_button.setOnClickListener(this);

        this.remainder = ((ManagingApplication) getApplicationContext()).remainders;

        recyclerView = findViewById(R.id.gameplay_draft_list);
        adapter = new GamePlayRecyclerAdapter(this, teams, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateSkipImages();
        updateLoseImage();
    }

    //TODO Undo function? with confirmation?
    @Override
    public void onBackPressed(){}

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        draftOrder.clear();
        if(v.getId() == R.id.win_button){
            for(int i = 0; i < NUMTEAMS; i++) {
                if(teams.get(i) != focusTeam && notFirst(teams.get(i))) {
                    teams.get(i).incLoss();
                    int NUMLOSSES = 4;
                    if (teams.get(i).getLosses() >= NUMLOSSES) {
                        teams.get(i).setLosses(0);
                        teams.get(i).incWin();
                        updateWins(teams.get(i));
                    }
                }
            }
            focusTeam.setLosses(0);
            focusTeam.incWin();
            updateWins(focusTeam);
            reorder();
        }else if(v.getId() == R.id.skip_button){
            if(SKIPANYTIME || focusTeam.getLosses() > 0){
                focusTeam.skip();
                updateWins(focusTeam);
                reorder();
            }
        }
        updateSkipImages();
        updateLoseImage();
    }

    private boolean notFirst(Team team) {
        for(int i = 0; i < NUMTEAMS; i++){
            if(team.getWins() < teams.get(i).getWins()){
                return true;
            }
        }
        return false;
    }

    private void reorder() {
        int team = focusTeam.getTeam();
        ArrayList<Team> newTeams = new ArrayList<>(teams);
        Collections.sort(newTeams);
        int[] pos = compareArraylists(teams, newTeams);
        teams.clear();
        teams.addAll(newTeams);

        if(pos != null) {
            adapter.notifyItemMoved(pos[0], pos[1]);
        }
        for(int i = 0; i < NUMTEAMS; i++){
            if(teams.get(i).getTeam() == team){
                focusTeam = teams.get(i);
            }
        }
    }

    //Given a before arrayList and after arrayList, find the one movement.
    private int[] compareArraylists(ArrayList<Team> before, ArrayList<Team> after){
        //Iterate till there is a difference
        int to_pos = 0;

        while(before.get(to_pos).getTeam() == (after.get(to_pos).getTeam())){
            to_pos++;
            if(to_pos == before.size() - 1 || to_pos == after.size() - 1){
                return null;
            }
        }
        Team to_find = after.get(to_pos);
        int from_pos = to_pos;

        //Now find where this new item comes from
        while(before.get(from_pos).getTeam() != (to_find.getTeam())){
            from_pos++;
            if(from_pos >= before.size()){
                return null;
            }
        }
        //returns from position and two position
        return new int[] {from_pos,to_pos};
    }

    private void updateSkipImages(){
        Log.d(TAG,"Team: "+focusTeam.getTeam() +" has "+focusTeam.getSkips()+" skips left.");
        switch (focusTeam.getSkips()){
            case 0:
                if(skip0.getVisibility() != View.GONE) skip0.setVisibility(View.INVISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.INVISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
            case 1:
                skip0.setVisibility(View.VISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.INVISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
            case 2:
                skip0.setVisibility(View.VISIBLE);
                skip1.setVisibility(View.VISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
            case 3:
                skip0.setVisibility(View.VISIBLE);
                skip1.setVisibility(View.VISIBLE);
                skip2.setVisibility(View.VISIBLE);
                break;
        }
        switch (focusTeam.getTeam()){
            case 0:
                if(skip0.getVisibility() != View.GONE) skip0.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
                if(skip1.getVisibility() != View.GONE) skip1.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
                if(skip2.getVisibility() != View.GONE) skip2.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
                break;
            case 1:
                if(skip0.getVisibility() != View.GONE) skip0.setColorFilter( 0xff0000ff, PorterDuff.Mode.MULTIPLY );
                if(skip1.getVisibility() != View.GONE) skip1.setColorFilter( 0xff0000ff, PorterDuff.Mode.MULTIPLY );
                if(skip2.getVisibility() != View.GONE) skip2.setColorFilter( 0xff0000ff, PorterDuff.Mode.MULTIPLY );
                break;
            case 2:
                if(skip0.getVisibility() != View.GONE) skip0.setColorFilter( 0xff00ff00, PorterDuff.Mode.MULTIPLY );
                if(skip1.getVisibility() != View.GONE) skip1.setColorFilter( 0xff00ff00, PorterDuff.Mode.MULTIPLY );
                if(skip2.getVisibility() != View.GONE) skip2.setColorFilter( 0xff00ff00, PorterDuff.Mode.MULTIPLY );
                break;
            case 3:
                if(skip0.getVisibility() != View.GONE) skip0.setColorFilter( 0xffffff00, PorterDuff.Mode.MULTIPLY );
                if(skip1.getVisibility() != View.GONE) skip1.setColorFilter( 0xffffff00, PorterDuff.Mode.MULTIPLY );
                if(skip2.getVisibility() != View.GONE) skip2.setColorFilter( 0xffffff00, PorterDuff.Mode.MULTIPLY );
                break;
        }
    }

    private void updateLoseImage(){
        this.win_counter.setText(getString(R.string.winString,focusTeam.getWins()));
        Log.d(TAG,"UpdateLoseImage, Team: "+focusTeam+" getLosses = "+focusTeam.getLosses());
        switch (focusTeam.getLosses()) {
            case 0:
                this.loss_counter.setImageResource(R.drawable.lose_0);
                break;
            case 1:
                this.loss_counter.setImageResource(R.drawable.lose_1);
                break;
            case 2:
                this.loss_counter.setImageResource(R.drawable.lose_2);
                break;
            case 3:
                this.loss_counter.setImageResource(R.drawable.lose_3);
                break;
        }
        switch (focusTeam.getTeam()){
            case 0:
                this.loss_counter.setColorFilter(0xffff0000, PorterDuff.Mode.MULTIPLY );
                break;
            case 1:
                this.loss_counter.setColorFilter( 0xff0000ff, PorterDuff.Mode.MULTIPLY );
                break;
            case 2:
                this.loss_counter.setColorFilter( 0xff00ff00, PorterDuff.Mode.MULTIPLY );
                break;
            case 3:
                this.loss_counter.setColorFilter( 0xffffff00, PorterDuff.Mode.MULTIPLY );
                break;
        }
    }

    private void updateWins(Team team) {
        if(team == focusTeam)
            win_counter.setText(getString(R.string.winString,focusTeam.getWins()));

        if(team.getWins() < NUMWINS) {
            team.updatePointersFromWin();
            adapter.notifyItemChanged(teams.indexOf(team));
        }

        Log.d(TAG,focusTeam.getTeam()+":"+focusTeam.getWins()+"/"+NUMWINS+"Team X Wins/NumWins");
        if(focusTeam.getWins() == NUMWINS){
            Intent intent = new Intent(this, WinningActivity.class);
            intent.putExtra("team",focusTeam.getTeam());
            Bundle bundle = new Bundle();
            focusTeam.addRandoms();
            bundle.putSerializable("comp",focusTeam.getFighters());
            intent.putExtras(bundle);

            startActivity(intent);
        }
        if(GAMEMODE.equals("Draft As You Go")){
            if(team == focusTeam) {
                Intent intent = new Intent(this, DraftActivity.class);
                intent.putExtra("prevActivity", "GamePlayActivity");
                for (int i = 0; i < team.getTeamSize(); i++)
                    draftOrder.add(0,team.getTeam());
                intent.putIntegerArrayListExtra("draftOrder", draftOrder);
                startActivity(intent);
            }else{
                for (int i = 0; i < team.getTeamSize(); i++)
                    draftOrder.add(team.getTeam());
            }
        }
    }

    public void setFocusTeam(int position){
        int team = teams.get(position).getTeam();
        this.focusTeam = teams.get(position);
        win_counter.setText(getString(R.string.winString,focusTeam.getWins()));
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
        updateSkipImages();
        updateLoseImage();
    }

    @Override
    public void onTeamClick(int position) {
        setFocusTeam(position);
    }

    @Override
    public void onResume() {
        for(int i = 0; i < NUMTEAMS; i++){
            adapter.notifyItemChanged(i);
            Log.d(TAG,"Team: "+i+" points:"+ Arrays.toString(teams.get(i).getPointers()) +" fighters:"+teams.get(i).getFighters());
        }
        super.onResume();
    }
}

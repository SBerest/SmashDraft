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

    //private static final String TAG = "GamePlayActivity";
    private static String mGameMode;
    private static int mNumTeams;
    private static int mNumWins;
    private static boolean mSkipAnytime;

    private Button skip_button = null;
    private Button win_button = null;
    private TextView win_counter = null;
    private ImageView loss_counter = null;
    private ImageView skip0 = null;
    private ImageView skip1 = null;
    private ImageView skip2 = null;
    boolean mSkipOn;
    private final ArrayList<Integer> mDraftOrder = new ArrayList<>();

    private Team focusTeam;

    private GamePlayRecyclerAdapter adapter;
    private final ArrayList<Team> teams = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay2);

        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        mGameMode = sharedPreferences.getString("gameMode","Draft As You Go");
        mNumTeams = sharedPreferences.getInt("numTeams",4);
        switch(mNumTeams){
            case 4:
                teams.add(((ManagingApplication) getApplicationContext()).team3);
            case 3:
                teams.add(0,((ManagingApplication) getApplicationContext()).team2);
            case 2:
                teams.add(0,((ManagingApplication) getApplicationContext()).team0);
                teams.add(1,((ManagingApplication) getApplicationContext()).team1);
        }
        focusTeam = teams.get(0);

        int numChars = sharedPreferences.getInt("numCharacters", 8);
        mNumWins = sharedPreferences.getInt("numCharacters",8) + sharedPreferences.getInt("numRandoms",2);
        if(mGameMode.equals("Columns"))
            mNumWins = numChars / 4;


        int numSkips = sharedPreferences.getInt("numSkips", 1);
        mSkipAnytime = sharedPreferences.getBoolean("skipAnyTime",false);

        mSkipOn = numSkips != 0;
        if(mSkipOn) {
            skip_button = findViewById(R.id.skip_button);
            skip_button.setVisibility(View.VISIBLE);
            skip_button.setOnClickListener(this);
        }

        /* //TODO convert to new formatting
        skip0 = findViewById(R.id.skip0);
        skip1 = findViewById(R.id.skip1);
        skip2 = findViewById(R.id.skip2);
        switch (numSkips){
            case 0:
                skip0.setVisibility(View.GONE);
            case 1:
                skip1.setVisibility(View.GONE);
            case 2:
                skip2.setVisibility(View.GONE);
                break;
            case 3:
                skip0.setVisibility(View.VISIBLE);
                skip1.setVisibility(View.VISIBLE);
                skip2.setVisibility(View.VISIBLE);
        }*/

        win_button = findViewById(R.id.win_button);
        win_button.setOnClickListener(this);
        win_counter = findViewById(R.id.number_of_wins);
        loss_counter = findViewById(R.id.losses_image);

        RecyclerView recyclerView = findViewById(R.id.gameplay_draft_list);
        adapter = new GamePlayRecyclerAdapter(this, teams, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateSkipImages();
        updateLoseImage();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        mDraftOrder.clear();
        if(v.getId() == R.id.win_button){
            for(int i = 0; i < mNumTeams; i++)
                if(teams.get(i) != focusTeam && notFirst(teams.get(i))) {
                    teams.get(i).incLoss();
                    int numLosses = 4;
                    if (teams.get(i).getLosses() >= numLosses) {
                        teams.get(i).setLosses(0);
                        teams.get(i).incWin();
                        updateWins(teams.get(i));
                    }
                }
            focusTeam.setLosses(0);
            focusTeam.incWin();
            updateWins(focusTeam);
            reorder();
        }else if(v.getId() == R.id.skip_button)
            if(mSkipAnytime || focusTeam.getLosses() > 0){
                if(focusTeam.skip()) {
                    if(focusTeam.getSkips() == 0) {
                        this.skip_button.setTextColor(Color.GRAY);
                        this.skip_button.setEnabled(false);
                    }
                    updateWins(focusTeam);
                    reorder();
                }
        }
        updateSkipImages();
        updateLoseImage();
    }

    private boolean notFirst(Team team) {
        for(int i = 0; i < mNumTeams; i++)
            if(team.getWins() < teams.get(i).getWins())
                return true;
        return false;
    }

    private void reorder() {
        int team = focusTeam.getTeam();
        ArrayList<Team> newTeams = new ArrayList<>(teams);
        Collections.sort(newTeams);
        int[] pos = compareArraylists(teams, newTeams);
        teams.clear();
        teams.addAll(newTeams);

        if(pos != null)
            adapter.notifyItemMoved(pos[0], pos[1]);

        for(int i = 0; i < mNumTeams; i++)
            if(teams.get(i).getTeam() == team)
                focusTeam = teams.get(i);
    }

    //Given a before arrayList and after arrayList, find the one movement.
    private int[] compareArraylists(ArrayList<Team> before, ArrayList<Team> after){
        //Iterate till there is a difference
        int to_pos = 0;

        while(before.get(to_pos).getTeam() == (after.get(to_pos).getTeam())){
            to_pos++;
            if(to_pos == before.size() - 1 || to_pos == after.size() - 1)
                return null;
        }
        Team to_find = after.get(to_pos);
        int from_pos = to_pos;

        //Now find where this new item comes from
        while(before.get(from_pos).getTeam() != (to_find.getTeam())){
            from_pos++;
            if(from_pos >= before.size())
                return null;
        }
        //returns from position and two position
        return new int[] {from_pos,to_pos};
    }

    private void updateSkipImages(){
        /*
        switch (focusTeam.getSkips()){
            case 3:
                if(skip0.getVisibility() != View.GONE) skip0.setVisibility(View.VISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.VISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.VISIBLE);
                break;
            case 2:
                if(skip0.getVisibility() != View.GONE) skip0.setVisibility(View.VISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.VISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
            case 1:
                if(skip0.getVisibility() != View.GONE) skip0.setVisibility(View.VISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.INVISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
            case 0:
                if(skip0.getVisibility() != View.GONE) skip0.setVisibility(View.INVISIBLE);
                if(skip1.getVisibility() != View.GONE) skip1.setVisibility(View.INVISIBLE);
                if(skip2.getVisibility() != View.GONE) skip2.setVisibility(View.INVISIBLE);
                break;
        }
        int teamColor = Color.BLACK;
        switch (focusTeam.getTeam()){
            case 0:
                teamColor = Color.RED;
                break;
            case 1:
                teamColor = Color.BLUE;
                break;
            case 2:
                teamColor = Color.GREEN;
                break;
            case 3:
                teamColor = Color.YELLOW;
                break;
        }
        if(skip0.getVisibility() == View.VISIBLE) skip0.setColorFilter(teamColor, PorterDuff.Mode.MULTIPLY);
        if(skip1.getVisibility() == View.VISIBLE) skip1.setColorFilter(teamColor, PorterDuff.Mode.MULTIPLY);
        if(skip2.getVisibility() == View.VISIBLE) skip2.setColorFilter(teamColor, PorterDuff.Mode.MULTIPLY); TODO convert to new formatting

         */
    }

    private void updateLoseImage(){
    /*
        this.win_counter.setText(getString(R.string.winString,focusTeam.getWins()));
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
                this.loss_counter.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
                break;
            case 1:
                this.loss_counter.setColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY);
                break;
            case 2:
                this.loss_counter.setColorFilter(Color.YELLOW, PorterDuff.Mode.MULTIPLY);
                break;
            case 3:
                this.loss_counter.setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
                break;
        }
         TODO convert to new formatting */
    }

    private void updateWins(Team team) {
        /*
        if(team == focusTeam)
            win_counter.setText(getString(R.string.winString,focusTeam.getWins()));
            TODO convert to new formatting */

        if(team.getWins() < mNumWins) {
            team.updatePointersFromWin();
            adapter.notifyItemChanged(teams.indexOf(team));
        }else{
            Intent intent = new Intent(this, WinningActivity.class);
            ((ManagingApplication) getApplicationContext()).winningTeamNum = focusTeam.getTeam();
            ((ManagingApplication) getApplicationContext()).winningTeamComp = focusTeam.getFighters();
            focusTeam.addRandoms();
            startActivity(intent);
        }
        if(mGameMode.equals("Draft As You Go")){
            mDraftOrder.addAll(0,Collections.nCopies(team.getTeamSize(), team.getTeam()));
            if(team == focusTeam) {
                Intent intent = new Intent(this, DraftActivity.class);
                intent.putExtra("prevActivity", "GamePlayActivity");
                intent.putIntegerArrayListExtra("draftOrder", mDraftOrder);
                startActivity(intent);
            }
        }
    }

    @Override
    //Sets the focus team
    public void onTeamClick(int position) {
        int team = teams.get(position).getTeam();
        this.focusTeam = teams.get(position);
        win_counter.setText(getString(R.string.winString,focusTeam.getWins()));
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int teamColor = Color.BLACK;
        switch (team) {
            case 0:
                teamColor = Color.RED;
                break;
            case 1:
                teamColor = Color.BLUE;
                break;
            case 2:
                teamColor = Color.GREEN;
                break;
            case 3:
                teamColor = Color.YELLOW;
                break;
        }
        window.setStatusBarColor(teamColor);
        this.win_counter.setTextColor(teamColor);
        this.win_button.setTextColor(teamColor);
        if(mSkipOn) {
            if(focusTeam.getSkips() > 0) {
                this.skip_button.setTextColor(teamColor);
                this.skip_button.setEnabled(true);
            }
            else {
                this.skip_button.setTextColor(Color.GRAY);
                this.skip_button.setEnabled(false);
            }
        }
        updateSkipImages();
        updateLoseImage();
    }


    // This seems to be needed as fighters are updated during the draft activity but this activity happens in the background
    @Override
    public void onResume() {
        for(int i = 0; i < mNumTeams; i++)
            adapter.notifyItemChanged(i);
        super.onResume();
    }

    //TODO Undo function? with confirmation?
    @Override
    public void onBackPressed(){}

}

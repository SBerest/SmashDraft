package com.example.smashdraft;

import android.content.SharedPreferences;
import android.util.Log;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Team implements Serializable, Comparable<Team>  {
    private static final String TAG = "Team";
    private final int team;
    private final int teamSize;
    private int wins = 0;
    private int losses = 0;
    private final ArrayList<Fighter> fighters;
    private final int[] pointers;
    private final int mNumRandoms;
    private int mNumSkips;
    private final String mGameMode;
    private final int randomLocation; //0 = no randoms, 1 = randoms first, 2 = randoms last.
    private boolean onRandoms;
    private int randomsDone;

    Team(ArrayList<Fighter> fighters, int team, SharedPreferences sharedPreferences) {
        this.team = team;
        this.fighters = fighters;
        String teamSizeString = "numRed";
        if(team == 1) teamSizeString = "numBlue";
        else if(team == 2) teamSizeString = "numGreen";
        else if(team == 3) teamSizeString = "numYellow";

        this.teamSize = sharedPreferences.getInt(teamSizeString,2);
        this.pointers = new int[this.teamSize];
        this.mNumRandoms = sharedPreferences.getInt("numRandoms",2);
        boolean randomAtEnd = sharedPreferences.getBoolean("randomEnd",true);
        this.mNumSkips = sharedPreferences.getInt("numSkips",0);
        this.mGameMode = sharedPreferences.getString("gameMode","Draft As You Go");
        this.randomLocation = randomAtEnd? 2 : 1;
        this.onRandoms = randomAtEnd;
        this.randomsDone = 0;

        setPointers();
    }

    public void setPointers(){
        if(mGameMode.equals("Columns")){
            setPointersColumns();
        }else if(mGameMode.equals("Draft As You Go")){
            setPointersToOne();
        }else{
            setPointersDefault();
        }
    }

    private void setPointersToOne() {
        switch (this.teamSize){
            case 4:
                this.pointers[3] = 3;
            case 3:
                this.pointers[2] = 2;
            case 2:
                this.pointers[1] = 1;
            case 1:
                this.pointers[0] = 0;
        }
    }

    private void setPointersColumns() {
        this.pointers[3] = 3;
        this.pointers[2] = 2;
        this.pointers[1] = 1;
        this.pointers[0] = 0;
    }

    private void setPointersDefault() {
        switch (this.teamSize){
            case 4:
                this.pointers[3] = this.fighters.size() - (this.fighters.size()/4);
            case 3:
                this.pointers[2] = this.fighters.size()/4;
            case 2:
                this.pointers[1] = this.fighters.size()-1;
            case 1:
                this.pointers[0] = 0;
        }
        Log.d(TAG,"Team:"+this.team+" pointers:"+ Arrays.toString(this.pointers)+" fighters:"+this.fighters);
    }

    public String asString(){
        return "Team: "+this.team+"Fighters: "+this.fighters;
    }

    public int[] getPointers(){
        return this.pointers;
    }
    public int getTeamSize(){
        return this.teamSize;
    }

    public int getImageId(int num) {
        Log.d(TAG,"pointers:"+ Arrays.toString(pointers));
        if(pointers[num] == -1){
            return R.drawable.img_00_question;
        }
        if(pointers[num] < this.fighters.size())
            return this.fighters.get(pointers[num]).getImageId();
        return R.drawable.img_00_question;
    }

    @Override
    public int compareTo(Team team) {
        return  team.getWins() - this.getWins();
    }

    public int getTeam() {
        return this.team;
    }

    public int getWins() {
        return this.wins;
    }

    public void incWin() {
        this.wins++;
    }

    public void incLoss() {
        this.losses++;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public ArrayList<Fighter> getFighters(){
        return this.fighters;
    }

    public Boolean skip(){
        if(mNumSkips > 0){
            Log.d(TAG,"Skip:"+mNumSkips);
            mNumSkips -= 1;
            this.wins++;
            this.losses = 0;
            return true;
        }
        return false;
    }

    public void updatePointersFromWin() {
        if(onRandoms){
            this.randomsDone += 1;
            if(randomsDone >= mNumRandoms)
                onRandoms = false;
        }
        else {
            if (mGameMode.equals("Draft As You Go") || mGameMode.equals("Columns")) {
                for (int i = 0; i < teamSize; i++)
                    pointers[i] += teamSize;
            } else {
                if (randomLocation == 1 && wins == mNumRandoms)
                    setPointersDefault();
                else
                    changePointers();
            }
        }
    }

    private void changePointers() {
        switch (teamSize) {
            case 4:
                pointers[3] -= 1;
                if (pointers[3] < 0)
                    pointers[3] = this.fighters.size() - 1;
            case 3:
                pointers[2] += 1;
                if (pointers[2] >= this.fighters.size())
                    pointers[2] = 0;
            case 2:
                pointers[1] -= 1;
                if (pointers[1] < 0)
                    pointers[1] = this.fighters.size() - 1;
            case 1:
                pointers[0] += 1;
                if (pointers[0] >= this.fighters.size())
                    pointers[0] = 0;
        }
    }

    public int getSkips() {
        return mNumSkips;
    }

    public boolean contains(Fighter someFighter) {
        for(Fighter fighter: this.fighters){
            if(someFighter.getName().equals(fighter.getName())){
                return true;
            }
        }
        return false;
    }
}

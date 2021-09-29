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
    private String mGameMode;
    private int randomLocation; //0 = no randoms, 1 = randoms first, 2 = randoms last.

    Team(ArrayList<Fighter> fighters, int team, SharedPreferences sharedPreferences) {
        this.team = team;
        this.fighters = fighters;
        String teamSizeString = "NUMT0";
        if(team == 1) teamSizeString = "NUMT1";
        else if(team == 2) teamSizeString = "NUMT2";
        else if(team == 3) teamSizeString = "NUMT3";

        this.teamSize = sharedPreferences.getInt(teamSizeString,2);
        this.pointers = new int[this.teamSize];
        this.mNumRandoms = sharedPreferences.getInt("numRandoms",2);
        boolean randomAtEnd = sharedPreferences.getBoolean("randomEnd",true);
        this.mNumSkips = sharedPreferences.getInt("numSkips",0);
        this.mGameMode = sharedPreferences.getString("gameMOde","Draft As You Go");;

        if(mNumRandoms == 0) randomLocation = 0;
        randomLocation = randomAtEnd? 2 : 1;
        Log.d(TAG,"Fighters = "+fighters);
        if(randomLocation != 1)
            if(mGameMode.equals("Columns")){
                setPointersColumns();
            }else if(mGameMode.equals("Draft As You Go")){
                setPointersToOne();
            }else{
                setPointersDefault();
            }
        else{
            setPointersToNegOne();
        }
    }

    private void setPointersToOne() {
        switch (teamSize){
            case 4:
                pointers[3] = 3;
            case 3:
                pointers[2] = 2;
            case 2:
                pointers[1] = 1;
            case 1:
                pointers[0] = 0;
        }
    }

    private void setPointersColumns() {
        pointers[3] = 3;
        pointers[2] = 2;
        pointers[1] = 1;
        pointers[0] = 0;
    }

    private void setPointersDefault() {
        switch (teamSize){
            case 4:
                pointers[3] = fighters.size() - (fighters.size()/4);
            case 3:
                pointers[2] = fighters.size()/4;
            case 2:
                pointers[1] = fighters.size()-1;
            case 1:
                pointers[0] = 0;
        }
    }

    private void setPointersToNegOne(){
        switch (teamSize) {
            case 4:
                pointers[3] = -1;
            case 3:
                pointers[2] = -1;
            case 2:
                pointers[1] = -1;
            case 1:
                pointers[0] = -1;
        }
    }
    public int[] getPointers(){
        return this.pointers;
    }
    @Override
    public String toString() {
        StringBuilder toRet = new StringBuilder();
        for(Fighter fighter:fighters){
            toRet.append(fighter.getName());
            toRet.append(" | ");
        }
        return toRet.toString();
    }

    public int getTeamSize(){
        return this.teamSize;
    }

    public int getImageId(int num) {
        Log.d(TAG, "getImageId Fighters:"+fighters);
        Log.d(TAG, "getImageId Pointers:"+Arrays.toString(pointers));
        if(pointers[num] == -1){
            return R.drawable.img_00_question;
        }
        if(pointers[num] < fighters.size())
            return fighters.get(pointers[num]).getImageId();
        return R.drawable.img_00_loading;
    }

    @Override
    public int compareTo(Team team) {
        return  team.getWins() - this.getWins();
    }

    public int getTeam() {
        return this.team;
    }

    public int getWins() {
        return wins;
    }

    public void incWin() {
        this.wins++;
    }

    public void skip(){
        if(mNumSkips > 0){
            mNumSkips -= 1;
            incWin();
            this.losses = 0;
        }
        else Log.d(TAG,"Ran out of skips.");
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

    public void updatePointersFromWin() {
        if(mGameMode.equals("Draft As You Go") || mGameMode.equals("Columns")){
            for(int i = 0; i < teamSize; i++)
                pointers[i] += teamSize;
        }
        else{
            if (randomLocation != 0) {
                if ((randomLocation == 1 && wins < mNumRandoms) || (randomLocation == 2 && fighters.size() <= wins)) {
                    setPointersToNegOne();
                } else if (randomLocation == 1 && wins == mNumRandoms) {
                    setPointersDefault();
                } else {
                    changePointers();
                }
            } else {
                changePointers();
            }
        }
    }

    private void changePointers() {
        switch (teamSize) {
            case 4:
                pointers[3] -= 1;
                if (pointers[3] < 0)
                    pointers[3] = fighters.size() - 1;
            case 3:
                pointers[2] += 1;
                if (pointers[2] >= fighters.size())
                    pointers[2] = 0;
            case 2:
                pointers[1] -= 1;
                if (pointers[1] < 0)
                    pointers[1] = fighters.size() - 1;
            case 1:
                pointers[0] += 1;
                if (pointers[0] >= fighters.size())
                    pointers[0] = 0;
        }
    }

    public void addRandoms() {
        Fighter random = new Fighter(R.drawable.img_00_question,"Random");
        switch (randomLocation){
            case 1:
                for(int i = 0; i < mNumRandoms; i++)
                    this.fighters.add(0,random);
                break;
            case 2:
                for(int i = 0; i < mNumRandoms; i++)
                    this.fighters.add(random);
                break;
        }
    }

    public int getSkips() {
        return mNumSkips;
    }

    public boolean contains(Fighter someFighter) {
        for(Fighter fighter:fighters){
            if(someFighter.getName().equals(fighter.getName())){
                return true;
            }
        }
        return false;
    }
}

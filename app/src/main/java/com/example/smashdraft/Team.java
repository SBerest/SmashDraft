package com.example.smashdraft;



import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class Team implements Serializable, Comparable<Team>  {
    private static final String TAG = "Active Fighters";
    private final int team;
    private final int teamSize;
    private int wins = 0;
    private int losses = 0;
    private ArrayList<Fighter> fighters;
    private int[] pointers;
    private int numRandoms;
    Fighter random;
    private int randomLocation; //0 = no randoms, 1 = randoms first, 2 = randoms last.

    Team(ArrayList<Fighter> fighters, int team, int size, int numRandoms, boolean randomAtEnd) {
        this.team = team;
        this.teamSize = size;
        this.fighters = fighters;
        this.pointers = new int[size];
        this.numRandoms = numRandoms;

        random = new Fighter(R.drawable.img_00_question,"Random");

        if(numRandoms == 0) randomLocation = 0;
        randomLocation = randomAtEnd? 2 : 1;
        Log.d(TAG,"randomLocation = "+randomLocation);
        if(randomLocation != 1)
            setPointersDefault();
        else{
            setPointersToNegOne();
        }
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
        if(pointers[num] == -1){
            return random.getImageId();
        }
        return fighters.get(pointers[num]).getImageId();
    }

    public String getName(int num) {
        if(pointers[num] == -1){
            return random.getName();
        }
        return fighters.get(pointers[num]).getName();
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
        if(randomLocation != 0) {
            if((randomLocation == 1 && wins < numRandoms) || (randomLocation == 2 && fighters.size() <= wins)){
                setPointersToNegOne();
            }else if (randomLocation == 1 && wins == numRandoms){
                setPointersDefault();
            }else{
                changePointers();
            }
        }
        else{
            changePointers();
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
}

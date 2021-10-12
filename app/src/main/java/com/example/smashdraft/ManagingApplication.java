package com.example.smashdraft;

import android.app.Application;

import java.util.ArrayList;

public class ManagingApplication extends Application {
    Team team0 = null;
    Team team1 = null;
    Team team2 = null;
    Team team3 = null;
    ArrayList<Fighter> allFighters = null;
    ArrayList<Fighter> remainders = null;
    int winningTeamNum = -1;
    ArrayList<Fighter> winningTeamComp = null;

    int getFighter(Fighter fighter){
        if(team0 != null)
            if(team0.contains(fighter)) return 0;
        if(team1 != null)
            if(team1.contains(fighter)) return 1;
        if(team2 != null)
            if(team2.contains(fighter)) return 2;
        if(team3 != null)
            if(team3.contains(fighter)) return 3;
        return -1;
    }
}

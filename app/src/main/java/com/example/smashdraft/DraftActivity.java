package com.example.smashdraft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class DraftActivity extends AppCompatActivity implements DraftRecyclerAdapter.onFighterListener {

    private static final String TAG = "Draft Activity";
    private int NUMTEAMS;

    private AutoCompleteTextView editText;

    private final ArrayList<Fighter> fighters = new ArrayList<>(); //all fighter_cell
    private final ArrayList<Fighter> fullFighters = new ArrayList<>();
    private final ArrayList<Fighter> alphaSortedFighters = new ArrayList<>();

    RecyclerView mRecyclerView;
    DraftRecyclerAdapter adapter;

    private Menu menu;
    int indexOfRecentRandom;
    boolean eye = true;
    boolean alphaSort = false;
    final Context mContext = this;
    private int DRAFTINGNUM;
    boolean auto;
    static String GAMEMODE;
    String PREVACTIVITY;

    ArrayList<ArrayList<Fighter>> spDrafted = new ArrayList<>();

    int spTeam;
    private ArrayList<Integer> draftOrder;
    private int draftOrderIndex = 0;
    boolean listView = true;
    ArrayList<Fighter> lastDrafted;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        GAMEMODE = sharedPreferences.getString("gameMode", "Draft As You Go");
        NUMTEAMS = sharedPreferences.getInt("numTeams", 4);
        if (!GAMEMODE.equals("Draft As You Go")) {
            if (NUMTEAMS == 2) {
                draftOrder = new ArrayList<>(Arrays.asList(0, 1));
            } else if (NUMTEAMS == 3) {
                draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 2, 1, 0));
            } else if (NUMTEAMS == 4) {
                draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 3, 2, 1, 0));
            }
        }
        lastDrafted = new ArrayList<>();
        switch(NUMTEAMS){
            case 4:
                if (((ManagingApplication) getApplicationContext()).team3 == null)
                    ((ManagingApplication) getApplicationContext()).team3 = new Team(new ArrayList<>(), 3, sharedPreferences);
                spDrafted.add(0, ((ManagingApplication) getApplicationContext()).team3.getFighters());
                lastDrafted.addAll(spDrafted.get(0));
            case 3:
                if (((ManagingApplication) getApplicationContext()).team2 == null)
                    ((ManagingApplication) getApplicationContext()).team2 = new Team(new ArrayList<>(), 2, sharedPreferences);
                spDrafted.add(0, ((ManagingApplication) getApplicationContext()).team2.getFighters());
                lastDrafted.addAll(spDrafted.get(0));
            case 2:
                if (((ManagingApplication) getApplicationContext()).team0 == null)
                    ((ManagingApplication) getApplicationContext()).team0 = new Team(new ArrayList<>(), 0, sharedPreferences);
                if (((ManagingApplication) getApplicationContext()).team1 == null)
                    ((ManagingApplication) getApplicationContext()).team1 = new Team(new ArrayList<>(), 1, sharedPreferences);
                spDrafted.add(0, ((ManagingApplication) getApplicationContext()).team0.getFighters());
                lastDrafted.addAll(spDrafted.get(0));
                spDrafted.add(1, ((ManagingApplication) getApplicationContext()).team1.getFighters());
                lastDrafted.addAll(spDrafted.get(1));
        }

        lastDrafted.add(new Fighter(R.drawable.img_00_question,"Random"));
        indexOfRecentRandom = lastDrafted.size()-1;

        Log.d(TAG,"Last Drafted"+lastDrafted.toString());

        this.DRAFTINGNUM = sharedPreferences.getInt("numCharacters", 8);

        //set up auto complete bar
        ArrayList<String> mNames = new ArrayList<>();
        for (int i = 0; i < fighters.size(); i++) {
            mNames.add(fighters.get(i).getName());
        }

        final ArrayAdapter<String> autoAdapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, mNames);
        AutoCompleteTextView actv = findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(autoAdapter);//setting the adapter data into the AutoCompleteTextView
        actv.setTextColor(Color.parseColor("#FF7F00"));
        actv.setOnFocusChangeListener((v, hasFocus) -> auto = hasFocus);
        actv.setOnItemClickListener((parent, arg1, pos, id) -> confirmDraft((Fighter)parent.getItemAtPosition(pos)));

        initFighters();
        Log.d(TAG,"FIRST CHARACTER:"+fighters.get(0));
        initRecyclerView();

        switch (GAMEMODE) {
            case "Draft As You Go":
                Intent intent = this.getIntent();
                PREVACTIVITY = intent.getStringExtra("prevActivity");
                if(spDrafted.get(0).size() != 0) {
                    draftOrder = intent.getIntegerArrayListExtra("draftOrder");
                }if(spDrafted.get(0).size() == 0 || draftOrder == null){
                    if (NUMTEAMS == 2) {
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1));
                    } else if (NUMTEAMS == 3) {
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1, 2, 2));
                    } else if (NUMTEAMS == 4) {
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3));
                    }
                }
                assert draftOrder != null;
                spTeam = draftOrder.get(0);
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                switch (draftOrder.get(0)){
                    case 0:
                        window.setStatusBarColor(getResources().getColor(R.color.darker_red,null));
                        break;
                    case 1:
                        window.setStatusBarColor(getResources().getColor(R.color.darker_blue,null));
                        break;
                    case 2:
                        window.setStatusBarColor(getResources().getColor(R.color.darker_green,null));
                        break;
                    case 3:
                        window.setStatusBarColor(getResources().getColor(R.color.darker_yellow,null));
                        break;
                }
                DRAFTINGNUM = draftOrder.size();
                break;
            case "Locked In Random":
                ArrayList<Fighter> randomFighters = new ArrayList<>(fighters);
                Collections.shuffle(randomFighters);
                for (int i = 0; i < NUMTEAMS; i++) {
                    for (int j = 0; j < DRAFTINGNUM; j++) {
                        Fighter tempFighter = randomFighters.get(0);
                        randomFighters.remove(0);
                        spDrafted.get(i).add(tempFighter);
                    }
                }
                Log.d(TAG, "" + spDrafted);
                goToGameplay();
                break;
            case "Columns":
                for(int i = 0; i < 12; i++){
                    spDrafted.get(0).add(fighters.get(i));
                    spDrafted.get(0).add(fighters.get(12+i));
                    spDrafted.get(0).add(fighters.get(24+i));
                    spDrafted.get(0).add(fighters.get(36+i));

                    spDrafted.get(1).add(fighters.get(11-i));
                    spDrafted.get(1).add(fighters.get(23-i));
                    spDrafted.get(1).add(fighters.get(35-i));
                    spDrafted.get(1).add(fighters.get(47-i));
                }
                for(int i = 0; i < 12; i+=2){
                    spDrafted.get(0).add(fighters.get(59-i));
                    spDrafted.get(0).add(fighters.get(58-i));
                    spDrafted.get(0).add(fighters.get(71-i));
                    spDrafted.get(0).add(fighters.get(70-i));

                    spDrafted.get(1).add(fighters.get(48+i));
                    spDrafted.get(1).add(fighters.get(49+i));
                    spDrafted.get(1).add(fighters.get(60+i));
                    spDrafted.get(1).add(fighters.get(61+i));
                }

                for(int i = 0; i < 9; i++){
                    spDrafted.get(0).add(fighters.get(72+i));
                    spDrafted.get(1).add(fighters.get(81-i));
                }

                Log.d(TAG,spDrafted.get(0)+" team 0");
                Log.d(TAG,spDrafted.get(1)+" team 1");
                goToGameplay();
                break;
        }

        fullFighters.addAll(fighters);
        alphaSortedFighters.addAll(fighters);
        Collections.sort(alphaSortedFighters);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.change_view, menu);
        inflater.inflate(R.menu.change_sort, menu);
        inflater.inflate(R.menu.visibility, menu);
        this.menu = menu;

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");

        if (item.getItemId() == R.id.action_eye) {
            Log.d(TAG, "eye clicked");
            eye = !eye;
            if (eye) {
                menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.eye_open));
            } else {
                menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.eye_close));
            }
            changeVisibilityOfDrafted();
            return true;

        } else if (item.getItemId() == R.id.action_sort) {
            Log.d(TAG, "sort clicked");
            alphaSort = !alphaSort;
            if (alphaSort) {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_1to9));
            } else {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_atoz));
            }
            changeOrderOfDrafted();
            return true;

        } else if (item.getItemId() == R.id.action_view) {
            Log.d(TAG, "view clicked");
            listView = !listView;
            if (listView) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_list));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            } else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_grid));
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            }
            mRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void changeVisibilityOfDrafted() {
        Log.d(TAG,"changeVisibilityOfDrafted");
        Parcelable recyclerViewState;
        recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();
        if (eye) {
            fighters.clear();
            if (alphaSort && alphaSortedFighters != null) changeVisibilityHelper(alphaSortedFighters);
            else if (fullFighters != null) changeVisibilityHelper(fullFighters);
        } else eyeClosed();
        mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    void changeVisibilityHelper(ArrayList<Fighter> arrayList){
        fighters.addAll(arrayList);
        ArrayList<Integer> draftedPos = new ArrayList<>();
        for (int i = 0; i < NUMTEAMS; i++)
            for (Fighter fighter : spDrafted.get(i)) {
                arrayList.indexOf(fighter);
                draftedPos.add(arrayList.indexOf(fighter));
            }
        Collections.sort(draftedPos);
        for (int pos : draftedPos)
            adapter.notifyItemInserted(pos);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void changeOrderOfDrafted() {
        Log.d(TAG, "changeOrderOfDrafted");
        if (alphaSort) {
            fighters.clear();
            fighters.addAll(alphaSortedFighters);
            if (!eye) {
                eyeClosed();
            }
        } else {
            fighters.clear();
            fighters.addAll(fullFighters);
            if (!eye) {
                eyeClosed();
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void eyeClosed() {
        Log.d(TAG,"eyeClosed");
        //resetPositions must be called multiple times
        for (int i = 0; i < NUMTEAMS; i++) {
            Log.d(TAG,"EyeClosed spDrafted:"+spDrafted.toString());
            for (Fighter fighter : spDrafted.get(i)) {
                int pos = fighters.indexOf(fighter);
                fighters.remove(fighter);
                adapter.notifyItemRemoved(pos);

            }
        }
        Log.d(TAG,"EyeClosed fighters:"+fighters);
    }

    void nextTeam() {
        Log.d(TAG,"nextTeam");
        if (draftOrderIndex < draftOrder.size() - 1) {
            draftOrderIndex++;
            spTeam = draftOrder.get(draftOrderIndex);
        } else {
            spTeam = 0;
            draftOrderIndex = 0;
        }
        changeBarColor();
    }

    void prevTeam() {
        Log.d(TAG,"prevTeam");
        if (draftOrderIndex > 0) {
            draftOrderIndex -= 1;
        } else {
            draftOrderIndex = draftOrder.size() - 1;
        }
        spTeam = draftOrder.get(draftOrderIndex);
        changeBarColor();
    }


    void changeBarColor() {
        Log.d(TAG,"changeBarColor");
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        switch(spTeam){
        case 0:
            window.setStatusBarColor(getResources().getColor(R.color.darker_red,null));
            break;
        case 1:
            window.setStatusBarColor(getResources().getColor(R.color.darker_blue,null));
            break;
        case 2:
            window.setStatusBarColor(getResources().getColor(R.color.darker_green,null));
            break;
        case 3:
            window.setStatusBarColor(getResources().getColor(R.color.darker_yellow,null));
            break;
        }

    }

    int getPrevTeam(){
        Log.d(TAG,"getPrevTeam");
        if(draftOrderIndex > 0){
            return draftOrder.get(draftOrderIndex -1);
        }
        else{
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void confirmDraft(Fighter fighter) {
        Log.d(TAG,"confirmDraft");
        final Fighter temp = fighter;
        //undo
        if(lastDrafted.size() != 0 && lastDrafted.get(lastDrafted.size()-1) == fighter){
            Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();
            Fighter removedFighter = spDrafted.get(getPrevTeam()).get(spDrafted.get(getPrevTeam()).size()-1);
            spDrafted.get(getPrevTeam()).remove(spDrafted.get(getPrevTeam()).size()-1);
            adapter.notifyItemChanged(fighters.indexOf(removedFighter));
            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            lastDrafted.remove(lastDrafted.size()-1);
            prevTeam();
        }else if(notDrafted(fighter)){
            AlertDialog.Builder builder = new AlertDialog.Builder(DraftActivity.this);
            builder.setCancelable(true);
            builder.setIcon(fighter.getImageId());
            builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>Are you sure you want to draft <b>\n" + fighter.getName() + "</b>?</font>",63));

            builder.setPositiveButton("Accept", (dialog, which) -> {
                editText.setText("");
                draftCharacter(temp);
                dialog.dismiss();
            });
            builder.setNegativeButton("Reject", (dialog, which) -> dialog.dismiss());

            AlertDialog alert = builder.create();
            alert.show();
            Button confirmButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            Button rejectButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            switch (spTeam){
                case 0:
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_red);
                    confirmButton.setTextColor(getResources().getColor(R.color.white,null));
                    rejectButton.setTextColor(getResources().getColor(R.color.white,null));
                    break;
                case 1:
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_blue);
                    confirmButton.setTextColor(getResources().getColor(R.color.white,null));
                    rejectButton.setTextColor(getResources().getColor(R.color.white,null));
                    break;
                case 2:
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_green);
                    confirmButton.setTextColor(getResources().getColor(R.color.white,null));
                    rejectButton.setTextColor(getResources().getColor(R.color.white,null));
                    break;
                case 3:
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_yellow);
                    confirmButton.setTextColor(getResources().getColor(R.color.white,null));
                    rejectButton.setTextColor(getResources().getColor(R.color.white,null));

                    break;
            }
        }
    }

    private boolean notDrafted(Fighter someFighter) {
        for(int i = 0; i < NUMTEAMS; i++){
            for(Fighter fighter:lastDrafted){
                if(someFighter.getName().equals(fighter.getName())){
                    return false;
                }
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void draftCharacter(Fighter fighter) {
        Log.d(TAG,"draftCharacter "+fighter.getName());
        Log.d(TAG,"draftOrder: "+draftOrder.toString()+" draftOrderIndex:"+ draftOrderIndex);
        hideKeyboard(this);
        lastDrafted.add(fighter);

        spDrafted.get(spTeam).add(fighter);
        if(!eye){
            adapter.notifyItemRemoved(fighters.indexOf(fighter));
            fighters.remove(fighter);
        }
        if(eye)
            adapter.notifyItemChanged(fighters.indexOf(fighter));
        nextTeam();

        Log.d(TAG,spDrafted+" "+DRAFTINGNUM);
        //if all teams are the right length go to gameplay
        boolean flag = true;
        if(GAMEMODE.equals("Draft As You Go")){
            flag = lastDrafted.size()-indexOfRecentRandom-1 == draftOrder.size();
        }else {
            for (int i = 0; i < spDrafted.size(); i++) {
                if (spDrafted.get(i).size() < DRAFTINGNUM) {
                    Log.d(TAG, "Team " + i);
                    flag = false;
                    break;
                }
            }
        }
        if(flag){
            goToGameplay();
        }
    }

    Fighter getFighter(String name) {
        Log.d(TAG,"getFighter "+name);
        for(Fighter someFighter:fighters){
            if(someFighter.getName().equals(name)) {
                return someFighter;
            }
        }
        return null;
    }

    private void goToGameplay() {
        Log.d(TAG,"Going to Gameplay");

        ArrayList<Fighter> remainders = ((ManagingApplication) getApplicationContext()).remainders;
        if(remainders == null) {
            remainders = new ArrayList<>(fighters);
        }

        switch(NUMTEAMS){
            case 4:
                remainders.removeAll(spDrafted.get(3));
            case 3:
                remainders.removeAll(spDrafted.get(2));
            case 2:
                remainders.removeAll(spDrafted.get(0));
                remainders.removeAll(spDrafted.get(1));
        }

        if(PREVACTIVITY.equals("MainActivity")) {
            Intent intent = new Intent(this, GamePlayActivity.class);
            startActivity(intent);
        }
        else{
            onBackPressed();
        }
    }


    private void initFighters(){
        if(((ManagingApplication) getApplicationContext()).allFighters == null) {
            ((ManagingApplication) getApplicationContext()).allFighters = new ArrayList<>();
            ArrayList<Fighter> allFighters = ((ManagingApplication) getApplicationContext()).allFighters;

            allFighters.add(new Fighter(R.drawable.img_01_mario, "Mario"));
            allFighters.add(new Fighter(R.drawable.img_02_dk, "Donkey Kong"));
            allFighters.add(new Fighter(R.drawable.img_03_link, "Link"));
            allFighters.add(new Fighter(R.drawable.img_04_samus, "Samus"));
            allFighters.add(new Fighter(R.drawable.img_05_dark_samus, "Dark Samus"));
            allFighters.add(new Fighter(R.drawable.img_06_yoshi, "Yoshi"));
            allFighters.add(new Fighter(R.drawable.img_07_kirby, "Kirby"));
            allFighters.add(new Fighter(R.drawable.img_08_fox, "Fox"));
            allFighters.add(new Fighter(R.drawable.img_09_pikachu, "Pikachu"));
            allFighters.add(new Fighter(R.drawable.img_10_luigi, "Luigi"));
            allFighters.add(new Fighter(R.drawable.img_11_ness, "Ness"));
            allFighters.add(new Fighter(R.drawable.img_12_captain, "Captain Falcon"));
            allFighters.add(new Fighter(R.drawable.img_13_jiggly, "Jigglypuff"));
            allFighters.add(new Fighter(R.drawable.img_14_peach, "Peach"));
            allFighters.add(new Fighter(R.drawable.img_15_daisy, "Daisy"));
            allFighters.add(new Fighter(R.drawable.img_16_bowser, "Bowser"));
            allFighters.add(new Fighter(R.drawable.img_17_ice, "Ice Climbers"));
            allFighters.add(new Fighter(R.drawable.img_18_sheik, "Sheik"));
            allFighters.add(new Fighter(R.drawable.img_19_zelda, "Zelda"));
            allFighters.add(new Fighter(R.drawable.img_20_dr, "Dr. Mario"));
            allFighters.add(new Fighter(R.drawable.img_21_pichu, "Pichu"));
            allFighters.add(new Fighter(R.drawable.img_22_falco, "Falco"));
            allFighters.add(new Fighter(R.drawable.img_23_marth, "Marth"));
            allFighters.add(new Fighter(R.drawable.img_24_lucina, "Lucina"));
            allFighters.add(new Fighter(R.drawable.img_25_young, "Young Link"));
            allFighters.add(new Fighter(R.drawable.img_26_ganon, "Ganondorf"));
            allFighters.add(new Fighter(R.drawable.img_27_mewtwo, "Mewtwo"));
            allFighters.add(new Fighter(R.drawable.img_28_roy, "Roy"));
            allFighters.add(new Fighter(R.drawable.img_29_chrom, "Chrom"));
            allFighters.add(new Fighter(R.drawable.img_30_game, "Game and Watch"));
            allFighters.add(new Fighter(R.drawable.img_31_meta, "Metaknight"));
            allFighters.add(new Fighter(R.drawable.img_32_pit, "Pit"));
            allFighters.add(new Fighter(R.drawable.img_33_dark_pit, "Dark Pit"));
            allFighters.add(new Fighter(R.drawable.img_34_zero, "Zero Suit Samus"));
            allFighters.add(new Fighter(R.drawable.img_35_wario, "Wario"));
            allFighters.add(new Fighter(R.drawable.img_36_snake, "Snake"));
            allFighters.add(new Fighter(R.drawable.img_37_ike, "Ike"));
            allFighters.add(new Fighter(R.drawable.img_38_trainer, "Pokemon Trainer"));
            allFighters.add(new Fighter(R.drawable.img_39_diddy, "Diddy Kong"));
            allFighters.add(new Fighter(R.drawable.img_40_lucas, "Lucas"));
            allFighters.add(new Fighter(R.drawable.img_41_sonic, "Sonic"));
            allFighters.add(new Fighter(R.drawable.img_42_king, "King Dedede"));
            allFighters.add(new Fighter(R.drawable.img_43_olimar, "Olimar"));
            allFighters.add(new Fighter(R.drawable.img_44_lucario, "Lucario"));
            allFighters.add(new Fighter(R.drawable.img_45_rob, "R.O.B."));
            allFighters.add(new Fighter(R.drawable.img_46_toon, "Toon Link"));
            allFighters.add(new Fighter(R.drawable.img_47_wolf, "Wolf"));
            allFighters.add(new Fighter(R.drawable.img_48_villager, "Villager"));
            allFighters.add(new Fighter(R.drawable.img_49_mega, "Megaman"));
            allFighters.add(new Fighter(R.drawable.img_50_wii, "Wii Fit Trainer"));
            allFighters.add(new Fighter(R.drawable.img_51_rosalina, "Rosalina"));
            allFighters.add(new Fighter(R.drawable.img_52_mac, "Little Mac"));
            allFighters.add(new Fighter(R.drawable.img_53_greninja, "Greninja"));
            allFighters.add(new Fighter(R.drawable.img_54_palutena, "Palutena"));
            allFighters.add(new Fighter(R.drawable.img_55_pacman, "Pacman"));
            allFighters.add(new Fighter(R.drawable.img_56_robin, "Robin"));
            allFighters.add(new Fighter(R.drawable.img_57_shulk, "Shulk"));
            allFighters.add(new Fighter(R.drawable.img_58_bowserjr, "Bowser J"));
            allFighters.add(new Fighter(R.drawable.img_59_duck, "Duck Hunt"));
            allFighters.add(new Fighter(R.drawable.img_60_ryu, "Ryu"));
            allFighters.add(new Fighter(R.drawable.img_61_ken, "Ken"));
            allFighters.add(new Fighter(R.drawable.img_62_cloud, "Cloud"));
            allFighters.add(new Fighter(R.drawable.img_63_corrin, "Corrin"));
            allFighters.add(new Fighter(R.drawable.img_64_bayonetta, "Bayonetta"));
            allFighters.add(new Fighter(R.drawable.img_65_ink, "Inkling"));
            allFighters.add(new Fighter(R.drawable.img_66_ridley, "Ridley"));
            allFighters.add(new Fighter(R.drawable.img_67_simon, "Simon"));
            allFighters.add(new Fighter(R.drawable.img_68_richter, "Richter"));
            allFighters.add(new Fighter(R.drawable.img_69_rool, "King K. Rool"));
            allFighters.add(new Fighter(R.drawable.img_70_isabelle, "Isabelle"));
            allFighters.add(new Fighter(R.drawable.img_71_incin, "Incineroar"));
            allFighters.add(new Fighter(R.drawable.img_72_flower, "Piranha Plant"));
            allFighters.add(new Fighter(R.drawable.img_73_joker, "Joker"));
            allFighters.add(new Fighter(R.drawable.img_74_hero, "Hero"));
            allFighters.add(new Fighter(R.drawable.img_75_banjo, "Banjo & Kazooie"));
            allFighters.add(new Fighter(R.drawable.img_76_terry, "Terry"));
            allFighters.add(new Fighter(R.drawable.img_77_byleth, "Byleth"));
            allFighters.add(new Fighter(R.drawable.img_78_minmin, "Min Min"));
            allFighters.add(new Fighter(R.drawable.img_79_steve, "Steve"));
            allFighters.add(new Fighter(R.drawable.img_80_sephiroth, "Sephiroth"));
            allFighters.add(new Fighter(R.drawable.img_81_pyra, "Pyra and Mythra"));
            allFighters.add(new Fighter(R.drawable.img_82_kazuya, "Kazuya"));
        }
        else fighters.clear();
        fighters.addAll(((ManagingApplication) getApplicationContext()).allFighters);
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recyclerview.");

        mRecyclerView = findViewById(R.id.draftRecyclerView);
        adapter = new DraftRecyclerAdapter(this, fighters, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);

        ArrayList<Fighter> autoCompleteFighters = new ArrayList<>(fighters);

        editText = findViewById(R.id.autoCompleteTextView);
        AutoCompleteCharacterAdapter autoAdapter = new AutoCompleteCharacterAdapter(this, autoCompleteFighters);
        editText.setAdapter(autoAdapter);
        adapter.notifyDataSetChanged();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed(){
        if(lastDrafted.size() > 0 && PREVACTIVITY.equals("MainActivity")){
            Log.d(TAG,"Undoing "+lastDrafted.get(lastDrafted.size()-1));
            confirmDraft(lastDrafted.get(lastDrafted.size()-1));
        }
        else{
            Log.d(TAG,"Back to GamePlay?");
            super.onBackPressed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onFighterClick(int position) {
        Log.d(TAG,"On Fighter Click: "+ position);
        confirmDraft(fighters.get(position));
    }

    boolean getListView(){return this.listView;}
}

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
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.IntStream;

public class DraftActivity extends AppCompatActivity implements DraftRecyclerAdapter.onFighterListener {
    private static final String TAG = "Draft Activity";
    private int numTeams;
    private int numToDraft;
    private String gameMode;
    private String prevActivity;

    private final ArrayList<Fighter> fighters = new ArrayList<>(); //current visible fighters for the recycler view
    private final ArrayList<Fighter> autoCompleteFighters = new ArrayList<>();
    private final ArrayList<Fighter> idSortedFighters = new ArrayList<>(); //backup of fighters sorted by date (default sort)
    private final ArrayList<Fighter> alphaSortedFighters = new ArrayList<>(); // //backup of fighters sorted by alphabet (default sort)

    private final ArrayList<ArrayList<Fighter>> spDrafted = new ArrayList<>(); //Fighters drafted
    private ArrayList<Integer> draftOrder; //order that teams will be drafted
    private ArrayList<Fighter> lastDrafted;
    private int draftOrderIndex;

    private Menu menu;
    private final Context mContext = this;
    private RecyclerView mRecyclerView;
    private DraftRecyclerAdapter adapter;

    private boolean auto;
    private AutoCompleteTextView actv;
    private AutoCompleteCharacterAdapter autoFighterAdapter;

    private boolean showDrafted = true;
    private boolean alphaSort = false;
    private boolean listView = false;
    private int indexOfRecentRandom;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        //Grab variables from settings
        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        gameMode = sharedPreferences.getString("gameMode", "Draft As You Go");
        numTeams = sharedPreferences.getInt("numTeams", 4);
        lastDrafted = new ArrayList<>();
        this.numToDraft = sharedPreferences.getInt("numCharacters", 8);

        //Set up the teams if they are not saved otherwise grab them from the Managing Application
        switch(numTeams){
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

        initFighters();
        initRecyclerView();
        initGamemodes();
        initAutoComplete();

        //Define sorted fighter arraylists
        idSortedFighters.addAll(fighters);
        alphaSortedFighters.addAll(fighters);
        Collections.sort(alphaSortedFighters);
    }

    //set up auto complete bar
    private void initAutoComplete() {
        autoCompleteFighters.addAll(fighters);
        actv = findViewById(R.id.autoCompleteTextView);
        actv.setThreshold(1);
        actv.setTextColor(Color.parseColor("#FF7F00"));
        actv.setOnFocusChangeListener(this::autoCompleteFocusChanged);
        actv.setOnItemClickListener((parent, arg1, pos, id) -> confirmDraft((Fighter) parent.getItemAtPosition(pos)));
        autoFighterAdapter = new AutoCompleteCharacterAdapter(this, autoCompleteFighters);
        actv.setAdapter(autoFighterAdapter);
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
        mRecyclerView = findViewById(R.id.draftRecyclerView);
        adapter = new DraftRecyclerAdapter(this, fighters, this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6)); //should be 3
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true);
    }

    //Different setups for different game modes
    private void initGamemodes() {
        Intent intent = this.getIntent();
        prevActivity = intent.getStringExtra("prevActivity");
        switch (gameMode) {
            case "Draft As You Go":
                if(spDrafted.get(0).size() != 0) {
                    draftOrder = intent.getIntegerArrayListExtra("draftOrder");
                }
                if(spDrafted.get(0).size() == 0 || draftOrder == null){
                    if (numTeams == 2)
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1));
                    else if (numTeams == 3)
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1, 2, 2));
                    else if (numTeams == 4)
                        draftOrder = new ArrayList<>(Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3));

                }
                draftOrderIndex = 0;

                //Set window bar colour to the drafting team
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                switch (draftOrder.get(0)){
                    case 0:
                        window.setStatusBarColor(getResources().getColor(R.color.red,null));
                        break;
                    case 1:
                        window.setStatusBarColor(getResources().getColor(R.color.blue,null));
                        break;
                    case 2:
                        window.setStatusBarColor(getResources().getColor(R.color.green,null));
                        break;
                    case 3:
                        window.setStatusBarColor(getResources().getColor(R.color.yellow,null));
                        break;
                }
                numToDraft = draftOrder.size();
                break;

            //draft all fighters before starting the game.
            case "Draft Up Front":
                if (numTeams == 2)
                    draftOrder = new ArrayList<>(Arrays.asList(0, 1));
                else if (numTeams == 3)
                    draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 2, 1, 0));
                else if (numTeams == 4)
                    draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 3, 2, 1, 0));
                break;

            //Randomly select fighters to add to each team.
            case "Locked In Random":
                ArrayList<Fighter> randomFighters = new ArrayList<>(fighters);
                Collections.shuffle(randomFighters);
                for (int i = 0; i < numTeams; i++) {
                    for (int j = 0; j < numToDraft; j++) {
                        Fighter tempFighter = randomFighters.get(0);
                        randomFighters.remove(0);
                        spDrafted.get(i).add(tempFighter);
                    }
                }
                goToGameplay();
                break;

            //hard coded values for echos visible, no mii fighters, kazuya patch
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
                goToGameplay();
                break;
        }
    }

    private void autoCompleteFocusChanged(View v, Boolean hasFocus) {
        auto = hasFocus;
        if(auto) {
            mRecyclerView.setAlpha((float) 0.4);
        }
        else {
            mRecyclerView.setAlpha(1);
            actv.setText("");
            actv.dismissDropDown();
            hideKeyboard(this);
        }
        autoFighterAdapter.notifyDataSetChanged();
        Log.d(TAG,"auto:"+auto);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
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
        //the eye button hides all drafted fighters
        if (item.getItemId() == R.id.action_eye) {
            showDrafted = !showDrafted;
            if (showDrafted)
                menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.eye_open));
            else
                menu.getItem(2).setIcon(ContextCompat.getDrawable(this, R.drawable.eye_close));

            changeVisibilityOfDrafted();
            return true;
        //the sort button switches between sort by order and sort by release date. The symbol is what will be switched to
        } else if (item.getItemId() == R.id.action_sort) {
            alphaSort = !alphaSort;
            if (alphaSort)
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_1to9));
            else
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_atoz));
            changeOrderOfDrafted();
            return true;

            //switch between grid view and list view
        } else if (item.getItemId() == R.id.action_view) {
            listView = !listView;
            if (listView) {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_grid));
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            } else {
                menu.getItem(0).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_list));
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6)); //should be 3
            }
            mRecyclerView.setAdapter(adapter);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeVisibilityOfDrafted() {
        Parcelable recyclerViewState;
        recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();
        if (showDrafted) {
            fighters.clear();
            if (alphaSort && alphaSortedFighters != null) changeVisibilityHelper(alphaSortedFighters);
            else if (idSortedFighters != null) changeVisibilityHelper(idSortedFighters);
        } else eyeClosed();
        mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    void changeVisibilityHelper(ArrayList<Fighter> arrayList){
        fighters.addAll(arrayList);
        ArrayList<Integer> draftedPos = new ArrayList<>();
        for (int i = 0; i < numTeams; i++)
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
            if (!showDrafted) {
                eyeClosed();
            }
        } else {
            fighters.clear();
            fighters.addAll(idSortedFighters);
            if (!showDrafted) {
                eyeClosed();
            }
        }
        adapter.notifyItemRangeChanged(0,15);
    }

    //remove all drafted fighters from fighters and notify the adapter
    private void eyeClosed() {
        for (int i = 0; i < numTeams; i++) {
            for (Fighter fighter : spDrafted.get(i)) {
                int pos = fighters.indexOf(fighter);
                fighters.remove(fighter);
                adapter.notifyItemRemoved(pos);
            }
        }
    }

    void nextTeam() {
        if (draftOrderIndex < draftOrder.size() - 1) {
            draftOrderIndex++;
        } else {
            draftOrderIndex = 0;
        }
        changeBarColor();
    }

    void prevTeam() {
        if (draftOrderIndex > 0) {
            draftOrderIndex -= 1;
        } else {
            draftOrderIndex = draftOrder.size() - 1;
        }
        changeBarColor();
    }

    void changeBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        switch(draftOrder.get(draftOrderIndex)){
        case 0:
            window.setStatusBarColor(getResources().getColor(R.color.red,null));
            break;
        case 1:
            window.setStatusBarColor(getResources().getColor(R.color.blue,null));
            break;
        case 2:
            window.setStatusBarColor(getResources().getColor(R.color.green,null));
            break;
        case 3:
            window.setStatusBarColor(getResources().getColor(R.color.yellow,null));
            break;
        }
    }

    int getPrevTeam(){
        if(draftOrderIndex > 0){
            return draftOrder.get(draftOrderIndex -1);
        }
        else{
            return draftOrder.get(0);
        }
    }

    public void confirmDraft(Fighter fighter) {
        final Fighter temp = fighter;

        if(notDrafted(fighter)){
            AlertDialog.Builder builder = new AlertDialog.Builder(DraftActivity.this);
            builder.setCancelable(true);
            builder.setIcon(fighter.getImageId());
            builder.setTitle(Html.fromHtml("<font color='#FFFFFF'>Are you sure you want to draft <b>\n" + fighter.getName() + "</b>?</font>",63));
            builder.setPositiveButton("Accept", (dialog, which) -> {
                draftFighter(temp);
                dialog.dismiss();
                actv.setFocusable(false);
                actv.setFocusableInTouchMode(true);
                actv.setCursorVisible(false);
                actv.clearFocus();
            });
            builder.setNegativeButton("Reject", (dialog, which) -> {
                dialog.dismiss();
                actv.setFocusable(false);
                actv.setFocusableInTouchMode(true);
                actv.setCursorVisible(false);
                actv.clearFocus();
            });
            builder.setOnDismissListener(dialog -> actv.setText(""));
            AlertDialog alert = builder.create();
            alert.show();
            Button confirmButton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            Button rejectButton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            switch (draftOrder.get(draftOrderIndex)){
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
        }else if(lastDrafted.size() != 0 && lastDrafted.get(lastDrafted.size()-1) == fighter){
            //Undo draft if it is the most recent
            Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();
            Fighter removedFighter = spDrafted.get(getPrevTeam()).get(spDrafted.get(getPrevTeam()).size()-1);
            spDrafted.get(getPrevTeam()).remove(spDrafted.get(getPrevTeam()).size()-1);
            adapter.notifyItemChanged(fighters.indexOf(removedFighter));
            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            lastDrafted.remove(lastDrafted.size()-1);
            prevTeam();
        }
    }

    //check if the fighter has been drafted
    private boolean notDrafted(Fighter someFighter) {
        for(Fighter fighter:lastDrafted){
            if(someFighter.getName().equals(fighter.getName()))
                return false;
        }
        return true;
    }

    void draftFighter(Fighter fighter) {
        if(!fighter.getName().equals("Random")) {
            lastDrafted.add(fighter);
            spDrafted.get(draftOrder.get(draftOrderIndex)).add(fighter);
            if (!showDrafted) {
                adapter.notifyItemRemoved(fighters.indexOf(fighter));
                fighters.remove(fighter);
            }
            else
                adapter.notifyItemChanged(fighters.indexOf(fighter));
            nextTeam();

            //if all teams are the right length go to gameplay
            boolean flag = true;
            if (gameMode.equals("Draft As You Go"))
                flag = lastDrafted.size() - indexOfRecentRandom - 1 == draftOrder.size();
            else
                for (int i = 0; i < spDrafted.size(); i++)
                    if (spDrafted.get(i).size() < numToDraft) {
                        flag = false;
                        break;
                    }
            if (flag)
                goToGameplay();
        }
    }

    private void goToGameplay() {
        ArrayList<Fighter> fightersNotDrafted = ((ManagingApplication) getApplicationContext()).remainders;
        if(fightersNotDrafted == null)
            fightersNotDrafted = new ArrayList<>(fighters);

        switch(numTeams){
            case 4:
                fightersNotDrafted.removeAll(spDrafted.get(3));
            case 3:
                fightersNotDrafted.removeAll(spDrafted.get(2));
            case 2:
                fightersNotDrafted.removeAll(spDrafted.get(0));
                fightersNotDrafted.removeAll(spDrafted.get(1));
        }
        Team t0 = ((ManagingApplication) getApplicationContext()).team0;
        if (t0 != null && IntStream.of(t0.getPointers()).anyMatch(x -> x == -1))
            t0.setPointers();

        Team t1 = ((ManagingApplication) getApplicationContext()).team0;
        if (t1 != null && IntStream.of(t1.getPointers()).anyMatch(x -> x == -1))
            t1.setPointers();

        Team t2 = ((ManagingApplication) getApplicationContext()).team0;
        if (t2 != null && IntStream.of(t2.getPointers()).anyMatch(x -> x == -1))
            t2.setPointers();

        Team t3 = ((ManagingApplication) getApplicationContext()).team0;
        if (t3 != null && IntStream.of(t3.getPointers()).anyMatch(x -> x == -1))
            t3.setPointers();

        //check if we can go back to gameplay activity or if we have to call a new intent
        if(prevActivity != null && prevActivity.equals("MainActivity")) {
            Intent intent = new Intent(this, GamePlayActivity.class);
            startActivity(intent);
        }
        else{
            Log.d(TAG,"goToGameplay");
            super.onBackPressed();
        }
    }

    //If keyboard is open close keyboard.
    public void hideKeyboard(Activity activity) {
        if(this.getWindowManager().getCurrentWindowMetrics().getWindowInsets().isVisible(WindowInsets.Type.ime())) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
    }

    //Undo draft
    @Override
    public void onBackPressed(){
        if(lastDrafted.size() > 0 && !lastDrafted.get(lastDrafted.size() - 1).getName().equals("Random"))
            confirmDraft(lastDrafted.get(lastDrafted.size()-1));
        else if(prevActivity.equals("MainActivity"))
            super.onBackPressed();
    }

    @Override
    public void onFighterClick(int position) {
        if(!auto)
            confirmDraft(fighters.get(position));
        else
            actv.clearFocus();
    }

    boolean getListView(){return this.listView;}
}


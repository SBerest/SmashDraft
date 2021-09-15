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

    private ArrayList<Fighter> fighters = new ArrayList<>(); //all fighter_cell
    private final ArrayList<Fighter> fullFighters = new ArrayList<>();
    private final ArrayList<Fighter> alphaSortedFighters = new ArrayList<>();

    private ArrayList<ArrayList<Fighter>> spDrafted = new ArrayList<>();

    RecyclerView mRecyclerView;
    DraftRecyclerAdapter adapter;

    private Menu menu;

    boolean eye = true;
    boolean alphaSort = false;
    final Context mContext = this;
    private int DRAFTINGNUM;
    boolean auto;

    int spTeam = 0;
    private ArrayList<Integer> draftOrder;
    private int pointer = 0;
    boolean listView = true;
    ArrayList<Fighter> lastDrafted;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft);

        SharedPreferences sharedPreferences = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String GAMEMODE = sharedPreferences.getString("gameMode", "");
        NUMTEAMS = sharedPreferences.getInt("numTeams", 4);
        if (NUMTEAMS == 2) {
            draftOrder = new ArrayList<>(Arrays.asList(0, 1));
        } else if (NUMTEAMS == 3) {
            draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 2, 1, 0));
        } else if (NUMTEAMS == 4) {
            draftOrder = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 3, 2, 1, 0));
        }

        for (int i = 0; i < NUMTEAMS; i++)
            spDrafted.add(new ArrayList<>());

        this.DRAFTINGNUM = sharedPreferences.getInt("numCharacters", 8);
        Log.d(TAG, "Drafting Num: " + DRAFTINGNUM);

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
        actv.setOnItemClickListener((parent, arg1, pos, id) -> confirmDraft(getFighter(parent.getItemAtPosition(pos).toString())));

        lastDrafted = new ArrayList<>();

        initImageBitmaps();
        initRecyclerView();
        resetPositions();

        //TODO implement Draft as you go, differentiate in code between that and Draft up front (current code)
        //TODO implement Columns
        switch (GAMEMODE) {
            case "Locked In Random":
                ArrayList<Fighter> randomFighters = new ArrayList<>(fighters);
                Collections.shuffle(randomFighters);
                for (int i = 0; i < NUMTEAMS; i++) {
                    for (int j = 0; j < DRAFTINGNUM; j++) {
                        Fighter tempFighter = randomFighters.get(0);
                        randomFighters.remove(tempFighter);
                        spDrafted.get(i).add(tempFighter);
                    }
                }
                Log.d(TAG, "" + spDrafted);
                goToGameplay();
                break;
            case "Columns":

                break;
        }

        fullFighters.addAll(fighters);
        alphaSortedFighters.addAll(fighters);
        Collections.sort(alphaSortedFighters);
        resetPositions(alphaSortedFighters);
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
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_atoz));
            } else {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.symb_1to9));
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
        Parcelable recyclerViewState;
        recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();
        if (eye) {
            fighters.clear();
            ArrayList<Fighter> sortedFighter = new ArrayList<>();

            if (alphaSort && alphaSortedFighters != null) {
                fighters.addAll(alphaSortedFighters);

                for (int i = 0; i < NUMTEAMS; i++) {
                    for (Fighter fighter : spDrafted.get(i)) {
                        sortedFighter.add(alphaSortedFighters.get(alphaSortedFighters.indexOf(fighter)));
                    }
                }

                resetPositions(alphaSortedFighters);
                sortedFighter.sort(new FighterPositionComparator());
                for (Fighter fighter : sortedFighter) {
                    Log.d(TAG, fighter.getName() + " " + fighter.getPosition());
                    adapter.notifyItemInserted(fighter.getPosition());
                }
                resetPositions();
            } else if (fullFighters != null) {
                fighters.addAll(fullFighters);

                for (int i = 0; i < NUMTEAMS; i++) {
                    for (Fighter fighter : spDrafted.get(i)) {
                        sortedFighter.add(fullFighters.get(fullFighters.indexOf(fighter)));
                    }
                }

                resetPositions();
                sortedFighter.sort(new FighterPositionComparator());
                for (Fighter fighter : sortedFighter) {
                    Log.d(TAG, fighter.getName() + " " + fighter.getPosition());
                    adapter.notifyItemInserted(fighter.getPosition());
                }
                resetPositions();
            }

        } else {
            eyeClosed();
        }

        resetPositions();
        mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
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
        resetPositions();
    }

    private void eyeClosed() {
        //resetPositions must be called multiple times
        for (int i = 0; i < NUMTEAMS; i++) {
            for (Fighter fighter : spDrafted.get(i)) {
                fighters.remove(fighter);
                adapter.notifyItemRemoved(fighter.getPosition());
                resetPositions();
            }
        }
    }

    int draftedTeam(Fighter some_fighter) {
        for (int i = 0; i < NUMTEAMS; i++) {
            if (spDrafted.get(i).contains(some_fighter))
                return i;
        }
        return -1;
    }

    void nextTeam() {
        if (pointer < draftOrder.size() - 1) {
            pointer += 1;
            spTeam = draftOrder.get(pointer);
        } else {
            spTeam = 0;
            pointer = 0;
        }
        changeBarColor();
    }

    void prevTeam() {
        if (pointer > 0) {
            pointer -= 1;
        } else {
            pointer = draftOrder.size() - 1;
        }
        spTeam = draftOrder.get(pointer);
        changeBarColor();
    }


    void changeBarColor() {
        Log.d(TAG,"Team bum "+ spTeam);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        switch(spTeam){
        case 0:
            window.setStatusBarColor(getResources().getColor(R.color.darker_blue,null));
            break;
        case 1:
            window.setStatusBarColor(getResources().getColor(R.color.darker_red,null));
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
        if(pointer > 0){
            return draftOrder.get(pointer-1);
        }
        else{
            return 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void confirmDraft(Fighter fighter) {

        final Fighter temp = fighter;

        //undo
        if(lastDrafted.size() != 0 && lastDrafted.get(lastDrafted.size()-1) == fighter){
            Parcelable recyclerViewState;
            recyclerViewState = Objects.requireNonNull(mRecyclerView.getLayoutManager()).onSaveInstanceState();

            spDrafted.get(getPrevTeam()).remove(spDrafted.get(getPrevTeam()).size()-1);
            mRecyclerView.setAdapter(null);
            mRecyclerView.setLayoutManager(null);
            mRecyclerView.setAdapter(adapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
            adapter.notifyDataSetChanged();
            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            lastDrafted.remove(lastDrafted.size()-1);
            prevTeam();
        }
        else if(!lastDrafted.contains(fighter)){

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
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_blue);
                    confirmButton.setTextColor(getResources().getColor(R.color.white,null));
                    rejectButton.setTextColor(getResources().getColor(R.color.white,null));
                    break;
                case 1:
                    alert.getWindow().setBackgroundDrawableResource(R.color.darker_red);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void draftCharacter(Fighter fighter) {
        Log.d(TAG,"draftCharacter "+fighter.getName());
        hideKeyboard(this);
        lastDrafted.add(fighter);

        spDrafted.get(spTeam).add(fighter);
        updateVisibilityOfDrafted(fighter);
        nextTeam();
        if(eye)
            adapter.notifyDataSetChanged();

        Log.d(TAG,spDrafted+" "+DRAFTINGNUM);
        //if all teams are the right length go to gameplay
        boolean flag = true;
        for(int i = 0; i < spDrafted.size(); i++){
            if(spDrafted.get(i).size() < DRAFTINGNUM){
                Log.d(TAG,"Team "+i);
                flag = false;
                break;
            }
        }
        if(flag){
            goToGameplay();
        }
    }

    private void updateVisibilityOfDrafted(Fighter fighter) {
        if(!eye){
            fighters.remove(fighter);
            adapter.notifyItemRemoved(fighter.getPosition());
        }
        resetPositions();

    }

    Fighter getFighter(String name) {
        for(Fighter someFighter:fighters){
            if(someFighter.getName().equals(name)) {
                return someFighter;
            }
        }
        return null;
    }

    private void goToGameplay() {
        Log.d(TAG,"Going to Gameplay");

        ArrayList<Fighter> toSend = new ArrayList<>(fullFighters);

        Intent intent = new Intent(this, GamePlayActivity.class);
        Bundle bundle = new Bundle();

        switch(NUMTEAMS){
            case 4:
                toSend.removeAll(spDrafted.get(3));
                bundle.putSerializable("t3", spDrafted.get(3));
                Log.d(TAG,"Team 3: "+spDrafted.get(3));
            case 3:
                toSend.removeAll(spDrafted.get(2));
                bundle.putSerializable("t2", spDrafted.get(2));
                Log.d(TAG,"Team 2: "+spDrafted.get(2));
            case 2:
                toSend.removeAll(spDrafted.get(0));
                toSend.removeAll(spDrafted.get(1));
                bundle.putSerializable("t0", spDrafted.get(0));
                Log.d(TAG,"Team 0: "+spDrafted.get(0));
                bundle.putSerializable("t1", spDrafted.get(1));
                Log.d(TAG,"Team 1: "+spDrafted.get(1));

        }

        bundle.putSerializable("remainders", toSend);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void initImageBitmaps(){
        Log.d(TAG,"initImageBitmaps: preparing bitmaps.");
        fighters.add(new Fighter( R.drawable.img_01_mario,"Mario"));

        fighters.add(new Fighter( R.drawable.img_02_dk,"Donkey Kong"));

        fighters.add(new Fighter( R.drawable.img_03_link,"Link"));

        fighters.add(new Fighter( R.drawable.img_04_samus,"Samus"));

        fighters.add(new Fighter( R.drawable.img_05_dark_samus,"Dark Samus"));

        fighters.add(new Fighter( R.drawable.img_06_yoshi,"Yoshi"));

        fighters.add(new Fighter( R.drawable.img_07_kirby,"Kirby"));

        fighters.add(new Fighter( R.drawable.img_08_fox,"Fox"));

        fighters.add(new Fighter( R.drawable.img_09_pikachu,"Pikachu"));

        fighters.add(new Fighter( R.drawable.img_10_luigi,"Luigi"));

        fighters.add(new Fighter( R.drawable.img_11_ness,"Ness"));

        fighters.add(new Fighter( R.drawable.img_12_captain,"Captain Falcon"));

        fighters.add(new Fighter( R.drawable.img_13_jiggly,"Jigglypuff"));

        fighters.add(new Fighter( R.drawable.img_14_peach,"Peach"));

        fighters.add(new Fighter( R.drawable.img_15_daisy,"Daisy"));

        fighters.add(new Fighter( R.drawable.img_16_bowser,"Bowser"));

        fighters.add(new Fighter( R.drawable.img_17_ice,"Ice Climbers"));

        fighters.add(new Fighter( R.drawable.img_18_sheik,"Sheik"));

        fighters.add(new Fighter( R.drawable.img_19_zelda,"Zelda"));

        fighters.add(new Fighter( R.drawable.img_20_dr,"Dr. Mario"));

        fighters.add(new Fighter( R.drawable.img_21_pichu,"Pichu"));

        fighters.add(new Fighter( R.drawable.img_22_falco,"Falco"));

        fighters.add(new Fighter( R.drawable.img_23_marth,"Marth"));

        fighters.add(new Fighter( R.drawable.img_24_lucina,"Lucina"));

        fighters.add(new Fighter( R.drawable.img_25_young,"Young Link"));

        fighters.add(new Fighter( R.drawable.img_26_zero,"Zero Suit Samus"));

        fighters.add(new Fighter( R.drawable.img_27_ganon,"Ganondorf"));

        fighters.add(new Fighter( R.drawable.img_28_snake,"Snake"));

        fighters.add(new Fighter( R.drawable.img_29_mewtwo,"Mewtwo"));

        fighters.add(new Fighter( R.drawable.img_30_roy,"Roy"));

        fighters.add(new Fighter( R.drawable.img_31_chrom,"Chrom"));

        fighters.add(new Fighter( R.drawable.img_32_game,"Game and Watch"));

        fighters.add(new Fighter( R.drawable.img_33_meta,"Metaknight"));

        fighters.add(new Fighter( R.drawable.img_34_pit,"Pit"));

        fighters.add(new Fighter( R.drawable.img_35_dark_pit,"Dark Pit"));

        fighters.add(new Fighter( R.drawable.img_36_wario,"Wario"));

        fighters.add(new Fighter( R.drawable.img_37_ike,"Ike"));

        fighters.add(new Fighter( R.drawable.img_38_trainer,"Pokemon Trainer"));

        fighters.add(new Fighter( R.drawable.img_39_diddy,"Diddy Kong"));

        fighters.add(new Fighter( R.drawable.img_40_lucas,"Lucas"));

        fighters.add(new Fighter( R.drawable.img_41_sonic,"Sonic"));

        fighters.add(new Fighter( R.drawable.img_42_king,"King Dedede"));

        fighters.add(new Fighter( R.drawable.img_43_olimar,"Olimar"));

        fighters.add(new Fighter( R.drawable.img_44_lucario,"Lucario"));

        fighters.add(new Fighter( R.drawable.img_45_rob,"R.O.B."));

        fighters.add(new Fighter( R.drawable.img_46_toon,"Toon Link"));

        fighters.add(new Fighter( R.drawable.img_47_wolf,"Wolf"));

        fighters.add(new Fighter( R.drawable.img_48_villager,"Villager"));

        fighters.add(new Fighter( R.drawable.img_49_mega,"Megaman"));

        fighters.add(new Fighter( R.drawable.img_50_wii,"Wii Fit Trainer"));

        fighters.add(new Fighter( R.drawable.img_51_rosalina,"Rosalina"));

        fighters.add(new Fighter( R.drawable.img_52_mac,"Little Mac"));

        fighters.add(new Fighter( R.drawable.img_53_greninja,"Greninja"));

        fighters.add(new Fighter( R.drawable.img_54_palutena,"Palutena"));

        fighters.add(new Fighter( R.drawable.img_55_pacman,"Pacman"));

        fighters.add(new Fighter( R.drawable.img_56_robin,"Robin"));

        fighters.add(new Fighter( R.drawable.img_57_shulk,"Shulk"));

        fighters.add(new Fighter( R.drawable.img_58_bowserjr,"Bowser Jr"));

        fighters.add(new Fighter( R.drawable.img_59_duck,"Duck Hunt"));

        fighters.add(new Fighter( R.drawable.img_60_ryu,"Ryu"));

        fighters.add(new Fighter( R.drawable.img_61_ken,"Ken"));

        fighters.add(new Fighter( R.drawable.img_62_cloud,"Cloud"));

        fighters.add(new Fighter( R.drawable.img_63_corrin,"Corrin"));

        fighters.add(new Fighter( R.drawable.img_64_bayonetta,"Bayonetta"));

        fighters.add(new Fighter( R.drawable.img_65_ink,"Inkling"));

        fighters.add(new Fighter( R.drawable.img_66_ridley,"Ridley"));

        fighters.add(new Fighter( R.drawable.img_67_simon,"Simon"));

        fighters.add(new Fighter( R.drawable.img_68_richter,"Richter"));

        fighters.add(new Fighter( R.drawable.img_69_rool,"King K. Rool"));

        fighters.add(new Fighter( R.drawable.img_70_isabelle,"Isabelle"));

        fighters.add(new Fighter( R.drawable.img_71_incin,"Incineroar"));

        fighters.add(new Fighter( R.drawable.img_72_flower,"Piranha Plant"));

        fighters.add(new Fighter( R.drawable.img_73_joker,"Joker"));

        fighters.add(new Fighter( R.drawable.img_74_hero,"Hero"));

        fighters.add(new Fighter( R.drawable.img_75_banjo,"Banjo & Kazooie"));

        fighters.add(new Fighter( R.drawable.img_76_terry,"Terry"));

        fighters.add(new Fighter( R.drawable.img_77_byleth,"Byleth"));

        fighters.add(new Fighter( R.drawable.img_78_minmin,"Min Min"));

        fighters.add(new Fighter( R.drawable.img_79_steve,"Steve"));

        fighters.add(new Fighter( R.drawable.img_80_sephiroth,"Sephiroth"));

        fighters.add(new Fighter( R.drawable.img_81_pyra,"Pyra and Mythra"));

        fighters.add(new Fighter( R.drawable.img_82_kazuya,"Kazuya"));

    }

    private void resetPositions() {
        for(int i = 0; i < fighters.size(); i++){
            fighters.get(i).setPosition(i);
        }
    }

    private void resetPositions(ArrayList<Fighter> AList) {
        for(int i = 0; i < AList.size(); i++){
            AList.get(i).setPosition(i);
        }
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
        if(lastDrafted.size() > 0){
            Log.d(TAG,"Undoing "+lastDrafted.get(lastDrafted.size()-1));
            confirmDraft(lastDrafted.get(lastDrafted.size()-1));
        }
        else{
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

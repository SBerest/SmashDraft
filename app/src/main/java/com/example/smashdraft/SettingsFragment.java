package com.example.smashdraft;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener{
    final String TAG = "SettingsFragment";
    HashMap<String,String> gameModeDescriptions = new HashMap<>();
    LinkedList<Preference> teamQueue;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor spEditor;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        sharedPreferences = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        spEditor = sharedPreferences.edit();

        Preference gameModePref = findPreference("gameMode");
        assert gameModePref != null;
        gameModePref.setOnPreferenceChangeListener(this);

        gameModeDescriptions.put("Draft As You Go","Alternate between drafting and playing. \nTeams: 2 - 4");
        gameModeDescriptions.put("Draft Up Front","Draft all characters before playing. \nTeams: 2 - 4");
        gameModeDescriptions.put("Locked In Random","Alternate between drafting and playing. \nTeams: 2 - 4");
        gameModeDescriptions.put("Columns","Each team goes across the characters as a column. \nTeams: 2");

        SeekBarPreference numTeamsPref = findPreference("numTeams");
        assert numTeamsPref != null;
        numTeamsPref.setOnPreferenceChangeListener(this);

        SeekBarPreference numRedPref = findPreference("numRed");
        assert numRedPref != null;
        numRedPref.setOnPreferenceChangeListener(this);
        SeekBarPreference numBluePref = findPreference("numBlue");
        assert numBluePref != null;
        numBluePref.setOnPreferenceChangeListener(this);
        SeekBarPreference numGreenPref = findPreference("numGreen");
        assert numGreenPref != null;
        numGreenPref.setOnPreferenceChangeListener(this);
        SeekBarPreference numYellowPref = findPreference("numYellow");
        assert numYellowPref != null;
        numYellowPref.setOnPreferenceChangeListener(this);

        teamQueue = new LinkedList<>();
        teamQueue.add(numYellowPref);
        teamQueue.add(numGreenPref);
        teamQueue.add(numBluePref);
        teamQueue.add(numRedPref);

        SeekBarPreference numCharactersPref = findPreference("numCharacters");
        assert numCharactersPref != null;
        numCharactersPref.setOnPreferenceChangeListener(this);

        SeekBarPreference numRandomsPref = findPreference("numRandoms");
        assert numRandomsPref != null;
        numRandomsPref.setOnPreferenceChangeListener(this);

        SwitchPreference randomSwitchPref = findPreference("randomSwitch");
        assert randomSwitchPref != null;
        randomSwitchPref.setOnPreferenceChangeListener(this);

        SeekBarPreference numSkipsPref = findPreference("numSkips");
        assert numSkipsPref != null;
        numSkipsPref.setOnPreferenceChangeListener(this);

        SwitchPreference skipSwitchPref = findPreference("skipSwitch");
        assert skipSwitchPref != null;
        skipSwitchPref.setOnPreferenceChangeListener(this);
        
        setSettingsFromSaved();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG,"\nPreference: "+preference + " | newValue: "+newValue);
        switch (preference.getKey()) {
            case "gameMode":
                preference.setTitle((String) newValue);
                preference.setSummary(gameModeDescriptions.get(newValue));
                spEditor.putString("gameMode",(String) newValue);
                spEditor.commit();
                break;
            case "numTeams":
                SeekBarPreference numGreenPref = findPreference("numGreen");
                SeekBarPreference numYellowPref = findPreference("numYellow");

                assert numGreenPref != null;
                numGreenPref.setEnabled(true);
                numGreenPref.setIcon(R.drawable.smash_green);
                assert numYellowPref != null;
                numYellowPref.setEnabled(true);
                numYellowPref.setIcon(R.drawable.smash_yellow);

                if (!teamQueue.contains(numGreenPref))
                    teamQueue.add(numGreenPref);
                if (!teamQueue.contains(numYellowPref))
                    teamQueue.add(numYellowPref);

                switch ((int) newValue) {
                    case 2:
                        numGreenPref.setIcon(R.drawable.smash_grey);
                        numGreenPref.setValue(0);
                        numGreenPref.setEnabled(false);
                        teamQueue.remove(numGreenPref);
                    case 3:
                        numYellowPref.setIcon(R.drawable.smash_grey);
                        numYellowPref.setValue(0);
                        numYellowPref.setEnabled(false);
                        teamQueue.remove(numYellowPref);
                        break;
                }

                spEditor.putInt("numTeams",(int) newValue);
                spEditor.putInt("numRed", ((SeekBarPreference) Objects.requireNonNull(findPreference("numRed"))).getValue());
                spEditor.putInt("numBlue",((SeekBarPreference) Objects.requireNonNull(findPreference("numBlue"))).getValue());
                spEditor.putInt("numGreen",((SeekBarPreference) Objects.requireNonNull(findPreference("numGreen"))).getValue());
                spEditor.putInt("numYellow",((SeekBarPreference) Objects.requireNonNull(findPreference("numYellow"))).getValue());
                spEditor.commit();

                break;
            case "numRed":
            case "numBlue":
            case "numGreen":
            case "numYellow":
                teamQueue.remove(preference);
                teamQueue.add(preference);

                //Need to do this as the value doesn't get "really" updated until the return of this function
                ((SeekBarPreference)preference).setValue((int) newValue);
                balanceTeamValues();

                spEditor.putInt("numRed", ((SeekBarPreference) Objects.requireNonNull(findPreference("numRed"))).getValue());
                spEditor.putInt("numBlue",((SeekBarPreference) Objects.requireNonNull(findPreference("numBlue"))).getValue());
                spEditor.putInt("numGreen",((SeekBarPreference) Objects.requireNonNull(findPreference("numGreen"))).getValue());
                spEditor.putInt("numYellow",((SeekBarPreference) Objects.requireNonNull(findPreference("numYellow"))).getValue());
                spEditor.commit();
                break;

            case "numRandoms":
                spEditor.putInt("numRandoms",(int) newValue);
                spEditor.commit();
                SwitchPreference randomSwitch = findPreference("randomSwitch");
                randomSwitch.setEnabled((int) newValue != 0);
                break;

            case "randomSwitch":
                if ((Boolean) newValue) {
                    preference.setSummary("The random character(s) will be at the end.");
                } else {
                    preference.setSummary("The random character(s) will be at the start.");
                }

                spEditor.putBoolean("randomEnd",(boolean) newValue);
                spEditor.commit();
                break;

            case "numCharacters":
                spEditor.putInt("numCharacters",(int) newValue);
                spEditor.commit();
                break;

            case "numSkips":
                String toSend = "Allow a team to skip " + newValue + " character";
                if ((int) newValue != 1) {
                    toSend += "s";
                }
                toSend += ".";
                preference.setSummary(toSend);

                SwitchPreference skipSwitch = findPreference("skipSwitch");
                skipSwitch.setEnabled((int) newValue != 0);

                spEditor.putInt("numSkips",(int) newValue);
                spEditor.commit();
                break;

            case "skipSwitch":
                if ((Boolean) newValue) {
                    preference.setSummary("A team can only skip while STRICLY behind.");
                } else {
                    preference.setSummary("A team can skip at any time.");
                }
                spEditor.putBoolean("skipBehind",(boolean) newValue);
                spEditor.commit();
                break;
        }
        return true;
    }

    //This can't be the most efficient way but this just sets all the settings to the saved settings
    private void setSettingsFromSaved() {
        if(sharedPreferences != null) {
            String gameMode = sharedPreferences.getString("gameMode","Draft As You Go");
            Log.d(TAG,"gameMode: "+gameMode);
            Preference gameModePref = findPreference("gameMode");
            assert gameModePref != null;
            gameModePref.setTitle(gameMode);
            gameModePref.setSummary(gameModeDescriptions.get(gameMode));

            int numTeams = sharedPreferences.getInt("numTeams",4);
            Log.d(TAG,"numTeams: "+numTeams);
            SeekBarPreference numTeamsPref = findPreference("numTeams");
            assert numTeamsPref != null;
            numTeamsPref.setValue(numTeams);
            SeekBarPreference numGreenPref = findPreference("numGreen");
            SeekBarPreference numYellowPref = findPreference("numYellow");

            switch (numTeams) {
                case 2:
                    assert numGreenPref != null;
                    numGreenPref.setIcon(R.drawable.smash_grey);
                    numGreenPref.setValue(0);
                    numGreenPref.setEnabled(false);
                    teamQueue.remove(numGreenPref);
                    break;
                case 3:
                    assert numYellowPref != null;
                    numYellowPref.setIcon(R.drawable.smash_grey);
                    numYellowPref.setValue(0);
                    numYellowPref.setEnabled(false);
                    teamQueue.remove(numYellowPref);
            }

            int numRed = sharedPreferences.getInt("numRed",2);
            Log.d(TAG,"numRed: "+numRed);
            SeekBarPreference numRedPref = findPreference("numRed");
            assert numRedPref != null;
            numRedPref.setValue(numRed);

            int numBlue = sharedPreferences.getInt("numBlue",2);
            Log.d(TAG,"numBlue: "+numBlue);
            SeekBarPreference numBluePref = findPreference("numBlue");
            assert numBluePref != null;
            numBluePref.setValue(numBlue);

            int numGreen = sharedPreferences.getInt("numGreen",2);
            Log.d(TAG,"numGreen: "+numGreen);
            assert numGreenPref != null;
            numGreenPref.setValue(numGreen);

            int numYellow = sharedPreferences.getInt("numYellow",2);
            Log.d(TAG,"numYellow: "+numYellow);
            assert numYellowPref != null;
            numYellowPref.setValue(numYellow);

            int numCharacters = sharedPreferences.getInt("numCharacters",8);
            SeekBarPreference numCharactersPref = findPreference("numCharacters");
            assert numCharactersPref != null;
            numCharactersPref.setValue(numCharacters);

            int numRandoms = sharedPreferences.getInt("numRandoms",2);
            SeekBarPreference numRandomsPref = findPreference("numRandoms");
            assert numRandomsPref != null;
            numRandomsPref.setValue(numRandoms);

            boolean randomEnd = sharedPreferences.getBoolean("randomEnd",true);
            Log.d(TAG,"randomEnd: "+randomEnd);
            SwitchPreference randomSwitchPref = findPreference("randomSwitch");
            assert randomSwitchPref != null;
            randomSwitchPref.setChecked(randomEnd);

            int numSkips = sharedPreferences.getInt("numSkips",1);
            Log.d(TAG,"numSkips: "+numSkips);
            SeekBarPreference numSkipsPref = findPreference("numSkips");
            assert numSkipsPref != null;
            numSkipsPref.setValue(numSkips);

            boolean skipBehind = sharedPreferences.getBoolean("skipBehind",true);
            Log.d(TAG,"skipBehind: "+skipBehind);
            SwitchPreference skipSwitchPref = findPreference("skipSwitch");
            assert skipSwitchPref != null;
            skipSwitchPref.setChecked(skipBehind);

            onPreferenceChange(gameModePref,gameMode);
            onPreferenceChange(numTeamsPref,numTeams);
            onPreferenceChange(numBluePref,numBlue);
            onPreferenceChange(numRedPref,numRed);
            onPreferenceChange(numGreenPref,numGreen);
            onPreferenceChange(numYellowPref,numYellow);
            onPreferenceChange(numCharactersPref,numCharacters);
            onPreferenceChange(numRandomsPref,numRandoms);
            onPreferenceChange(randomSwitchPref,randomEnd);
            onPreferenceChange(numSkipsPref,numSkips);
            onPreferenceChange(skipSwitchPref,skipBehind);
        }
    }

    void balanceTeamValues() {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < teamQueue.size(); i++) {
            sum += ((SeekBarPreference) teamQueue.get(i)).getValue();
        }
        while (sum > 8 && count < 10000) {

            count += 1;
            SeekBarPreference head = (SeekBarPreference) teamQueue.pop();

            int diff = sum - 8;
            if (head.getValue() - 1 > diff) {
                sum -= diff;
                head.setValue(head.getValue() - diff);
            } else {
                sum -= head.getValue() - 1;
                head.setValue(1);
            }
            teamQueue.add(head);
        }

    }
}

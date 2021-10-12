package com.example.smashdraft;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

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
        SeekBarPreference numTeamsPref = findPreference("numTeams");
        SeekBarPreference numRedPref = findPreference("numRed");
        SeekBarPreference numBluePref = findPreference("numBlue");
        SeekBarPreference numGreenPref = findPreference("numGreen");
        SeekBarPreference numYellowPref = findPreference("numYellow");
        SeekBarPreference numCharactersPref = findPreference("numCharacters");
        SeekBarPreference numRandomsPref = findPreference("numRandoms");
        SwitchPreference randomSwitchPref = findPreference("randomSwitch");
        SeekBarPreference numSkipsPref = findPreference("numSkips");
        SwitchPreference skipSwitchPref = findPreference("skipSwitch");

        assert gameModePref != null;
        assert numTeamsPref != null;
        assert numRedPref != null;
        assert numBluePref != null;
        assert numGreenPref != null;
        assert numYellowPref != null;
        assert numCharactersPref != null;
        assert numRandomsPref != null;
        assert randomSwitchPref != null;
        assert numSkipsPref != null;
        assert skipSwitchPref != null;
        gameModePref.setOnPreferenceChangeListener(this);
        numTeamsPref.setOnPreferenceChangeListener(this);
        numRedPref.setOnPreferenceChangeListener(this);
        numBluePref.setOnPreferenceChangeListener(this);
        numGreenPref.setOnPreferenceChangeListener(this);
        numYellowPref.setOnPreferenceChangeListener(this);
        numCharactersPref.setOnPreferenceChangeListener(this);
        numRandomsPref.setOnPreferenceChangeListener(this);
        randomSwitchPref.setOnPreferenceChangeListener(this);
        numSkipsPref.setOnPreferenceChangeListener(this);
        skipSwitchPref.setOnPreferenceChangeListener(this);

        teamQueue = new LinkedList<>();
        teamQueue.add(numYellowPref);
        teamQueue.add(numGreenPref);
        teamQueue.add(numBluePref);
        teamQueue.add(numRedPref);

        gameModeDescriptions.put("Draft As You Go","Alternate between drafting and playing. \nTeams: 2 - 4");
        gameModeDescriptions.put("Draft Up Front","Draft all characters before playing. \nTeams: 2 - 4");
        gameModeDescriptions.put("Locked In Random","Randomly fill the teams with characters. (No Duplicates) \nTeams: 2 - 4");
        gameModeDescriptions.put("Columns","Each team goes across the characters as a column. \nTeams: 2");

        setSettingsFromSaved();
    }

    private void setSettingsFromSaved() {
        if(sharedPreferences != null) {
            String gameMode = sharedPreferences.getString("gameMode","Draft As You Go");
            Preference gameModePref = findPreference("gameMode");
            int numTeams = sharedPreferences.getInt("numTeams",4);
            SeekBarPreference numTeamsPref = findPreference("numTeams");
            int numCharacters = sharedPreferences.getInt("numCharacters",8);
            SeekBarPreference numCharactersPref = findPreference("numCharacters");
            int numRandoms = sharedPreferences.getInt("numRandoms",2);
            SeekBarPreference numRandomsPref = findPreference("numRandoms");
            boolean randomEnd = sharedPreferences.getBoolean("randomEnd",true);
            SwitchPreference randomSwitchPref = findPreference("randomSwitch");
            int numSkips = sharedPreferences.getInt("numSkips",1);
            SeekBarPreference numSkipsPref = findPreference("numSkips");
            boolean skipAnyTime = sharedPreferences.getBoolean("skipAnyTime",true);
            SwitchPreference skipSwitchPref = findPreference("skipSwitch");

            assert gameModePref != null;
            assert numTeamsPref != null;
            assert numCharactersPref != null;
            assert randomSwitchPref != null;
            assert numRandomsPref != null;
            assert numSkipsPref != null;
            assert skipSwitchPref != null;
            onPreferenceChange(gameModePref,gameMode);
            onPreferenceChange(numTeamsPref,numTeams);
            onPreferenceChange(numCharactersPref,numCharacters);
            onPreferenceChange(randomSwitchPref,randomEnd);
            onPreferenceChange(numRandomsPref,numRandoms);
            onPreferenceChange(numSkipsPref,numSkips);
            onPreferenceChange(skipSwitchPref,skipAnyTime);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "gameMode": {
                preference.setTitle((String) newValue);
                preference.setSummary(gameModeDescriptions.get(newValue));
                SeekBarPreference numTeamsPref = findPreference("numTeams");
                SeekBarPreference numRedPref = findPreference("numRed");
                SeekBarPreference numBluePref = findPreference("numBlue");
                SeekBarPreference numGreenPref = findPreference("numGreen");
                SeekBarPreference numYellowPref = findPreference("numYellow");
                SeekBarPreference numCharPref = findPreference("numCharacters");

                assert numTeamsPref != null;
                assert numRedPref != null;
                assert numBluePref != null;
                assert numGreenPref != null;
                assert numYellowPref != null;
                assert numCharPref != null;

                if (newValue.toString().equals("Columns")) {
                    numTeamsPref.setValue(2);
                    numTeamsPref.setEnabled(false);

                    numRedPref.setValue(4);
                    numRedPref.setEnabled(false);
                    numBluePref.setValue(4);
                    numBluePref.setEnabled(false);

                    numGreenPref.setIcon(R.drawable.smash_grey);
                    numGreenPref.setMin(0);
                    numGreenPref.setValue(0);
                    numGreenPref.setEnabled(false);

                    numYellowPref.setIcon(R.drawable.smash_grey);
                    numYellowPref.setMin(0);
                    numYellowPref.setValue(0);
                    numYellowPref.setEnabled(false);

                    numCharPref.setEnabled(false);
                    numCharPref.setMax(72);
                    numCharPref.setValue(17);

                    spEditor.putInt("numTeams",numTeamsPref.getValue());
                    spEditor.putInt("numRed",numRedPref.getValue());
                    spEditor.putInt("numBlue",numBluePref.getValue());
                    spEditor.putInt("numGreen",numGreenPref.getValue());
                    spEditor.putInt("numYellow",numYellowPref.getValue());
                    spEditor.putInt("numCharacters",numCharPref.getValue());
                }else {
                    numTeamsPref.setEnabled(true);
                    numRedPref.setEnabled(true);
                    numBluePref.setEnabled(true);
                    numCharPref.setEnabled(true);
                    numGreenPref.setMin(1);
                    numYellowPref.setMin(1);
                    numCharPref.setMax(18);
                }
                spEditor.putString("gameMode", (String) newValue);
                spEditor.commit();
                break;
            }
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
                        numGreenPref.setMin(0);
                        numGreenPref.setValue(0);
                        numGreenPref.setEnabled(false);
                        teamQueue.remove(numGreenPref);
                        numYellowPref.setIcon(R.drawable.smash_grey);
                        numYellowPref.setMin(0);
                        numYellowPref.setValue(0);
                        numYellowPref.setEnabled(false);
                        teamQueue.remove(numYellowPref);
                        break;
                    case 3:
                        numGreenPref.setMin(1);
                        numGreenPref.setValue(1);
                        numYellowPref.setIcon(R.drawable.smash_grey);
                        numYellowPref.setMin(0);
                        numYellowPref.setValue(0);
                        numYellowPref.setEnabled(false);
                        teamQueue.remove(numYellowPref);
                        break;
                    default:
                        numGreenPref.setMin(1);
                        numGreenPref.setValue(1);
                        numYellowPref.setMin(1);
                        numYellowPref.setValue(1);
                        break;
                }
                balanceTeamValues();

                spEditor.putInt("numTeams",(int) newValue);
                spEditor.putInt("numRed", ((SeekBarPreference) Objects.requireNonNull(findPreference("numRed"))).getValue());
                spEditor.putInt("numBlue",((SeekBarPreference) Objects.requireNonNull(findPreference("numBlue"))).getValue());
                spEditor.putInt("numGreen",numGreenPref.getValue());
                spEditor.putInt("numYellow",numYellowPref.getValue());
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
                spEditor.putInt("numBlue", ((SeekBarPreference) Objects.requireNonNull(findPreference("numBlue"))).getValue());
                spEditor.putInt("numGreen", ((SeekBarPreference) Objects.requireNonNull(findPreference("numGreen"))).getValue());
                spEditor.putInt("numYellow", ((SeekBarPreference) Objects.requireNonNull(findPreference("numYellow"))).getValue());
                spEditor.commit();
                break;

            case "numRandoms":
                spEditor.putInt("numRandoms",(int) newValue);
                spEditor.commit();
                SwitchPreference randomSwitch = findPreference("randomSwitch");
                assert randomSwitch != null;
                randomSwitch.setEnabled((int) newValue != 0);
                spEditor.commit();
                break;

            case "randomSwitch":
                if ((Boolean) newValue)
                    preference.setSummary("The random character(s) will be at the end.");
                else
                    preference.setSummary("The random character(s) will be at the start.");


                spEditor.putBoolean("randomEnd",(boolean) newValue);
                spEditor.commit();
                break;

            case "numCharacters":
                spEditor.putInt("numCharacters", (int) newValue);
                spEditor.commit();
                break;

            case "numSkips":
                String toSend = "Allow a team to skip " + newValue + " character";
                if ((int) newValue != 1) toSend += "s";
                toSend += ".";
                preference.setSummary(toSend);

                SwitchPreference skipSwitch = findPreference("skipSwitch");
                assert skipSwitch != null;
                skipSwitch.setEnabled((int) newValue != 0);

                spEditor.putInt("numSkips",(int) newValue);
                spEditor.commit();
                break;

            case "skipSwitch":
                if ((Boolean) newValue)
                    preference.setSummary("A team can skip at any time.");
                else
                    preference.setSummary("A team can only skip while STRICTLY behind.");

                spEditor.putBoolean("skipAnyTime",(boolean) newValue);
                spEditor.commit();
                break;
        }
        return true;
    }

    void balanceTeamValues() {
        int sum = 0;
        int count = 0;
        for (int i = 0; i < teamQueue.size(); i++)
            sum += ((SeekBarPreference) teamQueue.get(i)).getValue();

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

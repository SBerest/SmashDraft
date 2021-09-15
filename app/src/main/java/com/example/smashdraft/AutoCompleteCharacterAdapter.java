package com.example.smashdraft;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AutoCompleteCharacterAdapter extends ArrayAdapter<Fighter>{
    private final List<Fighter> characterListFull;

    AutoCompleteCharacterAdapter(@NonNull Context context, @NonNull List<Fighter> characterList) {
        super(context, 0, characterList);
        characterListFull = new ArrayList<>(characterList);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return characterFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fighter_autocomplete_row, parent, false);
        }

        ImageView imageViewFace = convertView.findViewById(R.id.autoCharacterImage);
        TextView textViewName = convertView.findViewById(R.id.autoCharacterName);

        Fighter character = getItem(position);

        if(character!=null){
            textViewName.setText(character.getName());
            imageViewFace.setImageResource(character.getImageId());
        }

        return convertView;
    }

    private final Filter characterFilter = new Filter(){

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d(TAG,"Start Filter");
            FilterResults results = new FilterResults();
            List<Fighter> suggestions = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                Log.d(TAG,constraint + " Found nothing");
                suggestions.addAll(characterListFull);
            } else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(Fighter item: characterListFull){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        Log.d(TAG,filterPattern +" Found "+item.getName());
                        suggestions.add(item);
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d(TAG,"publicResults");
            clear();
            addAll((ArrayList<Fighter>)results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            Log.d(TAG,"convertResultToString");
            return ((Fighter) resultValue).getName();
        }
    };
}
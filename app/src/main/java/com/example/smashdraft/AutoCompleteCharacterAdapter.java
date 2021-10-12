package com.example.smashdraft;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteCharacterAdapter extends ArrayAdapter<Fighter>{
    private final List<Fighter> mFighters;

    AutoCompleteCharacterAdapter(@NonNull Context context, @NonNull List<Fighter> fighters) {
        super(context, 0, fighters);
        mFighters = new ArrayList<>(fighters);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return characterFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fighter_autocomplete_row, parent, false);

        ImageView imageViewIcon = convertView.findViewById(R.id.autoCharacterImage);
        TextView textViewName = convertView.findViewById(R.id.autoCharacterName);
        Fighter fighter = getItem(position);

        if(fighter != null){
            imageViewIcon.setImageResource(fighter.getImageId());
            textViewName.setText(fighter.getName());
        }
        switch (((ManagingApplication) getContext().getApplicationContext()).getFighter(fighter)){
            case 0:
                textViewName.setTextColor(getContext().getColor(R.color.red));
                break;
            case 1:
                textViewName.setTextColor(getContext().getColor(R.color.blue));
                break;
            case 2:
                textViewName.setTextColor(getContext().getColor(R.color.green));
                break;
            case 3:
                textViewName.setTextColor(getContext().getColor(R.color.yellow));
                break;
        }
        return convertView;
    }

    private final Filter characterFilter = new Filter(){
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Fighter> suggestions = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                suggestions.addAll(mFighters);
            } else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(Fighter item: mFighters){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((ArrayList<Fighter>) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Fighter) resultValue).getName();
        }
    };
}
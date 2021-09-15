package com.example.smashdraft;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class DraftRecyclerAdapter extends RecyclerView.Adapter<DraftRecyclerAdapter.DraftRecyclerViewHolder> {

    private static final String TAG = "DraftRecyclerAdapter";
    private final ArrayList<Fighter> mFighters;
    private final onFighterListener mOnFighterListener;
    private final Context mContext;


    public interface onFighterListener{
        void onFighterClick(int position);
    }

     DraftRecyclerAdapter(Context context, ArrayList<Fighter> fighters, onFighterListener onFighterListener){
        this.mContext = context;
        this.mFighters = fighters;
        this.mOnFighterListener = onFighterListener;
    }

    @NonNull
    @Override
    public DraftRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        boolean list = ((DraftActivity) mContext).getListView();
        View v;
        if(list){
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fighter_cell, viewGroup, false);
        }else{
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fighter_grid, viewGroup, false);
        }
        return new DraftRecyclerViewHolder(v, mContext, mOnFighterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftRecyclerViewHolder holder, int position) {
        Fighter fighter = mFighters.get(position);

        holder.mImageView.setImageResource(fighter.getImageId());
        if(((DraftActivity)mContext).getListView() && holder.mTextView != null){
            holder.mTextView.setText(fighter.getName());

            //set Colors of text when drafted
            switch (((DraftActivity) mContext).draftedTeam(fighter)) {
                case 0:
                    holder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.blue));
                    break;
                case 1:
                    holder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                    break;
                case 2:
                    holder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                    break;
                case 3:
                    holder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.yellow));
                    break;
                default:
                    holder.mTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    break;
            }
        }
        else if(!((DraftActivity)mContext).getListView() && holder.mItemView != null){
            Log.d(TAG,"mFighterCell");
            //set Colors of text when drafted
            switch (((DraftActivity) mContext).draftedTeam(fighter)) {
                case 0:
                    holder.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.blue));
                    break;
                case 1:
                    holder.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
                    break;
                case 2:
                    holder.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.green));
                    break;
                case 3:
                    holder.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.yellow));
                    break;
                default:
                    holder.mItemView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
                    break;
            }
        }
    }

    public static class DraftRecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mImageView;
        TextView mTextView;
        View mItemView;
        Context mContext;
        onFighterListener mOnFighterListener;

        DraftRecyclerViewHolder(@NonNull View itemView, Context context, onFighterListener tOnFighterListener) {
            super(itemView);

            this.mImageView = itemView.findViewById(R.id.characterImage);
            this.mTextView = itemView.findViewById(R.id.characterName);
            this.mOnFighterListener = tOnFighterListener;
            this.mItemView = itemView;
            mContext = context;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnFighterListener.onFighterClick(getAdapterPosition());
        }
    }
    @Override
    public int getItemCount() {
        return mFighters.size();
    }


}

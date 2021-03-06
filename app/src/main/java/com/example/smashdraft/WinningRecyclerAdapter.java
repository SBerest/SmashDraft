package com.example.smashdraft;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WinningRecyclerAdapter extends RecyclerView.Adapter<WinningRecyclerAdapter.ViewHolder> {

    private final ArrayList<Fighter> mFighters;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private final int mNumColumns;
    private final Activity mWinningActivity;


    // data is passed into the constructor
     WinningRecyclerAdapter(Context context, ArrayList<Fighter> data, int numColumns, Activity winningActivity) {
        this.mInflater = LayoutInflater.from(context);
        this.mFighters = data;
        this.mNumColumns = numColumns;
        this.mWinningActivity = winningActivity;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup ViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.winning_fighter_cell, ViewGroup, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Fighter fighter = mFighters.get(position);

        DisplayMetrics dm = mWinningActivity.getResources().getDisplayMetrics();
        holder.itemView.getLayoutParams().width = dm.widthPixels / mNumColumns;

        int numRows = (int)(Math.ceil((double) mFighters.size()/mNumColumns));
        holder.itemView.getLayoutParams().height = ((int) (dm.heightPixels*0.75) / numRows);
        

        holder.mImageView.setImageResource(fighter.getImageId());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mFighters.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mImageView;
        View mItemView;

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.win_char);
            itemView.setOnClickListener(this);
            this.mItemView = itemView;
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
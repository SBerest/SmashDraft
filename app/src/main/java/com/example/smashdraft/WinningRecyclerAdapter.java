package com.example.smashdraft;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class WinningRecyclerAdapter extends RecyclerView.Adapter<WinningRecyclerAdapter.ViewHolder> {

    private ArrayList<Fighter> mFighters;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
     WinningRecyclerAdapter(Context context, ArrayList<Fighter> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mFighters = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup ViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.winning_fighter_cell, ViewGroup, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Fighter fighter = mFighters.get(position);
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

        ViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.win_char);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Fighter getItem(int id) {
        return mFighters.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
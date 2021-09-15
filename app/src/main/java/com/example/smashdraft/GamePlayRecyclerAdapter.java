package com.example.smashdraft;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class GamePlayRecyclerAdapter extends RecyclerView.Adapter<GamePlayRecyclerAdapter.GamePlayViewHolder> {

    private static final String TAG = "GamePlayRecyclerAdapter";
    private final ArrayList<Team> mTeams;
    private final onTeamListener mOnTeamListener;

    private final Context mContext;


    public interface onTeamListener{
        void onTeamClick(int position);
    }

    static class GamePlayViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView mImageView0;
        ImageView mImageView1;
        ImageView mImageView2;
        ImageView mImageView3;
        View mView;
        Context mContext;
        onTeamListener mOnTeamListener;

        GamePlayViewHolder(@NonNull View itemView, Context context, onTeamListener tOnTeamListener) {
            super(itemView);
            mImageView0 = itemView.findViewById(R.id.gameplay_char_0);
            mImageView1 = itemView.findViewById(R.id.gameplay_char_1);
            mImageView2 = itemView.findViewById(R.id.gameplay_char_2);
            mImageView3 = itemView.findViewById(R.id.gameplay_char_3);
            mOnTeamListener = tOnTeamListener;
            mView = itemView;
            mContext = context;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnTeamListener.onTeamClick(getAdapterPosition());
        }
    }

     GamePlayRecyclerAdapter(Context context, ArrayList<Team> teams, onTeamListener onTeamListener){
        mContext = context;
        mTeams = teams;
        mOnTeamListener = onTeamListener;
    }

    @NonNull
    @Override
    public GamePlayViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.team_cell, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = (int) (viewGroup.getHeight() * 0.25);
        itemView.setLayoutParams(layoutParams);

        return new GamePlayViewHolder(itemView, mContext, mOnTeamListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GamePlayViewHolder holder, int position) {
        Team activeFighters = mTeams.get(position);


        if (activeFighters.getTeamSize() == 1){
            holder.mImageView0.setImageResource(activeFighters.getImageId(0));
            holder.mImageView1.setVisibility(View.GONE);
            holder.mImageView2.setVisibility(View.GONE);
            holder.mImageView3.setVisibility(View.GONE);
        }
        else if(activeFighters.getTeamSize() == 2){
            holder.mImageView0.setImageResource(activeFighters.getImageId(0));
            holder.mImageView1.setImageResource(activeFighters.getImageId(1));
            holder.mImageView2.setVisibility(View.GONE);
            holder.mImageView3.setVisibility(View.GONE);
        }
        else if(activeFighters.getTeamSize() == 3){
            holder.mImageView0.setImageResource(activeFighters.getImageId(0));
            holder.mImageView1.setImageResource(activeFighters.getImageId(1));
            holder.mImageView2.setImageResource(activeFighters.getImageId(2));
            holder.mImageView3.setVisibility(View.GONE);
        }
        else if(activeFighters.getTeamSize() == 4){
            holder.mImageView0.setImageResource(activeFighters.getImageId(0));
            holder.mImageView1.setImageResource(activeFighters.getImageId(1));
            holder.mImageView2.setImageResource(activeFighters.getImageId(2));
            holder.mImageView3.setImageResource(activeFighters.getImageId(3));
        }

        //set Colors of text when drafted
        switch(activeFighters.getTeam()){
            case 0:
                holder.itemView.setBackgroundColor(mContext.getColor(R.color.red));
                break;
            case 1:
                holder.itemView.setBackgroundColor(mContext.getColor(R.color.blue));
                break;
            case 2:
                holder.itemView.setBackgroundColor(mContext.getColor(R.color.green));
                break;
            case 3:
                holder.itemView.setBackgroundColor(mContext.getColor(R.color.yellow));
                break;
            default:
                holder.itemView.setBackgroundColor(mContext.getColor(R.color.white));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTeams.size();
    }

}

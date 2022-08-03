package com.example.smashdraft;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
        ArrayList<ImageView> mImageViews = new ArrayList<>();
        ArrayList<ImageView> mSkipViews = new ArrayList<>();
        ImageView mLosses;
        TextView mWin;
        View mView;
        Context mContext;
        onTeamListener mOnTeamListener;
        View mItemView;

        GamePlayViewHolder(@NonNull View itemView, Context context, onTeamListener tOnTeamListener) {
            super(itemView);
            mItemView = itemView;
            mImageViews.add(itemView.findViewById(R.id.gameplay_char_0));
            mImageViews.add(itemView.findViewById(R.id.gameplay_char_1));
            mImageViews.add(itemView.findViewById(R.id.gameplay_char_2));
            mImageViews.add(itemView.findViewById(R.id.gameplay_char_3));
            mSkipViews.add(itemView.findViewById(R.id.skip0));
            mSkipViews.add(itemView.findViewById(R.id.skip1));
            mSkipViews.add(itemView.findViewById(R.id.skip2));
            mLosses = itemView.findViewById(R.id.losses_image);
            mWin = itemView.findViewById(R.id.number_of_wins);
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
<<<<<<< Updated upstream
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.team_cell, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = (int) (viewGroup.getHeight() * 0.25);
        itemView.setLayoutParams(layoutParams);
=======
        Configuration newConfig = viewGroup.getResources().getConfiguration();
        View itemView = null;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.team_cell_land, viewGroup, false);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = (int) (viewGroup.getWidth() * 0.25);
            layoutParams.height = (viewGroup.getHeight());
            itemView.setLayoutParams(layoutParams);
        }//Portrait
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.team_cell_port, viewGroup, false);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.width = (viewGroup.getWidth());
            layoutParams.height = (int) (viewGroup.getHeight() * 0.25);
            itemView.setLayoutParams(layoutParams);
        }
>>>>>>> Stashed changes

        assert itemView != null;
        return new GamePlayViewHolder(itemView, mContext, mOnTeamListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GamePlayViewHolder holder, int position) {
        Team myTeam = mTeams.get(position);

        //Set Character icons
        for(int i = 0; i < 4; i++){
            if(i < myTeam.getTeamSize()) {
                holder.mImageViews.get(i).setImageResource(myTeam.getImageId(i));
            }
            else {
                holder.mImageViews.get(i).setVisibility(View.GONE);
            }
        }

        //Set Wins
        holder.mWin.setText(myTeam.getWins()+"");

        //Set Losses
        switch (myTeam.getLosses()){
            case 1:
                holder.mLosses.setImageResource(R.drawable.lose_1);
                break;
            case 2:
                holder.mLosses.setImageResource(R.drawable.lose_2);
                break;
            case 3:
                holder.mLosses.setImageResource(R.drawable.lose_3);
                break;
            default:
            case 0:
                holder.mLosses.setImageResource(R.drawable.lose_0);
                break;
        }

        //Set Skips
        switch (myTeam.getSkips()){
            case 0:
                holder.mSkipViews.get(0).setVisibility(View.INVISIBLE);
            case 1:
                holder.mSkipViews.get(1).setVisibility(View.INVISIBLE);
            case 2:
                holder.mSkipViews.get(2).setVisibility(View.INVISIBLE);
                break;
            case 3:
                holder.mSkipViews.get(0).setVisibility(View.VISIBLE);
                holder.mSkipViews.get(1).setVisibility(View.VISIBLE);
                holder.mSkipViews.get(2).setVisibility(View.VISIBLE);
        }

        //Set Background Colour
        switch (myTeam.getTeam()){
            case 0:
                holder.mView.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.red));
                break;
            case 1:
                holder.mView.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.blue));
                break;
            case 2:
                holder.mView.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.green));
                break;
            case 3:
                holder.mView.setBackgroundColor(ContextCompat.getColor(this.mContext, R.color.yellow));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mTeams.size();
    }

}

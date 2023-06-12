package io.agora.metachat.example.adapter.idol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agora.metachat.example.models.idol.BgmMusic;
import io.agora.mediaplayer.Constants;
import io.agora.metachat.example.R;

public class BgmRecyclerAdapter extends RecyclerView.Adapter<BgmRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<BgmMusic> mDataList;

    private OnItemClick mOnItemClick;
    private int mCurrentPosition;

    public BgmRecyclerAdapter(Context context, List<BgmMusic> list) {
        this.mContext = context;
        this.mDataList = list;
        mCurrentPosition = -1;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.mOnItemClick = onItemClick;
    }

    public void setCurrentPosition(int currentPosition) {
        this.mCurrentPosition = currentPosition;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.idol_item_bgm_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (null != mDataList) {
            final BgmMusic bgmMusic = mDataList.get(position);
            if (mCurrentPosition == position) {
                if (Constants.MediaPlayerState.PLAYER_STATE_OPENING == bgmMusic.getState()) {
                    holder.playStateIcon.setVisibility(View.INVISIBLE);
                    holder.playState.setVisibility(View.VISIBLE);
                    holder.playState.setText(mContext.getResources().getString(R.string.loading));
                } else if (Constants.MediaPlayerState.PLAYER_STATE_PLAYING == bgmMusic.getState()) {
                    holder.playStateIcon.setVisibility(View.VISIBLE);
                    holder.playState.setVisibility(View.VISIBLE);
                    holder.playState.setText(mContext.getResources().getString(R.string.playing));
                } else {
                    holder.playStateIcon.setVisibility(View.INVISIBLE);
                    holder.playState.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.playStateIcon.setVisibility(View.INVISIBLE);
                holder.playState.setVisibility(View.INVISIBLE);
            }

            holder.songName.setText(bgmMusic.getName());
            holder.singer.setText(bgmMusic.getSinger());

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClick) {
                        mCurrentPosition = position;
                        mOnItemClick.onItemClick(bgmMusic);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (null == mDataList) {
            return 0;
        }
        return mDataList.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout layout;
        private final TextView songName;

        private final TextView singer;

        private final AppCompatImageView playStateIcon;

        private final TextView playState;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            songName = itemView.findViewById(R.id.song_name);
            singer = itemView.findViewById(R.id.singer);
            playStateIcon = itemView.findViewById(R.id.play_state_icon);
            playState = itemView.findViewById(R.id.play_state);
        }
    }

    public interface OnItemClick {
        void onItemClick(BgmMusic bgmMusic);
    }
}

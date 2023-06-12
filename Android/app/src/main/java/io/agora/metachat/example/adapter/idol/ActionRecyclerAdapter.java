package io.agora.metachat.example.adapter.idol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.idol.ActionInfo;

public class ActionRecyclerAdapter extends RecyclerView.Adapter<ActionRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<ActionInfo> mDataList;

    private OnItemClick mOnItemClick;
    private int mCurrentPosition;

    public ActionRecyclerAdapter(Context context, List<ActionInfo> list) {
        this.mContext = context;
        this.mDataList = list;
        mCurrentPosition = 0;
    }

    public void setInitPosition(int position) {
        this.mCurrentPosition = position;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.mOnItemClick = onItemClick;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.idol_item_action_list, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (null != mDataList) {
            final ActionInfo actionInfo = mDataList.get(position);
            boolean currentImageChecked = mCurrentPosition == position;

            holder.actionImage.setStrokeWidthResource(R.dimen.select_border_width);
            if (currentImageChecked) {
                holder.actionImage.setStrokeColorResource(R.color.select_border_color);
                holder.checkedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.actionImage.setStrokeColorResource(R.color.select_border_color_null);
                holder.checkedIcon.setVisibility(View.GONE);
            }

            Glide.with(mContext).load(actionInfo.getActionResId()).into(holder.actionImage);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClick) {
                        mCurrentPosition = position;
                        mOnItemClick.onItemClick(actionInfo);
                        notifyDataSetChanged();
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
        private final ShapeableImageView actionImage;

        private final ShapeableImageView checkedIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            actionImage = itemView.findViewById(R.id.action_img);
            checkedIcon = itemView.findViewById(R.id.checked_icon);
        }
    }

    public interface OnItemClick {
        void onItemClick(ActionInfo actionInfo);
    }
}

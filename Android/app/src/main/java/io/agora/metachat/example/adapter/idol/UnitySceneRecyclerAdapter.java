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
import io.agora.metachat.example.models.idol.UnitySceneInfo;

public class UnitySceneRecyclerAdapter extends RecyclerView.Adapter<UnitySceneRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<UnitySceneInfo> mDataList;

    private OnItemClick mOnItemClick;
    private int mCurrentPosition;

    public UnitySceneRecyclerAdapter(Context context, List<UnitySceneInfo> list) {
        this.mContext = context;
        this.mDataList = list;
        mCurrentPosition = 0;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.mOnItemClick = onItemClick;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.idol_item_unity_scene_list, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (null != mDataList) {
            final UnitySceneInfo sceneInfo = mDataList.get(position);
            boolean currentImageChecked = mCurrentPosition == position;

            holder.sceneImg.setStrokeWidthResource(R.dimen.select_border_width);
            if (currentImageChecked) {
                holder.sceneImg.setStrokeColorResource(R.color.select_border_color);
                holder.checkedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.sceneImg.setStrokeColorResource(R.color.select_border_color_null);
                holder.checkedIcon.setVisibility(View.GONE);
            }

            Glide.with(mContext).load(sceneInfo.getSceneResId()).into(holder.sceneImg);

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClick) {
                        mCurrentPosition = position;
                        mOnItemClick.onItemClick(sceneInfo);
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
        private final ShapeableImageView sceneImg;

        private final ShapeableImageView checkedIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            sceneImg = itemView.findViewById(R.id.scene_img);
            checkedIcon = itemView.findViewById(R.id.checked_icon);
        }
    }

    public interface OnItemClick {
        void onItemClick(UnitySceneInfo sceneInfo);
    }
}

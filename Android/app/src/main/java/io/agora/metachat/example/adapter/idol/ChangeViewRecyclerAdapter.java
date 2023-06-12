package io.agora.metachat.example.adapter.idol;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agora.metachat.example.R;
import io.agora.metachat.example.models.idol.ActionMenu;

public class ChangeViewRecyclerAdapter extends RecyclerView.Adapter<ChangeViewRecyclerAdapter.MyViewHolder> {
    private final Context mContext;
    private final List<ActionMenu> mDataList;

    private OnItemClick mOnItemClick;
    private int mCurrentPosition;

    public ChangeViewRecyclerAdapter(Context context, List<ActionMenu> list) {
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.idol_item_change_view_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (null != mDataList) {
            if (mCurrentPosition == position) {
                holder.textView.setTextColor(mContext.getResources().getColor(R.color.item_checked));
            } else {
                holder.textView.setTextColor(mContext.getResources().getColor(R.color.item_uncheck));
            }
            final ActionMenu menu = mDataList.get(position);
            holder.textView.setText(menu.getMenuName());
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mOnItemClick) {
                        mCurrentPosition = position;
                        mOnItemClick.onItemClick(menu);
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
        private final TextView textView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            textView = itemView.findViewById(R.id.content);
        }
    }

    public interface OnItemClick {
        void onItemClick(ActionMenu actionMenu);
    }

    public static class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, @NonNull View view,
                                   RecyclerView parent, @NonNull RecyclerView.State state) {
            outRect.left = 0;
            outRect.right = 0;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildAdapterPosition(view) == 0)
                outRect.top = space;
        }
    }
}

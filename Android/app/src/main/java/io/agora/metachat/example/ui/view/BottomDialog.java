package io.agora.metachat.example.ui.view;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import io.agora.metachat.example.R;


public class BottomDialog {
    private static final String TAG = BottomDialog.class.getSimpleName();

    private Context mContext;
    private final CustomBottomSheetDialog dialog;
    private final RecyclerView recyclerView;

    private final TextView dialogTitle;


    public BottomDialog(Context context) {
        dialog = new CustomBottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.idol_bottom_dialog_layout, null, false);
        dialog.setContentView(view);

        recyclerView = view.findViewById(R.id.music_list);
        dialogTitle = view.findViewById(R.id.dialog_title);
    }

    public BottomDialog initRecyclerView(RecyclerView.LayoutManager layout, RecyclerView.Adapter adapter) {
        if (null != recyclerView) {
            recyclerView.setLayoutManager(layout);
            recyclerView.setAdapter(adapter);
        }
        return this;
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }


    public BottomDialog setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            dialogTitle.setVisibility(View.VISIBLE);
            dialogTitle.setText(title);
        }
        return this;
    }
}
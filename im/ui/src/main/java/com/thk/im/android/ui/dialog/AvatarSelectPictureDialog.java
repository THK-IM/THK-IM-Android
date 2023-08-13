package com.thk.im.android.ui.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.thk.im.android.ui.R;

/**
 * 头像选择底部对话框
 */
public class AvatarSelectPictureDialog extends Dialog implements View.OnClickListener {


    public AvatarSelectPictureDialog(@NonNull Context context) {
        this(context, R.style.BottomSheetStyle);
    }

    public AvatarSelectPictureDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_avatar_select_picture);
        findViewById(R.id.tv_photo).setOnClickListener(this);
        findViewById(R.id.save_imge).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_photo) {
            if (listener != null) {
                listener.onSelectPictureItemClick();
            }
        } else if (id == R.id.save_imge) {
            if (listener != null) {
                listener.onSelectCameraItemClick();
            }
        } else if (id == R.id.tv_cancel) {
        }
        dismiss();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = this.getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.dimAmount = 0.4f;
        params.gravity = Gravity.BOTTOM;
        this.getWindow().setAttributes(params);
        setCanceledOnTouchOutside(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setListener(OnSelectPictureItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnSelectPictureItemClickListener {
        void onSelectPictureItemClick();

        void onSelectCameraItemClick();
    }

    private OnSelectPictureItemClickListener listener;


}


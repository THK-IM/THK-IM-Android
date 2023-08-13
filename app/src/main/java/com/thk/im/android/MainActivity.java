package com.thk.im.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thk.im.android.core.IMManager;
import com.thk.im.android.core.api.BaseSubscriber;
import com.thk.im.android.databinding.ActivityMainBinding;
import com.thk.im.android.db.entity.Session;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Random random = new Random();
                long uid = Math.abs(random.nextInt(100000));
                createSingleSession(uid);
            }
        });

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), SessionActivity.class);
                startActivity(intent);
            }
        });

        binding.edtRoom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    binding.buttonThird.setText("进入" + charSequence + "房间");
                } else {
                    binding.buttonThird.setText("创建房间");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.buttonThird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WebRtcActivity.class);
                if (Objects.requireNonNull(binding.edtRoom.getText()).toString().length() > 0) {
                    String roomId = binding.edtRoom.getText().toString();
                    intent.putExtra("room_id", roomId);
                }
                startActivity(intent);
            }
        });

        showSoftInput(this);
    }

    private void createSingleSession(long uid) {
        BaseSubscriber<Session> subscriber = new BaseSubscriber<Session>() {
            @Override
            public void onNext(Session session) {
                if (session != null) {
                    Toast.makeText(MainActivity.this, "id:" + session.getId() + ", 创建成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@Nullable Throwable t) {
                super.onError(t);
                Toast.makeText(MainActivity.this, "创建失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        IMManager.INSTANCE.getMessageModule().getSession(uid, new HashMap<>()).subscribe(subscriber);
        disposable.add(subscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    public void showSoftInput(Activity activity) {
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isActive() && activity.getCurrentFocus() != null) {
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }


}
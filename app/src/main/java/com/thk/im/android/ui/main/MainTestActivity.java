package com.thk.im.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.thk.im.android.MediaTestActivity;
import com.thk.im.android.SessionActivity;
import com.thk.im.android.databinding.ActivityMainTestBinding;

import java.util.Random;

import io.reactivex.disposables.CompositeDisposable;

public class MainTestActivity extends AppCompatActivity {

    private ActivityMainTestBinding binding;
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainTestBinding.inflate(getLayoutInflater());
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

        binding.buttonAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainTestActivity.this, MediaTestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void createSingleSession(long uid) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

}
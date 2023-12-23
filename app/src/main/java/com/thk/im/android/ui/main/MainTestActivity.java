package com.thk.im.android.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.thk.im.android.MediaTestActivity;
import com.thk.im.android.SessionActivity;
import com.thk.im.android.core.base.BaseSubscriber;
import com.thk.im.android.core.base.RxTransform;
import com.thk.im.android.core.IMCoreManager;
import com.thk.im.android.core.db.entity.Session;
import com.thk.im.android.databinding.ActivityMainTestBinding;

import java.util.Random;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;

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
        BaseSubscriber<Session> subscriber = new BaseSubscriber<Session>() {
            @Override
            public void onNext(Session session) {
                if (session != null) {
                    Toast.makeText(MainTestActivity.this, "id:" + session.getId() + ", 创建成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@Nullable Throwable t) {
                super.onError(t);
                Toast.makeText(MainTestActivity.this, "创建失败:" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        IMCoreManager.INSTANCE.getMessageModule()
                .createSingleSession(uid)
                .flatMap(new Function<Session, Flowable<Session>>() {
                    @Override
                    public Flowable<Session> apply(Session session) throws Exception {
                        IMCoreManager.INSTANCE.getImDataBase().sessionDao().insertOrUpdateSessions(session);
                        return Flowable.just(session);
                    }
                })
                .compose(RxTransform.INSTANCE.flowableToMain())
                .subscribe(subscriber);
        disposable.add(subscriber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

}
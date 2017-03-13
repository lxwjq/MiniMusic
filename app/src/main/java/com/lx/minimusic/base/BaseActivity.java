package com.lx.minimusic.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.lx.minimusic.service.PlayService;

/**
 * Created by 李祥 on 2017/3/2.
 */

public abstract class BaseActivity extends FragmentActivity {

    public PlayService mPlayService;
    private boolean isBound = false;

    private PlayService.MusicUpdateLinstener mMusicUpdateLinstener = new PlayService.MusicUpdateLinstener() {
        @Override
        public void publish(int progress) {
            BaseActivity.this.publish(progress);
        }

        @Override
        public void onChange(int postion) {
            BaseActivity.this.onChange(postion);
        }
    };

    public abstract void publish(int progress);

    public abstract void onChange(int postion);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder = (PlayService.PlayBinder) service;
            mPlayService = playBinder.getBinder();

            mPlayService.setMusicUpdateLinstener(mMusicUpdateLinstener);

            mMusicUpdateLinstener.onChange(mPlayService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BaseActivity.this.mPlayService = null;
            isBound = false;
        }
    };

    public void bindPlayService() {
        if (!isBound) {
            Intent intent = new Intent(this, PlayService.class);
            bindService(intent, conn, Context.BIND_AUTO_CREATE);
            isBound = true;
        }
    }

    public void unBindPlayService() {
        if (isBound) {
            unbindService(conn);
            isBound = false;
        }
    }
}

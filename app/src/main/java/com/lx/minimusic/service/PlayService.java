package com.lx.minimusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lx.minimusic.app.MiniMusicApplication;
import com.lx.minimusic.bean.Mp3Info;
import com.lx.minimusic.utils.MediaUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 李祥 on 2017/3/1.
 * 音乐播放的服务组件：
 * 实现功能：
 * 播放
 * 暂停
 * 切换
 * 播放进度
 */

public class PlayService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer mMediaPlayer;

    /**
     * 当前播放的位置
     */
    private int currentPosition;
    public List<Mp3Info> mMusicData;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    public PlayService() {

    }

    private boolean isPause = false;

    public boolean getPause() {
        return isPause;
    }

    /**
     * 顺序播放
     */
    public static final int order_play = 1;
    /**
     * 随机播放
     */
    public static final int random_play = 2;
    /**
     * 单曲播放
     */
    public static final int single_play = 3;

    private int play_mode = 1;

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mMediaPlayer.reset();
        return false;
    }

    /**
     * 音乐列表
     */
    public static final int MUSIC_LIST = 1;
    /**
     * 我喜欢的
     */
    public static final int MY_LIKE_LIST = 2;
    /**
     * 最近播放
     */
    public static final int RECORS_LIST = 3;


    private int changeLikeList = MUSIC_LIST;

    public int getChangeLikeList() {
        return changeLikeList;
    }

    public void setChangeLikeList(int changeLikeList) {
        this.changeLikeList = changeLikeList;
    }

    private Random mRandom = new Random();

    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case order_play:
                next();
                break;
            case random_play:
                int i = mRandom.nextInt(mMusicData.size());
                play(i);
                break;
            case single_play:
                play(currentPosition);
                break;

            default:
                break;
        }
    }

    public class PlayBinder extends Binder {
        public PlayService getBinder() {
            return PlayService.this;
        }

    }

    Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {

            while (true) {

                if (mMusicUpdateLinstener != null && mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMusicUpdateLinstener.publish(getCurrent());
                }
                try {
                    Thread.sleep(500);  //休眠线程  防止出现线程的阻塞
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mExecutorService != null && !mExecutorService.isShutdown()) {
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMusicData = MediaUtils.getMusicData(PlayService.this);
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mExecutorService.execute(updateRunnable);

        MiniMusicApplication application = (MiniMusicApplication) getApplication();
        currentPosition = application.mSp.getInt("currentPostion", 0);
        play_mode = application.mSp.getInt("play_mode", 1);
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * 列表点击播放
     */
    public void play(int position) {
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mMusicData.size()) {
            position = 0;
        }
        mp3Info = mMusicData.get(position);
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(this, Uri.parse(mp3Info.url));
            mMediaPlayer.prepare();
            mMediaPlayer.start();

            currentPosition = position;

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mMusicUpdateLinstener != null) {
            mMusicUpdateLinstener.onChange(currentPosition);
        }

    }

    /**
     * 暂停
     */
    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }

        isPause = true;
    }

    /**
     * 下一首
     */
    public void next() {
        //判断是否为最后一首
        if (currentPosition + 1 >= mMusicData.size()) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);
    }

    /**
     * 上一首
     */
    public void pre() {
        if (currentPosition - 1 < 0) {
            currentPosition = mMusicData.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 跳转到固定位置
     *
     * @return
     */
    public void seekTo(int msec) {
        mMediaPlayer.seekTo(msec);
    }


    public int getCurrent() {
        if (mMediaPlayer.isPlaying() && mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }

    }

    /**
     * 暂停之后音乐播放
     */
    public void start() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    /**
     * 更新状态的接口  (回调)
     */
    public interface MusicUpdateLinstener {
        /**
         * 进度条更新
         *
         * @param progress 进度
         */
        public void publish(int progress);

        public void onChange(int postion);
    }

    private MusicUpdateLinstener mMusicUpdateLinstener;

    public void setMusicUpdateLinstener(MusicUpdateLinstener musicUpdateLinstener) {
        mMusicUpdateLinstener = musicUpdateLinstener;
    }

    public List<Mp3Info> getMusicData() {
        return mMusicData;
    }

    public void setMusicData(List<Mp3Info> musicData) {
        mMusicData = musicData;
    }
}

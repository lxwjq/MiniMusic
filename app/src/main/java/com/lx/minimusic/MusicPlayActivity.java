package com.lx.minimusic;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lx.minimusic.app.MiniMusicApplication;
import com.lx.minimusic.base.BaseActivity;
import com.lx.minimusic.bean.Mp3Info;
import com.lx.minimusic.service.PlayService;
import com.lx.minimusic.utils.MediaUtils;

import static com.lx.minimusic.R.id.iv_play_start;

public class MusicPlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "MusicPlayActivity";
    private SeekBar mSbPlay;
    private static final int TIME_UPDATE = 0;
    private TextView mTvEndTime, mTvPlayName, mTvStartTime;
    private ImageView mIvPlayNext, mIvPlayPre, mIvPlayStart, mIvPlayControl, mIcPlayIcon;
    private MiniMusicApplication mApplication;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case TIME_UPDATE:
                    MusicPlayActivity.this.mTvStartTime.setText(MediaUtils.formatTime(msg.arg1));
                    break;
            }
        }
    };
    private boolean mIsPuase;
    private ImageView mIvBackHome;
    private ImageView mIvLike;
    private RelativeLayout mRlMusicPlay;
    private RelativeLayout mRlLrcPlay;

    @Override
    public void publish(int progress) {
//        mTvStartTime.setText(MediaUtils.formatTime(progress));
        Message message = mHandler.obtainMessage(TIME_UPDATE);
        message.arg1 = progress;
        mHandler.sendMessage(message);
        mSbPlay.setProgress(progress);
    }

    @Override
    public void onChange(int postion) {
        Mp3Info mp3Info = mPlayService.mMusicData.get(postion);
        mTvPlayName.setText(mp3Info.title + "-" + mp3Info.artist);
        mTvEndTime.setText(mp3Info.duration);

        //获取专辑图片
        Bitmap musicBitemp = MediaUtils.getMusicBitemp(MusicPlayActivity.this, mp3Info.id, mp3Info.albumId);
        mIcPlayIcon.setImageBitmap(musicBitemp);

        mSbPlay.setProgress(0);
        mSbPlay.setMax(mp3Info.duration_copy);  //设置进度的最大值

        if (mPlayService.isPlaying()) {
            mIvPlayStart.setImageResource(R.mipmap.pause);
        } else {
            mIvPlayStart.setImageResource(R.mipmap.play);
        }
        switch (mPlayService.getPlay_mode()) {
            case PlayService.order_play:
                mIvPlayControl.setImageResource(R.mipmap.order);
                mIvPlayControl.setTag(PlayService.order_play);
                break;
            case PlayService.random_play:
                mIvPlayControl.setImageResource(R.mipmap.random);
                mIvPlayControl.setTag(PlayService.random_play);
                break;
            case PlayService.single_play:
                mIvPlayControl.setImageResource(R.mipmap.single);
                mIvPlayControl.setTag(PlayService.single_play);
                break;
        }

        //显示是否为收藏状态
        try {
            Mp3Info likeInfo = mApplication.dbUtils.
                    findFirst(Selector.from(Mp3Info.class).
                            where("mp3InfoId", "=", mp3Info.id));
            if (likeInfo == null) {
                mIvLike.setImageResource(R.drawable.a_l);
                Log.e(TAG, "不是收藏");
            } else {
                if (likeInfo.getIsLike() == 1) {
                    mIvLike.setImageResource(R.drawable.a_m);
                    Log.e(TAG, "likeInfo的信息" + likeInfo.toString() + ": " + mp3Info.getMp3InfoId());
                    Log.e(TAG, "是收藏");
                }
            }

        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_music_play);

        mIsPuase = getIntent().getBooleanExtra("isPuase", false);
        mApplication = (MiniMusicApplication) getApplication();
        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindPlayService();
    }


    private void initUI() {
        mIcPlayIcon = (ImageView) findViewById(R.id.ic_play_icon);
        mIvPlayControl = (ImageView) findViewById(R.id.iv_play_control);
        mIvPlayStart = (ImageView) findViewById(iv_play_start);
        mIvPlayPre = (ImageView) findViewById(R.id.iv_play_pre);
        mIvPlayNext = (ImageView) findViewById(R.id.iv_play_next);
        mTvStartTime = (TextView) findViewById(R.id.tv_start_time);
        mTvPlayName = (TextView) findViewById(R.id.tv_play_name);
        mTvEndTime = (TextView) findViewById(R.id.tv_end_time);
        mSbPlay = (SeekBar) findViewById(R.id.sb_play);
        mIvBackHome = (ImageView) findViewById(R.id.iv_back_home);
        mIvLike = (ImageView) findViewById(R.id.iv_like);
        mRlMusicPlay = (RelativeLayout) findViewById(R.id.rl_music_play);
        mRlLrcPlay = (RelativeLayout) findViewById(R.id.rl_lrc_play);

        mIvPlayStart.setOnClickListener(this);
        mIvPlayNext.setOnClickListener(this);
        mIvPlayPre.setOnClickListener(this);
        mIvPlayControl.setOnClickListener(this);
        mIvBackHome.setOnClickListener(this);
        mSbPlay.setOnSeekBarChangeListener(this);
        mIvLike.setOnClickListener(this);
        mRlMusicPlay.setOnClickListener(this);
        mRlLrcPlay.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_start:
                if (mPlayService.isPlaying()) {
                    mIvPlayStart.setImageResource(R.mipmap.play);

                    mPlayService.pause();
                } else {
                    if (mPlayService.getPause()) {
                        mPlayService.start();
                        mIvPlayStart.setImageResource(R.mipmap.player_btn_pause_normal);
                    } else {
                        mPlayService.play(mPlayService.getCurrentPosition());
                    }

                    mIsPuase = false;
                }

                break;

            case R.id.iv_play_next:
                mPlayService.next();
                break;

            case R.id.iv_play_pre:
                mPlayService.pre();
                break;

            case R.id.iv_play_control:
                int tag = (int) mIvPlayControl.getTag();
                switch (tag) {
                    case PlayService.order_play:
                        //转化为随机模式
                        mIvPlayControl.setImageResource(R.mipmap.random);
                        mIvPlayControl.setTag(PlayService.random_play);
                        mPlayService.setPlay_mode(PlayService.random_play);
                        Toast.makeText(this, R.string.random, Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.random_play:
                        //转化为单曲模式
                        mIvPlayControl.setImageResource(R.mipmap.single);
                        mIvPlayControl.setTag(PlayService.single_play);
                        mPlayService.setPlay_mode(PlayService.single_play);
                        Toast.makeText(this, R.string.single, Toast.LENGTH_SHORT).show();
                        break;
                    case PlayService.single_play:
                        //转化为随机模式
                        mIvPlayControl.setImageResource(R.mipmap.order);
                        mIvPlayControl.setTag(PlayService.order_play);
                        mPlayService.setPlay_mode(PlayService.order_play);
                        Toast.makeText(this, R.string.order, Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case R.id.iv_back_home:
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);

                overridePendingTransition(R.anim.next_in_home_up, R.anim.next_out_home_up);
                finish();
                break;

            case R.id.iv_like:
                //收藏
                Mp3Info mp3Info = mPlayService.mMusicData.get(mPlayService.getCurrentPosition());
                Log.e(TAG, "mp3Info的信息" + mp3Info.toString() + ": " + mPlayService.getCurrentPosition());
                try {
                    Mp3Info likeInfo =
                            mApplication.dbUtils.findFirst(Selector.from(Mp3Info.class).
                                    where("mp3InfoId", "=", getId(mp3Info)));

                    if (likeInfo == null) {
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);  //设置为收藏
                        mApplication.dbUtils.save(mp3Info);
                        Log.e(TAG, "不存在，设置为收藏");
                        mIvLike.setImageResource(R.drawable.a_m);  //变成红心
                    } else {
                        Log.e(TAG, "likeInfo的信息" + likeInfo.toString());
                        int id = likeInfo.getIsLike();
                        Log.e(TAG, "id:----" + id);
                        if (id == 1) {
                            likeInfo.setIsLike(0);
                            Log.e(TAG, "id == 1");
                            mIvLike.setImageResource(R.drawable.a_l);
                        } else {
                            likeInfo.setIsLike(1);
                            Log.e(TAG, "id ！= 1");
                            mIvLike.setImageResource(R.drawable.a_m);
                        }

                    }
                    mApplication.dbUtils.update(likeInfo, "isLike", "id", "mp3InfoId");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.rl_lrc_play:
                break;

            case R.id.rl_music_play:
                break;
        }
    }

    private long getId(Mp3Info mp3Info) {
        long id = 0;
        switch (mPlayService.getChangeLikeList()) {
            case PlayService.MUSIC_LIST:
                id = mp3Info.mp3InfoId;
                Log.e(TAG, "音乐列表");
                break;
            case PlayService.MY_LIKE_LIST:
                id = mp3Info.id;
                Log.e(TAG, "我的收藏");
                break;
            default:
                break;
        }
        Log.e(TAG, "返回的id:---" + id);
        return id;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mPlayService.seekTo(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);

        overridePendingTransition(R.anim.next_in_home_up, R.anim.next_out_home_up);
        finish();
    }
}

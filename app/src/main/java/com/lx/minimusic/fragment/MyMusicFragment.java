package com.lx.minimusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lx.minimusic.MainActivity;
import com.lx.minimusic.MusicPlayActivity;
import com.lx.minimusic.R;
import com.lx.minimusic.bean.Mp3Info;
import com.lx.minimusic.service.PlayService;
import com.lx.minimusic.utils.MediaUtils;

import java.util.List;

import static com.lx.minimusic.R.id.tv_singer;
import static com.lx.minimusic.R.id.tv_song_name;

/**
 * Created by 李祥 on 2017/3/1.
 * 音乐显示Fragment
 */
public class MyMusicFragment extends Fragment {

    private ListView mLvMusic;
    private MainActivity mContext;

    /**
     * 本地音乐信息集合
     */
    private List<Mp3Info> mMusicList;
    private ImageView mIvMusicIcon;
    private TextView mTvSongName;
    private TextView mTvSinger;
    private ImageView mIvStart;
    private ImageView mIvNext;

    private boolean isPause = false;
    private MyMusicAdapter mAdapter;
    private SwipeRefreshLayout mSwipeContainer;

    /**
     * @return 返回一个MyMusicFragment对象
     */
    public static MyMusicFragment newInstance() {
        MyMusicFragment musicFragment = new MyMusicFragment();
        return musicFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (MainActivity) context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext.bindPlayService();
        View view = inflater.inflate(R.layout.my_music_list_layout, null);
        //初始化布局
        initUI(view);
        //初始化数据
        // initData();
        return view;
    }

    /**
     * 初始化数据
     */
    public void initData() {
        mMusicList = MediaUtils.getMusicData(mContext);
        mAdapter = new MyMusicAdapter();
        mLvMusic.setAdapter(mAdapter);
        mLvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mContext.mPlayService.getChangeLikeList() != mContext.mPlayService.MUSIC_LIST) {
                    mContext.mPlayService.setMusicData(mMusicList);
                    mContext.mPlayService.setChangeLikeList(mContext.mPlayService.MUSIC_LIST);
                }
                mContext.mPlayService.play(position);
            }
        });

        savePlayRecord();
    }

    /**
     * 保存播放时间
     */
    private void savePlayRecord() {
        Mp3Info mp3Info = mContext.mPlayService.getMusicData().get(mContext.mPlayService.getCurrentPosition());

        try {
            Mp3Info playRecordMp3Info = mContext.mApplication.dbUtils.
                    findFirst(Selector.from(Mp3Info.class).
                            where("mp3InfoId", "=", mp3Info.getId()));

            if (playRecordMp3Info == null) {
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                mContext.mApplication.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mContext.mApplication.dbUtils.update(playRecordMp3Info, "playTime");

            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

        mContext.unBindPlayService();
    }

    /**
     * 初始化控件
     */
    private void initUI(View view) {

        mLvMusic = (ListView) view.findViewById(R.id.lv_music);
        mIvMusicIcon = (ImageView) view.findViewById(R.id.iv_music_icon);
        mIvStart = (ImageView) view.findViewById(R.id.iv_start);
        mIvNext = (ImageView) view.findViewById(R.id.iv_next);
        mTvSongName = (TextView) view.findViewById(tv_song_name);
        mTvSinger = (TextView) view.findViewById(tv_singer);
        mSwipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);


        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //停止刷新
                        //  更新音乐列表
                        mMusicList = MediaUtils.getMusicData(mContext);
                        mAdapter.notifyDataSetChanged();
                        mSwipeContainer.setRefreshing(false);

                    }
                }, 1000);
            }
        });
        //设置颜色
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        mIvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mContext.mPlayService.isPlaying()) {
                    //判断是否正在播放
                    mIvStart.setImageResource(R.mipmap.player_btn_play_normal);
                    mContext.mPlayService.pause();

                    isPause = true;
                } else {
                    if (mContext.mPlayService.getPause()) {
                        mContext.mPlayService.start();
                        mIvStart.setImageResource(R.mipmap.player_btn_pause_normal);
                    } else {
                        mContext.mPlayService.play(mContext.mPlayService.getCurrentPosition());
                    }

                    isPause = false;
                }

            }
        });

        mIvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.mPlayService.next();
            }
        });

        mIvMusicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent musicPlayIntent = new Intent(mContext, MusicPlayActivity.class);
                musicPlayIntent.putExtra("isPuase", isPause);
                startActivity(musicPlayIntent);

                mContext.overridePendingTransition(R.anim.next_in_home, R.anim.next_out_home);
            }
        });

    }

    class MyMusicAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMusicList.size();
        }

        @Override
        public Object getItem(int position) {
            return mMusicList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.list_item_music, null);
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_item_singer = (TextView) convertView.findViewById(R.id.tv_item_singer);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Mp3Info mp3Info = mMusicList.get(position);
            holder.tv_title.setText(mp3Info.title);
            holder.tv_time.setText(mp3Info.duration + "");
            holder.tv_item_singer.setText(mp3Info.artist);
            return convertView;
        }
    }


    static class ViewHolder {
        public TextView tv_title;
        public TextView tv_item_singer;
        public TextView tv_time;

    }


    /**
     * 改变播放音乐的状态
     *
     * @param pos
     */
    public void changeUIState(int pos) {
        if (pos >= 0 && pos < mContext.mPlayService.mMusicData.size()) {
            Mp3Info mp3Info = mContext.mPlayService.mMusicData.get(pos);
            mTvSongName.setText(mp3Info.title);
            mTvSinger.setText(mp3Info.artist);
            if (mContext.mPlayService.isPlaying()) {
                mIvStart.setImageResource(R.mipmap.pause);
            } else {
                mIvStart.setImageResource(R.mipmap.play);
            }

            Bitmap musicBitemp = MediaUtils.getMusicBitemp(mContext, mp3Info.id, mp3Info.albumId);
            mIvMusicIcon.setImageBitmap(musicBitemp);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.stopService(new Intent(mContext, PlayService.class));
        mContext.finish();
    }
}

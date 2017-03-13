package com.lx.minimusic;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;
import com.lx.minimusic.app.MiniMusicApplication;
import com.lx.minimusic.base.BaseActivity;
import com.lx.minimusic.bean.Mp3Info;

import java.util.List;

public class LikeMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "LikeMusicActivity";
    private ListView mLvLikeMusic;
    private MiniMusicApplication mApplication;
    private List<Mp3Info> mMp3InfoList;
    private boolean isChange = false;

    @Override
    public void publish(int progress) {

    }

    @Override
    public void onChange(int postion) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_like_music);

        initUI();
        initEvent();
        initData();
    }


    @Override
    public void onResume() {
        super.onResume();

        bindPlayService();  //界面可见时绑定服务
    }

    @Override
    public void onPause() {
        super.onPause();

        unBindPlayService();   //界面不可是取消绑定服务
    }


    /**
     * 初始化UI
     */
    private void initUI() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("我喜欢的");
        actionBar.setIcon(R.mipmap.app_logo2);
        mLvLikeMusic = (ListView) findViewById(R.id.lv_like_music);
    }

    /**
     * 初始化控件的点击事件
     */
    private void initEvent() {
        mLvLikeMusic.setOnItemClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mApplication = (MiniMusicApplication) getApplication();
        try {
            List<Mp3Info> list = mApplication.dbUtils
                    .findAll(Selector.from(Mp3Info.class).
                            where("isLike", "=", "1"));
            if (list == null || list.size() == 0) {
                return;
            }

            mMp3InfoList = list;
            MyLikeAdapter myLikeAdapter = new MyLikeAdapter();
            mLvLikeMusic.setAdapter(myLikeAdapter);

        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    class MyLikeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMp3InfoList.size();

        }

        @Override
        public Object getItem(int position) {
            return mMp3InfoList.get(position);
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
                convertView = View.inflate(LikeMusicActivity.this, R.layout.list_item_music, null);
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_item_singer = (TextView) convertView.findViewById(R.id.tv_item_singer);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Mp3Info mp3Info = mMp3InfoList.get(position);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (mPlayService.getChangeLikeList() != mPlayService.MY_LIKE_LIST) {
            Log.e(TAG, "不是MY_LIKE_LIST");
            Log.e(TAG, "我的收藏列表:" + mMp3InfoList.toString());
            mPlayService.setMusicData(mMp3InfoList);
            mPlayService.setChangeLikeList(mPlayService.MY_LIKE_LIST);
        }
        mPlayService.play(position);

        savePlayRecord();  //保存播放时间
    }

    /**
     * 保存播放时间
     */
    private void savePlayRecord() {
        Mp3Info mp3Info = mPlayService.getMusicData().get(mPlayService.getCurrentPosition());

        try {
            Mp3Info playRecordMp3Info = mApplication.dbUtils.
                    findFirst(Selector.from(Mp3Info.class).
                            where("mp3InfoId", "=", mp3Info.getMp3InfoId()));

            if (playRecordMp3Info == null) {
                mp3Info.setPlayTime(System.currentTimeMillis());
                mApplication.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mApplication.dbUtils.update(playRecordMp3Info, "playTime");

            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}

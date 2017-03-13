package com.lx.minimusic;

import android.app.ActionBar;
import android.os.Bundle;
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
import com.lx.minimusic.service.PlayService;
import com.lx.minimusic.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class PlayRecordActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView mLvRecordMusic;
    private MiniMusicApplication mApplication;
    private TextView mTvRecord;
    private ArrayList<Mp3Info> mMp3Infos;

    @Override
    public void publish(int progress) {

    }

    @Override
    public void onChange(int postion) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record);
        initUI();
        initData();
        initEvent();
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
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("最近播放");
        actionBar.setIcon(R.mipmap.app_logo2);
        mTvRecord = (TextView) findViewById(R.id.tv_record);
        mLvRecordMusic = (ListView) findViewById(R.id.lv_record_music);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mApplication = (MiniMusicApplication) getApplication();

        try {
            //查询最近播放的数据 排序  取前五个
            List<Mp3Info> list = mApplication.dbUtils.findAll
                    (Selector.from(Mp3Info.class).
                            where("playTime", "!=", 0).
                            orderBy("playTime", true).limit(Constant.PLAY_MAX_NUMBER));

            if (list == null || list.size() == 0) {
                return;
            } else {
                mMp3Infos = (ArrayList<Mp3Info>) list;
                MyLikeAdapter myLikeAdapter = new MyLikeAdapter();

                mLvRecordMusic.setAdapter(myLikeAdapter);

            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }


    private void initEvent() {
        mLvRecordMusic.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mPlayService.getChangeLikeList() != PlayService.RECORS_LIST) {
            mPlayService.setChangeLikeList(PlayService.RECORS_LIST);
            mPlayService.setMusicData(mMp3Infos);
        }
        mPlayService.play(position);
    }


    class MyLikeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mMp3Infos.size();
        }

        @Override
        public Object getItem(int position) {
            return mMp3Infos.get(position);
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
                convertView = View.inflate(PlayRecordActivity.this, R.layout.list_item_music, null);
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_item_singer = (TextView) convertView.findViewById(R.id.tv_item_singer);
                holder.tv_time = (TextView) convertView.findViewById(R.id.tv_time);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Mp3Info mp3Info = mMp3Infos.get(position);
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
}

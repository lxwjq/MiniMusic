package com.lx.minimusic.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;
import com.lx.minimusic.utils.Constant;

/**
 * Created by 李祥 on 2017/3/6.
 */

public class MiniMusicApplication extends Application {

    public static SharedPreferences mSp;
    public static DbUtils dbUtils;
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mSp = this.getSharedPreferences("miniMusic", Context.MODE_PRIVATE);

        dbUtils = DbUtils.create(this, Constant.DB_NAME);
        mContext = getApplicationContext();
    }
}

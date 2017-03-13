package com.lx.minimusic.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.lx.minimusic.app.MiniMusicApplication;

/**
 * Created by 李祥 on 2017/3/8.
 * 隐藏输入法
 */
public class AppUtils {
    public static void hideInputMethod(View view) {
        InputMethodManager methodManager = (InputMethodManager) MiniMusicApplication.mContext.
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (methodManager.isActive()) {
            methodManager.hideSoftInputFromWindow
                    (view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

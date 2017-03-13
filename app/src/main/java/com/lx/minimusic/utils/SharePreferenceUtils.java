package com.lx.minimusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharePreference工具类
 * 
 * @author LX
 */
public class SharePreferenceUtils {

	private static SharedPreferences mSP;

	/**
	 * 得到SharePreference保存的boolean
	 * 
	 * @param ctx
	 *            上下文
	 * @param key
	 *            键
	 * @param defValue
	 *            默认的值
	 * @return 返回boolean值
	 */
	public static boolean getBoolean(Context ctx, String key, boolean defValue) {
		if (mSP == null) {
			mSP = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return mSP.getBoolean(key, defValue);
	}

	/**
	 * 设置SharePreference中的值
	 * 
	 * @param ctx
	 *            上下文
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public static void setBoolean(Context ctx, String key, boolean value) {
		if (mSP == null) {
			mSP = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		mSP.edit().putBoolean(key, value).commit();
	}

	/**
	 * 得到SharePreference保存的String值
	 * 
	 * @param ctx
	 *            上下文
	 * @param key
	 *            键
	 * @param defValue
	 *            默认的值
	 * @return 返回String值
	 */
	public static String getString(Context ctx, String key, String defValue) {
		if (mSP == null) {
			mSP = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		return mSP.getString(key, defValue);
	}

	/**
	 * 设置SharePreference中的值
	 * 
	 * @param ctx
	 *            上下文
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	public static void setString(Context ctx, String key, String value) {
		if (mSP == null) {
			mSP = ctx.getSharedPreferences("config", Context.MODE_PRIVATE);
		}
		mSP.edit().putString(key, value).commit();
	}
}

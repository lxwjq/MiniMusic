package com.lx.minimusic.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 * 
 * @author LX
 */
public class ToastUtils {
	private static Toast toast = null;

	public static void showToast(Context context, String content) {
		if (toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void showToast(Context context, int resid) {
		showToast(context, context.getString(resid));
	}
}

package com.lx.minimusic.utils;

import android.support.compat.BuildConfig;
import android.util.Log;

public class LogUtils {

	/**
	 * 类名
	 */
	public static String className;

	/**
	 * 方法名
	 */
	public static String methodName;

	/**
	 * 行数
	 */
	public static int lineNumber;

	private LogUtils() {

	}

	public static boolean isDebuggable() {
		return BuildConfig.DEBUG;
	}

	private static String createLog(String log) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(className).append("|" + methodName).append("|")
				.append(lineNumber + ":");
		buffer.append(log);
		return buffer.toString();
	}

	private static void getMethodNames(StackTraceElement[] sElements) {
		className = sElements[1].getFileName();
		methodName = sElements[1].getMethodName();
		lineNumber = sElements[1].getLineNumber();
	}

	public static void e(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.e(className, createLog(message));
	}

	public static void i(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.i(className, createLog(message));
	}

	public static void d(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.d(className, createLog(message));
	}

	public static void v(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.v(className, createLog(message));
	}

	public static void w(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.w(className, createLog(message));
	}

	public static void wtf(String message) {
		if (!isDebuggable())
			return;

		getMethodNames(new Throwable().getStackTrace());
		Log.wtf(className, createLog(message));
	}

}

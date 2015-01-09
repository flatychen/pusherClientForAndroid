package cn.flaty.push.utils;

import android.util.Log;

public abstract class LogUtil {

	private static String getClassName() {
		StackTraceElement stackTraceElement = Thread.currentThread()
				.getStackTrace()[1];
		return stackTraceElement.getClassName();
	}

	public static void v(String message) {
		Log.v(getClassName(), message);
	}

	public static void d(String message) {
		Log.d(getClassName(), message);
	}

	public static void i(String message) {
		Log.i(getClassName(), message);
	}

	public static void w(String message) {
		Log.w(getClassName(), message);
	}

	public static void e(String message) {
		Log.e(getClassName(), message);
	}

}

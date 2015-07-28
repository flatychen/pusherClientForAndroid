package cn.flaty.push.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DeviceUtil {

	private static String TAG = "DeviceUtil";

	public static String getImei(Context context) {
		try {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getDeviceId();
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
		return null;
	}

	public static String getMacAddress(Context context) {
		try {
			WifiManager wm = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			return wm.getConnectionInfo().getMacAddress();
		} catch (Exception e) {
			Log.w(TAG, e.getMessage());
		}
		return null;
	}

	public static String getAndroidId(Context context) {
		return Secure.getString(context.getContentResolver(),
				Settings.Secure.ANDROID_ID);
	}

	public static String getMixDeviceId(Context context) {
		return new StringBuilder().append(getImei(context))
				.append(getMacAddress(context)).append(getAndroidId(context))
				.toString();
	}

	public static String getAndroidVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

}
package cn.flaty.push.utils;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class DeviceUtil {

	public static String getImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static String getMacAddress(Context context) {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
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

	public static String getAndroidVersion(){
		return android.os.Build.VERSION.RELEASE;
	}
	
	
}
package cn.flaty.push.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.util.Log;

public class NetWorkUtil {
	
	
	private ConnectivityManager connManger = null;
	
	
	public static enum NetType {
		WIFI, MOBILE, NONE
	}

	private static Context context = ApplicationUtil.getContext();

	/**
	 * 判断当前网络连接是否为手机网
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileConnected() {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo gprs = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		DetailedState detailedState = gprs.getDetailedState();
		return DetailedState.CONNECTED == detailedState;
	}

	/**
	 * 判断当前网络连接是否为wifi
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected() {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null) {
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 判断是否有网络连接
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetConnected() {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 获取当前的网络类型
	 * 
	 * @param context
	 * 
	 * @return {@link netType}
	 */
	public static NetType getNetType() {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null) {
			return NetType.NONE;
		}
		int nType = networkInfo.getType();

		if (nType == ConnectivityManager.TYPE_MOBILE) {
			return NetType.MOBILE;
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			return NetType.WIFI;
		}
		return NetType.NONE;
	}

	/**
	 * 获取IP地址
	 * 
	 * @return
	 */
	public static String getIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
		return null;
	}
}

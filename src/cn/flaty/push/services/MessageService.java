package cn.flaty.push.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import cn.flaty.push.utils.ApplicationUtil;
import cn.flaty.push.utils.NetWorkUtil;

public class MessageService extends IntentService {

	private static String TAG = "MessageService";
	
	private String host = "192.183.3.178";
	
	private int port = 11111;

	public MessageService() {
		super("MessageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ApplicationUtil.init(this.getApplicationContext());
		// 网络已连接
		if (NetWorkUtil.isNetConnected()) {
			Log.i(TAG,"开始连接服务器");
			MessageDispacher dispacher = new MessageDispacher();
			dispacher.connect(host, port);
		}
		
	}
}

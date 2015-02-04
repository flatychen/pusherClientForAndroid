package cn.flaty.push.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import cn.flaty.push.PushBootStrap;

public class MessageService extends IntentService {

	private static String TAG = "MessageService";



	public MessageService() {
		super("MessageService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "后台service启动");
		MessageDispacher.getInstance(this.getApplicationContext()).connect(PushBootStrap.host, PushBootStrap.port);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

}

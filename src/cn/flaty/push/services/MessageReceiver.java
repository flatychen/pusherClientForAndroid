package cn.flaty.push.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MessageReceiver extends BroadcastReceiver {

	private static String TAG = "MessageReceiver";
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent intent2 = new Intent(context, MessageService.class);
		context.startService(intent2);
	}



}

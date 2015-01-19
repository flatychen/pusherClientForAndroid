package cn.flaty.push.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.flaty.push.PushBootStrap;

public class MessageReceiver extends BroadcastReceiver {

	
	private String intentName = "android.net.conn.CONNECTIVITY_CHANGE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(intentName)){
			PushBootStrap.getInstance().start(context);
		}
	}



}

package cn.flaty.push.services;

import com.alibaba.fastjson.JSON;

import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cn.flaty.R;
import cn.flaty.push.model.GenericMessage;
import cn.flaty.push.model.PushMessage;
import cn.flaty.push.utils.ApplicationUtil;

public final class MessageDispacher extends MessageSupport implements
		Receiveable {

	private static String TAG = "MessageDispacher";
	
	private static volatile MessageDispacher dispacher;

	public static MessageDispacher getInstance() {
		if (dispacher == null) {
			synchronized (MessageDispacher.class) {
				dispacher = new MessageDispacher();
			}
		}
		return dispacher;
	}
	
	

	@Override
	public void receiveMsg(String msg) {
		GenericMessage gm = new GenericMessage(msg);
		// 文本通知
		if (gm.getCommond() == GenericMessage.text) {
			this.sendNotification(gm.getMessage());
		}
		
		
		
	}



	private void sendNotification(String message) {
		Log.i(TAG, message);
		PushMessage msg = JSON.parseObject(message, PushMessage.class);
		NotificationCompat.Builder build = new NotificationCompat.Builder(ApplicationUtil.getContext());
		build.setContentTitle(msg.getTitle()).setContentText(msg.getContent());
	}

}

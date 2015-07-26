package cn.flaty.push.core;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cn.flaty.push.entity.GenericMessage;
import cn.flaty.push.entity.PushMessage;

import com.alibaba.fastjson.JSON;

public final class MessageDispacher extends MessageSupport implements
		Receiveable {

	private MessageDispacher(Context applicationContext) {
		super(applicationContext);
	}

	private static String TAG = "MessageDispacher";

	private static volatile MessageDispacher dispacher;

	public static MessageDispacher getInstance(Context applicationContext) {
		if (dispacher == null) {
			synchronized (MessageDispacher.class) {
				dispacher = new MessageDispacher(applicationContext);
			}
		}
		return dispacher;
	}

	@Override
	public void receiveMsg(String msg) {
		GenericMessage gm = new GenericMessage(msg);
		Log.i(TAG, msg);
		Notification notifi = null;
		// 普通文本通知
		if (gm.getCommond() == GenericMessage.server_push_text) {
			notifi = this.bulidBaseNotification(gm.getMessage());
		}
		
		// bigView图片通知
		if (gm.getCommond() == GenericMessage.server_push_image) {
			notifi = this.bulidPictureNotification(gm.getMessage());
		}

		
		try {
			getNotificationManager().notify(getNotifyId(), notifi);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	private Notification bulidPictureNotification(String message) {
		PushMessage pushMessage = JSON.parseObject(message, PushMessage.class);
		NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle();
		//style.bigPicture(BitmapFactory.decodeResource(super.applicationContext.getResources(), R.drawable.logo));

		NotificationCompat.Builder build = new NotificationCompat.Builder(
				applicationContext);
		build.setContentTitle(pushMessage.getTitle())
				.setContentText(pushMessage.getContent())
				.setSmallIcon(applicationContext.getApplicationInfo().icon)
				.setDefaults(pushMessage.getFlag());
		return build.build();
		
	}

	private Notification bulidBaseNotification(String message) {
		PushMessage pushMessage = JSON.parseObject(message, PushMessage.class);
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				applicationContext);
		build.setContentTitle(pushMessage.getTitle())
				.setContentText(pushMessage.getContent())
				.setSmallIcon(applicationContext.getApplicationInfo().icon);
			//	.setDefaults(pushMessage.getFlags());
		return build.build();
	}


	private NotificationManager getNotificationManager() {
		return (NotificationManager) applicationContext
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}

	private int getNotifyId() {
		return applicationContext.getApplicationInfo().icon
				+ Calendar.getInstance().get(Calendar.MILLISECOND);
	}


}

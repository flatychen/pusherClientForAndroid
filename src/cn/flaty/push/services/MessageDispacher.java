package cn.flaty.push.services;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cn.flaty.push.R;
import cn.flaty.push.entity.GenericMessage;
import cn.flaty.push.entity.PushMessage;
import cn.flaty.push.utils.ApplicationUtil;

import com.alibaba.fastjson.JSON;

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
		style.bigPicture(BitmapFactory.decodeResource(ApplicationUtil.getContext().getResources(), R.drawable.logo));

		NotificationCompat.Builder build = new NotificationCompat.Builder(
				ApplicationUtil.getContext());
		build.setContentTitle(pushMessage.getTitle())
				.setContentText(pushMessage.getContent())
				.setSmallIcon(ApplicationUtil.getContext().getApplicationInfo().icon)
				.setDefaults(pushMessage.getFlag());
		return build.build();
		
	}

	private Notification bulidBaseNotification(String message) {
		PushMessage pushMessage = JSON.parseObject(message, PushMessage.class);
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				ApplicationUtil.getContext());
		build.setContentTitle(pushMessage.getTitle())
				.setContentText(pushMessage.getContent())
				.setSmallIcon(ApplicationUtil.getContext().getApplicationInfo().icon);
			//	.setDefaults(pushMessage.getFlags());
		return build.build();
	}


	private NotificationManager getNotificationManager() {
		return (NotificationManager) ApplicationUtil.getContext()
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}

	private int getNotifyId() {
		return ApplicationUtil.getContext().getApplicationInfo().icon
				+ Calendar.getInstance().get(Calendar.MILLISECOND);
	}


}

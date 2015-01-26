package cn.flaty.push.services;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cn.flaty.push.R;
import cn.flaty.push.model.GenericMessage;
import cn.flaty.push.model.PushMessage;
import cn.flaty.push.utils.ApplicationUtil;

import com.alibaba.fastjson.JSON;

public final class MessageDispacher extends MessageSupport implements
		Receiveable {
	private int i = 11;

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
		// 普通文本通知
		if (gm.getCommond() == GenericMessage.server_push_text) {
			this.sendNotification(gm.getMessage());
		}

	}

	/**
	 * 普通文本通知
	 * 
	 * @param message
	 */
	private void sendNotification(String message) {
		Log.i(TAG, message);
		PushMessage pushMessage = JSON.parseObject(message, PushMessage.class);
		try {
			Notification noti = this.getBaseBuilder(pushMessage).build();
			getNotificationManager().notify(this.getNotifyId(), noti);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private NotificationManager getNotificationManager() {
		return (NotificationManager) ApplicationUtil.getContext()
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
	}

	private int getNotifyId() {
		return ApplicationUtil.getContext().getApplicationInfo().icon
				+ Calendar.getInstance().get(Calendar.MILLISECOND);
	}

	private NotificationCompat.Builder getBaseBuilder(PushMessage pushMsg) {
		NotificationCompat.Builder build = new NotificationCompat.Builder(
				ApplicationUtil.getContext());
		build.setContentTitle(pushMsg.getTitle())
				.setContentText(pushMsg.getContent())
				.setSmallIcon(
						ApplicationUtil.getContext().getApplicationInfo().icon)
				.setDefaults(Notification.DEFAULT_ALL)
				;
		return build;
	}

}

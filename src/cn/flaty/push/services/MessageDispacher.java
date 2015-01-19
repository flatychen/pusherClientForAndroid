package cn.flaty.push.services;

import android.support.v4.app.NotificationCompat;
import cn.flaty.R;
import cn.flaty.push.model.GenericMessage;

public final class MessageDispacher extends MessageSupport implements
		Receiveable {

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
//		GenericMessage gm = new GenericMessage(msg);
//		if (gm.getCommond() == GenericMessage.text) {
//		}
//		NotificationCompat.Builder build = new NotificationCompat.Builder()
		
		
	}

}

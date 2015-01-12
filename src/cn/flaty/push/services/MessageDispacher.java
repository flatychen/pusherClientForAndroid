package cn.flaty.push.services;

import cn.flaty.push.model.GenericMessage;


public final class MessageDispacher extends MessageSupport implements Receiveable {

	@Override
	public void receiveMsg(String msg) {
		GenericMessage gm = new GenericMessage(msg);
	}
	
	
}

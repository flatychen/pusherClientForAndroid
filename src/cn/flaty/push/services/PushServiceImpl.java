package cn.flaty.push.services;


public final class PushServiceImpl extends PushSupport implements PushService {

	@Override
	public void receiveMsg(String msg) {
		System.out.println(msg);
	}
	
	
}

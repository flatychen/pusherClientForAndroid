package cn.flaty.push;

import android.content.Context;
import android.content.Intent;
import cn.flaty.push.services.MessageService;
import cn.flaty.push.utils.ApplicationUtil;

/**
 * 推送启动器
 * 
 * @author flaty
 * 
 */
public class PushBootStrap {
	

	/**
	 * 使用CAS
	 */
	private static volatile PushBootStrap push;
	
	private PushBootStrap() {}

	public static PushBootStrap getInstance() {
		// cas 比较
		if (push == null) {
			synchronized (PushBootStrap.class) {
				return new PushBootStrap();
			}
		}
		return push;
	}

	

	public void start(Context applicationContext) {
		this.init(applicationContext);
	}
	
	private void init(Context context){
		ApplicationUtil.init(context);
		this.connServer();
	}
	
	private void connServer() {
		Intent intent = new Intent(ApplicationUtil.getContext(),MessageService.class);
		ApplicationUtil.getContext().startService(intent);
	}
	
}

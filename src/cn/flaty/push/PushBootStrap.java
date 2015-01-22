package cn.flaty.push;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.flaty.push.services.MessageDispacher;
import cn.flaty.push.services.MessageService;
import cn.flaty.push.utils.ApplicationUtil;
import cn.flaty.push.utils.ServiceUtil;

/**
 * 推送启动器
 * 
 * @author flaty
 * 
 */
public class PushBootStrap {

	public static final String host = "192.183.3.178";

	public static final int port = 11111;
	
	private static String TAG = "PushBootStrap";

	private static volatile PushBootStrap push;

	private PushBootStrap() {
	}

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
		// init something
		ApplicationUtil.init(applicationContext.getApplicationContext());
		
		
		// connect
		this.connServer(applicationContext);
	}


	
	
	/**
	 * 连接服务器
	 */
	private void connServer(Context applicationContext) {
		
		
		
		// 检测是否启动
		if (ServiceUtil.isServiceRunning(ApplicationUtil.getContext(),
				"cn.flaty.services.MessageService")) {
			MessageDispacher.getInstance().connect(PushBootStrap.host, PushBootStrap.port);
		} else {
			Intent intent = new Intent(ApplicationUtil.getContext(),
					MessageService.class);
			ApplicationUtil.getContext().startService(intent);
		}
	}

}

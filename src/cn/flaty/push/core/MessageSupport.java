package cn.flaty.push.core;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import cn.flaty.push.entity.ClientInfo;
import cn.flaty.push.entity.GenericMessage;
import cn.flaty.push.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.push.nio.ReadWriteHandler;
import cn.flaty.push.nio.ReadWriteHandler.ChannelReadListener;
import cn.flaty.push.nio.ReadWriteHandler.ChannelWriteListener;
import cn.flaty.push.nio.SimpleEventLoop;
import cn.flaty.push.nio.SimpleEventLoop.STATE;
import cn.flaty.push.utils.DeviceUtil;
import cn.flaty.push.utils.DigestUtils;
import cn.flaty.push.utils.NetWorkUtil;
import cn.flaty.push.utils.PackageUtils;

import com.alibaba.fastjson.JSON;

public abstract class MessageSupport implements Receiveable {

	private static volatile ClientInfo clientInfo;

	public static Context applicationContext;

	private static String TAG = "MessageSupport";

	private static int MAX_RECONNCNT = 3;

	private AtomicInteger connCount = null;

	private static int HEART_BEAT_TIME = 30;

	private static int HEART_BEAT_DEPLAY = 5;

	private ScheduledExecutorService ses = null;

	private ExecutorService es = null;

	private Future<Integer> nioEvent;

	private ReadWriteHandler readWriteHandler;

	public MessageSupport(Context applicationContext) {
		super();
		MessageSupport.applicationContext = applicationContext;
		this.es = Executors.newFixedThreadPool(1);
	}

	@Deprecated
	private void heartBeat() {
		ses = Executors.newScheduledThreadPool(1);
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("push-heartBeat");
				ClientInfo ci = new ClientInfo();
				Log.i(TAG, "心跳");
				ci.setDid(DigestUtils.md5(DeviceUtil
						.getMixDeviceId(applicationContext)));
				readWriteHandler.doWrite(GenericMessage.client_heart
						+ JSON.toJSONString(ci));
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void connect(final String host, final int port) {
		if (NetWorkUtil.isConnected(applicationContext)) {
			Log.i(TAG, "开始连接服务器");
			this.connect0(host, port);
		} else {
			Log.i(TAG, "无网络连接");
		}
	}

	private void connect0(final String host, final int port) {
		this.connCount = new AtomicInteger(0);
		this.readWriteHandler = ReadWriteHandler.getInstance(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		if (SimpleEventLoop.state == STATE.stop) {
			nioEvent = readWriteHandler.connect(es);
		}

	}

	private static String prepareDeviceInfo() {
		if (clientInfo == null) {
			clientInfo = new ClientInfo();
			try {
				String appKey = (String) applicationContext.getPackageManager()
						.getApplicationInfo(
								applicationContext.getPackageName(),
								PackageManager.GET_META_DATA).metaData
						.get(ClientInfo.APPKEY);
				clientInfo.setAppKey(appKey);
				clientInfo.setDid(DigestUtils.md5(DeviceUtil
						.getMixDeviceId(applicationContext)));
				clientInfo.setAppVer(PackageUtils
						.getAppVersionCode(applicationContext));
				clientInfo.setOs(DeviceUtil.getAndroidVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return GenericMessage.client_connected + JSON.toJSONString(clientInfo);

	}

	private AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareDeviceInfo());
			heartBeat();
		}

		@Override
		public void fail() {
			if (connCount.incrementAndGet() <= MAX_RECONNCNT
					&& SimpleEventLoop.STATE.connnected != SimpleEventLoop.state) {
				try {

					Log.i(TAG, MessageFormat.format("建立连接失败，{0}秒后重试第{1}次",
							20 * connCount.get(), connCount.get()));
					Thread.sleep(20000 * connCount.get());
					nioEvent = readWriteHandler.connect(es);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (connCount.get() > MAX_RECONNCNT) {
				Log.i(TAG, MessageFormat.format("{0}次连接均失败！停止连接~~ ",
						MAX_RECONNCNT));
				SimpleEventLoop.state = STATE.stop;
			}
		}
	};

	private ChannelReadListener simpleChannelReadListener = new ChannelReadListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			nioEvent.cancel(true);
			ses.shutdownNow();
		}
	};

	private ChannelWriteListener simpleChannelWriteListener = new ChannelWriteListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			nioEvent.cancel(true);
			ses.shutdownNow();
		}
	};

}

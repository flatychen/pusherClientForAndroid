package cn.flaty.push.services;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Intent;
import android.util.Log;
import cn.flaty.push.PushBootStrap;
import cn.flaty.push.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.push.nio.ReadWriteHandler;
import cn.flaty.push.nio.ReadWriteHandler.ChannelReadListener;
import cn.flaty.push.nio.ReadWriteHandler.ChannelWriteListener;
import cn.flaty.push.nio.SimpleEventLoop;
import cn.flaty.push.nio.SimpleEventLoop.STATE;
import cn.flaty.push.utils.ApplicationUtil;
import cn.flaty.push.utils.NetWorkUtil;
import cn.flaty.push.utils.ServiceUtil;

public abstract class MessageSupport implements Receiveable {

	private static String TAG = "MessageSupport";

	private static int MAX_RECONNCNT = 3;

	private int reConnCnt = 0;

	private static int HEART_BEAT_TIME = 5;

	private static int HEART_BEAT_DEPLAY = 25;

	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private ExecutorService es = Executors.newFixedThreadPool(1);

	private ReadWriteHandler readWriteHandler;

	public MessageSupport() {
		super();
	}

	private void heartBeat() {
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				//Log.i(TAG, "~~心跳~~");
				//readWriteHandler.doWrite("心跳测试");
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void connect(final String host, final int port) {
		if (NetWorkUtil.isNetConnected()) {
			Log.i(TAG, "开始连接服务器");
			this.connect0(host, port);
		} else {
			Log.i(TAG, "无网络");
		}
	}

	public void connect0(final String host, final int port) {

		this.readWriteHandler = ReadWriteHandler.getInstance(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterConnectListener(simpleAfterConnectListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		if (SimpleEventLoop.state == STATE.stop) {
			readWriteHandler.connect(es);
		}

	}

	private String prepareDeviceInfo() {
		return "开始连接";
	}

	protected void sendMsg(String msg) {
		System.out.println(msg);
	}

	private AfterConnectListener simpleAfterConnectListener = new AfterConnectListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareDeviceInfo());
			heartBeat();
		}

		@Override
		public void fail() {
			if (reConnCnt++ < MAX_RECONNCNT
					&& SimpleEventLoop.STATE.connnected != SimpleEventLoop.state) {
				try {

					Log.i(TAG, MessageFormat.format("建立连接失败，{0}秒后重试第{1}次",
							20 * reConnCnt, reConnCnt));
					Thread.sleep(20000 * reConnCnt);
					readWriteHandler.connect(es);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (reConnCnt >= MAX_RECONNCNT) {
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
			ses.shutdownNow();
			es.shutdownNow();
		}
	};

	private ChannelWriteListener simpleChannelWriteListener = new ChannelWriteListener() {

		@Override
		public void success() {
		}

		@Override
		public void fail() {
			ses.shutdown();
			es.shutdown();
		}
	};

}

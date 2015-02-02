package cn.flaty.push.services;

import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

	private  AtomicInteger connCount = null;

	private static int HEART_BEAT_TIME = 5;

	private static int HEART_BEAT_DEPLAY = 25;

	private ScheduledExecutorService ses = null;

	private ExecutorService es = null;
	
	private Future<Integer> nioEvent;

	private ReadWriteHandler readWriteHandler;

	public MessageSupport() {
		super();
		this.es = Executors.newFixedThreadPool(1);
	}

	private void heartBeat() {
		ses = Executors.newScheduledThreadPool(1);
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("push-heartBeat");
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
			Log.i(TAG, "无网络连接");
		}
	}

	public void connect0(final String host, final int port) {
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

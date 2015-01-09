package cn.flaty.push.services;

import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.flaty.push.nio.ReadWriteHandler;
import cn.flaty.push.nio.AcceptHandler.AfterAcceptListener;
import cn.flaty.push.nio.ReadWriteHandler.ChannelReadListener;
import cn.flaty.push.nio.ReadWriteHandler.ChannelWriteListener;
import cn.flaty.push.utils.LogUtil;

public abstract class PushSupport implements PushService{


	private static int MAX_RECONNCNT = 5;

	private int reConnCnt = 0;

	private static int HEART_BEAT_TIME = 5;

	private static int HEART_BEAT_DEPLAY = 5;

	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

	private ExecutorService es = Executors.newFixedThreadPool(1);

	private ReadWriteHandler readWriteHandler;

	public PushSupport() {
		super();
	}


	private void heartBeat() {
		ses.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				LogUtil.i("----> 心跳~~");
				//readWriteHandler.doWrite("心跳测试");
			}
		}, HEART_BEAT_DEPLAY, HEART_BEAT_TIME, TimeUnit.SECONDS);
	}

	public void connect(final String host, final int port) {

		this.readWriteHandler = new ReadWriteHandler(this);
		readWriteHandler.InitEventLoop(host, port);
		readWriteHandler.setAfterAcceptListener(simpleAfterAcceptListener);
		readWriteHandler.setChannelReadListener(simpleChannelReadListener);
		readWriteHandler.setChannelWriteListener(simpleChannelWriteListener);
		// 异步连接开始
		es.submit(readWriteHandler);

	}

	private String prepareDeviceInfo() {
		return "开始连接";
	}

	protected void sendMsg(String msg) {
		System.out.println(msg);
	}

	private AfterAcceptListener simpleAfterAcceptListener = new AfterAcceptListener() {
		@Override
		public void success() {
			readWriteHandler.doWrite(prepareDeviceInfo());
			//heartBeat();
		}

		@Override
		public void fail() {
			if (reConnCnt++ < MAX_RECONNCNT) {
				try {
					LogUtil.i(MessageFormat.format(
							"---->建立连接失败，递递{0}次重试，现重试第{1}次", MAX_RECONNCNT,
							reConnCnt));
					Thread.sleep(2000 * reConnCnt);
					es.submit(readWriteHandler);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				LogUtil.i("---->5次连接均失败，关闭任务！ ");
				es.shutdown();
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

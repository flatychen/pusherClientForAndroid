package cn.flaty.push.nio;

import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import cn.flaty.push.nio.SimpleEventLoop.STATE;

public class ConnectHandler {

	private static String TAG = "ConnectHandler";

	private AfterConnectListener afterConnectListener;

	private volatile boolean connecting;

	public boolean connect(Selector selector, SocketChannel channel,
			InetSocketAddress socket, int timeOut) {
		SimpleEventLoop.state = STATE.connecting;
		// 提供简单超时检查
		this.connecting = true;
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				connecting = false;
			}
		}, timeOut);
		try {
			// 开始连接
			channel.configureBlocking(false);
			boolean connected = false;
			channel.connect(socket);
			while (connecting && !(connected)) {
				try {
					Thread.sleep(200);
					connected = channel.finishConnect();
				} catch (Exception e) {
					continue;
				}

			}

			if (!connected) {
				throw new SocketTimeoutException(" socket 连接超时 ");
			}

			channel.register(selector, SelectionKey.OP_READ);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
			SimpleEventLoop.clearUp(selector, channel, null);
			afterConnectListener.fail();
			return false;
		}
		afterConnectListener.success();
		SimpleEventLoop.state = STATE.connnected;
		Log.i(TAG, " 连接建立成功");
		return true;

	}

	public AfterConnectListener getAfterConnectListener() {
		return afterConnectListener;
	}

	public void setAfterConnectListener(
			AfterConnectListener afterConnectListener) {
		this.afterConnectListener = afterConnectListener;
	}

	public static interface AfterConnectListener {
		void success();

		void fail();
	}

}

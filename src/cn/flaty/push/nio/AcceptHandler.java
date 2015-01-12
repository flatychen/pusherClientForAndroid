package cn.flaty.push.nio;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class AcceptHandler {
	
	private static String TAG = "AcceptHandler";

	private volatile boolean connecting = true;

	private Selector selector;

	private SocketChannel channel;

	public void connect(Selector s, SelectionKey key, int timeOut)
			throws IOException {
		this.selector = s;
		this.channel = (SocketChannel) key.channel();

		// 提供超时检查
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				connecting = false;
			}
		}, timeOut);

		// 开始连接
		boolean connected = false;
		while (connecting && !connected) {
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

		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ);
		// channel.register(selector, SelectionKey.OP_WRITE);
		Log.i(TAG," 连接建立成功 ");

	}

	public static interface AfterAcceptListener {
		void success();

		void fail();
	}

}

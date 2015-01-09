package cn.flaty.push.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import cn.flaty.push.nio.AcceptHandler.AfterAcceptListener;
import cn.flaty.push.utils.AssertUtils;
import cn.flaty.push.utils.LogUtil;

public class SimpleEventLoop {



	private InetSocketAddress socket;

	/**
	 * accept
	 */
	private AcceptHandler accept;

	/**
	 * io handlers
	 */
	private ReadWriteHandler readWrite;

	private volatile Selector selector;

	private volatile SelectionKey key;

	public SimpleEventLoop(InetSocketAddress socket) {
		super();
		this.socket = socket;
	}

	public void openChannel() throws IOException {
		this.validate();
		// 获得一个Socket通道
		SocketChannel channel = SocketChannel.open();
		// 设置通道为非阻塞
		channel.configureBlocking(false);
		// 获得一个通道管理器
		this.selector = Selector.open();
		// 客户端连接服务器,其实方法执行并没有实现连接，需要在listen（）方法中调
		// 用channel.finishConnect();才能完成连接
		channel.connect(this.socket);
		// 将通道管理器和该通道绑定，并为该通道注册SelectionKey.OP_CONNECT事件。
		channel.register(selector, SelectionKey.OP_CONNECT);

	}

	/**
	 * FIXME cancle write key bug?
	 * 
	 * @throws IOException
	 */
	public void eventLoop() throws IOException {
		// 轮询访问selector
		while (selector.select() > 0) {
			// 获得selector中选中的项的迭代器
			Iterator<SelectionKey> keys = this.selector.selectedKeys()
					.iterator();
			while (keys.hasNext()) {
				key = keys.next();
				// 删除已选的key,以防重复处理
				keys.remove();

				if (key.isValid()) {
					if (key.isConnectable()) {
					// 连接事件
						AfterAcceptListener listener = readWrite
								.getAfterAcceptListener();
						try {
							accept.connect(selector, key);
						} catch (Exception e) {
							LogUtil.e("---->"+e.getMessage());
							clear();
							listener.fail();
							return;
						}

						this.initReadWriteHandler();
						listener.success();
						

					} else if (key.isReadable()) {
					// 可读事件	
						readWrite.doRead(key);
					}

//					else if (key.isWritable()) {
//						key.cancel();
//					
//
//					}

				}

			}

		}
	}

	private void initReadWriteHandler() {
		this.readWrite.setSelector(selector);
		this.readWrite.setChannel(key.channel());
	}

	private void validate() {
		AssertUtils.notNull(accept, "----> accept 属性不能主空");
		AssertUtils.notNull(readWrite, "----> readWrite 属性不能主空");

	}

	public void setAccept(AcceptHandler accept) {
		this.accept = accept;
	}

	public void setReadWrite(ReadWriteHandler readWrite) {
		this.readWrite = readWrite;
	}


	
	private void clear(){
		key.cancel();
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

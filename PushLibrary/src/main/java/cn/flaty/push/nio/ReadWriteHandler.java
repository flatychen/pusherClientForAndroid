package cn.flaty.push.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.util.Log;
import cn.flaty.push.core.Receiveable;
import cn.flaty.push.nio.ConnectHandler.AfterConnectListener;
import cn.flaty.push.nio.SimpleEventLoop.STATE;
import cn.flaty.push.pushFrame.FrameHead;
import cn.flaty.push.pushFrame.SimplePushHead;
import cn.flaty.push.pushFrame.SimplePushInFrame;
import cn.flaty.push.pushFrame.SimplePushOutFrame;
import cn.flaty.push.utils.ByteBufUtil;

public class ReadWriteHandler implements Callable<Integer> {

	private static String TAG = "ReadWriteHandler";

	private FrameHead frameHeader;

	private ByteBuf readBuf;

	private ByteBuf writeBuf;

	/**
	 * 读通道监听器
	 */
	private ChannelReadListener channelReadListener;

	/**
	 * 写通道监听器
	 */
	private ChannelWriteListener channelWriteListener;

	/**
	 * 选择器，用于注册
	 */
	private Selector selector;

	/**
	 * socket 通道
	 */
	private SocketChannel channel;

	/**
	 * 业务逻辑处理
	 */
	private Receiveable pushService;

	/**
	 * nio 事件循环
	 */
	private SimpleEventLoop eventLoop;

	private ConnectHandler connectHandler;

	private static volatile ReadWriteHandler readWriteHandler = null;

	public static ReadWriteHandler getInstance(Receiveable pushService) {
		if (readWriteHandler == null) {
			synchronized (ReadWriteHandler.class) {
				readWriteHandler = new ReadWriteHandler(pushService,
						new SimplePushHead());
			}
		}
		return readWriteHandler;
	}

	private ReadWriteHandler(Receiveable pushService) {
		this(pushService, new SimplePushHead());
	}

	private ReadWriteHandler(Receiveable pushService, FrameHead frameHeader) {
		super();
		this.pushService = pushService;
		this.frameHeader = frameHeader;

	}

	public void InitEventLoop(String host, int port) {
		this.connectHandler = new ConnectHandler();
		eventLoop = new SimpleEventLoop(new InetSocketAddress(host, port));
		eventLoop.setConnect(connectHandler);
		eventLoop.setReadWrite(this);
		this.readBuf = ByteBufUtil.ByteBuf();
		this.writeBuf = ByteBufUtil.compositBuf();
	}

	public void doWrite(String msg) {
		SimplePushOutFrame frame = new SimplePushOutFrame(frameHeader, msg);
		writeBuf.put(frame.getLength());
		writeBuf.put(frame.getHead());
		writeBuf.put(frame.getBody());
		try {
			this.write();
		} catch (Exception e) {
			SimpleEventLoop.state = STATE.stop;
			this.channelWriteListener.fail();
			Log.e(TAG, e.toString());
			return;
		}

	}

	public void write() throws IOException {
		writeBuf.flip();
		int byteToWrite = 0;
		if (writeBuf.isCompositBuf()) {
			ByteBuffer buffers[] = writeBuf.nioBuffers();
			for (ByteBuffer byteBuffer : buffers) {
				while (byteBuffer.remaining() > 0) {
					byteToWrite = channel.write(byteBuffer) + byteToWrite;
				}
			}
			writeBuf.position(writeBuf.position() + byteToWrite);
		} else {
			channel.write(writeBuf.nioBuffer());
		}
		writeBuf = writeBuf.resetBuf();
	}

	public void read() throws IOException {
		int byteToRead = 0;
		if (readBuf.isCompositBuf()) {
			ByteBuffer buffers[] = readBuf.nioBuffers();
			for (ByteBuffer byteBuffer : buffers) {
				byteToRead = channel.read(byteBuffer);
				readBuf.position(readBuf.position() + byteToRead);
			}
		} else {
			byteToRead = channel.read(readBuf.nioBuffer());
		}

	}

	public void doRead(SelectionKey key) {
		try {
			this.read();
		} catch (IOException e) {
			SimpleEventLoop.state = STATE.stop;
			SimpleEventLoop.clearUp(selector, channel, key);
			Log.e(TAG, "doRead fail!" + e.getMessage());
			this.channelReadListener.fail();
		}

		// 处理 tcp 强制断开连接 rst
		if (readBuf.position() == 0) {
			SimpleEventLoop.state = STATE.stop;
			SimpleEventLoop.clearUp(selector, channel, key);
			Log.e(TAG, "服务器端重置连接");
			this.channelReadListener.fail();
		}
		// 切包，直到拿到完整的包再纪续执行
		byte[] frameBytes = this.splitFrame();
		if (frameBytes == null) {
			return;
		}

		SimplePushInFrame frame = new SimplePushInFrame(frameHeader, frameBytes);
		String s = frame.getBody();

		// 业务逻辑处理
		pushService.receiveMsg(s);

		// try {
		// channel.register(selector, SelectionKey.OP_READ);
		// } catch (ClosedChannelException e) {
		// Log.e(TAG,e.getMessage());
		// e.printStackTrace();
		// }
	}

	/**
	 * 
	 * 
	 * @return
	 */
	private byte[] splitFrame() {

		//
		// 拆包开始;
		// 如果读取的字节大于包头长度，则一直等到长度为止，
		// 否则，一直读等到包头长度大于为止;
		//
		int bytesToRead = 0;
		// 反转buf
		readBuf.flip();
		if (readBuf.remaining() > frameHeader.byteLength()) {
			// 必须读到包的长度字节

			byte[] frameLengthBytes = new byte[frameHeader.byteLength()];
			readBuf.get(frameLengthBytes);
			// 包长度大小
			bytesToRead = frameHeader.bytesToInt(frameLengthBytes)
					+ frameHeader.headLength();

			if (readBuf.remaining() < bytesToRead) {
				// 没有完整读到所有包的内容,应继续读取

				if (readBuf.limit() == readBuf.capacity()) {
					// buffer 长度不够 ，需扩展buffer

					int oldBufLength = readBuf.capacity();
					int newBufLength = bytesToRead + frameHeader.byteLength();

					readBuf.position(readBuf.limit());
					// 扩展原包大小
					ByteBuf newBuffer = ByteBufUtil.wrapByteBuf(newBufLength
							- oldBufLength, readBuf);
					readBuf = newBuffer;
					return null;
				}

			}

		} else {
			//
			// 没读到包长度字节，调整buffer，继续读取
			// 注意postion 与 limit 设置
			// 接上次位置，继续读取
			//
			readBuf.position(readBuf.limit());
			readBuf.limit(readBuf.capacity());
			return null;
		}

		// 拆包完毕,已读一个完整的frame
		byte[] frame = new byte[bytesToRead];
		readBuf.get(frame);
		this.readBuf = readBuf.resetBuf();
		return frame;

	}

	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	public void setChannel(SelectableChannel schannel) {
		this.channel = (SocketChannel) schannel;
	}

	@SuppressWarnings("finally")
	public Future<Integer> connect(ExecutorService es) {
		Future<Integer> f = null;
		try {
			f = es.submit(this);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return f;
		}
	}

	public ChannelReadListener getChannelReadListener() {
		return channelReadListener;
	}

	public void setChannelReadListener(ChannelReadListener channelReadListener) {
		this.channelReadListener = channelReadListener;
	}

	public ChannelWriteListener getChannelWriteListener() {
		return channelWriteListener;
	}

	public void setChannelWriteListener(
			ChannelWriteListener channelWriteListener) {
		this.channelWriteListener = channelWriteListener;
	}

	public void setAfterConnectListener(
			AfterConnectListener afterConnectListener) {
		this.connectHandler.setAfterConnectListener(afterConnectListener);
	}

	public static interface ChannelReadListener {
		void success();
		void fail();
	}

	public static interface ChannelWriteListener {
		void success();

		void fail();
	}

	@Override
	public Integer call(){
		try {
			Thread.currentThread().setName("push-nio");
			eventLoop.openChannel();
			eventLoop.eventLoop();
		} catch (IOException e) {
			e.printStackTrace();
			return 1;
		}
		return 0;
	}

}

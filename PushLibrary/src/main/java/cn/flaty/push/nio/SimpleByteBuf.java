package cn.flaty.push.nio;

import java.nio.ByteBuffer;

public class SimpleByteBuf implements ByteBuf{
	

	private ByteBuffer buffer;

	public SimpleByteBuf() {
		this(BUFFER_SIZE);
	}
	
	public SimpleByteBuf(int bufSize) {
		super();
		this.buffer = ByteBuffer.allocate(bufSize);
	}
	
	
	public final SimpleByteBuf clear() {
		buffer.clear();
		return this;
	}

	public final SimpleByteBuf flip() {
		buffer.flip();
		return this;
	}

	public SimpleByteBuf  position(int i) {
		buffer.position(i);
		return this;
	}

	public int  position() {
		return buffer.position();
	}
	
	public SimpleByteBuf  limit(int i) {
		buffer.limit(i);
		return this;
	}

	public int  limit() {
		return buffer.limit();
	}
	
	public int  remaining() {
		return buffer.remaining();
	}

	public int  capacity() {
		return buffer.capacity();
	}

	public final SimpleByteBuf put(byte b[]) {
		buffer.put(b);
		return this;
	}

	public final SimpleByteBuf get(byte b[]) {
		buffer.get(b);
		return this;
	}
	


	@Override
	public int nioBufferSize() {
		return 1;
	}

	@Override
	public ByteBuffer nioBuffer() {
		return this.buffer;
	}

	@Override
	public ByteBuffer[] nioBuffers() {
		return new ByteBuffer[]{buffer};
	}

	@Override
	public ByteBuf get(byte[] dst, int offset, int length) {
		 buffer.get(dst, offset, length);
		 return this;
	}

	@Override
	public ByteBuf put(byte[] src, int offset, int length) {
		buffer.put(src, offset, length);
		return this;
	}

//	@Override
//	public void reset() {
//		this.clear();
//	}
	
	@Override
	public boolean isCompositBuf() {
		return false;
	}

	@Override
	public ByteBuf resetBuf() {
		this.clear();
		return this;
	}

}

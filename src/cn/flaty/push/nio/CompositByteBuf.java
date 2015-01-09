package cn.flaty.push.nio;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import cn.flaty.push.utils.ByteBufUtil;

/**
 * 
 * 组合的bytebuf <br>
 * put 方法可自动扩容
 * 
 * @author flatychen
 * 
 */
public class CompositByteBuf implements ByteBuf {

	private int capacity = 0;

	private int position = 0;

	private int limit = 0;

	private int currentBufferIndex = 0;
	
	private int currentBufferOffset = 0;

	private int buffersSize = 0;

	private List<ByteBuf> buffers;

	public CompositByteBuf() {
		this(1, ByteBufUtil.ByteBuf());
	}

	public CompositByteBuf(ByteBuf buf) {
		this(1, buf);
	}

	public CompositByteBuf(int compontSize, ByteBuf buf) {
		super();
		if (compontSize < 1) {
			throw new IllegalArgumentException("----> size非法，至少大于等于1 ");
		}
		this.buffers = new ArrayList<ByteBuf>();
		buffers.add(buf);
		this.addCompont(compontSize - 1);
		this.currentBufferIndex = compontSize == 1 ? 0 : 1;
		this.buffersSize = compontSize;
		this.position = buf.position();
		this.capacity = this.limit = compontSize == 1 ? buf.capacity() : BUFFER_SIZE * compontSize + buf.capacity();
	}

	private void addCompont(int size) {
		for (int i = 0; i < size; i++) {
			this.buffers.add(new SimpleByteBuf(BUFFER_SIZE));
		}
	}

	public final CompositByteBuf clear() {
		for (ByteBuf buffer : buffers) {
			buffer.clear();
		}
		return this;
	}

	public final CompositByteBuf flip() {
		limit = position;
		position = 0;
		this.currentBufferIndex = 0;
		for (ByteBuf buf : this.buffers) {
			buf.flip();
		}
		return this;
	}

	public final ByteBuf position(int i) {
		if ((i > limit) || (i < 0)) {
			throw new IllegalArgumentException("----> position非法");
		}
		int __offsetSize = i / BUFFER_SIZE;
		int _offset = i % BUFFER_SIZE;
		this.currentBufferIndex = __offsetSize;
		this.currentBufferOffset = _offset;
		
//		if (__offsetSize != 0) {
//			this.currentBufferIndex = _offset == 0 ? __offsetSize
//					: __offsetSize;
//		} else {
//			this.currentBufferIndex = 0;
//		}
		this.position = i;
		return this;
	}

	public final int position() {
		return this.position;
	}

	public final CompositByteBuf limit(int i) {
		if ((i > capacity) || (i < 0))
			throw new IllegalArgumentException();
		this.limit = i;
		return this;
	}

	public final int limit() {
		return this.limit;
	}

	public final int remaining() {
		return this.limit() - this.position();
	}

	public final int capacity() {
		return this.capacity;
	}

	public final CompositByteBuf put(byte b[]) {
		this.put(b, 0, b.length);
		return this;
	}

	public final CompositByteBuf get(byte b[]) {
		this.get(b, 0, b.length);
		return this;
	}

	@Override
	public final int nioBufferSize() {
		return buffersSize;
	}

	@Override
	public final ByteBuffer nioBuffer() {
		return buffers.get(0).nioBuffer();
	}

	@Override
	public final ByteBuffer[] nioBuffers() {
		ByteBuffer[] bufs = new ByteBuffer[buffersSize - currentBufferIndex];
		for (int i = currentBufferIndex, j = 0; i < buffersSize; i++, j++) {
			bufs[j] = buffers.get(i).nioBuffer();
		}
		return bufs;
	}

	@Override
	public final ByteBuf get(byte[] dst, int offset, int length) {
		if (length > this.remaining()) {
			throw new BufferOverflowException();
		}

		int _remaining = 0;
		int _offset = 0;
		int _lastLength = length;

		int i = this.currentBufferIndex;
		for (; i < this.buffersSize; i++) {
			ByteBuf buf = buffers.get(i);
			_remaining = buf.remaining();

			//
			// 判断当前所处buffer含有dst 长度所有数据，否则遍历其它buffer继续get
			//
			if (_lastLength <= _remaining) {
				buf.get(dst, _offset, _lastLength);
				break;
			}

			buf.get(dst, _offset, _remaining);

			_offset = _offset + _remaining;
			_lastLength = _lastLength - _remaining;
		}

		this.position(length);
		return this;
	}

	@Override
	public ByteBuf put(byte[] src, int offset, int length) {
		// 自动扩容
		int _length = length;
		ByteBuf currentBuf = this.buffers.get(this.currentBufferIndex);
		int remaining = currentBuf.remaining();
		if (_length <= remaining) {
			currentBuf.put(src, offset, _length);
		} else {
			
			// 自动扩容开始
			
			// 首先填满当前buf所在剩余容量
			currentBuf.put(src, offset, remaining);
			offset = offset + remaining;
			_length = _length - remaining;

			int sizeToExtend = _length % ByteBuf.BUFFER_SIZE == 0 ? sizeToExtend = _length
					/ ByteBuf.BUFFER_SIZE
					: (_length / ByteBuf.BUFFER_SIZE) + 1;
			
			// 需要扩容
			if(sizeToExtend > 0){
				this.addCompont(sizeToExtend);
				this.capacity = this.limit = this.capacity() + sizeToExtend * ByteBuf.BUFFER_SIZE;
				this.buffersSize = this.buffersSize + sizeToExtend;
			}
			for(int i = this.currentBufferIndex + 1  ; i < this.buffersSize ; i++){
				currentBuf = this.buffers.get(i);
				remaining = currentBuf.remaining();
				if(_length <= remaining){
					currentBuf.put(src, offset, _length);
					break;
				}
				currentBuf.put(src, offset, remaining);
				offset = offset + remaining;
				_length = _length - remaining;
			}

		}
		
		this.position(this.position + length);
		return this;
	}

//	@Override
//	public void reset() {
//		int _size = this.buffersSize  > 2 ? (this.buffersSize / 2 + 1) : this.buffersSize;
//		
//		for (; _size < buffers.size();) {
//			this.buffers.remove(_size);
//		}
//		
//		this.clear();
//		this.position(0);
//		this.buffersSize = _size;
//		this.limit = this.capacity = ByteBuf.BUFFER_SIZE * _size;
//		this.clear();
//		
//	}

	@Override
	public boolean isCompositBuf() {
		return true;
	}

	@Override
	public ByteBuf resetBuf() {
		return buffers.get(0);
	}

}

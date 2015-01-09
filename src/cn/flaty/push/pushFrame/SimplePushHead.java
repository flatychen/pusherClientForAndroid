package cn.flaty.push.pushFrame;

import cn.flaty.push.utils.ByteUtil;

public  class SimplePushHead implements FrameHead {

	public final int FRAME_LENGTH_BYTES = 4;
	
	public final int MAX_LENGTH = Integer.MAX_VALUE;
	
	public final int HEAD_LENGTH_BYTES = 4;

	
	@Override
	public int byteLength() {
		return FRAME_LENGTH_BYTES;
	}

	@Override
	public int maxLength() {
		return MAX_LENGTH;
	}


	@Override
	public int headLength() {
		return HEAD_LENGTH_BYTES;
	}

	@Override
	public int bytesToInt(byte[] b) {
		if(b.length != this.FRAME_LENGTH_BYTES){
			throw new IllegalArgumentException("----> 包长度数组非法");
		}
		return ByteUtil.byteArrayToInt(b);
	}

	@Override
	public byte[] intToBytes(int length) {
		return ByteUtil.intToByteArray(length);
	}

	

}

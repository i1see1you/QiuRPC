package com.qiusuoba.nettyrpc.protocol;  

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

import com.qiusuoba.nettyrpc.common.Constants;

/**
 *请求解码 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class RpcRequestDecode extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 2) {
			return null;
		}
		byte byte1 = buffer.readByte();
		byte byte2 = buffer.readByte();
		if (byte1!=Constants.MAGIC_HIGH || byte2!=Constants.MAGIC_LOW) {
			throw new RuntimeException("magic number not right");
		}
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		RpcRequest request = MySerializerFactory.getInstance(Constants.DEFAULT_RPC_CODE_MODE).decodeRequest(in);
		return request;
	}

}

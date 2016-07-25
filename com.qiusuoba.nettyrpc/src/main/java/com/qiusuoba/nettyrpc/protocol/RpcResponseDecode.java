package com.qiusuoba.nettyrpc.protocol;  

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

/**
 *响应解码 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class RpcResponseDecode  extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext context, Channel channel,
			ChannelBuffer buffer) throws Exception {
		if (buffer.readableBytes() < 4) {
			return null;
		}
		int length = buffer.getInt(buffer.readerIndex());
		if (buffer.readableBytes() < length + 4) {
			return null;
		}
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		RpcResponse response = ProtobufSerializer.getInstance().decodeResponse(in);
		return response;
	}
}

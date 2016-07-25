package com.qiusuoba.nettyrpc.protocol;  

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

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
		if (buffer.readableBytes() < 4) {
			return null;
		}
		int length = buffer.getInt(buffer.readerIndex());
		if (buffer.readableBytes() < length + 4) {
			return null;
		}
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		RpcRequest request = ProtobufSerializer.getInstance().decodeRequest(in);
		return request;
	}

}

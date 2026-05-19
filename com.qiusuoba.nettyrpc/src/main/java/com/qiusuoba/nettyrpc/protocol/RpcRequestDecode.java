package com.qiusuoba.nettyrpc.protocol;  

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import com.qiusuoba.nettyrpc.common.Constants;

/**
 *请求解码 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class RpcRequestDecode extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 2) {
			return;
		}
		in.markReaderIndex();
		byte byte1 = in.readByte();
		byte byte2 = in.readByte();
		if (byte1 != Constants.MAGIC_HIGH || byte2 != Constants.MAGIC_LOW) {
			throw new RuntimeException("magic number not right");
		}
		ByteBufInputStream input = new ByteBufInputStream(in);
		RpcRequest request = MySerializerFactory.getInstance(Constants.DEFAULT_RPC_CODE_MODE).decodeRequest(input);
		out.add(request);
	}

}

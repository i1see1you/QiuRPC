package com.qiusuoba.nettyrpc.protocol;  

import java.io.ByteArrayOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import com.qiusuoba.nettyrpc.common.Constants;

/**
 *响应编码
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class RpcResponseEncode extends MessageToByteEncoder<RpcResponse> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RpcResponse response, ByteBuf out) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);
		baos.write(Constants.MAGIC_BYTES);
		MySerializerFactory.getInstance(Constants.DEFAULT_RPC_CODE_MODE).encodeResponse(baos, response);
		out.writeBytes(baos.toByteArray());
	}

}

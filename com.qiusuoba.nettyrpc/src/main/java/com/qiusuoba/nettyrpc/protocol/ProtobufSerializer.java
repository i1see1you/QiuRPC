package com.qiusuoba.nettyrpc.protocol;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.qiusuoba.nettyrpc.util.IOUtils;
import com.qiusuoba.nettyrpc.util.SchemaCache;

/**
 *Protobuf序列化封装的辅助类 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class ProtobufSerializer implements ISerializer{
	
	private static final ProtobufSerializer INSTANCE = new ProtobufSerializer();

	private ProtobufSerializer() {
	}

	public static ISerializer getInstance() {
		return INSTANCE;
	}

	protected <T> int writeObject(LinkedBuffer buffer, T object,
			Schema<T> schema) {
		return ProtobufIOUtil.writeTo(buffer, object, schema);
	}

	protected <T> void parseObject(byte[] bytes, T template, Schema<T> schema) {
		ProtobufIOUtil.mergeFrom(bytes, template, schema);
	}
	
	protected <T> void parseObject(InputStream in, T template, Schema<T> schema) {
		try {
			ProtobufIOUtil.mergeFrom(in, template, schema);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public RpcRequest decodeRequest(InputStream inputStream)
			throws IOException {
		return decode(inputStream, new RpcRequest());
	}

	public void encodeResponse(OutputStream outputStream, RpcResponse result)
			throws  IOException {
		encode(outputStream, result);
	}

	public RpcResponse decodeResponse(InputStream inputStream)
			throws  IOException {
		return decode(inputStream, new RpcResponse());
	}

	public void encodeRequest(OutputStream outputStream, RpcRequest request)
			throws  IOException {
		encode(outputStream, request);
	}

	/**
	 * 将对象序列化为二进制流
	 * @param out
	 * @param object
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> void encode(OutputStream out, T object) throws IOException {
		LinkedBuffer buffer = LinkedBuffer.allocate();
		Schema schema = null;
		if (null == object) {
			schema = SchemaCache.getSchema(Object.class);
		} else {
			schema = SchemaCache.getSchema(object.getClass());
		}
		int length = writeObject(buffer, object, schema);
//		IOUtils.writeInt(out, length);
		LinkedBuffer.writeTo(out, buffer);
	}

	/**
	 * 将二进制流解析为对象
	 * @param in
	 * @param template
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> T decode(InputStream in, T template) throws IOException {
		Schema schema = SchemaCache.getSchema(template.getClass());
//		int length = IOUtils.readInt(in);
//		byte[] bytes = new byte[length];
//		IOUtils.readFully(in, bytes, 0, length);
		parseObject(in, template, schema);
		return template;
	}
}

package com.qiusuoba.nettyrpc.protocol;  

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
  
public interface ISerializer {
	
	public RpcRequest decodeRequest(InputStream inputStream)
			throws IOException ;

	public void encodeResponse(OutputStream outputStream, RpcResponse result)
			throws  IOException ;

	public RpcResponse decodeResponse(InputStream inputStream)
			throws  IOException ;

	public void encodeRequest(OutputStream outputStream, RpcRequest request)
			throws  IOException ;
}

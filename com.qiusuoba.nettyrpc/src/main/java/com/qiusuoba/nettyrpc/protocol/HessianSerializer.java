package com.qiusuoba.nettyrpc.protocol;  

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
  
public class HessianSerializer implements ISerializer{
	private static final HessianSerializer INSTANCE = new HessianSerializer();
	
	private HessianSerializer()
	{
		
	}
	
	public static ISerializer getInstance() {
		return INSTANCE;
	}

	@Override
	public RpcRequest decodeRequest(InputStream inputStream) throws IOException {
		Hessian2Input in = new Hessian2Input(inputStream);  
		RpcRequest obj = (RpcRequest)in.readObject();  
		return obj;
	}

	@Override
	public void encodeResponse(OutputStream outputStream, RpcResponse result)
			throws IOException {
         Hessian2Output out = new Hessian2Output(outputStream);  
         out.writeObject(result);  
         out.flush();
	}

	@Override
	public RpcResponse decodeResponse(InputStream inputStream)
			throws IOException {
		  Hessian2Input in = new Hessian2Input(inputStream);  
		  RpcResponse obj = (RpcResponse)in.readObject();  
		  return obj;
	}

	@Override
	public void encodeRequest(OutputStream outputStream, RpcRequest request)
			throws IOException {
        Hessian2Output out = new Hessian2Output(outputStream);  
        out.writeObject(request);
        out.flush();
	}
	
}

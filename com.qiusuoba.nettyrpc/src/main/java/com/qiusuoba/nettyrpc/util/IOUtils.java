package com.qiusuoba.nettyrpc.util;  

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *IO util类 
 *@Author:caimin 
 *@Since:2016年7月25日  
 *@Version:
 */
public class IOUtils {

	/**
	 * block until exactly <code>length</code> bytes read into
	 * <code>bytes</code>
	 * 
	 * @param in
	 * @param bytes
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public static void readFully(InputStream in, byte[] bytes, int offset,
			int length) throws IOException {
		if (length < 0) {
			throw new IndexOutOfBoundsException();
		}
		int n = 0;
		while (n < length) {
			int count = in.read(bytes, offset + n, length - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}

	/**
	 * write an integer to the output stream
	 * 
	 * @param out
	 * @param value
	 * @throws IOException
	 */
	public static void writeInt(OutputStream out, int value) throws IOException {
		out.write((value >>> 24) & 0xFF);
		out.write((value >>> 16) & 0xFF);
		out.write((value >>> 8) & 0xFF);
		out.write((value >>> 0) & 0xFF);
	}

	/**
	 * read an integer from the input stream
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static int readInt(InputStream in) throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new EOFException();
		}
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public static void closeQuietly(Closeable closeable) {
		if (null == closeable) {
			return;
		}
		try {
			closeable.close();
		} catch (Throwable t) {
		}
	}

	public static void closeQuietly(Socket socket) {
		if (null == socket) {
			return;
		}
		if (!socket.isInputShutdown()) {
			try {
				socket.shutdownInput();
			} catch (IOException e) {
			}
		}
		if (!socket.isOutputShutdown()) {
			try {
				socket.shutdownOutput();
			} catch (IOException e) {
			}
		}
		try {
			socket.close();
		} catch (Throwable t) {
		}
	}


}

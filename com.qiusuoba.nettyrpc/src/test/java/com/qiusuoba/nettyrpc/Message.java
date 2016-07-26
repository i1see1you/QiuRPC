package com.qiusuoba.nettyrpc;  

import java.io.Serializable;
import java.util.Date;
  
public class Message implements Serializable{
	private String msg;
	
	private Date data;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}
	
}

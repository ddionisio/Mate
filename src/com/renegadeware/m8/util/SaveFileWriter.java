package com.renegadeware.m8.util;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

public class SaveFileWriter {
	public final HashMap<String, byte[]> dataMap;
	
	public SaveFileWriter() {
		dataMap = new HashMap<String, byte[]>();
	}
	
	public void save(OutputStream os) throws Exception {
		int num = dataMap.size();
		
		byte[] fourBytes = new byte[4];
		
		//write the number of elements
		Util.intToByteArray(num, fourBytes, 0);
		os.write(fourBytes);
		
		Set<java.util.Map.Entry<String, byte[]>> data = dataMap.entrySet();
		for(java.util.Map.Entry<String, byte[]> e : data) {
			//write name
			String name = e.getKey();
			
			byte[] nameBuf = name.getBytes();
			
			if(nameBuf.length > 0) {
				os.write(nameBuf);
				os.write(0);
				
				byte[] d = e.getValue();
				if(d != null && d.length > 0) {
					//write the byte length
					Util.intToByteArray(d.length, fourBytes, 0);
					os.write(fourBytes);
					
					//write the bytes
					os.write(d);
				}
				else {
					//just put 4 wasted zero bytes
					Util.intToByteArray(0, fourBytes, 0);
					os.write(fourBytes);
				}
			}
			else {
				os.write(0);
			}
		}
	}
	
	public SaveFileWriter putBoolean(String key, boolean val) {
		byte[] b = new byte[1];
		b[0] = (byte)(val ? 1 : 0);
		
		dataMap.put(key, b);
		
		return this;
	}
	
	public SaveFileWriter putByte(String key, byte val) {
		byte[] b = new byte[1];
		b[0] = val;
		
		dataMap.put(key, b);
		
		return this;
	}
	
	public SaveFileWriter putFloat(String key, float val) {
		byte[] b = new byte[4];
		Util.floatToByteArray(val, b, 0);
		
		dataMap.put(key, b);
		
		return this;
	}
	
	public SaveFileWriter putInt(String key, int val) {
		byte[] b = new byte[4];
		Util.intToByteArray(val, b, 0);
		
		dataMap.put(key, b);
		
		return this;
	}
	
	public SaveFileWriter putLong(String key, long val) {
		byte[] b = new byte[8];
		Util.longToByteArray(val, b, 0);
		
		dataMap.put(key, b);
		
		return this;
	}
	
	public SaveFileWriter putString(String key, String val) {
		dataMap.put(key, val.getBytes());
		
		return this;
	}
	
	public SaveFileWriter putBytes(String key, byte[] b) {
		dataMap.put(key, b);
		
		return this;
	}
}

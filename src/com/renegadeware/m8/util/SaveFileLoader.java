package com.renegadeware.m8.util;

import java.io.InputStream;

public class SaveFileLoader {
	public FixedSizeMap<String, byte[]> dataMap;
	
	public SaveFileLoader(InputStream is) throws Exception {
		byte[] fourBytes = new byte[4];
		
		//read the headers
		is.read(fourBytes, 0, 4);
		int numHeaders = Util.byteArrayToInt(fourBytes, 0);
		
		dataMap = new FixedSizeMap<String, byte[]>(numHeaders);
		
		for(int i = 0; i < numHeaders; i++) {
			//read the string
			StringBuffer str = new StringBuffer();
			int c = is.read();
			while(c != 0 && c != -1) {
				str.append((char)c);
				c = is.read();
			}
			
			if(str.length() > 0) {
				//read number of bytes
				is.read(fourBytes, 0, 4);
				int numBytes = Util.byteArrayToInt(fourBytes, 0);
				byte[] bytes = null;
				
				if(numBytes > 0) {
					bytes = new byte[numBytes];
					is.read(bytes, 0, numBytes);
				}
				else {
					bytes = null;
				}
				
				dataMap.put(str.toString(), bytes);
			}
		}
	}
	
	public boolean getBoolean(String key, boolean defVal) {
		byte[] b = dataMap.get(key);
		if(b != null && b.length > 0) {
			return b[0] == 1;
		}
		
		return defVal;
	}
	
	public float getFloat(String key, float defVal) {
		byte[] b = dataMap.get(key);
		if(b != null && b.length == 4) {
			return Util.byteArrayToFloat(b, 0);
		}
		
		return defVal;
	}
	
	public int getInt(String key, int defVal) {
		byte[] b = dataMap.get(key);
		if(b != null && b.length == 4) {
			return Util.byteArrayToInt(b, 0);
		}
		
		return defVal;
	}
	
	public long getLong(String key, long defVal) {
		byte[] b = dataMap.get(key);
		if(b != null && b.length == 8) {
			return Util.byteArrayToLong(b, 0);
		}
		
		return defVal;
	}
	
	public String getString(String key, String defVal) {
		byte[] b = dataMap.get(key);
		if(b != null) {
			int num = b.length;
			if(b != null && num > 0) {
				StringBuffer str = new StringBuffer(num);
				for(int i = 0; i < num; i++) {
					str.append((char)b[i]);
				}
				return str.toString();
			}
		}
		
		return defVal;
	}
	
	public byte[] getBytes(String key) {
		return dataMap.get(key);
	}
	
	public boolean hasItem(String key) {
		return dataMap.containsKey(key);
	}
}

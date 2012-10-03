package com.renegadeware.m8.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.renegadeware.m8.DebugLog;

import android.content.SharedPreferences;
import android.util.SparseBooleanArray;

/**
 * Convenient static data serialization.
 * 
 * @author ddionisio
 *
 */
public abstract class SaveFile {
	
	private final byte[] fourBytes = new byte[4];
	
	public abstract int getVersion();
	
	private SparseBooleanArray _loadSparseBooleanArray(SparseBooleanArray sa, String name, SaveFileLoader loader) {
		
		byte[] bytes = loader.getBytes(name);
		int num = bytes != null ? bytes.length/5 : 0;
		if(num > 0) {
			if(sa == null) {
				sa = new SparseBooleanArray(num);
			}
			
			for(int i = 0; i < num; i++) {
				int id = Util.byteArrayToInt(bytes, i*5);
				boolean b = bytes[i*5 + 4] == 1;
				sa.append(id, b);
			}
		}
		else if(sa == null) {
			sa = new SparseBooleanArray();
		}
		
		return sa;
	}
	
	private final static int _ArrayDataOfs = 4;
	
	private Object _loadFieldArray(String base, String type, int typeInd, Object arrayObj, SaveFileLoader loader) throws Exception {
		byte[] bytes;
		int num;
		
		switch(type.charAt(typeInd)) {
		case 'Z':
			bytes = loader.getBytes(base);
			if(bytes != null) {
				int loadLen = bytes.length;
				
				boolean[] array = arrayObj != null ? (boolean[])arrayObj : new boolean[loadLen];
				
				num = loadLen < array.length ? loadLen : array.length;
				
				for(int d = 0; d < num; d++) {
					array[d] = bytes[d] == 1;
				}
				return array;
			}
			break;
			
		case 'B':
			bytes = loader.getBytes(base);
			if(bytes != null) {
				return bytes.clone();
			}
			break;
			
		case 'F':
			bytes = loader.getBytes(base);
			if(bytes != null) {
				int loadLen = bytes.length/4;
				
				float[] array = (float[])arrayObj;
				
				num = loadLen < array.length ? loadLen : array.length;
				
				for(int d = 0; d < num; d++) {
					array[d] = Util.byteArrayToFloat(bytes, d*4);
				}
				return array;
			}
			break;
			
		case 'I':
			bytes = loader.getBytes(base);
			if(bytes != null) {
				int loadLen = bytes.length/4;
				
				int[] array = (int[])arrayObj;
				
				num = loadLen < array.length ? loadLen : array.length;
				
				for(int d = 0; d < num; d++) {
					array[d] = Util.byteArrayToInt(bytes, d*4);
				}
				return array;
			}
			break;
			
		case 'J':
			bytes = loader.getBytes(base);
			if(bytes != null) {
				int loadLen = bytes.length/8;
				
				long[] array = (long[])arrayObj;
				
				num = loadLen < array.length ? loadLen : array.length;
				
				for(int d = 0; d < num; d++) {
					array[d] = Util.byteArrayToLong(bytes, d*8);
				}
				return array;
			}
			break;

		case 'L':
		{
			int semiColon = type.indexOf(';');
			String className = type.substring(typeInd+1, semiColon);
			
			Object[] array = (Object[])arrayObj;
						
			if(className.compareTo("java.lang.String") == 0) {
				bytes = loader.getBytes(base);
				if(bytes != null) {
					//first 4 bytes represent the number of strings
					int loadLen = Util.byteArrayToInt(bytes, 0);
					
					if(array == null) {
						array = new String[loadLen];
					}
					
					num = loadLen < array.length ? loadLen : array.length;
					
					//go through the byte array and pick out each string
					//each string is separated by a null/0
					int cCount = 0;
					
					for(int d = 0; d < num; d++) {
						StringBuffer sb = new StringBuffer();
						
						char sc;
						do {
							sc = (char) bytes[_ArrayDataOfs+cCount];
							if(sc != 0) {
								sb.append(sc);
							}
							
							cCount++;
						} while(sc != 0);
						
						array[d] = sb.toString();
					}
					
					return array;
				}
			}
			else if(className.compareTo("android.util.SparseBooleanArray") == 0) {
				bytes = loader.getBytes(base+".length");
				if(bytes != null) {
					int loadLen = Util.byteArrayToInt(bytes, 0);
									
					if(array == null) {
						array = new SparseBooleanArray[loadLen];
					}
					
					num = loadLen < array.length ? loadLen : array.length;
					
					for(int d = 0; d < num; d++) {
						array[d] = _loadSparseBooleanArray((SparseBooleanArray)array[d], 
								base+'['+d+']', loader);
					}
					
					return array;
				}
			}
			else {
				bytes = loader.getBytes(base+".length");
				if(bytes != null) {
					int loadLen = Util.byteArrayToInt(bytes, 0);
					
					if(array == null) {
						array = (Object[])Util.instantiateClassArray(className, loadLen);
					}
					
					num = loadLen < array.length ? loadLen : array.length;
					
					for(int d = 0; d < num; d++) {
						if(array[d] == null) {
							array[d] = Util.instantiateClass(className);
						}
						_loadClass(base+'['+d+']', array[d], array[d].getClass(), loader);
					}
				}
				
				return array;
			}
			
			break;
		}
		case '[':
			bytes = loader.getBytes(base+".length");
			if(bytes != null) {
				Object[] array = (Object[])arrayObj;
				
				int loadLen = Util.byteArrayToInt(bytes, 0);
				
				int typeSubInd = typeInd+1;
				
				int d;
				
				if(array == null) {
					Object first = _loadFieldArray(base+"[0]", type, typeSubInd, null, loader);
					array = (Object[])Array.newInstance(first.getClass(), loadLen);
					array[0] = first;

					num = loadLen;
					d = 1;
				}
				else {
					num = loadLen < array.length ? loadLen : array.length;
					d = 0;
				}
				
				for(; d < num; d++) {
					array[d] = _loadFieldArray(base+'['+d+']', type, typeSubInd, array[d], loader);
				}
				
				return array;
			}
			break;
		}
		
		return arrayObj;
	}
	
	private void _loadField(String base, Object obj, Field f, SaveFileLoader loader) throws Exception {
		Class<?> type = f.getType();
		String typeName = type.getName();
												
		f.setAccessible(true);
		
		String varName = base.length() > 0 ? base + '.' + f.getName() : f.getName();
		
		if(type.isArray()) {			
			f.set(obj, _loadFieldArray(varName, typeName, 1, f.get(obj), loader));
		}
		else if(typeName.compareTo("boolean") == 0) {
			f.setBoolean(obj, loader.getBoolean(varName, f.getBoolean(obj)));
		}
		else if(typeName.compareTo("float") == 0) {
			f.setFloat(obj, loader.getFloat(varName, f.getFloat(obj)));
		}
		else if(typeName.compareTo("int") == 0) {
			f.setInt(obj, loader.getInt(varName, f.getInt(obj)));
		}
		else if(typeName.compareTo("long") == 0) {
			f.setLong(obj, loader.getLong(varName, f.getLong(obj)));
		}
		else if(typeName.compareTo("java.lang.String") == 0) {
			f.set(obj, loader.getString(varName, (String)f.get(obj)));
		}
		else if(typeName.compareTo("android.util.SparseBooleanArray") == 0) {
			SparseBooleanArray sa = (SparseBooleanArray)f.get(obj);
			f.set(obj, _loadSparseBooleanArray(sa, varName, loader));
		}
		else {
			Object c = f.get(obj);
			if(c == null) {
				f.set(obj, type.newInstance());
			}
			_loadClass(varName, c, type, loader);
		}
	}
	
	private void _loadClass(String base, Object obj, Class<?> c, SaveFileLoader loader) throws Exception {
		Field[] fields = c.getDeclaredFields();

		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			
			int mod = f.getModifiers();
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
				continue;
			}
			
			_loadField(base, obj, f, loader);
		}
	}
	
	public void load(InputStream is) throws Exception {
		is.read(fourBytes, 0, 4);
		int version = Util.byteArrayToInt(fourBytes, 0);
		
		//TODO: backwards compatibility
		
		SaveFileLoader loader = new SaveFileLoader(is);
		
		_loadClass("", this, getClass(), loader);
	}
	
	
	private void _saveSparseBooleanArray(SparseBooleanArray sa, String name, SaveFileWriter save) {
		byte[] bytes = null;
		
		if(sa != null) {
			int num = sa.size();
			if(num > 0) {
				bytes = new byte[num*5];
				
				for(int i = 0; i < num; i++) {
					int id = sa.keyAt(i);
					boolean b = sa.valueAt(i);
					
					Util.intToByteArray(id, bytes, i*5);
					bytes[i*5 + 4] = (byte)(b ? 1 : 0);
				}
			}
		}
	}
	
	private void _saveFieldArray(String base, String type, int typeInd, Object arrayObj, SaveFileWriter save) throws Exception {
		
		if(arrayObj == null) { //uninitialized array...
			return;
		}
		
		int num;
		
		switch(type.charAt(typeInd)) {
		case 'Z':
		{
			boolean[] array = (boolean[])arrayObj;
			num = array.length;
			
			byte[] dat = new byte[num];
			
			for(int d = 0; d < num; d++) {
				dat[d] = (byte) (array[d] ? 1 : 0);
			}
			
			save.putBytes(base, dat);
			break;
		}
		case 'B':
		{
			save.putBytes(base, (byte[])arrayObj);
			break;
		}
		case 'F':
		{
			float[] array = (float[])arrayObj;
			num = array.length;
			
			byte[] dat = new byte[num*4];
			
			for(int d = 0; d < num; d++) {
				Util.floatToByteArray(array[d], dat, d*4);
			}
			
			save.putBytes(base, dat);
			break;
		}
		case 'I':
		{
			int[] array = (int[])arrayObj;
			num = array.length;
			
			byte[] dat = new byte[num*4];
						
			for(int d = 0; d < num; d++) {
				Util.intToByteArray(array[d], dat, d*4);
			}
			
			save.putBytes(base, dat);
			break;
		}
		case 'J':
		{
			long[] array = (long[])arrayObj;
			num = array.length;
			
			byte[] dat = new byte[num*8];
						
			for(int d = 0; d < num; d++) {
				Util.longToByteArray(array[d], dat, d*8);
			}
			
			save.putBytes(base, dat);
			break;
		}
		case 'L':
		{
			int semiColon = type.indexOf(';');
			String className = type.substring(typeInd+1, semiColon);
			
			Object[] array = (Object[])arrayObj;
			
			if(className.compareTo("java.lang.String") == 0) {
				num = array.length;
				
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				
				//write the number of strings
				Util.intToByteArray(num, fourBytes, 0);
				bs.write(fourBytes);
				
				//write each string with a null/0 termination
				for(int d = 0; d < num; d++) {
					String str = (String)array[d];
					bs.write(str.getBytes());
					bs.write(0);
				}
				
				save.putBytes(base, bs.toByteArray());
			}
			else if(className.compareTo("android.util.SparseBooleanArray") == 0) {
				Util.intToByteArray(array.length, fourBytes, 0);
				save.putBytes(base+".length", fourBytes);
				
				for(int d = 0; d < array.length; d++) {
					_saveSparseBooleanArray((SparseBooleanArray)array[d], base+'['+d+']', save);
				}
			}
			else {
				Util.intToByteArray(array.length, fourBytes, 0);
				save.putBytes(base+".length", fourBytes);
				
				for(int d = 0; d < array.length; d++) {
					if(array[d] != null) {
						_saveClass(base+'['+d+']', array[d], array[d].getClass(), save);
					}
				}
			}
			break;
		}
		case '[':
			Object[] array = (Object[])arrayObj;
			
			Util.intToByteArray(array.length, fourBytes, 0);
			save.putBytes(base+".length", fourBytes);
			
			for(int d = 0; d < array.length; d++) {
				_saveFieldArray(base+'['+d+']', type, typeInd+1, array[d], save);
			}
			break;
		}
	}
	
	private void _saveField(String base, Object obj, Field f, SaveFileWriter save) throws Exception {
		Class<?> type = f.getType();
		String typeName = type.getName();
								
		f.setAccessible(true);
		
		String varName = base.length() > 0 ? base + '.' + f.getName() : f.getName();
		
		if(type.isArray()) {			
			_saveFieldArray(varName, typeName, 1, f.get(obj), save);
		}
		else if(typeName.compareTo("boolean") == 0) {
			save.putBoolean(varName, f.getBoolean(obj));
		}
		else if(typeName.compareTo("byte") == 0) {
			save.putByte(varName, f.getByte(obj));
		}
		else if(typeName.compareTo("float") == 0) {
			save.putFloat(varName, f.getFloat(obj));
		}
		else if(typeName.compareTo("int") == 0) {
			save.putInt(varName, f.getInt(obj));
		}
		else if(typeName.compareTo("long") == 0) {
			save.putLong(varName, f.getLong(obj));
		}
		else if(typeName.compareTo("java.lang.String") == 0) {
			String str = (String)f.get(obj); 
			if(str != null && str.length() > 0) {
				save.putString(varName, str);
			}
		}
		else if(typeName.compareTo("android.util.SparseBooleanArray") == 0) {
			SparseBooleanArray sa = (SparseBooleanArray)f.get(obj);
			if(sa != null) {
				_saveSparseBooleanArray(sa, varName, save);
			}
		}
		else {
			Object c = f.get(obj);
			if(c != null) {
				_saveClass(varName, c, type, save);
			}
		}
	}
	
	private void _saveClass(String base, Object obj, Class<?> c, SaveFileWriter save) throws Exception {
		Field[] fields = c.getDeclaredFields();

		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			
			int mod = f.getModifiers();
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
				continue;
			}
			
			_saveField(base, obj, f, save);
		}
	}

	public void save(OutputStream os) throws Exception {
		//write version
		Util.intToByteArray(getVersion(), fourBytes, 0);
		os.write(fourBytes, 0, 4);
		
		SaveFileWriter save = new SaveFileWriter();
		
		_saveClass("", this, getClass(), save);
		
		save.save(os);
	}
}

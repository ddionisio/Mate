package com.renegadeware.m8;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.content.SharedPreferences;

/**
 * Convenience class to manipulate your preferences based on class' member variables.
 * <p>
 * note: this only stores booleans, floats, ints, longs, and strings.
 * 
 * @author ddionisio
 *
 */
public abstract class Preference {
	public static final void clearAll(SharedPreferences prefs) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.clear();
		edit.commit();
	}
	
	private String name;
	
	public Preference() {
		name = "";
	}
	
	public Preference(String name) {
		this.name = name;
	}
	
	private void _loadFieldArray(String base, String type, int typeInd, Object arrayObj, SharedPreferences prefs) throws Exception {
				
		switch(type.charAt(typeInd)) {
		case 'Z':
		{
			boolean[] array = (boolean[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				array[d] = prefs.getBoolean(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'F':
		{
			float[] array = (float[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				array[d] = prefs.getFloat(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'I':
		{
			int[] array = (int[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				array[d] = prefs.getInt(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'J':
		{
			long[] array = (long[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				array[d] = prefs.getLong(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'L':
		{
			Object[] array = (Object[])arrayObj;
			Class<?> c = array[0].getClass();
			
			if(c.getName().compareTo("java.lang.String") == 0) {
				for(int d = 0; d < array.length; d++) {
					array[d] = prefs.getString(base+'['+d+']', (String)array[d]);
				}
			}
			else {
				for(int d = 0; d < array.length; d++) {
					_loadClass(base+'['+d+']', array[d], c, prefs);
				}
			}
			break;
		}
		case '[':
			Object[] array = (Object[])arrayObj;
			
			for(int d = 0; d < array.length; d++) {
				_loadFieldArray(base+'['+d+']', type, typeInd+1, array[d], prefs);
			}
			break;
		}
	}
	
	private void _loadField(String base, Object obj, Field f, SharedPreferences prefs) throws Exception {
		Class<?> type = f.getType();
		String typeName = type.getName();
								
		f.setAccessible(true);
		
		String varName = base.length() > 0 ? base + '.' + f.getName() : f.getName();
		
		if(type.isArray()) {			
			_loadFieldArray(varName, typeName, 1, f.get(obj), prefs);
		}
		else if(typeName.compareTo("boolean") == 0) {
			f.setBoolean(obj, prefs.getBoolean(varName, f.getBoolean(obj)));
		}
		else if(typeName.compareTo("float") == 0) {
			f.setFloat(obj, prefs.getFloat(varName, f.getFloat(obj)));
		}
		else if(typeName.compareTo("int") == 0) {
			f.setInt(obj, prefs.getInt(varName, f.getInt(obj)));
		}
		else if(typeName.compareTo("long") == 0) {
			f.setLong(obj, prefs.getLong(varName, f.getLong(obj)));
		}
		else if(typeName.compareTo("java.lang.String") == 0) {
			f.set(obj, prefs.getString(varName, (String)f.get(obj)));
		}
		else {
			_loadClass(varName, f.get(obj), type, prefs);
		}
	}
	
	private void _loadClass(String base, Object obj, Class<?> c, SharedPreferences prefs) throws Exception {
		Field[] fields = c.getDeclaredFields();

		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			
			int mod = f.getModifiers();
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
				continue;
			}
			
			_loadField(base, obj, f, prefs);
		}
	}
	
	public void load(SharedPreferences prefs) {
		try {
			_loadClass(name, this, getClass(), prefs);
		} catch(Exception e) {
			DebugLog.e("GamePreference", "Failed loading: "+e.toString(), e);
		}
	}
	
	private void _saveFieldArray(String base, String type, int typeInd, Object arrayObj, SharedPreferences.Editor edit) throws Exception {
		
		switch(type.charAt(typeInd)) {
		case 'Z':
		{
			boolean[] array = (boolean[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.putBoolean(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'F':
		{
			float[] array = (float[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.putFloat(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'I':
		{
			int[] array = (int[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.putInt(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'J':
		{
			long[] array = (long[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.putLong(base+'['+d+']', array[d]);
			}
			break;
		}
		case 'L':
		{
			Object[] array = (Object[])arrayObj;
			Class<?> c = array[0].getClass();
			
			if(c.getName().compareTo("java.lang.String") == 0) {
				for(int d = 0; d < array.length; d++) {
					edit.putString(base+'['+d+']', (String)array[d]);
				}
			}
			else {
				for(int d = 0; d < array.length; d++) {
					_saveClass(base+'['+d+']', array[d], c, edit);
				}
			}
			break;
		}
		case '[':
			Object[] array = (Object[])arrayObj;
			
			for(int d = 0; d < array.length; d++) {
				_saveFieldArray(base+'['+d+']', type, typeInd+1, array[d], edit);
			}
			break;
		}
	}
	
	private void _saveField(String base, Object obj, Field f, SharedPreferences.Editor edit) throws Exception {
		Class<?> type = f.getType();
		String typeName = type.getName();
		
		f.setAccessible(true);
		
		String varName = base.length() > 0 ? base + '.' + f.getName() : f.getName();
		
		if(type.isArray()) {			
			_saveFieldArray(varName, typeName, 1, f.get(obj), edit);
		}
		else if(typeName.compareTo("boolean") == 0) {
			edit.putBoolean(f.getName(), f.getBoolean(obj));
		}
		else if(typeName.compareTo("float") == 0) {
			edit.putFloat(f.getName(), f.getFloat(obj));
		}
		else if(typeName.compareTo("int") == 0) {
			edit.putInt(f.getName(), f.getInt(obj));
		}
		else if(typeName.compareTo("long") == 0) {
			edit.putLong(f.getName(), f.getLong(obj));
		}
		else if(typeName.compareTo("java.lang.String") == 0) {
			edit.putString(f.getName(), (String)f.get(obj));
		}
		else {
			_saveClass(varName, f.get(obj), type, edit);
		}
	}
	
	private void _saveClass(String base, Object obj, Class<?> c, SharedPreferences.Editor edit) throws Exception {
		Field[] fields = c.getDeclaredFields();

		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			
			int mod = f.getModifiers();
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
				continue;
			}
			
			_saveField(base, obj, f, edit);
		}
	}

	public void save(SharedPreferences prefs) {
		
		SharedPreferences.Editor edit = prefs.edit();
		
		//remove all the existing crap
		edit.clear();
		
		try {
			_saveClass(name, this, getClass(), edit);
		} catch(Exception e) {
			edit.clear();
			DebugLog.e("GamePreference", "Failed saving: "+e.toString(), e);
		} finally {
			edit.commit();
		}
	}
	
	private void _clearFieldArray(String base, String type, int typeInd, Object arrayObj, SharedPreferences.Editor edit) throws Exception {
		
		switch(type.charAt(typeInd)) {
		case 'Z':
		{
			boolean[] array = (boolean[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.remove(base+'['+d+']');
			}
			break;
		}
		case 'F':
		{
			float[] array = (float[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.remove(base+'['+d+']');
			}
			break;
		}
		case 'I':
		{
			int[] array = (int[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.remove(base+'['+d+']');
			}
			break;
		}
		case 'J':
		{
			long[] array = (long[])arrayObj;
			for(int d = 0; d < array.length; d++) {
				edit.remove(base+'['+d+']');
			}
			break;
		}
		case 'L':
		{
			Object[] array = (Object[])arrayObj;
			Class<?> c = array[0].getClass();
			
			if(c.getName().compareTo("java.lang.String") == 0) {
				for(int d = 0; d < array.length; d++) {
					edit.remove(base+'['+d+']');
				}
			}
			else {
				for(int d = 0; d < array.length; d++) {
					_clearClass(base+'['+d+']', array[d], c, edit);
				}
			}
			break;
		}
		case '[':
			Object[] array = (Object[])arrayObj;
			
			for(int d = 0; d < array.length; d++) {
				_clearFieldArray(base+'['+d+']', type, typeInd+1, array[d], edit);
			}
			break;
		}
	}
	
	private void _clearClass(String base, Object obj, Class<?> c, SharedPreferences.Editor edit) throws Exception {
		Field[] fields = c.getDeclaredFields();

		for(int i = 0; i < fields.length; i++) {
			Field f = fields[i];
						
			int mod = f.getModifiers();
			
			if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)) {
				continue;
			}
			
			Class<?> type = f.getType();
			String typeName = type.getName();
			String varName = base.length() > 0 ? base + '.' + f.getName() : f.getName();
			
			if(typeName.compareTo("boolean") == 0
					|| typeName.compareTo("float") == 0
					|| typeName.compareTo("int") == 0
					|| typeName.compareTo("long") == 0
					|| typeName.compareTo("java.lang.String") == 0) {
				edit.remove(varName);
			}
			else if(type.isArray()) {
				_clearFieldArray(varName, typeName, 1, f.get(obj), edit);
			}
			else {
				_clearClass(varName, f.get(obj), type, edit);
			}
		}
	}

	public void clear(SharedPreferences prefs) {
		SharedPreferences.Editor edit = prefs.edit();
		
		try {
			_clearClass(name, this, getClass(), edit);
			edit.commit();
		} catch(Exception e) {
			DebugLog.e("GamePreference", "Failed to clear: "+e.toString(), e);
		}
	}
}

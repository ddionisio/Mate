package com.renegadeware.m8.util;

import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.math.Vector2;

public class XmlSection extends DataSection {
	
	public static XmlSection createFromResourceId(Context context, int id) {
		XmlSection last = null;

		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			ArrayList<XmlSection> stack = new ArrayList<XmlSection>();
			XmlSection cur = null;
			
			int eventType = xml.getEventType();

			while(eventType != XmlPullParser.END_DOCUMENT) {
				
				if(eventType == XmlPullParser.START_TAG) {
					String tagName = xml.getName();
					int attrCount = xml.getAttributeCount();
					
					if(stack.size() > 0) {
						last = stack.get(stack.size()-1);
					}
					
					// create and push in stack
					cur = new XmlSection(tagName);
					stack.add(cur);
					
					// read attributes
					for(int i = 0; i < attrCount; ++i) {						
						cur.attrs.put(xml.getAttributeName(i), xml.getAttributeValue(i));
					}
					
					//if there is a last one, append current to last's children
					if(last != null) {
						last.children.add(cur);
						cur.parent = new WeakReference<DataSection>(last);
					}
				}
				else if(eventType == XmlPullParser.TEXT) {
					if(cur != null && !xml.isWhitespace()) {
						cur.setString(xml.getText().trim());
					}
				}
				else if(eventType == XmlPullParser.END_TAG) {
					if(stack.size() > 0) {
						last = stack.remove(stack.size()-1);
					}
				}
				
				eventType = xml.next();
			}

		} catch(Exception e) {
			DebugLog.e("XmlSection.createFromResourceId", e.toString(), e);
		} finally {
			xml.close();
		}

		return last;
	}

	public XmlSection(String tag) {
		this.tag = tag;
		
		attrs = new HashMap<String, String>();
		children = new ArrayList<XmlSection>();
	}
	
	public int sizeInBytes() {
		int sz = 0;
		sz += tag.length() + value.length();
		for(XmlSection d : children) {
			sz += d.sizeInBytes();
		}
		return sz;
	}
	
	@Override
	public DataSection getParent() {
		if(parent == null) {
			return null;
		}
		
		return parent.get();
	}
	
	@Override
	public DataSection findChild(String tag) {
		for(XmlSection d : children) {
			if(d.sectionName().compareTo(tag) == 0) {
				return d;
			}
		}
		
		return null;
	}
		
	@Override
	public int countChildren() {
		return children.size();
	}

	@Override
	public DataSection openChild(int index) {
		return children.get(index);
	}

	@Override
	public DataSection newSection(String tag) {
		XmlSection newSec = new XmlSection(tag);
		newSec.parent = new WeakReference<DataSection>(this);
		children.add(newSec);
		return newSec;
	}
	
	@Override
	public DataSection insertSection(String tag, int index) {
		if(index == -1) {
			return newSection(tag);
		}
		
		XmlSection newSec = new XmlSection(tag);
		newSec.parent = new WeakReference<DataSection>(this);
		children.add(index, newSec);
		return newSec;
	}

	@Override
	public void delChild(String tag) {
		
		Iterator<XmlSection> iter = children.iterator();
		while(iter.hasNext()) {
			XmlSection d = iter.next();
			if(d.sectionName().compareTo(tag) == 0) {
				d.parent = null;
				iter.remove();
			}
		}
	}

	@Override
	public void delChild(DataSection section) {
		Iterator<XmlSection> iter = children.iterator();
		while(iter.hasNext()) {
			XmlSection d = iter.next();
			if(d == section) {
				d.parent = null;
				iter.remove();
			}
		}
	}

	@Override
	public void delChildren() {
		for(XmlSection d : children) {
			d.parent = null;
		}
		
		children.clear();
	}

	@Override
	public String sectionName() {
		return tag;
	}

	@Override
	public int bytes() {
		return sizeInBytes();
	}

	@Override
	public boolean save(OutputStream out) {
		//TODO: implement me
		return false;
	}

	/////////////////////////////////////////////////////
	// Methods for reading the value of the DataSection

	@Override
	public boolean asBool() { 
		return Boolean.parseBoolean(value);
	}

	@Override
	public int asInt(int defaultVal) {
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException e) {
			try {
				return Integer.parseInt(value, 16);
			}
			catch(NumberFormatException e2) {
				return defaultVal;
			}
		}
	}

	@Override
	public long asLong(long defaultVal ) { 
		try {
			return Long.parseLong(value);
		} catch(NumberFormatException e) {
			try {
				return Long.parseLong(value, 16);
			}
			catch(NumberFormatException e2) {
				return defaultVal;
			}
		}
	}

	@Override
	public float asFloat(float defaultVal) { 
		try {
			return Float.parseFloat(value);
		} catch(NumberFormatException e) {
			return defaultVal; 
		}
	}

	@Override
	public double asDouble(double defaultVal) { 
		try {
			return Double.parseDouble(value);
		} catch(NumberFormatException e) {
			return defaultVal; 
		}
	}

	@Override
	public String asString(int flags) { 
		return flags == DataSection.FLAG_INCLUDE_TRIM_WHITESPACE ? value.trim() : value; 
	}

	@Override
	public Vector2 asVector2(Vector2 defaultVal) {
		String[] vals = value.split("[, \\t\\n\\x0B\\f\\r]");
		if(vals.length >= 2) {
			Vector2 vec = new Vector2();
			try {
				vec.x = Float.parseFloat(vals[0]);
				vec.y = Float.parseFloat(vals[1]);
			} catch(NumberFormatException e) {
				return defaultVal; 
			}
		}
		return defaultVal;
	}

	/////////////////////////////////////////////////////
	// Methods for writing the value of the DataSection

	@Override
	public boolean setBool(boolean value) { 
		this.value = value ? "true" : "false";
		return true;
	}

	@Override
	public boolean setInt(int value) {
		this.value = String.valueOf(value); 
		return true;
	}

	@Override
	public boolean setLong(long value) { 
		this.value = String.valueOf(value);
		return true;
	}

	@Override
	public boolean setFloat(float value) { 
		this.value = String.valueOf(value);
		return true;
	}

	@Override
	public boolean setDouble(double value) { 
		this.value = String.valueOf(value);
		return true;
	}

	@Override
	public boolean setString(String value) {
		this.value = value;
		return true; 
	}

	@Override
	public boolean setVector2(Vector2 value) {
		this.value = String.valueOf(value);
		return true;
	}
	
	////////////////////////////////////////////
	// attributes
	
	@Override
	public String getAttributeAsString(String attr, int flags) {
		String val = attrs.get(attr);
		if(val != null) {
			return flags == DataSection.FLAG_INCLUDE_TRIM_WHITESPACE ? val.trim() : val;
		}
		
		return ""; 
	}
	
	@Override
	public int getAttributeAsInt(String attr, int defaultVal) {
		String val = attrs.get(attr);
		if(val != null) {
			try {
				return Integer.parseInt(val);
			} catch(NumberFormatException e) {
				try {
					return Integer.parseInt(val, 16);
				}
				catch(NumberFormatException e2) {
					return defaultVal;
				}
			}
		}
		
		return defaultVal; 
	}
	
	@Override
	public long getAttributeAsLong(String attr, long defaultVal) {
		String val = attrs.get(attr);
		if(val != null) {
			try {
				return Long.parseLong(val);
			} catch(NumberFormatException e) {
				try {
					return Long.parseLong(val, 16);
				}
				catch(NumberFormatException e2) {
					return defaultVal;
				}
			}
		}
		
		return defaultVal; 
	}
	
	@Override
	public float getAttributeAsFloat(String attr, float defaultVal) {
		String val = attrs.get(attr);
		if(val != null) {
			try {
				return Float.parseFloat(val);
			} catch(NumberFormatException e) {
				return defaultVal;
			}
		}
		
		return defaultVal; 
	}
	
	@Override
	public double getAttributeAsDouble(String attr, double defaultVal) {
		String val = attrs.get(attr);
		if(val != null) {
			try {
				return Double.parseDouble(val);
			} catch(NumberFormatException e) {
				return defaultVal;
			}
		}
		
		return defaultVal; 
	}
	
	@Override
	public Vector2 getAttributeAsVector2(String attr, Vector2 defaultVal) {
		String val = attrs.get(attr);
		if(val != null) {
			String[] vals = val.split("[, \\t\\n\\x0B\\f\\r]");
			if(vals.length >= 2) {
				Vector2 vec = new Vector2();
				try {
					vec.x = Float.parseFloat(vals[0]);
					vec.y = Float.parseFloat(vals[1]);
				} catch(NumberFormatException e) {
					return defaultVal.clone();
				}
			}
		}
		
		return defaultVal.clone();
	}
	
	@Override
	public boolean setAttributeString(String attr, String val) {
		attrs.put(attr, val);
		return true; 
	}

	@Override
	public boolean setAttributeInt(String attr, int val) {
		attrs.put(attr, String.valueOf(val));
		return true; 
	}

	@Override
	public boolean setAttributeLong(String attr, long val) {
		attrs.put(attr, String.valueOf(val));
		return true; 
	}

	@Override
	public boolean setAttributeFloat(String attr, float val) {
		attrs.put(attr, String.valueOf(val));
		return true;
	}

	@Override
	public boolean setAttributeDouble(String attr, double val) {
		attrs.put(attr, String.valueOf(val));
		return true;
	}

	@Override
	public boolean setAttributeVector2(String attr, Vector2 val) {
		attrs.put(attr, String.valueOf(val));
		return false;
	}
	

	protected boolean writeToXmlStream(OutputStream stream) {
		//TODO: implement me
		return false;
	}
	
	private String tag;
	private String value;
	private WeakReference<DataSection> parent;
	private final HashMap<String, String> attrs;
	private final ArrayList<XmlSection> children;
}

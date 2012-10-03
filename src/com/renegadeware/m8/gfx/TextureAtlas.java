package com.renegadeware.m8.gfx;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;
import com.renegadeware.m8.util.Util;

public class TextureAtlas extends Resource {
	public static final String TEXTURE_ATLAS_TAG = "TextureAtlas";
	public static final String IMAGE_ATTR = "imagePath";
	
	public static final String SUB_TEXTURE_TAG = "SubTexture";
	public static final String NAME_ATTR = "name";
	public static final String X_ATTR = "x";
	public static final String Y_ATTR = "y";
	public static final String WIDTH_ATTR = "width";
	public static final String HEIGHT_ATTR = "height";
	
	// These are indices in elements
	
	public static final int ELEM_X = 0;
	
	/** note: this is bottom relative to crap opengl image space */
	public static final int ELEM_Y = 1;
	public static final int ELEM_WIDTH = 2;
	public static final int ELEM_HEIGHT = 3;
	
	private Texture texture;
	private final HashMap<String, int[]> elements;
	private int size;
	
	public TextureAtlas(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		elements = Util.newHashMap();
	}
	
	public Texture getTexture() {
		return texture;
	}
	
	/**
	 * You should treat the elements of the return value as read-only
	 * @param name The sub texture label
	 * @return the elements array consisting of the dimension within the texture (x,y,width,height),
	 * null if given name is not found.
	 */
	public int[] getElement(String name) {
		assert elements != null;
		
		return elements.get(name);
	}

	@Override
	protected void loadImpl() {
		final Context context = systemRegistry.contextParameters.context;
		assert context != null;
		
		texture = null;
		elements.clear();
		size = 0;
		
		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			int eventType = xml.getEventType();
			int attrCount;
			
			while(eventType != XmlPullParser.END_DOCUMENT) {
				
				if(eventType == XmlPullParser.START_TAG) {
					String tagName = xml.getName();
					attrCount = xml.getAttributeCount();
					
					if(tagName.compareTo(TEXTURE_ATLAS_TAG) == 0) {
						for(int i = 0; i < attrCount; ++i) {
							if(xml.getAttributeName(i).compareTo(IMAGE_ATTR) == 0) {
								String imagePath = xml.getAttributeValue(i);
								texture = (Texture)systemRegistry.textureManager.getById(systemRegistry.textureManager.getResourceIdByFilename(imagePath));
								break;
							}
						}
					}
					else if(tagName.compareTo(SUB_TEXTURE_TAG) == 0) {
						String name = "", attr, val;
						final int ref[] = new int[4];
						
						// go through the attributes and assemble the sub texture data
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(NAME_ATTR) == 0) {
								name = val;
							}
							else if(attr.compareTo(X_ATTR) == 0) {
								ref[ELEM_X] = Integer.parseInt(val);
							}
							else if(attr.compareTo(Y_ATTR) == 0) {
								ref[ELEM_Y] = Integer.parseInt(val);
								
							}
							else if(attr.compareTo(WIDTH_ATTR) == 0) {
								ref[ELEM_WIDTH] = Integer.parseInt(val);
							}
							else if(attr.compareTo(HEIGHT_ATTR) == 0) {
								ref[ELEM_HEIGHT] = Integer.parseInt(val);
							}
						}
						
						if(name.length() > 0) {
							// shift the y value based on opengl weirdness axis
							ref[ELEM_Y] += ref[ELEM_HEIGHT];
							
							//register the sub texture
							elements.put(name, ref);
							
							size += 16;
						}
					}
				}
				
				eventType = xml.next();
			}
		} catch(Exception e) {
			DebugLog.e("Texture Atlas load", e.toString(), e);
		} finally {
			xml.close();
		}
	}

	@Override
	protected void unloadImpl() {
		texture = null;
		elements.clear();
		size = 0;
	}

	@Override
	protected int calculateSize() {
		return size;
	}
}

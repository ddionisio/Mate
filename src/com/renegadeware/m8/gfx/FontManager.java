package com.renegadeware.m8.gfx;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class FontManager extends ResourceManager {
	
	public static final String Type = "font";

	public FontManager() {
		super(0);
	}

	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_FONT;
	}

	@Override
	public String name() {
		return Type;
	}

	@Override
	public String defType() {
		return "drawable";
	}

	@Override
	protected Resource createImpl(int id, String group, boolean isManual,
			ManualResourceLoader loader, Resource.Param params) {
		return (Resource)new Font(this, id, group, isManual, loader);
	}

	@Override
	public void parseScript(int id, String groupName) {
		final Context context = systemRegistry.contextParameters.context;
		assert context != null;
		
		final TextureManager txtMgr = systemRegistry.textureManager;
		assert txtMgr != null;
						
		//we just want the page tag to get the attribute for texture
		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			String tag;
			
			for(int eventType = xml.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xml.next()) {
				tag = xml.getName();
				
				if(eventType == XmlPullParser.START_TAG) {
					if(tag.compareTo(Font.PAGE_TAG) == 0) {
						// get the image path
						for(int i = 0; i < xml.getAttributeCount(); ++i) {
							if(xml.getAttributeName(i).compareTo(Font.FILE_ATTR) == 0) {
								String imgPath = xml.getAttributeValue(i);
								if(imgPath != null && imgPath.length() > 0) {
									// Get the id of the imagePath value
									int imgId = txtMgr.getResourceIdByFilename(imgPath);
									if(imgId > 0) {
										// declare image id for our group
										//if(!systemRegistry.resourceGroupManager.resourceIsDeclared(imgId)) {
											systemRegistry.resourceGroupManager.declareResource(imgId, txtMgr.name(), 
													groupName, null);
										//}

										// done looking for the image path, get out
										break;
									}
									else {
										throw new Exception("Unable to get id for texture: "+imgPath);
									}
								}
								else {
									throw new Exception("XML invalid image path: "+id);
								}
							}
						}
					}
				}
				else if(eventType == XmlPullParser.END_TAG) {
					if(tag.compareTo(Font.PAGES_TAG) == 0) {
						break;
					}
				}
			}
		} catch (Exception e) {
			DebugLog.e(name(), e.toString(), e);
		}
		finally {
			xml.close();
		}
	}
}

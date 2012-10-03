package com.renegadeware.m8.gfx;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceGroupManager;
import com.renegadeware.m8.res.ResourceManager;

public class SpriteManager extends ResourceManager {
	
	public static final String Type = "sprite";
		
	public SpriteManager() {
		super(0);
	}

	@Override
	public int loadingOrder() {
		return DEFAULT_ORDER_SPRITE;
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
		return (Resource)new Sprite(this, id, group, isManual, loader);
	}

	@Override
	public void parseScript(int id, String groupName) {
		// get the texture atlas id
		// make sure id hasn't been declared yet
		// use the atlas manager with id to parse it
		final Context context = systemRegistry.contextParameters.context;
		assert context != null;
		
		final ResourceGroupManager resGrpMgr = systemRegistry.resourceGroupManager;
		assert resGrpMgr != null;
		
		final TextureAtlasManager txtAtlasMgr = systemRegistry.textureAtlasManager;
		assert txtAtlasMgr != null;
		
		//we just want the first tag to get the attribute for texture atlas
		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			int textureAtlasId = 0;
			
			int eventType = xml.getEventType();
			while(eventType != XmlPullParser.END_DOCUMENT) {
				// The first tag found will be Sprite
				if(eventType == XmlPullParser.START_TAG) {
					// Get the attribute 'texture_atlas'
					int attrCount = xml.getAttributeCount();
					boolean parseTextureAtlas = false;
					for(int i = 0; i < attrCount; ++i) {
						if(xml.getAttributeName(i).compareTo(Sprite.TEXTURE_ATLAS_ATTR) == 0) {
							final String atlasName = xml.getAttributeValue(i);
							textureAtlasId = txtAtlasMgr.getResourceIdByName(atlasName); 
							
							//declare texture atlas for our group
							if(textureAtlasId > 0) {
								//if(!resGrpMgr.resourceIsDeclared(textureAtlasId)) {
									resGrpMgr.declareResource(textureAtlasId, txtAtlasMgr.name(), groupName, null);
									
									// declare texture within texture atlas
									txtAtlasMgr.parseScript(textureAtlasId, groupName);
								//}
							}
							else {
								throw new Exception("Unable to get id for texture atlas: "+atlasName);
							}
							break;
						}
					}
					
					if(textureAtlasId <= 0) {
						throw new Exception("Unable to find attr: "+Sprite.TEXTURE_ATLAS_ATTR+" for XML: "+id);
					}
					break;
				}
				
				eventType = xml.next();
			}
		} catch (Exception e) {
			DebugLog.e(name(), e.toString(), e);
		}
		finally {
			xml.close();
		}
	}
}

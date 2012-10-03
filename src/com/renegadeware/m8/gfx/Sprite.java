package com.renegadeware.m8.gfx;

import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;
import com.renegadeware.m8.util.Util;

/**
 * 
 * @author ddionisio
 *
 */
public class Sprite extends Resource {
	
	public static final String SPRITE_TAG = "Sprite";
	public static final String TEXTURE_ATLAS_ATTR = "texture_atlas";
	public static final String FPS_ATTR = "fps";
	public static final String WIDTH_ATTR = "width";
	public static final String HEIGHT_ATTR = "height";
	
	public static final String STATE_TAG = "State";
	public static final String STATE_NAME_ATTR = "name";
	public static final String STATE_LOOP_ATTR = "loop";
	
	public static final String FRAME_TAG = "Frame";
	public static final String FRAME_OFS_X_ATTR = "offset_x";
	public static final String FRAME_OFS_Y_ATTR = "offset_y";
	public static final String FRAME_OFS_SCALE_X_ATTR = "offset_scale_x";
	public static final String FRAME_OFS_SCALE_Y_ATTR = "offset_scale_y";
	public static final String FRAME_ATLAS_ATTR = "atlas";
	public static final String FRAME_NUM_FRAMES_ATTR = "num_frames";
	
	/** State ends at last frame */
	public static final int LOOP_NONE = 0;
		
	/** State goes back to first frame and plays again */
	public static final int LOOP_REPEAT = 1;
	public static final String LOOP_REPEAT_NAME = "repeat";
	
	/** At last frame, go reverse and end at first frame */
	public static final int LOOP_REVERSE = 2;
	public static final String LOOP_REVERSE_NAME = "reverse";
	
	/** At last frame, go reverse and play forward again on first frame. */
	public static final int LOOP_REVERSE_REPEAT = 3;
	public static final String LOOP_REVERSE_REPEAT_NAME = "reverse_repeat";
	
	public static final class Frame {
		public final float offsetX;
		public final float offsetY;
		public final float width;
		public final float height;
		public final float numFrames;
		public final int[] crop;
		
		public Frame(float offsetX, float offsetY, float width, float height, float numFrames, int[] crop) {
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.width = width;
			this.height = height;
			this.numFrames = numFrames;
			this.crop = crop;
		}
	}
	
	public static final class State {
		public final int loopMode;
		
		/** Only use this as reference, don't modify the contents! */
		public final Frame[] frames;
		
		private float _maxFrames;
				
		public State(int numFrames, int loopMode) {
			frames = new Frame[numFrames];
			
			this.loopMode = loopMode;
		}
				
		/** You better be sure that new frames have the same length */
		public void copyFrames(Frame[] newFrames) {
			System.arraycopy(newFrames, 0, frames, 0, newFrames.length);
			
			_calcMaxFrames();
		}
		
		protected void _calcMaxFrames() {
			_maxFrames = 0;
			int len = frames.length;
			for(int i = 0; i < len; i++) {
				_maxFrames += frames[i].numFrames;
			}
		}
		
		public float getMaxFrames() {
			return _maxFrames;
		}
	}
	
	private final HashMap<String, State> states;
	private TextureAtlas textureAtlas;
	private float fps;
	private float width;
	private float height;

	public Sprite(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		states = Util.newHashMap();
	}
	
	public float getFPS() {
		return fps;
	}
	
	public State getState(String name) {
		return states.get(name);
	}
	
	public float getWidth() {
		return width;
	}
	
	public float getHeight() {
		return height;
	}
	
	public Texture getTexture() {
		return textureAtlas.getTexture();
	}

	@Override
	protected void loadImpl() {
		final Context context = systemRegistry.contextParameters.context;
		assert context != null;
		
		final TextureAtlasManager txtAtlasMgr = systemRegistry.textureAtlasManager;
		assert txtAtlasMgr != null;
		
		states.clear();
		
		// go through the xml
		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			int eventType = xml.getEventType();
			String attr;
			String val;
			int attrCount;
			
			String curStateName = "";
			int curStateLoopMode = LOOP_NONE;
			final ArrayList<Frame> curStateFrames = new ArrayList<Frame>();
			
			while(eventType != XmlPullParser.END_DOCUMENT) {
				
				if(eventType == XmlPullParser.START_TAG) {
					String tagName = xml.getName();
					attrCount = xml.getAttributeCount();
					
					if(tagName.compareTo(SPRITE_TAG) == 0) {
						
						
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(TEXTURE_ATLAS_ATTR) == 0) {
								textureAtlas = (TextureAtlas)txtAtlasMgr.getByName(val);
								if(textureAtlas == null) {
									throw new Exception("Texture atlas not loaded: "+val);
								}
							}
							else if(attr.compareTo(FPS_ATTR) == 0) {
								fps = Float.parseFloat(val);
							}
							else if(attr.compareTo(WIDTH_ATTR) == 0) {
								width = Float.parseFloat(val);
							}
							else if(attr.compareTo(HEIGHT_ATTR) == 0) {
								height = Float.parseFloat(val);
							}
						}
					}
					else if(tagName.compareTo(STATE_TAG) == 0) {
						// reset current state values
						curStateName = "";
						curStateLoopMode = LOOP_NONE;
						curStateFrames.clear();
						
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(STATE_NAME_ATTR) == 0) {
								curStateName = val;
							}
							else if(attr.compareTo(STATE_LOOP_ATTR) == 0) {
								if(val.compareTo(LOOP_REPEAT_NAME) == 0) {
									curStateLoopMode = LOOP_REPEAT;
								}
								else if(val.compareTo(LOOP_REVERSE_NAME) == 0) {
									curStateLoopMode = LOOP_REVERSE;
								}
								else if(val.compareTo(LOOP_REVERSE_REPEAT_NAME) == 0) {
									curStateLoopMode = LOOP_REVERSE_REPEAT;
								}
							}
						}
					}
					else if(tagName.compareTo(FRAME_TAG) == 0) {
						float ofsX = 0, ofsY = 0, numFrames = 0;
						boolean doScaleX=false,doScaleY=false;
						float width=0, height=0;
						float scaleX=0, scaleY=0;
						
						int [] crops = null;
						
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(FRAME_OFS_X_ATTR) == 0) {
								ofsX = Float.parseFloat(val);
							}
							else if(attr.compareTo(FRAME_OFS_Y_ATTR) == 0) {
								ofsY = Float.parseFloat(val);
							}
							if(attr.compareTo(WIDTH_ATTR) == 0) {
								width = Float.parseFloat(val);
							}
							else if(attr.compareTo(HEIGHT_ATTR) == 0) {
								height = Float.parseFloat(val);
							}
							else if(attr.compareTo(FRAME_ATLAS_ATTR) == 0) {
								crops = textureAtlas.getElement(val);								
								if(crops == null) {
									throw new Exception("Texture atlas crops not found: "+val);
								}
							}
							else if(attr.compareTo(FRAME_NUM_FRAMES_ATTR) == 0) {
								numFrames = Float.parseFloat(val);
							}
							else if(attr.compareTo(FRAME_OFS_SCALE_X_ATTR) == 0) {
								doScaleX = true;
								scaleX = Float.parseFloat(val);
							}
							else if(attr.compareTo(FRAME_OFS_SCALE_Y_ATTR) == 0) {
								doScaleY = true;
								scaleY = Float.parseFloat(val);
							}
						}
						
						//use the width and height from raw image if both are not specified
						if(width == 0) {
							width = crops[2];
						}
						
						if(height == 0) {
							height = crops[3];
						}
						
						//override offset if scales are set
						if(doScaleX) {
							ofsX = width*scaleX;
						}
						
						if(doScaleY) {
							ofsY = height*scaleY;
						}
						
						// stuff it in our state frame buffer
						curStateFrames.add(new Frame(ofsX, ofsY, width, height, numFrames, crops));
					}
				}
				else if(eventType == XmlPullParser.END_TAG) {
					String tagName = xml.getName();
					
					if(tagName.compareTo(STATE_TAG) == 0) {
						if(curStateName.length() > 0 && curStateFrames.size() > 0) {
							// create state
							State newState = new State(curStateFrames.size(), curStateLoopMode);
							
							// copy the frames
							curStateFrames.toArray(newState.frames);
							
							newState._calcMaxFrames();
							
							// stuff it in
							states.put(curStateName, newState);
						}
					}
				}
				
				eventType = xml.next();
			}
			
		} catch(Exception e) {
			DebugLog.e("Sprite load", e.toString(), e);
		} finally {
			xml.close();
		}
	}

	@Override
	protected void unloadImpl() {
		states.clear();
	}

	@Override
	protected int calculateSize() {
		return 0;
	}
}

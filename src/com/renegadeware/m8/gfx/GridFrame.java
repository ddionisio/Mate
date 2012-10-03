package com.renegadeware.m8.gfx;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ResourceManager;

public class GridFrame extends Grid {
	
	public static final String FRAME_TAG = "Frame";
	public static final String TEXTURE_ATLAS_ATTR = "texture_atlas";
	
	public static final String BODY_TAG = "Body";
	public static final String REF_ATTR = "ref";
	public static final String COLOR_ATTR = "color";
	public static final String PADDING_X_ATTR = "padX";
	public static final String PADDING_Y_ATTR = "padY";
	
	public static final String CORNER_TAG = "Corner";
	public static final String ID_ATTR = "id";
	public static final String WIDTH_ATTR = "width";
	public static final String HEIGHT_ATTR = "height";
	public static final String FLIP_H_ATTR = "flipH";
	public static final String FLIP_V_ATTR = "flipV";
	
	public static final String BORDER_TAG = "Border";
	public static final String REFS_ATTR = "refs";
	public static final String COUNT_ATTR = "count";
		
	//
	
	public static final int INDEX_CORNER_UPPER_LEFT = 0;
	public static final int INDEX_CORNER_UPPER_RIGHT = 1;
	public static final int INDEX_CORNER_LOWER_LEFT = 2;
	public static final int INDEX_CORNER_LOWER_RIGHT = 3;
	
	public static final int INDEX_BORDER_TOP = 0;
	public static final int INDEX_BORDER_BOTTOM = 1;
	public static final int INDEX_BORDER_LEFT = 2;
	public static final int INDEX_BORDER_RIGHT = 3;
	
	private float bodyPaddingX;
	private float bodyPaddingY;
			
	private final CornerData[] corners;
	private final BorderData[] borders;
	
	private final Color bodyColor;
	
	private TextureAtlas textureAtlas;
	
	private boolean bodyUsesTexture;
	
	public GridFrame(ResourceManager creator, int id, String group) {
		super(0, 0, creator, id, group);
		
		corners = new CornerData[4];
		borders = new BorderData[4];
		
		bodyColor = new Color(0,0,0,0);
	}
	
	public Texture getTexture() {
		return textureAtlas != null ? textureAtlas.getTexture() : null;
	}
	
	public boolean isBodyValid() {
		return bodyUsesTexture || bodyColor.alpha > 0;
	}
	
	public boolean isBodyUsingTexture() {
		return bodyUsesTexture;
	}
	
	public Color getBodyColor() {
		return bodyColor;
	}
	
	public float getBodyPaddingX() {
		 return bodyPaddingX;
	}
	
	public float getBodyPaddingY() {
		 return bodyPaddingY;
	}
	
	public void drawBody(GL10 gl) {
		if(isBodyValid()) {
			drawStrip(gl, 0, 6);
		}
	}
	
	public boolean isCornerValid(int cornerInd) {
		return corners[cornerInd] != null;
	}
	
	public float getCornerWidth(int cornerInd) {
		CornerData c = corners[cornerInd];
		return c != null ? c.width : 0;
	}
	
	public float getCornerHeight(int cornerInd) {
		CornerData c = corners[cornerInd];
		return c != null ? c.height : 0;
	}
	
	public void drawCorner(GL10 gl, int cornerInd) {
		CornerData c = corners[cornerInd];
		if(c != null) {
			drawStrip(gl, c.indOfs, 6);
		}
	}
	
	public boolean isBorderValid(int borderInd) {
		return borders[borderInd] != null;
	}
	
	public float getBorderCellSize(int borderInd) {
		BorderData b = borders[borderInd];
		return b != null ? b.size : 0;
	}
	
	public void drawBorder(GL10 gl, int borderInd) {
		BorderData b = borders[borderInd];
		if(b != null) {
			drawStrip(gl, b.indOfs, b.count);
		}
	}
	
	public float getLeftExtend() {
		float largest = getCornerWidth(INDEX_CORNER_UPPER_LEFT);
		
		float cornerLowLeft = getCornerWidth(INDEX_CORNER_LOWER_LEFT);
		if(cornerLowLeft > largest) {
			largest = cornerLowLeft;
		}
		
		float borderLeft = getBorderCellSize(INDEX_BORDER_LEFT);
		if(borderLeft > largest) {
			return borderLeft;
		}
		
		return largest;
	}
	
	public float getTopExtend() {
		float largest = getCornerHeight(INDEX_CORNER_UPPER_LEFT);
		
		float cornerUpRight = getCornerHeight(INDEX_CORNER_UPPER_RIGHT);
		if(cornerUpRight > largest) {
			largest = cornerUpRight;
		}
		
		float borderUp = getBorderCellSize(INDEX_BORDER_TOP);
		if(borderUp > largest) {
			return borderUp;
		}
		
		return largest;
	}
	
	public float getRightExtend() {
		float largest = getCornerWidth(INDEX_CORNER_UPPER_RIGHT);
		
		float cornerLowRight = getCornerWidth(INDEX_CORNER_LOWER_RIGHT);
		if(cornerLowRight > largest) {
			largest = cornerLowRight;
		}
		
		float borderRight = getBorderCellSize(INDEX_BORDER_RIGHT);
		if(borderRight > largest) {
			return borderRight;
		}
		
		return largest;
	}
	
	public float getBottomExtend() {
		float largest = getCornerHeight(INDEX_CORNER_LOWER_LEFT);
		
		float cornerLowRight = getCornerHeight(INDEX_CORNER_LOWER_RIGHT);
		if(cornerLowRight > largest) {
			largest = cornerLowRight;
		}
		
		float borderLow = getBorderCellSize(INDEX_BORDER_BOTTOM);
		if(borderLow > largest) {
			return borderLow;
		}
		
		return largest;
	}
	
	@Override
	protected void prepareImpl() {		
		if(!isBufferCreated()) {
			final Context context = systemRegistry.contextParameters.context;
			assert context != null;
			
			final TextureAtlasManager txtAtlasMgr = systemRegistry.textureAtlasManager;
			assert txtAtlasMgr != null;
			
			final CornerLoadData[] cornerLoads = new CornerLoadData[4];
			final BorderLoadData[] borderLoads = new BorderLoadData[4];
			
			String bodyRef = null;
			
			numCol = 0;
			numRow = 1;
			
			XmlResourceParser xml = context.getResources().getXml(id);
			try {
				String attr;
				String val;
				int attrCount;
				
				for(int eventType = xml.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xml.next()) {
					if(eventType == XmlPullParser.START_TAG) {
						String tagName = xml.getName();
						attrCount = xml.getAttributeCount();
						
						if(tagName.compareTo(FRAME_TAG) == 0) {
							for(int i = 0; i < attrCount; ++i) {
								attr = xml.getAttributeName(i);
								val = xml.getAttributeValue(i);
								
								if(attr.compareTo(TEXTURE_ATLAS_ATTR) == 0) {
									textureAtlas = (TextureAtlas)txtAtlasMgr.getByName(val);
									if(textureAtlas == null) {
										throw new Exception("Texture atlas not loaded: "+val);
									}
								}
							}
						}
						else if(tagName.compareTo(BODY_TAG) == 0) {
							
							int[] clr = {255,255,255,255};
							
							for(int i = 0; i < attrCount; ++i) {
								attr = xml.getAttributeName(i);
								val = xml.getAttributeValue(i);
								
								if(attr.compareTo(REF_ATTR) == 0) {
									bodyRef = val;
								}
								else if(attr.compareTo(COLOR_ATTR) == 0) {
									String[] vals = val.split(",");
									for(int c = 0; c < vals.length; c++) {
										clr[c] = Integer.parseInt(vals[c]);
									}
								}
								else if(attr.compareTo(PADDING_X_ATTR) == 0) {
									bodyPaddingX = Float.parseFloat(val);
								}
								else if(attr.compareTo(PADDING_Y_ATTR) == 0) {
									bodyPaddingY = Float.parseFloat(val);
								}
							}
							
							bodyColor.red = clr[0]/255.0f;
							bodyColor.green = clr[1]/255.0f;
							bodyColor.blue = clr[2]/255.0f;
							bodyColor.alpha = clr[3]/255.0f;
							
							bodyUsesTexture = bodyRef != null;
														
							numCol++;
						}
						else if(tagName.compareTo(CORNER_TAG) == 0) {
							CornerLoadData corner = new CornerLoadData();
							int ind = -1;
							
							for(int i = 0; i < attrCount; ++i) {
								attr = xml.getAttributeName(i);
								val = xml.getAttributeValue(i);
								
								if(attr.compareTo(ID_ATTR) == 0) {
									if(val.compareTo("UL") == 0) {
										ind = INDEX_CORNER_UPPER_LEFT;
										corner.ofsX = -1;
									}
									else if(val.compareTo("UR") == 0) {
										ind = INDEX_CORNER_UPPER_RIGHT;
									}
									else if(val.compareTo("LL") == 0) {
										ind = INDEX_CORNER_LOWER_LEFT;
										corner.ofsX = -1;
										corner.ofsY = -1;
									}
									else if(val.compareTo("LR") == 0) {
										ind = INDEX_CORNER_LOWER_RIGHT;
										corner.ofsY = -1;
									}
								}
								else if(attr.compareTo(REF_ATTR) == 0) {
									corner.ref = val;
								}
								else if(attr.compareTo(WIDTH_ATTR) == 0) {
									corner.width = Float.parseFloat(val);
								}
								else if(attr.compareTo(HEIGHT_ATTR) == 0) {
									corner.height = Float.parseFloat(val);
								}
								else if(attr.compareTo(FLIP_H_ATTR) == 0) {
									corner.flipH = Boolean.parseBoolean(val);
								}
								else if(attr.compareTo(FLIP_V_ATTR) == 0) {
									corner.flipV = Boolean.parseBoolean(val);
								}
							}
							
							if(ind >= 0 && ind < 4) {
								corner.ofsX *= corner.width;
								corner.ofsY *= corner.height;
								cornerLoads[ind] = corner;
								numCol++;
							}
						}
						else if(tagName.compareTo(BORDER_TAG) == 0) {
							BorderLoadData border = new BorderLoadData();
							
							int ind = -1;
							
							for(int i = 0; i < attrCount; ++i) {
								attr = xml.getAttributeName(i);
								val = xml.getAttributeValue(i);
								
								if(attr.compareTo(ID_ATTR) == 0) {
									if(val.compareTo("T") == 0) {
										ind = INDEX_BORDER_TOP;
									}
									else if(val.compareTo("B") == 0) {
										ind = INDEX_BORDER_BOTTOM;
										border.ofs = -1;
									}
									else if(val.compareTo("L") == 0) {
										ind = INDEX_BORDER_LEFT;
										border.ofs = -1;
									}
									else if(val.compareTo("R") == 0) {
										ind = INDEX_BORDER_RIGHT;
									}
								}
								else if(attr.compareTo(REF_ATTR) == 0) {
									border.refs.add(val);
								}
								else if(attr.compareTo(REFS_ATTR) == 0) {
									String[] vals = val.split(",");
									for(int c = 0; c < vals.length; c++) {
										border.refs.add(vals[c].trim());
									}
								}
								else if(attr.compareTo(WIDTH_ATTR) == 0 || attr.compareTo(HEIGHT_ATTR) == 0) {
									border.cellSize = Float.parseFloat(val);
								}
								else if(attr.compareTo(COUNT_ATTR) == 0) {
									border.count = Integer.parseInt(val);
								}
								else if(attr.compareTo(FLIP_H_ATTR) == 0) {
									border.flipH = Boolean.parseBoolean(val);
								}
								else if(attr.compareTo(FLIP_V_ATTR) == 0) {
									border.flipV = Boolean.parseBoolean(val);
								}
							}
							
							if(ind >= 0 && ind < 4) {
								border.ofs *= border.cellSize;
								borderLoads[ind] = border;
								numCol += border.count;
							}
						}
					}
				}
			} catch(Exception e) {
				DebugLog.e("GridFrame load", e.toString(), e);
			} finally {
				xml.close();
			}
			
			//
			//texture atlas must exist!
			assert textureAtlas != null : "No texture atlas loaded!";
			
			//load the buffer if not yet loaded
			super.prepareImpl();
			
			//fill in the buffer
			final Texture texture = textureAtlas.getTexture();
			
			int ind = 0;
			
			//body
			if(isBodyValid()) {
				initCell(ind, texture, bodyRef, false, false, 0, 0, 1, 1);
				ind++;
			}
					
			//corners
			for(int i = 0; i < cornerLoads.length; ++i) {
				CornerLoadData cd = cornerLoads[i];
				if(cd != null && cd.isValid()) {
					corners[i] = new CornerData(ind*6, cd.width, cd.height);
					
					initCell(ind, texture, cd.ref, cd.flipH, cd.flipV, cd.ofsX, cd.ofsY, cd.width, cd.height);
					ind++;
				}
			}
			
			//borders
			for(int i = 0; i < borderLoads.length; ++i) {
				BorderLoadData bd = borderLoads[i];
				if(bd != null && bd.isValid()) {
					borders[i] = new BorderData(bd.cellSize, ind*6, bd.count*6);
					
					if(i == INDEX_BORDER_LEFT || i == INDEX_BORDER_RIGHT) {
						initCellsVertical(ind, bd.count, texture, bd.refs, 
								bd.flipH, bd.flipV, bd.ofs, bd.cellSize);
					}
					else {
						initCellsHorizontal(ind, bd.count, texture, bd.refs, 
								bd.flipH, bd.flipV, bd.ofs, bd.cellSize);
					}
					
					ind += bd.count;
				}
			}
		}	
	}
	
	@Override
	protected void unprepareImpl() {
		numCol = numRow = 0;
		
		textureAtlas = null;
		
		for(int i = 0; i < 4; ++i) {
			corners[i] = null;
			borders[i] = null;
		}
		
		bodyUsesTexture = false;
		bodyColor.reset();
		
		super.unprepareImpl();
	}
			
	private void initCellsHorizontal(int ind, int count,
			Texture texture, ArrayList<String> textureAtlasElements, 
			boolean textureFlipH, boolean textureFlipV,
			float y, float height) {
		
		int elemCount = textureAtlasElements.size();
		
		float x = 0, width = 1.0f/count;
		for(int i = 0; i < count; ++i, x += width) {
			initCell(ind+i, texture, textureAtlasElements.get(i%elemCount), textureFlipH, textureFlipV, x, y, width, height);
		}
	}
	
	private void initCellsVertical(int ind, int count,
			Texture texture, ArrayList<String> textureAtlasElements, 
			boolean textureFlipH, boolean textureFlipV,
			float x, float width) {
		
		int elemCount = textureAtlasElements.size();
		
		float y = 0, height = 1.0f/count;
		for(int i = 0; i < count; ++i, y += height) {
			initCell(ind+i, texture, textureAtlasElements.get(i%elemCount), textureFlipH, textureFlipV, x, y, width, height);
		}
	}
	
	private static final float GL_MAGIC_OFFSET = 0.375f;
	
	private void initCell(int ind, 
			Texture texture, String textureAtlasElement, 
			boolean textureFlipH, boolean textureFlipV,
			float x, float y, float width, float height) {
		
		float s1=0,s2=1,t1=0,t2=1;
		
		if(texture != null && textureAtlasElement != null && textureAtlasElement.length() > 0) {
			
			float trw = 1.0f/texture.width;
			float trh = 1.0f/texture.height;
			
			final int crop[] = textureAtlas.getElement(textureAtlasElement);
			
			if(textureFlipH) {
				s1 = crop[0]+crop[2]; s1 = (s1-GL_MAGIC_OFFSET)*trw;
				s2 = crop[0]; s2 = (s2+GL_MAGIC_OFFSET)*trw;
			}
			else {
				s1 = crop[0]; s1 = (s1+GL_MAGIC_OFFSET)*trw;
				s2 = crop[0]+crop[2]; s2 = (s2-GL_MAGIC_OFFSET)*trw;
			}
			
			if(textureFlipV) {
				t1 = crop[1]; t1 = (t1-GL_MAGIC_OFFSET)*trh;
				t2 = crop[1]-crop[3]; t2 = (t2+GL_MAGIC_OFFSET)*trh; 
			}
			else {
				t1 = crop[1]-crop[3]; t1 = (t1+GL_MAGIC_OFFSET)*trh;
				t2 = crop[1]; t2 = (t2-GL_MAGIC_OFFSET)*trh;
			}
			
		}
		
		final float[][] uvs = {{s1, t2}, {s2, t2}, {s1, t1}, {s2, t1}};
						
		float vX1 = x;
		float vY1 = y;
		float vX2 = x + width;
		float vY2 = y + height;
		
		final float[][] positions = { 
				{vX1, vY1, 0.0f}, 
				{vX2, vY1, 0.0f},
				{vX1, vY2, 0.0f}, 
				{vX2, vY2, 0.0f}};
		
		set(ind, 0, positions, uvs);
	}
	
	private class CornerLoadData {
		public String ref = null;
		public float width = 0;
		public float height = 0;
		public boolean flipH = false;
		public boolean flipV = false;
		public float ofsX=0;
		public float ofsY=0;
		
		public boolean isValid() {
			return ref != null && ref.length() > 0 && width > 0 && height > 0;
		}
	}
	
	private class BorderLoadData {
		public final ArrayList<String> refs = new ArrayList<String>();
		public float cellSize = 0;
		public boolean flipH = false;
		public boolean flipV = false;
		public int count = 1;
		public float ofs=0;
		
		public boolean isValid() {
			return refs.size() > 0 && count > 0 && cellSize > 0;
		}
	}
	
	private class CornerData {
		public final float width;
		public final float height;
		public final int indOfs;
		
		public CornerData(int indOfs, float width, float height) {
			this.indOfs = indOfs;
			this.width = width;
			this.height = height;
		}
	}
	
	private class BorderData {
		public final float size;
		public final int indOfs;
		public final int count;
		
		public BorderData(float size, int indOfs, int count) {
			this.size = size;
			this.indOfs = indOfs;
			this.count = count;
		}
	}
}

package com.renegadeware.m8.gfx;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.XmlResourceParser;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

public class Font extends Resource {
	
	public static final String INFO_TAG = "info";
	public static final String FACE_ATTR = "face";
	public static final String SIZE_ATTR = "size";
	
	public static final String COMMON_TAG = "common";
	public static final String LINE_HEIGHT_ATTR = "lineHeight";
	public static final String BASE_ATTR = "base";
	public static final String PAGES_ATTR = "pages";
	
	public static final String PAGES_TAG = "pages";
	
	public static final String PAGE_TAG = "page";
	public static final String ID_ATTR = "id";
	public static final String FILE_ATTR = "file";
	
	public static final String CHAR_TAG = "char";
	public static final String X_ATTR = "x";
	public static final String Y_ATTR = "y";
	public static final String WIDTH_ATTR = "width";
	public static final String HEIGHT_ATTR = "height";
	public static final String X_OFFSET_ATTR = "xoffset";
	public static final String Y_OFFSET_ATTR = "yoffset";
	public static final String X_ADVANCE_ATTR = "xadvance";
	public static final String PAGE_ATTR = "page";
	
	private Texture textures[];
	private final Character chars[];
	
	private String name;
	private float lineHeight;
	private float base;
	private float pointSize;

	public Font(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		chars = new Character[256];
	}

	public Font(int id) {
		super(id);
		
		chars = new Character[256];
	}
	
	public String getName() {
		return name;
	}
	
	public Texture getTexture(int page) {
		return textures[page];
	}
	
	public Texture[] getTextures() {
		return textures;
	}
	
	public float getLineHeight() {
		return lineHeight;
	}
	
	public float getBase() {
		return base;
	}
	
	public float getPointSize() {
		return pointSize;
	}
	
	public Character getChar(char c) {
		return chars[c];
	}
	
	public Character[] getChars() {
		return chars;
	}
	
	public float getStringWidth(String str) {
		float size = 0;
		
		final Character[] chars = this.chars;
		final int strCount = str.length();
		for(int i = 0; i < strCount; ++i) {
			char cInd = str.charAt(i);
			if(cInd == '\t') {
				Character space = chars[' '];
				size += space == null ? 0 : space.xAdvance*4;
			}
			else {
				Character c = chars[cInd];
				if(c != null) {
					size += c.xAdvance;
				}
			}
		}
		
		return size;
	}
	
	private final boolean isWhitespace(char c) {
		return c == ' ' || c == '\t' || c == '\r' || c == '\f';
	}
	
	public int getNumValidChar(String text, boolean ignoreSpaceChar) {
		int num = 0;
		final Character[] chars = this.chars;
		final int slen = text.length();
		for(int i = 0; i < slen; ++i) {
			final char cInd = text.charAt(i);
			Character c = chars[cInd];
			if(c != null) {
				if(!ignoreSpaceChar || !isWhitespace(cInd)) {
					num++;
				}
			}
		}
		return num;
	}
				
	public ArrayList<String> splitText(String text, float maxWidth, float pointSize) {
		final ArrayList<String> lines = new ArrayList<String>();
		
		final Character[] chars = this.chars;
		
		final float scale = pointSize == 0 ? 1 : pointSize/this.pointSize;
		
		Font.Character space = chars[' '];
		final float spaceAdvance = space == null ? 0 : space.xAdvance*scale;
		
		float x = 0, xWord = 0;
		int lineStartInd = 0;
		int lineCount = 0;
		int wordStartInd = 0;
		
		final int strSize = text.length();
		
		for(int i = 0; i < strSize; i++) {
			char cInd = text.charAt(i);
			
			//check for a newline
			if(cInd == '\n') {
				//stuff current string being parsed
				if(lineCount > 0) {
					lines.add(text.substring(lineStartInd, lineStartInd+lineCount).trim());
				}
				else {
					lines.add("");
				}

				xWord = x = 0;
				lineStartInd = wordStartInd = i+1;
				lineCount = 0;
			}
			else if (isWhitespace(cInd)) {
				// move x if it's a space or a tab
				if(cInd == ' ') {
					x += spaceAdvance;
				}
				else if(cInd == '\t') {
					x += spaceAdvance*4;
				}
				
				// did we hit the bound?
				if(x > maxWidth) {
					if(lineCount > 0) {
						lines.add(text.substring(lineStartInd, lineStartInd+lineCount).trim());
					}
					else {
						lines.add("");
					}

					xWord = x = 0;
					lineStartInd = wordStartInd = i+1;
					lineCount = 0;
				}
				else {
					//this will move the word's start to the next i
					wordStartInd = i+1;
					xWord = x;
				}
			}
			else {
				Character c = chars[cInd];
				if(c != null) {
					float adv = c.xAdvance*scale;
					x += adv;
					
					//check if char goes out of bound
					if(x > maxWidth) {
						//stuff current line minus the current word,
						//then set the line to the current word's start and move on
						if(wordStartInd == lineStartInd) {
							//the word is too big, just stuff it and move on
							lines.add(text.substring(lineStartInd, lineStartInd+lineCount));
							
							lineStartInd = wordStartInd = i;
							lineCount = 0;
							x = adv;
						}
						else {
							lines.add(text.substring(lineStartInd, wordStartInd).trim());
							
							lineStartInd = wordStartInd;
							lineCount = i-wordStartInd;
							x = x-xWord;
						}
						
						xWord = 0;
					}
				}
			}
			
			lineCount++;
		}
		
		//get the last line
		if(lineCount > 0) {
			lines.add(text.substring(lineStartInd, strSize).trim());
		}
		
		return lines;
	}

	@Override
	protected void loadImpl() {
		final Context context = systemRegistry.contextParameters.context;
		assert context != null;
		
		XmlResourceParser xml = context.getResources().getXml(id);
		try {
			int attrCount;
			String attr, val;
			boolean negPtSize = false;
			
			for(int eventType = xml.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xml.next()) {
				
				if(eventType == XmlPullParser.START_TAG) {
					String tagName = xml.getName();
					attrCount = xml.getAttributeCount();
					
					if(tagName.compareTo(INFO_TAG) == 0) {
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(FACE_ATTR) == 0) {
								name = val;
							}
							else if(attr.compareTo(SIZE_ATTR) == 0) {
								pointSize = Float.parseFloat(val);
								
								negPtSize = pointSize < 0;
								
								if(negPtSize) { //why is it negative?
									pointSize *= -1;
								}
							}
						}
					}
					else if(tagName.compareTo(COMMON_TAG) == 0) {
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(LINE_HEIGHT_ATTR) == 0) {
								lineHeight = Float.parseFloat(val);
							}
							else if(attr.compareTo(BASE_ATTR) == 0) {
								base = Float.parseFloat(val);
							}
							else if(attr.compareTo(PAGES_ATTR) == 0) {
								int numPages = Integer.parseInt(val);
								textures = new Texture[numPages];
							}
						}
					}
					else if(tagName.compareTo(PAGE_TAG) == 0) {
						int pId=-1, pTxtId=0;
						
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(ID_ATTR) == 0) {
								pId = Integer.parseInt(val);
							}
							else if(attr.compareTo(FILE_ATTR) == 0) {
								pTxtId = systemRegistry.textureManager.getResourceIdByFilename(val);
							}
						}
						
						if(pId >= 0 && pId < textures.length) {
							textures[pId] = (Texture)systemRegistry.textureManager.getById(pTxtId);
						}
					}
					else if(tagName.compareTo(CHAR_TAG) == 0) {
						int cId=-1, x=0, y=0, w=0, h=0, xOfs=0, yOfs=0, xAdv=0, pId=-1;
						
						for(int i = 0; i < attrCount; ++i) {
							attr = xml.getAttributeName(i);
							val = xml.getAttributeValue(i);
							
							if(attr.compareTo(ID_ATTR) == 0) {
								cId = Integer.parseInt(val);
							}
							else if(attr.compareTo(X_ATTR) == 0) {
								x = Integer.parseInt(val);
							}
							else if(attr.compareTo(Y_ATTR) == 0) {
								y = Integer.parseInt(val);
							}
							else if(attr.compareTo(WIDTH_ATTR) == 0) {
								w = Integer.parseInt(val);
							}
							else if(attr.compareTo(HEIGHT_ATTR) == 0) {
								h = Integer.parseInt(val);
							}
							else if(attr.compareTo(X_OFFSET_ATTR) == 0) {
								xOfs = Integer.parseInt(val);
							}
							else if(attr.compareTo(Y_OFFSET_ATTR) == 0) {
								yOfs = Integer.parseInt(val);
							}
							else if(attr.compareTo(X_ADVANCE_ATTR) == 0) {
								xAdv = Integer.parseInt(val);
							}
							else if(attr.compareTo(PAGE_ATTR) == 0) {
								pId = Integer.parseInt(val);
							}
						}
																		
						if(cId >= 0 && cId < chars.length && pId >= 0 && pId < textures.length) {
							chars[cId] = new Character(x, y, w, h, xOfs, negPtSize ? -yOfs : yOfs, xAdv, pId, lineHeight);
						}
					}
				}
			}
		} catch(Exception e) {
			DebugLog.e("Font load", e.toString(), e);
		} finally {
			xml.close();
		}
	}

	@Override
	protected void unloadImpl() {
		textures = null;
		
		for(int i = 0; i < chars.length; ++i) {
			chars[i] = null;
		}
	}

	@Override
	protected int calculateSize() {
		return 0;
	}
	
	public class Character {
		public final int crop[];
		
		public final float width;
		public final float height;
		
		public final float xOffset;
		public final float yOffset;
		
		public final float xAdvance;
		
		public final int page;
		
		public Character(int x, int y, int w, int h, int xOfs, int yOfs, int xAdv, int pg, float lineHeight) {
			
			crop = new int[4];
			crop[0] = x;
	        crop[1] = y+h;
	        crop[2] = w;
	        crop[3] = -h;
	        
	        width = w;
	        height = h;
	        
	        xOffset = xOfs;
	        yOffset = -yOfs-h+lineHeight;
	        
	        xAdvance = xAdv;
	        
	        page = pg;
		}
	}

}

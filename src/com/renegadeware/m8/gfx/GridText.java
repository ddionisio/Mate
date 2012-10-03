package com.renegadeware.m8.gfx;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ResourceManager;

public class GridText extends Grid {
	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	public static final int ALIGN_CENTER = 2;
		
	//data create after setting up
	private Font font;
	private float lineHeight;
	
	private float boxHeight;
	private float scale;
	
	private LineInfo[] lineInfo;
	
	public GridText(ResourceManager creator, int id, String group) {
		super(0, 0, creator, id, group);
		
	}
	
	public int getLineCount() {
		if(lineInfo == null) {
			return 0;
		}
		
		return lineInfo.length;
	}
	
	public float getWidth() {
		return ((GridTextParam)this.params).maxWidth;
	}
	
	public float getHeight() {
		return boxHeight;
	}
	
	public float getLineHeight() {
		return lineHeight;
	}
	
	public float getScale() {
		return scale;
	}
	
	public int getNumChars() {
		return numCol;
	}
	
	public int getCharOffsetLine(int lineIndex) {
		if(lineInfo == null) {
			return 0;
		}
		
		return lineInfo[lineIndex].charOffset;
	}
	
	public int getNumCharsLine(int lineIndex) {
		if(lineInfo == null) {
			return 0;
		}
		
		return lineInfo[lineIndex].numChar;
	}
	
	public Font getFont() {
		return font;
	}
	
	public void drawLines(GL10 gl, int startLineIndex, int lineCount) {
		assert startLineIndex >= 0 && startLineIndex < lineInfo.length && lineCount > 0;
		
		final LineInfo s = lineInfo[startLineIndex];
		final LineInfo e = lineInfo[startLineIndex + lineCount - 1];
		
		int index = s.charOffset;
		int count = (e.charOffset + e.numChar) - index;
		
		drawStrip(gl, index*6, count*6);
	}
	
	@Override
	protected void prepareImpl() {	
		if(!isBufferCreated()) {
			final GridTextParam params = (GridTextParam)this.params;
			if(params == null) {
				DebugLog.e("GridText", "Parameters are required for: "+id);
				return;
			}
			
			String text;
			if(params.stringParams != null && params.stringParams.length > 0) {
				text = systemRegistry.contextParameters.context.getResources().getString(id, params.stringParams);
			}
			else {
				text = systemRegistry.contextParameters.context.getResources().getString(id);
			}
			
			//final int fontId = params.fontId > 0 ? params.fontId : system;
			final int alignment = params.alignment;
			final float pointSize = params.pointSize;
						
			if(params.fontIdStr != null) {
				font = (Font)systemRegistry.fontManager.getByName(params.fontIdStr);
			}
			else if(params.fontId > 0) {
				font = (Font)systemRegistry.fontManager.getById(params.fontId);
			}
			
			assert font != null && font.isLoaded() : "Font is not found or loaded!";
									
			//initialize buffer
			//determine the number of characters to allocate the buffer
			numRow = 1;
			numCol = font.getNumValidChar(text, true);
				
			super.prepareImpl();
			
			scale = pointSize == 0 ? 1.0f : pointSize/font.getPointSize();
			
			final ArrayList<String> lines;
			
			//get the lines, if maxWidth == 0, then we just have one line
			float maxWidth = params.maxWidth;
			if(maxWidth == 0) {
				lines = new ArrayList<String>(1);
				lines.add(text);
				params.maxWidth = maxWidth = font.getStringWidth(text)*scale;
			}
			else {
				lines = font.splitText(text, maxWidth, pointSize);
			}
									
			//set the grid data
			
			//TODO: multi page support?
			final Texture texture = font.getTexture(0);
			
			assert texture != null : "No texture for font!";
			
			Font.Character space = font.getChar(' ');
			float spaceAdvance = space.xAdvance*scale;
			
			lineHeight = font.getLineHeight()*scale;
			
			if(lines.size() > 0) {
				boxHeight = lineHeight*lines.size();
				
				lineInfo = new LineInfo[lines.size()];
				
				int lineInd = 0;
				int ind = 0;
				float y = boxHeight-lineHeight; //start at top, so y=0 is bottom for consistency with
									 //other render objects
				
				for(String line : lines) {
					LineInfo newLineInfo = new LineInfo();
															
					//iterate through the line and form the quads
					final int cCount = line.length();
					if(cCount > 0) {
						int charCount = 0;
						float x;
						
						float tw = font.getStringWidth(line)*scale;
						
						switch(alignment) {
						case ALIGN_CENTER:
							x = (maxWidth-tw)*0.5f;
							break;
							
						case ALIGN_RIGHT:
							x = maxWidth-tw;
							break;
							
						default:
							x = 0;
						}
						
						for(int i = 0; i < cCount; ++i) {
							final char cInd = line.charAt(i);
							if(cInd == ' ') {
								x += spaceAdvance;
							}
							else if(cInd == '\t') {
								x += spaceAdvance*4;
							}
							else {
								Font.Character c = font.getChar(cInd);
								if(c != null) {
									setCell(ind+charCount, x, y, scale, texture, c);
									
									x += c.xAdvance*scale;
									
									charCount++;
								}
							}
						}
						
						//quads have been processed, set the line info
						newLineInfo.charOffset = ind;
						newLineInfo.numChar = charCount;
						
						ind += charCount;
					}
					
					//add new info, get to the next line
					lineInfo[lineInd] = newLineInfo;
					lineInd++;
					y -= lineHeight;
				}
			}
		}
	}
	
	@Override
	protected void unprepareImpl() {
		numCol = numRow = 0;
		
		lineInfo = null;
		font = null;
		
		super.unprepareImpl();
	}
		
	private static final float GL_MAGIC_OFFSET = 0.375f;
	
	private void setCell(int ind, float x, float y, float scale, Texture texture, Font.Character c) {
		//calculate the texture
		final int crop[] = c.crop;
		
		float trw = 1.0f/texture.width;
		float trh = 1.0f/texture.height;
		
		float s1 = crop[0]; s1 = (s1+GL_MAGIC_OFFSET)*trw;
		float s2 = crop[0]+crop[2]; s2 = (s2-GL_MAGIC_OFFSET)*trw;
		
		float t1 = crop[1]+crop[3]; t1 = (t1+GL_MAGIC_OFFSET)*trh;
		float t2 = crop[1]; t2 = (t2-GL_MAGIC_OFFSET)*trh;
						
		final float[][] uvs = {{s1, t2}, {s2, t2}, {s1, t1}, {s2, t1}};
		
		float vX1 = x + c.xOffset*scale;
		float vY1 = y + c.yOffset*scale;
		float vX2 = vX1 + c.width*scale;
		float vY2 = vY1 + c.height*scale;
		
		final float[][] positions = { 
				{vX1, vY1, 0.0f}, 
				{vX2, vY1, 0.0f},
				{vX1, vY2, 0.0f}, 
				{vX2, vY2, 0.0f}};
		
		set(ind, 0, positions, uvs);
	}
	
	private class LineInfo {
		public int charOffset;
		public int numChar;
		
		public LineInfo() {
			//default to empty line, numChar = 0 means it's an empty line
			charOffset = 0;
			numChar = 0;
		}
	}
}

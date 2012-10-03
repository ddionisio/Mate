package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.renegadeware.m8.gfx.Font.Character;
import com.renegadeware.m8.util.Util;

public class DrawableNumber extends DrawableObject {
	
	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	
	public static final int ALIGN_BOTTOM = 0;
	public static final int ALIGN_TOP = 1;
	
	public static final int ALIGN_CENTER = 2;
	
	public final Color color;
	
	protected Font font;
	
	protected float ptScale;
	
	private int number;
	private int base = 10;
	private final char[] chars;
	private int charCount;
	
	protected int align;
	protected int alignHeight;
	
	protected float width;
	protected float xOfs;
		
	public DrawableNumber(int maxChar) {
		color = new Color();
		chars = new char[maxChar];
				
		reset();
	}
	
	public void setFont(Font fnt) {
		font = fnt;
		
		calculateWidth();
		adjustXOfs();
	}
	
	public void setFont(Font fnt, float ptSize) {
		setFont(fnt);
		
		setPointSize(ptSize);
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setNumber(int num, char appendC) {
		number = num;
		
		if(appendC != 0) {
			chars[0] = appendC;
			charCount = Util.itoa(num, base, chars, 1);
		}
		else {
			charCount = Util.itoa(num, base, chars, 0);
		}
						
		calculateWidth();
		adjustXOfs();
	}
	
	public int getNumber() {
		return number;
	}
	
	public float getWidth() {
		if(ptScale != 0.0f) {
			return width*ptScale;
		}
		
		return width;
	}
	
	public float getHeight() {
		if(font != null) {
			if(ptScale != 0.0f) {
				return font.getLineHeight()*ptScale;
			}
			
			return font.getLineHeight();
		}
		
		return 0;
	}
	
	private void calculateWidth() {
		if(font != null) {
			//recalculate the width
			final Character[] chars = font.getChars();
			width = 0;
			
			final int numChar = charCount;
			final char[] numChars = this.chars;
			
			for(int i = 0; i < numChar; ++i) {
				Character c = chars[numChars[i]];
				if(c != null) {
					width += c.xOffset + c.xAdvance;
				}
			}
		}
	}
	
	private void adjustXOfs() {
		switch(align) {
		case ALIGN_CENTER:
			xOfs = -width*0.5f;
			break;
		case ALIGN_RIGHT:
			xOfs = -width;
			break;
		default:
			xOfs = 0;
			break;
		}
	}
	
	public void setPointSize(float size) {
		if(font != null) {
			ptScale = size/font.getPointSize();
		}
	}
	
	public void setAlign(int a) {
		if(align != a) {
			align = a;
			adjustXOfs();
		}
	}
	
	public int getAlign() {
		return align;
	}
	
	public void setAlignHeight(int a) {
		alignHeight = a;
	}
	
	public int geAlignHeight() {
		return alignHeight;
	}
	
	public void reset() {
		font = null;
		number = 0;
		charCount = 0;
		align = ALIGN_LEFT;
		alignHeight = ALIGN_BOTTOM;
		ptScale = 0.0f;
	}
	
	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL11 gl = (GL11)OpenGLSystem.getGL();
		GL11Ext glExt = (GL11Ext)gl;

		final int numChar = charCount;
		if (gl != null && font != null && numChar > 0) {
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				if(ptScale != 0.0f) {
					scaleX *= ptScale;
					scaleY *= ptScale;
				}
				
				gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				
				final Character[] chars = font.getChars();
				final Texture[] txts = font.getTextures();
				
				float curX = x + xOfs*scaleX;
				
				float adjustedY = y;
				
				switch(alignHeight) {
				case ALIGN_TOP:
					adjustedY -= font.getLineHeight()*scaleY;
					break;
				case ALIGN_CENTER:
					adjustedY -= font.getLineHeight()*0.5f*scaleY;
					break;
				}
				
				char[] charTexts = this.chars;
								
				for(int i = 0; i < numChar; ++i) {
					Character c = chars[charTexts[i]];
					if(c != null) {
						OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, txts[c.page].glId);
						
						gl.glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES, 
								c.crop, 0);
						
						final float ofsX = c.xOffset*scaleX;
						final float w = c.width * scaleX;
						
						glExt.glDrawTexfOES(
								(curX + ofsX) * screenScaleX, 
								(adjustedY + c.yOffset*scaleY) * screenScaleY, 
								getOrder(), 
								w * screenScaleX, 
								c.height * scaleY * screenScaleY);
						
						curX += (c.xAdvance*scaleX);
					}
				}
				
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				
				OpenGLSystem.clearTextureCropSignature();
			}
		}
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		// draw texture assumes only translation and scale
		draw(mtx[12], mtx[13], mtx[0], mtx[5], 0, screenScaleX, screenScaleY);
	}

}

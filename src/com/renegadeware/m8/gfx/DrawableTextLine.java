package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import com.renegadeware.m8.gfx.Font.Character;

public class DrawableTextLine extends DrawableObject {
	
	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_RIGHT = 1;
	
	public static final int ALIGN_BOTTOM = 0;
	public static final int ALIGN_TOP = 1;
	
	public static final int ALIGN_CENTER = 2;
	
	public final Color color;
	
	protected Font font;
	
	private String text;
	
	protected float ptScale;
	
	protected int align;
	protected int alignHeight;
	
	protected float width;
	protected float xOfs;
		
	public DrawableTextLine() {
		color = new Color();
		
		reset();
	}
	
	public void setFont(Font fnt) {
		font = fnt;
		
		calculateWidth();
		adjustXOfs();
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setText(String txt) {
		text = txt;
		
		calculateWidth();
		adjustXOfs();
	}
	
	public void setText(String txt, int align) {
		this.align = align;
		
		setText(txt);
	}
	
	public String getText() {
		return text;
	}
	
	public boolean isEmpty() {
		return font == null || text == null || text.length() == 0;
	}
	
	public float getWidth() {
		if(ptScale != 0.0f) {
			return width*ptScale;
		}
		
		return width;
	}
	
	public float getHeight() {
		if(font != null) {
			if(ptScale > 0.0f) {
				return font.getLineHeight()*ptScale;
			}
			
			return font.getLineHeight();
		}
		
		return 0;
	}
	
	private void calculateWidth() {
		if(font != null && text != null) {
			//recalculate the width
			final Character[] chars = font.getChars();
			width = 0;
			
			final int numChar = text.length();
			for(int i = 0; i < numChar; ++i) {
				Character c = chars[text.charAt(i)];
				if(c != null) {
					width += c.xAdvance;
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
		text = null;
		
		color.reset();
		align = ALIGN_LEFT;
		alignHeight = ALIGN_BOTTOM;
		text = "";
		ptScale = 0.0f;
	}
	
	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL11 gl = (GL11)OpenGLSystem.getGL();
		GL11Ext glExt = (GL11Ext)gl;
		
		final String text = this.text;

		final int numChar = text.length();
		if (gl != null && font != null && numChar > 0) {
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				
				if(ptScale != 0.0f) {
					scaleX *= ptScale;
					scaleY *= ptScale;
				}
				
				final int len = numChar;
				final Character[] chars = font.getChars();
				final int maxChars = chars.length;
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
								
				for(int i = 0; i < len; ++i) {
					final char cInd = text.charAt(i);
					if(cInd >= maxChars) {
						continue;
					}
					
					final Character c = chars[cInd];
					
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

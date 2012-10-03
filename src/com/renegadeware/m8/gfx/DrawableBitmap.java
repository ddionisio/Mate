/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11Ext;

import com.renegadeware.m8.obj.BaseObject;

/** 
 * Draws a screen-aligned bitmap to the screen.
 */
public class DrawableBitmap extends DrawableObject {
		
	public final Color color;
	public boolean useColor;
	
	public boolean useBlend;
	public int blendSrc; //used if useBlend is enabled
	public int blendDst;
	public int textureMode;

	public Texture texture;
	protected final int crop[];
	
	public float width;
	public float height;
	public float anchorX;
	public float anchorY;
	
	public DrawableBitmap(Texture texture, Color color, float width, float height) {
		super();
		this.crop = new int[4];
		this.color = new Color();
		
		reset();
		
		if(color != null) {
			this.color.set(color);
		}
		
		this.texture = texture;
		this.width = width;
		this.height = height;
		
		if(texture != null) {
			setCrop(0, texture.height, texture.width, texture.height);
		}
	}
	
	public DrawableBitmap(Texture texture) {
		super();
		this.crop = new int[4];
		this.color = new Color();
		
		reset();
		
		this.texture = texture;
		
		if(texture != null) {
			//this.width = texture.width;
			//this.height = texture.height;
			
			setCrop(0, texture.height, texture.width, texture.height);
		}
	}
	
	public DrawableBitmap() {
		super();
		
		this.crop = new int[4];
		this.color = new Color();
		
		reset();
	}
	
	public void reset() {
		texture = null;
		color.reset();
		useBlend = useColor = false;
		anchorX = 0;
		anchorY = 0;
		blendSrc = GL10.GL_SRC_ALPHA;
		blendDst = GL10.GL_ONE_MINUS_SRC_ALPHA;
		textureMode = GL10.GL_MODULATE;
	}
	
	public void copyTexture(DrawableBitmap otherD) {
		texture = otherD.texture;
		System.arraycopy(otherD.crop, 0, crop, 0, 4);
	}

	/**
	 * Begins drawing bitmaps. Sets the OpenGL state for rapid drawing.
	 * 
	 * @param gl  A pointer to the OpenGL context.
	 * @param viewWidth  The width of the screen.
	 * @param viewHeight  The height of the screen.
	 */
	public static void beginDrawing(GL10 gl, float viewWidth, float viewHeight) {
		gl.glShadeModel(GL10.GL_FLAT);
		gl.glEnable(GL10.GL_BLEND);
		
		//gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE); //additive
		
		gl.glColor4x(0x10000, 0x10000, 0x10000, 0x10000);

		gl.glMatrixMode(GL10.GL_PROJECTION);
		//gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0.0f, viewWidth, 0.0f, viewHeight, -1000.0f, 1000.0f);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		//gl.glPushMatrix();
		gl.glLoadIdentity();
		
		//gl.glEnable(GL10.GL_SCISSOR_TEST);
		//gl.glScissor(x, y, width, height);

		gl.glEnable(GL10.GL_TEXTURE_2D);

	}

	/**
	 * Draw the bitmap at a given x,y position, expressed in pixels, with the
	 * lower-left-hand-corner of the view being (0,0).
	 * 
	 * @param gl  A pointer to the OpenGL context
	 * @param x  The number of pixels to offset this drawable's origin in the x-axis.
	 * @param y  The number of pixels to offset this drawable's origin in the y-axis
	 * @param screenScaleX The horizontal scale factor between the bitmap resolution and the display resolution.
	 * @param screenScaleY The vertical scale factor between the bitmap resolution and the display resolution.
	 */
	@Override
	public void draw(float x, float y, float scaleX, float scaleY, float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		final Texture texture = this.texture;

		if (gl != null && texture != null) {
			assert texture.isLoaded();
			
			final float width = this.width * scaleX;
			final float height = this.height * scaleY;

			final float snappedX = anchorX == 0 ? x : x + anchorX * scaleX;
			final float snappedY = anchorY == 0 ? y : y + anchorY * scaleY;
			
			final Color c = this.color;
			
			if (c.alpha > 0.0f) {
				OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.glId);

				// This is necessary because we could be drawing the same texture with different
				// crop (say, flipped horizontally) on the same frame.
				OpenGLSystem.setTextureCrop(crop);
				
				if (useColor) {
					gl.glColor4f(c.red, c.green, c.blue, c.alpha);
				}
				
				if(useBlend) {
					gl.glBlendFunc(blendSrc, blendDst);
					gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, textureMode);
				}
				
				//DebugLog.d("fuuuu", "drawing at: "+(snappedX * scaleX)+", "+(snappedY * scaleY)+", "+(width * scaleX)+", "+(height * scaleY));

				((GL11Ext) gl).glDrawTexfOES(snappedX * screenScaleX, snappedY * screenScaleY, 
						getOrder(), width * screenScaleX, height * screenScaleY);

				if (useColor) {
					gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				}
				
				if(useBlend) {
					gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
					gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
				}
			}
		}
	}
	
	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		// draw texture assumes only translation and scale
		draw(mtx[12], mtx[13], mtx[0], mtx[5], 0, screenScaleX, screenScaleY);
	}

	/**
	 * Ends the drawing and restores the OpenGL state.
	 * 
	 * @param gl  A pointer to the OpenGL context.
	 */
	public static void endDrawing(GL10 gl) {
		//gl.glDisable(GL10.GL_BLEND);
		//gl.glMatrixMode(GL10.GL_PROJECTION);
		//gl.glPopMatrix();
		//gl.glMatrixMode(GL10.GL_MODELVIEW);
		//gl.glPopMatrix();
	}

	//TODO: create base bitmap, so we can have float version of setCrop: T[] crop 
	
	/**
	 * Changes the crop parameters of this bitmap.  Note that the underlying OpenGL texture's
	 * parameters are not changed immediately The crop is updated on the
	 * next call to draw().  Note that the image may be flipped by providing a negative width or
	 * height.
	 * 
	 * @param left
	 * @param bottom
	 * @param width
	 * @param height
	 */
	public void setCrop(int left, int bottom, int width, int height) {
		// Negative width and height values will flip the image.
		crop[0] = left;
        crop[1] = bottom;
        crop[2] = width;
        crop[3] = -height;
	}
	
	/*public void setCrop(int[] otherCrop) {
		System.arraycopy(otherCrop, 0, crop, 0, 4);
	}*/

	public int[] getCrop() {
		return crop;
	}

	/**
	 * Set with given texture and set crop to be the entire texture
	 * @param texture
	 */
	public void setTextureAutoCrop(Texture texture) {
		if(texture != null) {
			setCrop(0, texture.height, texture.width, texture.height);
		}
		
		this.texture = texture;
	}
	
	public void setTextureByAtlas(TextureAtlas atlas, String element) {
		texture = atlas.getTexture();
		
		if(element != null) {
			int elem[] = atlas.getElement(element);
			if(elem != null) {
				setCrop(elem[0],elem[1],elem[2],elem[3]);
				//width = elem[TextureAtlas.ELEM_WIDTH];
				//height = elem[TextureAtlas.ELEM_HEIGHT];
			}
		}
	}

	@Override
	public int getTextureId() {
		return texture == null ? 0 : texture.id;
	}

	@Override
	public boolean visibleAtPosition(float x, float y, float scaleX, float scaleY, float viewWidth, float viewHeight) {
		final float width = this.width * scaleX;
		final float height = this.height * scaleY;
		
		final float snappedX = x + anchorX*scaleX;
		final float snappedY = y + anchorY*scaleY;
		
		if (viewWidth > 0) {
			if (snappedX + width < 0 
					|| snappedX > viewWidth 
					|| snappedY + height < 0 
					|| snappedY > viewHeight
					|| color.alpha == 0.0f) {
				return false;
			}
		}
		
		return true;
	}

	public final void setFlip(boolean horzFlip, boolean vertFlip) {
		setCrop(horzFlip ? crop[0] + crop[2] : crop[0], 
				vertFlip ? crop[1] + crop[3] : crop[1], 
						horzFlip ? -crop[2] : crop[2],
								vertFlip ? crop[3] : -crop[3]);
	}
	
	public final boolean isFlippedHorizontal() {
		return crop[2] < 0;
	}
	
	public final boolean isFlippedVertical() {
		return crop[3] > 0;
	}
}

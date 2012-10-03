package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

import com.renegadeware.m8.R;
import com.renegadeware.m8.obj.BaseObject;

public class DrawableBitmapQuad extends DrawableBitmap {
	
	private final Grid grid;
	
	private float tCropS;
	private float tCropT;
	private float tCropScaleS;
	private float tCropScaleT;

	public DrawableBitmapQuad() {
		super();
		
		grid = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
	}
	
	public DrawableBitmapQuad(Texture texture) {
		super(texture);
		
		grid = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
	}

	public DrawableBitmapQuad(Texture texture, Color color, int width, int height) {
		super(texture, color, width, height);
		
		grid = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
	}
	
	//TODO: create base bitmap, so we can have float version of setCrop: T[] crop 
	
	@Override
	public void setCrop(int left, int bottom, int width, int height) {
		super.setCrop(left, bottom, width, height);
		
		tCropS = ((float) crop[0]) / texture.width;
		tCropScaleS = ((float) crop[2]) / texture.width;
		
		if(isFlippedVertical()) {
			tCropT = ((float) (crop[1] - crop[3])) / texture.height;
		}
		else {
			tCropT = ((float) (crop[1] + crop[3])) / texture.height;
		}	
		
		tCropScaleT = ((float) -crop[3]) / texture.height;
	}
	
	@Override
	public void draw(float x, float y, float scaleX, float scaleY, float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		final Texture texture = this.texture;

		if (gl != null && texture != null && grid != null) {
			final boolean isLoaded = texture.isLoaded();

			assert isLoaded;
			
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.glId);
				
				if (useColor) {
					gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				}
				
				if(useBlend) {
					gl.glBlendFunc(blendSrc, blendDst);
					gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, textureMode);
				}
				
				grid.beginDrawingStrips(gl, true);
								
				//set texture trans
				gl.glMatrixMode(GL10.GL_TEXTURE);
				gl.glPushMatrix();
				quadMtx[0] = tCropScaleS;
				quadMtx[1] = 0.0f;
				quadMtx[4] = 0.0f;
				quadMtx[5] = tCropScaleT;
				quadMtx[12] = tCropS;
				quadMtx[13] = tCropT;
				quadMtx[14] = 0.0f;
				gl.glLoadMatrixf(quadMtx, 0);
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glPushMatrix();
				//gl.glLoadIdentity();
				
				//world space
				if(rotate != 0.0f) {
					final float s = android.util.FloatMath.sin(rotate);
					final float c = android.util.FloatMath.cos(rotate);
					quadMtx[0] = c*scaleX;
					quadMtx[1] = s*scaleX;
					quadMtx[4] = -s*scaleY;
					quadMtx[5] = c*scaleY;
				}
				else {
					quadMtx[0] = scaleX;
					quadMtx[1] = 0.0f;
					quadMtx[4] = 0.0f;
					quadMtx[5] = scaleY;
				}
				quadMtx[12] = x;
				quadMtx[13] = y;
				quadMtx[14] = order;
				gl.glLoadMatrixf(quadMtx, 0);
				
				//model space
				final float width = this.width;
				final float height = this.height;
				
				quadMtx[0] = width;
				quadMtx[1] = 0.0f;
				quadMtx[4] = 0.0f;
				quadMtx[5] = height;
				quadMtx[12] = anchorX;
				quadMtx[13] = anchorY;
				quadMtx[14] = 0.0f;
				gl.glMultMatrixf(quadMtx, 0);
								
				grid.drawAllStrips(gl);
				
				//pop out current matrices used by model and texture
				gl.glPopMatrix();
				gl.glMatrixMode(GL10.GL_TEXTURE);
				gl.glPopMatrix();
												
				//grid.endDrawingStrips(gl);
				
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
	
	//TODO: refactor to reduce duplicate code!
	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		final Texture texture = this.texture;

		if (gl != null && texture != null && grid != null) {
			final boolean isLoaded = texture.isLoaded();

			assert isLoaded;
			
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.glId);
				
				if (useColor) {
					gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				}
				
				if(useBlend) {
					gl.glBlendFunc(blendSrc, blendDst);
					gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, textureMode);
				}
				
				grid.beginDrawingStrips(gl, true);
								
				//set texture trans
				gl.glMatrixMode(GL10.GL_TEXTURE);
				gl.glPushMatrix();
				
				quadMtx[0] = tCropScaleS;
				quadMtx[1] = 0.0f;
				quadMtx[4] = 0.0f;
				quadMtx[5] = tCropScaleT;
				quadMtx[12] = tCropS;
				quadMtx[13] = tCropT;
				quadMtx[14] = 0.0f;
				gl.glLoadMatrixf(quadMtx, 0);
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glPushMatrix();
				
				//world space
				gl.glLoadMatrixf(mtx, 0);
								
				//model space
				final float width = this.width;
				final float height = this.height;
				
				quadMtx[0] = width;
				quadMtx[1] = 0.0f;
				quadMtx[4] = 0.0f;
				quadMtx[5] = height;
				quadMtx[12] = anchorX;
				quadMtx[13] = anchorY;
				//gl.glLoadMatrixf(quadMtx, 0);
				gl.glMultMatrixf(quadMtx, 0);
								
				grid.drawAllStrips(gl);
				
				//grid.endDrawingStrips(gl);
				
				//pop out current matrices used by model and texture
				gl.glPopMatrix();
				gl.glMatrixMode(GL10.GL_TEXTURE);
				gl.glPopMatrix();
				
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
	public int getBufferId() {
		return grid.id;
	}
	
	/*
	 * gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadMatrixf(m, offset);
	 */
}

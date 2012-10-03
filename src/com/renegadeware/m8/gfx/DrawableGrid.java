package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

import com.renegadeware.m8.obj.BaseObject;

public class DrawableGrid extends DrawableObject {
	
	public Grid grid;
	
	public Texture texture;
	
	public final Color color;
	public boolean useColor;
	
	public int index;
	public int count;
	
	public float anchorX;
	public float anchorY;
	
	public DrawableGrid() {
		super();
		
		color = new Color();
		
		reset();
	}

	public DrawableGrid(Grid grid, Texture texture) {
		super();
		
		color = new Color();
		
		this.grid = grid;
		this.texture = texture;
	}
	
	public void reset() {
		grid = null;
		index = 0;
		count = 0;
		texture = null;
		color.reset();
		useColor = false;
	}
		
	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		
		GL10 gl = OpenGLSystem.getGL();
		
		if (gl != null && grid != null) {
			assert grid.isLoaded();
			
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				if(texture != null) {
					assert texture.isLoaded();
					
					OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.glId);
					grid.beginDrawingStrips(gl, true);
				}
				else {
					grid.beginDrawingStrips(gl, false);
				}
				
				if (useColor) {
					gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				}
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glPushMatrix();
				//gl.glLoadIdentity();
				
				//world space
				if(rotate != 0.0f) {
					final float s = android.util.FloatMath.sin(rotate);
					final float c = android.util.FloatMath.cos(rotate);
					quadMtx[0] = c*scaleX;
					quadMtx[1] = s;
					quadMtx[4] = -s;
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
				if(anchorX != 0 || anchorY != 0) {
					quadMtx[0] = 1.0f;
					quadMtx[1] = 0.0f;
					quadMtx[4] = 0.0f;
					quadMtx[5] = 1.0f;
					quadMtx[12] = anchorX;
					quadMtx[13] = anchorY;
					quadMtx[14] = 0.0f;
					gl.glMultMatrixf(quadMtx, 0);
				}
				
				if(count == 0) {
					grid.drawAllStrips(gl);
				}
				else {
					grid.drawStrip(gl, index, count);
				}
				
				//grid.endDrawingStrips(gl);
				
				gl.glPopMatrix();
				
				if (useColor) {
					gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				}
			}
		}
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		
		if (gl != null && grid != null) {
			assert grid.isLoaded();
			
			final Color clr = this.color;
			
			if (clr.alpha > 0.0f) {
				if(texture != null) {
					assert texture.isLoaded();
					
					OpenGLSystem.bindTexture(GL10.GL_TEXTURE_2D, texture.glId);
					grid.beginDrawingStrips(gl, true);
				}
				else {
					grid.beginDrawingStrips(gl, false);
				}
				
				if (useColor) {
					gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
				}
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				gl.glPushMatrix();
				//gl.glLoadIdentity();
				
				//world space
				gl.glLoadMatrixf(mtx, 0);
				
				//model space
				if(anchorX != 0 || anchorY != 0) {
					quadMtx[0] = 1.0f;
					quadMtx[1] = 0.0f;
					quadMtx[4] = 0.0f;
					quadMtx[5] = 1.0f;
					quadMtx[12] = anchorX;
					quadMtx[13] = anchorY;
					quadMtx[14] = 0.0f;
					gl.glMultMatrixf(quadMtx, 0);
				}
				
				if(count == 0) {
					grid.drawAllStrips(gl);
				}
				else {
					grid.drawStrip(gl, index, count);
				}
				
				//grid.endDrawingStrips(gl);
				
				gl.glPopMatrix();
				
				if (useColor) {
					gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				}
			}
		}
	}

	@Override
	public int getBufferId() {
		return grid != null ? grid.id : 0;
	}
	
	@Override
	public int getTextureId() {
		return texture != null ? texture.id : 0;
	}
}

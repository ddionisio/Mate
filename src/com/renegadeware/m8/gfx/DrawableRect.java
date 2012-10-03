package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

import com.renegadeware.m8.R;
import com.renegadeware.m8.obj.BaseObject;

public class DrawableRect extends DrawableObject {
	private static final float quadMtx[] = {1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	
	private final Grid grid;
	
	public final Color color;

	public DrawableRect() {
		super();
		
		color = new Color();
		
		grid = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
	}
	
	public DrawableRect(Color color) {
		super();
		
		this.color = color == null ? new Color() : new Color(color);
		
		grid = (Grid)BaseObject.systemRegistry.gridManager.getById(R.id.mate_texture_quad);
	}

	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();

		if (gl != null && grid != null) {
			
			if (color.alpha > 0.0f) {
				OpenGLSystem.bindTexture(0, -1);
	            
				gl.glColor4f(color.red, color.green, color.blue, color.alpha);
				
				grid.beginDrawingStrips(gl, false);		
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				//gl.glLoadIdentity();
																
				//view space
				//gl.glScalef(screenScaleX, screenScaleY, 1.0f);
				
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
				//gl.glMultMatrixf(quadMtx, 0);
				
				grid.drawAllStrips(gl);
				
				//grid.endDrawingStrips(gl);
				
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();

		if (gl != null && grid != null) {
			
			if (color.alpha > 0.0f) {
				OpenGLSystem.bindTexture(0, -1);
				
				gl.glColor4f(color.red, color.green, color.blue, color.alpha);
				
				grid.beginDrawingStrips(gl, false);		
				
				//set world trans
				gl.glMatrixMode(GL10.GL_MODELVIEW);
				//gl.glLoadIdentity();
																
				//view space
				//gl.glScalef(screenScaleX, screenScaleY, 1.0f);
				
				//world space
				gl.glLoadMatrixf(mtx, 0);
				//gl.glMultMatrixf(quadMtx, 0);
				
				grid.drawAllStrips(gl);
				
				//grid.endDrawingStrips(gl);
				
				gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			}
		}
	}

	@Override
	public int getBufferId() {
		return grid.id;
	}
}

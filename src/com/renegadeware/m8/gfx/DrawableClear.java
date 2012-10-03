package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

/**
 * Simply clear the entire screen, make sure this is the lowest priority when pushed
 * to the render system
 * 
 * @author ddionisio
 *
 */
public class DrawableClear extends DrawableObject {
	
	public final Color color;

	public DrawableClear() {
		color = new Color();
	}

	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		gl.glClearColor(color.red, color.green, color.blue, color.alpha);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT); 
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		gl.glClearColor(color.red, color.green, color.blue, color.alpha);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT); 
	}

}

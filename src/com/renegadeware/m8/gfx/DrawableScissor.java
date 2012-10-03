package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

/**
 * Use the x, y, scaleX, scaleY in render schedule draw to set the scissor up.  Scheduling with a matrix
 * doesn't do anything.
 * <p>
 * Set the enable to true to apply opengl scissor, false to disable it.
 * @author ddionisio
 *
 */
public class DrawableScissor extends DrawableObject {
	
	public boolean enable;

	public DrawableScissor() {
	}

	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		if (gl != null) {
			if(enable) {
				gl.glEnable(GL10.GL_SCISSOR_TEST);
				gl.glScissor(
						(int)(x*screenScaleX), 
						(int)(y*screenScaleY), 
						(int)(scaleX*screenScaleX), 
						(int)(scaleY*screenScaleY));
			}
			else {
				gl.glDisable(GL10.GL_SCISSOR_TEST);
			}
		}
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
	}

}

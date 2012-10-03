package com.renegadeware.m8.gfx;

import javax.microedition.khronos.opengles.GL10;

public class DrawableFrame extends DrawableObject {
	
	private final float frameMtx[] = {1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	
	protected GridFrame frame;
	
	//TODO: color?
	public Color bodyColorOverride;
	public final Color color;
	public boolean useColor;
	
	public float anchorX;
	public float anchorY;
	public float width;
	public float height;
	
	public DrawableFrame() {
		super();
		
		color = new Color();
		
		reset();
	}

	public DrawableFrame(GridFrame frame) {
		super();
		
		bodyColorOverride = null;
		color = new Color();
		useColor = false;
		
		setFrame(frame);
	}
	
	public void reset() {
		setFrame(null);
		bodyColorOverride = null;
		color.reset();
		useColor = false;
	}
	
	public void setFrame(GridFrame frame) {
		this.frame = frame;
	}
	
	public GridFrame getFrame() {
		return frame;
	}
		
	private void drawFrame(GL10 gl) {
		final Texture texture = frame.getTexture();
		boolean textureBounded = false;
		
		/////////////////////////////////////
		// body
										
		if(frame.isBodyValid()) {
			if(!frame.isBodyUsingTexture()) {
				OpenGLSystem.bindTextureNearestFilter(GL10.GL_TEXTURE_2D, -1);
			}
			else {
				OpenGLSystem.bindTextureNearestFilter(GL10.GL_TEXTURE_2D, texture.glId);
				textureBounded = true;
			}
			
			final Color clr = bodyColorOverride != null ? bodyColorOverride : frame.getBodyColor();
			if(useColor) {
				gl.glColor4f(color.red*clr.red, color.green*clr.green, color.blue*clr.blue, color.alpha*clr.alpha);
			}
			else {
				gl.glColor4f(clr.red, clr.green, clr.blue, clr.alpha);
			}
			
			float px = frame.getBodyPaddingX();
			float py = frame.getBodyPaddingY();
			
			if(px != 0 || py != 0) {
				frameMtx[0] = width - px*2;
				frameMtx[5] = height - py*2;
				frameMtx[12] = anchorX + px;
				frameMtx[13] = anchorY + py;
			}
			else {
				frameMtx[0] = width;
				frameMtx[5] = height;
				frameMtx[12] = anchorX;
				frameMtx[13] = anchorY;
			}
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawBody(gl);
			
			//reset gl state
			gl.glLoadMatrixf(quadMtx, 0);
			
			//TODO: opengl being funny again, mysterious no texture if not rebounded
			if(textureBounded) {
				OpenGLSystem.bindTextureNearestFilter(GL10.GL_TEXTURE_2D, -1);
			}
		}
		
		if(useColor) {
			gl.glColor4f(color.red, color.green, color.blue, color.alpha);
		}
		else {
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		
		//if(!textureBounded) {
			OpenGLSystem.bindTextureNearestFilter(GL10.GL_TEXTURE_2D, texture.glId);
		//}
		
		frameMtx[0] = 1.0f;
		frameMtx[5] = 1.0f;
		
		/////////////////////////////////////
		// corners
		
		//lower left
		if(frame.isCornerValid(GridFrame.INDEX_CORNER_LOWER_LEFT)) {
			frameMtx[12] = anchorX;
			frameMtx[13] = anchorY;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawCorner(gl, GridFrame.INDEX_CORNER_LOWER_LEFT);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		//lower right
		if(frame.isCornerValid(GridFrame.INDEX_CORNER_LOWER_RIGHT)) {
			frameMtx[12] = anchorX + width;
			frameMtx[13] = anchorY;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawCorner(gl, GridFrame.INDEX_CORNER_LOWER_RIGHT);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		//upper left
		if(frame.isCornerValid(GridFrame.INDEX_CORNER_UPPER_LEFT)) {
			frameMtx[12] = anchorX;
			frameMtx[13] = anchorY + height;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawCorner(gl, GridFrame.INDEX_CORNER_UPPER_LEFT);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		//upper right
		if(frame.isCornerValid(GridFrame.INDEX_CORNER_UPPER_RIGHT)) {
			frameMtx[12] = anchorX + width;
			frameMtx[13] = anchorY + height;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawCorner(gl, GridFrame.INDEX_CORNER_UPPER_RIGHT);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		/////////////////////////////////////
		// borders
		
		frameMtx[0] = width;
		
		//bottom
		if(frame.isBorderValid(GridFrame.INDEX_BORDER_BOTTOM)) {			
			frameMtx[12] = anchorX;
			frameMtx[13] = anchorY;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawBorder(gl, GridFrame.INDEX_BORDER_BOTTOM);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		//top
		if(frame.isBorderValid(GridFrame.INDEX_BORDER_TOP)) {
			frameMtx[12] = anchorX;
			frameMtx[13] = anchorY + height;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawBorder(gl, GridFrame.INDEX_BORDER_TOP);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		frameMtx[0] = 1.0f;
		frameMtx[5] = height;
		
		//left
		if(frame.isBorderValid(GridFrame.INDEX_BORDER_LEFT)) {			
			frameMtx[12] = anchorX;
			frameMtx[13] = anchorY;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawBorder(gl, GridFrame.INDEX_BORDER_LEFT);
			
			gl.glLoadMatrixf(quadMtx, 0); //reset gl state
		}
		
		//right
		if(frame.isBorderValid(GridFrame.INDEX_BORDER_RIGHT)) {
			frameMtx[12] = anchorX + width;
			frameMtx[13] = anchorY;
			
			gl.glMultMatrixf(frameMtx, 0);
			
			frame.drawBorder(gl, GridFrame.INDEX_BORDER_RIGHT);
			
			//gl.glLoadMatrixf(quadMtx, 0);
		}
		
		if(useColor) {
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	@Override
	public void draw(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		
		if (gl != null && frame != null && width > 0 && height > 0) {
			assert frame.isLoaded();
			
			frame.beginDrawingStrips(gl, true);
			
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
			
			drawFrame(gl);
			
			gl.glPopMatrix();
			
			
		}
	}

	@Override
	public void draw(float[] mtx, float screenScaleX, float screenScaleY) {
		GL10 gl = OpenGLSystem.getGL();
		
		if (gl != null && frame != null && width > 0 && height > 0) {
			assert frame.isLoaded();
			
			frame.beginDrawingStrips(gl, true);
			
			//set world trans
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glPushMatrix();
			
			//world space
			gl.glLoadMatrixf(mtx, 0);
			
			drawFrame(gl);
			
			gl.glPopMatrix();
		}
	}

	@Override
	public int getBufferId() {
		return frame != null ? frame.id : 0;
	}
	
	@Override
	public int getTextureId() {
		if(frame != null) {
			Texture t = frame.getTexture();
			return t != null ? t.id : 0;
		}
		
		return 0;
	}
}

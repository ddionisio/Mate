package com.renegadeware.m8.gfx;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLU;
import android.opengl.GLUtils;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

/**
 * Simple container class for textures.  Serves as a mapping between Android resource ids and
 * OpenGL texture names, and also as a placeholder object for textures that may or may not have
 * been loaded into vram.  Objects can cache Texture objects but should *never* cache the texture
 * name itself, as it may change at any time.
 */
public class Texture extends Resource {
	
	private static final int[] textureNameWorkspace = new int[1];
    private static final int[] cropWorkspace = new int[4];
	
    public int glId;
    public int width;
    public int height;
    public int rowBytes;

	public Texture(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		resetData();
		
	}

	@Override
	protected void loadImpl() {
		final Context context = systemRegistry.contextParameters.context;
		final GL10 gl = OpenGLSystem.getGL();
		
		assert context != null;
		assert gl != null;
		
		gl.glGenTextures(1, textureNameWorkspace, 0);
        
        int error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            DebugLog.d("Texture Load 1", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + id);
        }
        
        assert error == GL10.GL_NO_ERROR;
        
        int textureName = textureNameWorkspace[0];
        
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureName);
        
        error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            DebugLog.d("Texture Load 2", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + id);
        }
        
        assert error == GL10.GL_NO_ERROR;

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE); //GL10.GL_REPLACE);

        InputStream is = context.getResources().openRawResource(id);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            	e.printStackTrace();
                // Ignore.
            }
        }
        
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        
        error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            DebugLog.d("Texture Load 3", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + id);
        }
        
        assert error == GL10.GL_NO_ERROR;

        cropWorkspace[0] = 0;
        cropWorkspace[1] = bitmap.getHeight();
        cropWorkspace[2] = bitmap.getWidth();
        cropWorkspace[3] = -bitmap.getHeight();

        ((GL11) gl).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,
                        cropWorkspace, 0);

        this.glId = textureName;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.rowBytes = bitmap.getRowBytes();

        bitmap.recycle();
                
        error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            DebugLog.d("Texture Load 4", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + id);
        }
        
        assert error == GL10.GL_NO_ERROR;
	}

	@Override
	protected void unloadImpl() {
		final GL10 gl = OpenGLSystem.getGL();
		
		assert gl != null;
		
		if(glId != 0) {
			textureNameWorkspace[0] = glId;
			gl.glDeleteTextures(1, textureNameWorkspace, 0);
		}
		
		int error = gl.glGetError();
        if (error != GL10.GL_NO_ERROR) {
            DebugLog.d("Texture Delete", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + id);
        }
        
        assert error == GL10.GL_NO_ERROR;
		
		resetData();
	}
	
	@Override
	protected void invalidateImpl() {
		glId = 0;
	}

	@Override
	protected int calculateSize() {
		return height*rowBytes;
	}
	
	private void resetData() {
		glId = -1;
		width = 0;
		height = 0;
		rowBytes = 0;
	}

}

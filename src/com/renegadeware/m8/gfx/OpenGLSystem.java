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
import javax.microedition.khronos.opengles.GL11;
import javax.microedition.khronos.opengles.GL11Ext;

import android.opengl.GLU;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.obj.BaseObject;

/** 
 * An object wrapper for a pointer to the OpenGL context.  Note that the context is only valid
 * in certain threads at certain times (namely, in the Rendering thread during draw time), and at
 * other times getGL() will return null.
 */
public class OpenGLSystem extends BaseObject {

    private static GL10 sGL = null;
    
    //TODO: need a better system, like material
    private static int sLastBoundTexture;
    
    private static int sLastSetCropSignature;
    
    private static int sLastFilterUsed;
    
    public static final void setGL(GL10 gl) {
        sGL = gl;
        sLastBoundTexture = 0;
        sLastSetCropSignature = 0;
        sLastFilterUsed = 0;
    }

    /**
     * Only use this within the render thread, during surface create, ready, and update.
     * @return The gl for OpenGL use.
     */
    public static final GL10 getGL() {
        return sGL;
    }
    
    public static final void bindTexture(int target, int texture) {
        if (sLastBoundTexture != texture || sLastFilterUsed != 0) {        	
        	if(texture == -1) {
        		sGL.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        		sGL.glDisable(GL10.GL_TEXTURE_2D);
        	}
        	else {
        		if(sLastBoundTexture == -1) {
            		sGL.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            		sGL.glEnable(GL10.GL_TEXTURE_2D);        		
            	}
        		
	        	sGL.glBindTexture(target, texture);
	        	
	        	sGL.glTexParameterx(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	        	sGL.glTexParameterx(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	        	
	        	sLastFilterUsed = 0;
	            
	            sLastSetCropSignature = 0;
        	}
        	
        	sLastBoundTexture = texture;
        }
    }
    
    public static final void bindTextureNearestFilter(int target, int texture) {
        if (sLastBoundTexture != texture || sLastFilterUsed != 1) {        	
        	if(texture == -1) {
        		sGL.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        		sGL.glDisable(GL10.GL_TEXTURE_2D);
        	}
        	else {
        		if(sLastBoundTexture == -1) {
            		sGL.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            		sGL.glEnable(GL10.GL_TEXTURE_2D);        		
            	}
        		
	        	sGL.glBindTexture(target, texture);

	        	sGL.glTexParameterx(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
	        	sGL.glTexParameterx(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	        	
	        	sLastFilterUsed = 1;
	        	
	            sLastSetCropSignature = 0;
        	}
        	
        	sLastBoundTexture = texture;
        }
    }
    
    public static final void setTextureCrop(int[] crop) {
        int cropSignature = 0;
        cropSignature = (crop[0] + crop[1]) << 16;
        cropSignature |= crop[2] + crop[3];
        
        if (cropSignature != sLastSetCropSignature) {
            ((GL11) sGL).glTexParameteriv(GL10.GL_TEXTURE_2D, GL11Ext.GL_TEXTURE_CROP_RECT_OES,
                    crop, 0);
            
            /*int error = sGL.glGetError();
            if (error != GL10.GL_NO_ERROR) {
                DebugLog.d("Mate", "GLError: " + error + " (" + GLU.gluErrorString(error) + "): " + crop);
            }*/
            
            sLastSetCropSignature = cropSignature;
        }
    }
    
    public static final void clearTextureCropSignature() {
    	sLastSetCropSignature = 0;
    }
        
    public OpenGLSystem() {
        super();
        sGL = null;
    }

    public OpenGLSystem(GL10 gl) {
        sGL = gl;
    }

	@Override
	public void reset() {
	}
}

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

import com.renegadeware.m8.AllocationGuard;
import com.renegadeware.m8.obj.ObjectPool;
import com.renegadeware.m8.util.Util;

/**
 * DrawableObject is the base object interface for objects that can be rendered to the screen.
 * Note that objects derived from DrawableObject are passed between threads, and that care must be
 * taken when modifying drawable parameters to avoid side-effects (for example, the DrawableFactory
 * class can be used to generate fire-and-forget drawables).
 */
public abstract class DrawableObject extends AllocationGuard {
	protected static final float quadMtx[] = {1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	
	private static final int TEXTURE_SORT_BUCKET_SIZE = 1000;
	
	protected float order;
    protected ObjectPool parentPool;
    
    public abstract void draw(float x, float y, float scaleX, float scaleY, float rotate, float screenScaleX, float screenScaleY);
    public abstract void draw(float[] mtx, float screenScaleX, float screenScaleY);

    public DrawableObject() {
        super();
        
        order = 0.0f;
    }
    
    public void setOrder(float f) {
        order = f;
    }

    public float getOrder() {
        return order;
    }

    public void setParentPool(ObjectPool pool) {
        parentPool = pool;
    }

    public ObjectPool getParentPool() {
        return parentPool;
    }
    
    // Override to allow drawables to be sorted by texture.
    public int getTextureId() {
        return 0;
    }
    
    // Override to allow drawables to be sorted by buffer.
    public int getBufferId() {
        return 0;
    }
    
    // Function to allow drawables to specify culling rules.
    public boolean visibleAtPosition(float x, float y, float scaleX, float scaleY, float viewWidth, float viewHeight) {
        return true;
    }

    /*public int generateRenderPriority(int priority) {
    	final int sortBucket = priority * TEXTURE_SORT_BUCKET_SIZE;
		int sortOffset = 0;
		
			int sign = Util.sign(priority);
			final Texture t = getTexture();
			if(t != null) {
				sortOffset = (t.id % TEXTURE_SORT_BUCKET_SIZE) * sign;
			}
			final Grid g = getBuffer();
			if(g != null) {
				sortOffset += (g.id % TEXTURE_SORT_BUCKET_SIZE) * sign;
			}
			
		return sortBucket + sortOffset;
    }*/
}

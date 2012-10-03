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

import java.util.Comparator;

import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.ObjectManager;
import com.renegadeware.m8.obj.ObjectPool;
import com.renegadeware.m8.obj.PhasedObject;
import com.renegadeware.m8.obj.PhasedObjectManager;
import com.renegadeware.m8.obj.TObjectPool;
import com.renegadeware.m8.util.FixedSizeArray;
import com.renegadeware.m8.util.Util;


/**
 * Manages a double-buffered queue of renderable objects.  The game thread submits drawable objects
 * to the the active render queue while the render thread consumes drawables from the alternate
 * queue.  When both threads complete a frame the queues are swapped.  Note that this class can
 * manage any number (>=2) of render queues, but increasing the number over two means that the game
 * logic will be running significantly ahead of the rendering thread, which may make the user feel
 * that the controls are "loose."
 */
public class RenderSystem extends BaseObject {
	private RenderElementPool elementPool;
	private ObjectManager[] renderQueues;
	private int queueIndex;
	
	private final static RenderElementComparator comparator = new RenderElementComparator();

	private final static int DRAW_QUEUE_COUNT = 2;
	private final static int MAX_RENDER_OBJECTS_PER_FRAME = 384;
	private final static int MAX_RENDER_OBJECTS = MAX_RENDER_OBJECTS_PER_FRAME * DRAW_QUEUE_COUNT;
	
	public RenderSystem() {
        super();
        elementPool = new RenderElementPool(MAX_RENDER_OBJECTS);
        renderQueues = new ObjectManager[DRAW_QUEUE_COUNT];
        for (int x = 0; x < DRAW_QUEUE_COUNT; x++) {
        	PhasedObjectManager mgr = new PhasedObjectManager(MAX_RENDER_OBJECTS_PER_FRAME);
        	mgr.setComparator(comparator);
        	
            renderQueues[x] = mgr;
        }
        queueIndex = 0;
    }

	@Override
	public void reset() {

	}

	/** Note: You'll need to cull the object manually, and set camera position: Viewport.getObjectX/Y */
	public void scheduleForDraw(DrawableObject object, 
			float x, float y, float 
			scaleX, float scaleY, 
			float rotate, 
			int priority) {
		
			RenderElement element = elementPool.allocate();
			if (element != null) {
				element.set(object, x, y, scaleX, scaleY, rotate, priority);
				renderQueues[queueIndex].add(element);
			}
	}
	
	/** Note: make sure to take the camera location into account for the transformation and cull manually */
	public void scheduleForDraw(DrawableObject object, float[] mtx, int priority) {
		RenderElement element = elementPool.allocate();
		if (element != null) {
			element.set(object, mtx, priority);
			renderQueues[queueIndex].add(element);
		}
	}

	private void clearQueue(FixedSizeArray<BaseObject> objects) {
		final int count = objects.getCount();
		final Object[] objectArray = objects.getArray();
		final RenderElementPool elementPool = this.elementPool;
		for (int i = count - 1; i >= 0; i--) {
			RenderElement element = (RenderElement)objectArray[i];
			elementPool.release(element);
			objects.removeLast();
		}

	}

	public void swap(MateRenderer renderer) {
		renderQueues[queueIndex].commitUpdates();

		// This code will block if the previous queue is still being executed.
		renderer.setDrawQueue(renderQueues[queueIndex]); 

		final int lastQueue = (queueIndex == 0) ? DRAW_QUEUE_COUNT - 1 : queueIndex - 1;

		// Clear the old queue.
		FixedSizeArray<BaseObject> objects = renderQueues[lastQueue].getObjects();
		clearQueue(objects);

		queueIndex = (queueIndex + 1) % DRAW_QUEUE_COUNT;
	}

	/* Empties all draw queues and disconnects the game thread from the renderer. */
	public void emptyQueues(MateRenderer renderer) {
		renderer.setDrawQueue(null); 
		for (int x = 0; x < DRAW_QUEUE_COUNT; x++) {
			renderQueues[x].commitUpdates();
			FixedSizeArray<BaseObject> objects = renderQueues[x].getObjects();
			clearQueue(objects);

		}
	}

	public static final class RenderElement extends PhasedObject {
		public RenderElement() {
			super();
		}

		public void set(DrawableObject drawable, 
				float x, float y, 
				float sx, float sy, 
				float rotation, 
				int priority) {
			this.drawable = drawable;
			this.x = x;
			this.y = y;
			this.sx = sx;
			this.sy = sy;
			this.rot = rotation;
			
			if(drawable != null) {
				setPhase(priority);//drawable.generateRenderPriority(priority));
			}
		}
		
		public void set(DrawableObject drawable, float[] mtx, int priority) {
			this.drawable = drawable;
			this.mtx = mtx;
			
			if(drawable != null) {
				setPhase(priority);//drawable.generateRenderPriority(priority));
			}
		}

		public void reset() {
			drawable = null;
			x = 0.0f;
			y = 0.0f;
			sx = 1.0f;
			sy = 1.0f;
			rot = 0.0f;
			mtx = null;
		}

		public DrawableObject drawable;
		public float x;
		public float y;
		public float sx = 1.0f;
		public float sy = 1.0f;
		public float rot;
		public float[] mtx; //if this is not null, then use the drawTransform method
							//this will be limited to the elements: 0,5,12,13,14 for draw_texture
							//so don't use any transformation such as rotation
	}
	
	private static class RenderElementComparator implements Comparator<BaseObject>  {
        public int compare(BaseObject object1, BaseObject object2) {
            if (object1 != null && object2 != null) {
            	RenderElement r1 = (RenderElement)object1;
            	RenderElement r2 = (RenderElement)object2;
            	
            	int p1 = r1.phase;
            	int p2 = r2.phase;
            	
            	if(p1 == p2 && r1.drawable != null && r2.drawable != null) {
            		int tid1 = r1.drawable.getTextureId();
            		int tid2 = r2.drawable.getTextureId();
            		
            		if(tid1 == tid2) {
            			return r1.drawable.getBufferId() - r2.drawable.getBufferId();
            		}
            		else {
            			return tid1 - tid2;
            		}
            	}
 
                return p1 - p2;
                
            } else if (object1 == null && object2 != null) {
                return 1;
            } else if (object2 == null && object1 != null) {
                return -1;
            }
            
            return 0;
        }
    }

	private final class RenderElementPool extends TObjectPool<RenderElement> {

		RenderElementPool(int max) {
			super(max);
		}

		@Override
		public void release(Object element) {
			RenderElement renderable = (RenderElement)element;
			// if this drawable came out of a pool, make sure it is returned to that pool.
			final ObjectPool pool = renderable.drawable.getParentPool();
			if (pool != null) {
				pool.release(renderable.drawable);
			}
			// reset on release
			renderable.reset();
			super.release(element);
		}

		@Override
		protected void fill() {
			for (int x = 0; x < getSize(); x++) {
				getAvailable().add(new RenderElement());
			}
		}
	}
}

package com.renegadeware.m8.gfx;

import com.renegadeware.m8.math.Vector2;
import com.renegadeware.m8.obj.BaseObject;

public class Viewport extends BaseObject {
	private Vector2 focalPosition;
	
	public float width;
	public float height;
	private float halfWidth;
	private float halfHeight;
	
	public Viewport(float width, float height) {
		focalPosition = new Vector2(0, 0);
		
		this.width = width;  
		this.height = height;
		this.halfWidth = width/2;  
		this.halfHeight = height/2;
	}

	@Override
	public void reset() {	
		focalPosition.zero();
	}
	
	/** Returns the x position of the camera's look-at point. */
    public float getFocusPositionX() {
        return focalPosition.x;
    }
    
    /** Returns the y position of the camera's look-at point. */
    public float getFocusPositionY() {
        return focalPosition.y;
    }
        
    public float getObjectX(float x) {
    	return (x - focalPosition.x) + halfWidth;
    }
    
    public float getObjectY(float y) {
    	return (y - focalPosition.y) + halfHeight;
    }
}
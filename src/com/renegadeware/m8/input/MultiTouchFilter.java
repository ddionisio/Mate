package com.renegadeware.m8.input;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.obj.BaseObject;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.MotionEvent;

public class MultiTouchFilter extends SingleTouchFilter {
	private boolean checkedForMultitouch = false;
	private boolean supportsMultitouch = false;
	
    @Override
    public void updateTouch(MotionEvent event) {
		ContextParameters params = systemRegistry.contextParameters;
    	final int pointerCount = event.getPointerCount();
    	for (int x = 0; x < pointerCount; x++) {
    		final int action = event.getAction();
    		final int actualEvent = action & MotionEvent.ACTION_MASK;
    		final int id = event.getPointerId(x);
    		if (actualEvent == MotionEvent.ACTION_POINTER_UP || 
    				actualEvent == MotionEvent.ACTION_UP || 
    				actualEvent == MotionEvent.ACTION_CANCEL) {
        		BaseObject.systemRegistry.inputSystem.touchUp(id, 
        				event.getX(x) * (1.0f / params.viewScaleX), 
        				event.getY(x) * (1.0f / params.viewScaleY));
        	}
    		else if(actualEvent == MotionEvent.ACTION_MOVE) {
    			BaseObject.systemRegistry.inputSystem.touchMove(id, 
        				event.getX(x) * (1.0f / params.viewScaleX), 
        				event.getY(x) * (1.0f / params.viewScaleY));
    		}
    		else {
        		BaseObject.systemRegistry.inputSystem.touchDown(id, 
        				event.getX(x) * (1.0f / params.viewScaleX),
        				event.getY(x) * (1.0f / params.viewScaleY));
        	}
    	}
    }
    
    @Override
    public boolean supportsMultitouch(Context context) {
    	if (!checkedForMultitouch) {
    		PackageManager packageManager = context.getPackageManager();
    		supportsMultitouch = packageManager.hasSystemFeature("android.hardware.touchscreen.multitouch");
    		checkedForMultitouch = true;
    	}
    	
    	return supportsMultitouch;
    }
}

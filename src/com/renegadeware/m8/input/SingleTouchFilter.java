package com.renegadeware.m8.input;

import com.renegadeware.m8.ContextParameters;

import android.view.MotionEvent;

public class SingleTouchFilter extends TouchFilter {

	public void updateTouch(MotionEvent event) {
		ContextParameters params = systemRegistry.contextParameters;
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		systemRegistry.inputSystem.touchUp(0, event.getRawX() * (1.0f / params.viewScaleX), 
    				event.getRawY() * (1.0f / params.viewScaleY));
    	}
    	else if(event.getAction() == MotionEvent.ACTION_MOVE) {
    		systemRegistry.inputSystem.touchMove(0, event.getRawX() * (1.0f / params.viewScaleX), 
    				event.getRawY() * (1.0f / params.viewScaleY));
    	}
    	else {
    		systemRegistry.inputSystem.touchDown(0, event.getRawX() * (1.0f / params.viewScaleX),
    				event.getRawY() * (1.0f / params.viewScaleY));
    	}
    }
	@Override
	public void reset() {
	}

}

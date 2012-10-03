package com.renegadeware.m8.ui;

import com.renegadeware.m8.input.InputXY;

public abstract class ToggleUI extends BaseUI {
	
	protected boolean toggle;
	
	public ToggleUI() {
		super();
		
		inputEnabled = true;
	}
	
	public final boolean getToggle() {
		return toggle;
	}
	
	public final void setToggle(boolean enable) {
		if(toggle != enable) {
			toggle = enable;
			
			onToggleChange();
		}
	}
	
	@Override
	protected void inputTouchReleased(InputXY input) {
		setToggle(!toggle);
	}
	
	@Override
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		if(isInRegion(input.getX(), input.getY(), true)) {
			return true;
		}
		
		return false;
	}
	
	protected abstract void onToggleChange();

}

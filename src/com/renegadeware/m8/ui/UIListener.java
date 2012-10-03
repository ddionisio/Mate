package com.renegadeware.m8.ui;

import com.renegadeware.m8.input.InputXY;

public interface UIListener {
	public void inputTouchPressed(BaseUI ui, InputXY input);
	
	public void inputTouchReleased(BaseUI ui, InputXY input);
	
	public void inputTouchDrag(BaseUI ui, InputXY input);
}

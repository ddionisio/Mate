package com.renegadeware.m8.input;

import com.renegadeware.m8.math.Vector2;
import com.renegadeware.m8.obj.BaseObject;

public class InputSystem extends BaseObject {
	
	private int MAX_TOUCH_POINTS = 5;
	private InputXY touchPoints[];

	public InputSystem() {
		super();
		
		touchPoints = new InputXY[MAX_TOUCH_POINTS];
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			touchPoints[x] = new InputXY();
		}
		
		reset();
	}

	@Override
	public void reset() {
		// reset the touch input
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			touchPoints[x].reset();
		}
	}

	/* ******************************************************************************
	 * Touch Interfaces
	 * ******************************************************************************/
	
	public void touchDown(int index, float x, float y) {
		if (index < MAX_TOUCH_POINTS) {
			final InputXY touch = touchPoints[index];
			
			touch.press(
					systemRegistry.timeSystem.getGameTime(), 
					x, 
					systemRegistry.contextParameters.gameHeight - y);
		}
	}
	
	public void touchMove(int index, float x, float y) {
		if (index < MAX_TOUCH_POINTS) {
			final InputXY touch = touchPoints[index];
			
			// for some reason, touch event will use move even if 'down' was not called
			if(touch.getPressed()) {
				touch.move(systemRegistry.timeSystem.getGameTime(), 
						x, 
						systemRegistry.contextParameters.gameHeight - y);
			}
			else {
				touch.press(systemRegistry.timeSystem.getGameTime(), 
						x, 
						systemRegistry.contextParameters.gameHeight - y);
			}
		}
	}
	
	public void touchUp(int index, float x, float y) {
		if (index < MAX_TOUCH_POINTS) {
			final InputXY touch = touchPoints[index];
			
			touch.release();
		}
	}
	
	public boolean touchGetTriggered(int index, float time) {
		boolean triggered = false;
		if (index < MAX_TOUCH_POINTS) {
			triggered = touchPoints[index].getTriggered(time);
		}
		return triggered;
	}
	
	public boolean touchGetPressed(int index) {
		boolean pressed = false;
		if (index < MAX_TOUCH_POINTS) {
			pressed = touchPoints[index].getPressed();
		}
		return pressed;
	}
	
	public final void touchGetVector(int index, Vector2 vectorOut) {
		if (index < MAX_TOUCH_POINTS) {
			touchPoints[index].getVector(vectorOut);
		}
	}
	
	public final float touchGetX(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = touchPoints[index].getX();
		}
		return magnitude;
	}
	
	public final float touchGetY(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = touchPoints[index].getY();
		}
		return magnitude;
	}
	
	public final void touchGetVectorOnPress(int index, Vector2 vectorOut) {
		if (index < MAX_TOUCH_POINTS) {
			touchPoints[index].getVectorOnPress(vectorOut);
		}
	}
	
	public final float touchGetXOnPress(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = touchPoints[index].getXOnPress();
		}
		return magnitude;
	}
	
	public final float touchGetYOnPress(int index) {
		float magnitude = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			magnitude = touchPoints[index].getYOnPress();
		}
		return magnitude;
	}
	
	public final float touchGetLastPressedTime(int index) {
		float time = 0.0f;
		if (index < MAX_TOUCH_POINTS) {
			time = touchPoints[index].getLastPressedTime();
		}
		return time;
	}
	
	public InputXY touchFindPointerInRegion(float regionX, float regionY, float regionWidth, float regionHeight) {
		InputXY touch = null;
		for (int x = 0; x < MAX_TOUCH_POINTS; x++) {
			final InputXY pointer = touchPoints[x];
			final float px = pointer.getX();
			final float py = pointer.getY();

			if (pointer.getPressed() 
					&& px >= regionX
					&& py >= regionY
					&& px <= regionX + regionWidth
					&& py <= regionY + regionHeight) {
				touch = pointer;
				break;
			}
		}
		return touch;
	}

	public boolean touchGetTriggered(float gameTime) {
		boolean triggered = false;
		for (int x = 0; x < MAX_TOUCH_POINTS && !triggered; x++) {
			triggered = touchPoints[x].getTriggered(gameTime);
		}
		return triggered;
	}
}

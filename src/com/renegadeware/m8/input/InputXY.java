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


package com.renegadeware.m8.input;

import android.util.FloatMath;
import com.renegadeware.m8.math.Vector2;


public class InputXY {
	private InputButton xAxis;
	private InputButton yAxis;
	
	public InputXY() {
		xAxis = new InputButton();
		yAxis = new InputButton();
	}
	
	public InputXY(InputButton xAxis, InputButton yAxis) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
	}
	
	public final void press(float currentTime, float x, float y) {
		xAxis.press(currentTime, x);
		yAxis.press(currentTime, y);
	}
	
	public final void move(float currentTime, float x, float y) {
		xAxis.move(currentTime, x);
		yAxis.move(currentTime, y);
	}
	
	public final void release() {
		xAxis.release();
		yAxis.release();
	}
	
	public boolean getTriggered(float time) {
		return xAxis.getTriggered(time) || yAxis.getTriggered(time);
	}
	
	public boolean getPressed() {
		return xAxis.isPressed() || yAxis.isPressed();
	}
	
	public final float getDistanceXSq() {
		float x1 = xAxis.getMagnitudeOnPress();
		float x2 = xAxis.getMagnitude();
		
		float vX = x2-x1;
		
		return vX*vX;
	}
	
	public final float getDistanceYSq() {
		float y1 = yAxis.getMagnitudeOnPress();
		float y2 = yAxis.getMagnitude();
		
		float vY = y2-y1;
		
		return vY*vY;
	}
	
	/**
	 * Get distance^2 between origin press and current press
	 * @return distance^2
	 */
	public final float getDistanceSq() {
		float x1 = xAxis.getMagnitudeOnPress();
		float y1 = yAxis.getMagnitudeOnPress();
		
		float x2 = xAxis.getMagnitude();
		float y2 = yAxis.getMagnitude();
		
		float vX = x2-x1;
		float vY = y2-y1;
		
		return vX*vX + vY*vY;
	}
	
	/**
	 * Get distance between origin press and current press
	 * @return distance
	 */
	public final float getDistance() {
		return FloatMath.sqrt(getDistanceSq());
	}
	
	public final void getVector(Vector2 vectorOut) {
		vectorOut.x = xAxis.getMagnitude();
		vectorOut.y = yAxis.getMagnitude();
	}
	
	public final void getVectorOnPress(Vector2 vectorOut) {
		vectorOut.x = xAxis.getMagnitudeOnPress();
		vectorOut.y = yAxis.getMagnitudeOnPress();
	}
	
	public final float getX() {
		return xAxis.getMagnitude();
	}
	
	public final float getY() {
		return yAxis.getMagnitude();
	}
	
	public final float getXOnPress() {
		return xAxis.getMagnitudeOnPress();
	}
	
	public final float getYOnPress() {
		return yAxis.getMagnitudeOnPress();
	}
	
	public final float getLastPressedTime() {
		return Math.max(xAxis.getLastPressedTime(), yAxis.getLastPressedTime());
	}
	
	public final void releaseX() {
		xAxis.release();
	}
	
	public final void releaseY() {
		yAxis.release();
	}
	

	public void setMagnitude(float x, float y) {
		xAxis.setMagnitude(x);
		yAxis.setMagnitude(y);
	}
	
	public void reset() {
		xAxis.reset();
		yAxis.reset();
	}
	
	public void clone(InputXY other) {
		if (other.getPressed()) {
			press(other.getLastPressedTime(), other.getX(), other.getY());
		} else {
			release();
		}
	}
}

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

import com.renegadeware.m8.obj.BaseObject;

public class InputButton {
	private boolean isDown;
	private float lastPressedTime;
	private float downTime;
	private float magnitude;
	private float magnitudeOnPress;
	
	public void press(float currentTime, float magnitude) {
		if (!isDown) {
			isDown = true;
			downTime = currentTime;
			magnitudeOnPress = magnitude;
		} 
		this.magnitude = magnitude;
		lastPressedTime = currentTime;
	}
	
	public void move(float currentTime, float magnitude) { 
		this.magnitude = magnitude;
		lastPressedTime = currentTime;
	}
	
	public void release() {
		isDown = false;
	}

	public final boolean isPressed() {
		return isDown;
	}
	
	public final boolean getTriggered(float currentTime) {
		return isDown && currentTime - downTime <= BaseObject.systemRegistry.timeSystem.getFrameDelta() * 2.0f;
	}
	
	public final float getPressedDuration(float currentTime) {
		return currentTime - downTime;
	}
	
	public final float getLastPressedTime() {
		return lastPressedTime;
	}
	
	public final float getMagnitude() {
		return this.magnitude;
	}
	
	public final float getMagnitudeOnPress() {
		return magnitudeOnPress;
	}
	
	public final void setMagnitude(float magnitude) {
		this.magnitude = magnitude;
	}
	
	public final void reset() {
		isDown = false;
		magnitude = 0.0f;
		magnitudeOnPress = 0.0f;
		lastPressedTime = 0.0f;
		downTime = 0.0f;
	}
}

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

package com.renegadeware.m8;

import com.renegadeware.m8.math.Interpolate;
import com.renegadeware.m8.obj.BaseObject;

/**
 * Maintains a canonical time step, in seconds, for the entire game engine.  This time step
 * represents real changes in time but is only updated once per frame.
 */
// TODO: time distortion effects could go here, or they could go into a special object manager.
public class TimeSystem extends BaseObject {
    private float gameTime;
    private float realTime;
    private float freezeDelay;
    private float gameFrameDelta;
    private float realFrameDelta;
    
    private float targetScale;
    private float scaleDuration;
    private float scaleStartTime;
    private boolean easeScale;
    
    private static final float EASE_DURATION = 0.5f;
    
    public TimeSystem() {
    	super();
    	reset();
    }
            
    @Override
    public void reset() {
        gameTime = 0.0f; 
        realTime = 0.0f;
        freezeDelay = 0.0f;
        gameFrameDelta = 0.0f;
        realFrameDelta = 0.0f;
        
        targetScale = 1.0f;
        scaleDuration = 0.0f;
        scaleStartTime = 0.0f;
        easeScale = false;
    }

    @Override
    public void update(float timeDelta, BaseObject parent) {
    	realTime += timeDelta;
    	realFrameDelta = timeDelta;
    	
        if (freezeDelay > 0.0f) {
            freezeDelay -= timeDelta;
            gameFrameDelta = 0.0f;
        } else {
        	float scale = 1.0f;
        	if (scaleStartTime > 0.0f) {
        		final float scaleTime = realTime - scaleStartTime;
        		if (scaleTime > scaleDuration) {
        			scaleStartTime = 0;
        		} else {
        			if (easeScale) {
        				if (scaleTime <= EASE_DURATION) {
        					// ease in
        					scale = Interpolate.ease(1.0f, targetScale, EASE_DURATION, scaleTime);
        				} else if (scaleDuration - scaleTime < EASE_DURATION) {
        					// ease out
        					final float easeOutTime = EASE_DURATION - (scaleDuration - scaleTime);
        					scale = Interpolate.ease(targetScale, 1.0f, EASE_DURATION, easeOutTime);
        				} else {
        					scale = targetScale;
        				}
        			} else {
        				scale = targetScale;
        			}
        		}
            }
        	 
            gameTime += (timeDelta * scale);
            gameFrameDelta = (timeDelta * scale);
        }
        
       
    }

    public float getGameTime() {
        return gameTime;
    }
    
    public float getRealTime() {
        return realTime;
    }
    
    public float getFrameDelta() {
        return gameFrameDelta;
    }
    
    public float getRealTimeFrameDelta() {
        return realFrameDelta;
    }
    
    public void freeze(float seconds) {
        freezeDelay = seconds;
    }
    
    public void appyScale(float scaleFactor, float duration, boolean ease) {
    	targetScale = scaleFactor;
    	scaleDuration = duration;
    	easeScale = ease;
    	if (scaleStartTime <= 0.0f) {
    		scaleStartTime = realTime;
    	}
    }
}

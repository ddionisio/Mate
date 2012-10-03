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

package com.renegadeware.m8.math;

public final class Interpolate {
	
	public static float lerp(float start, float target, float t) {
		return start + ((target-start) * t);
	}
	
	public static float easeIn(float start, float target, float t) {
		return start + ((target-start)*t*t);
	}
	
	public static float easeOut(float start, float target, float t) {
		return start - ((target-start)*t*(t-2));
	}
	
	public static float easeInOut(float start, float target, float t, float strength) {
		if(strength > 0.0f) {
			float a = strength;
			float b = 1.0f - strength;
			return start + ((target-start)*(a*t*t + b*t));
		} else {
			float a = strength;
			float b = 1.0f + strength;
			return start + ((target-start)*(a*t*(t-2) + b*t));
		}
	}
	
	public static float easeSmooth(float start, float target, float between) {
		float t = (between - start) / (target - start);
		return t*t*(3.0f - 2.0f*t);
	}
	
	public static float damp(float t) {
		float x = 1 - (1 - t);
		return x*x*x;
	}
	
	public static float easeExpo(float start, float target, float t, float damp) {
		return target + (target-start)*(float)java.lang.Math.pow(damp, t);
	}
	
	public static float easeCubic(float start, float target, float t) {
		float x = 3*t*t - 2*t*t*t;
		return start + ((target-start) * x);
	}
    
    public static float lerp(float start, float target, float duration, float timeSinceStart)
    {
        float value = start;
        if (timeSinceStart > 0.0f && timeSinceStart < duration)
        {
            final float range = target - start;
            final float percent = timeSinceStart / duration;
            value = start + (range * percent);
        }
        else if (timeSinceStart >= duration)
        {
            value = target;
        }
        return value;
    }

    public static float ease(float start, float target, float duration, float timeSinceStart)
    {
        float value = start;
        if (timeSinceStart > 0.0f && timeSinceStart < duration)
        {
            final float range = target - start;
            final float percent = timeSinceStart / (duration / 2.0f);
            if (percent < 1.0f)
            {
                value = start + ((range / 2.0f) * percent * percent * percent);
            }
            else
            {
                final float shiftedPercent = percent - 2.0f;
                value = start + ((range / 2.0f) * 
                        ((shiftedPercent * shiftedPercent * shiftedPercent) + 2.0f));
            }
        }
        else if (timeSinceStart >= duration)
        {
            value = target;
        }
        return value;
    }
}

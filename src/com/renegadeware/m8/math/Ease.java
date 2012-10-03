package com.renegadeware.m8.math;

public final class Ease {

	public final static float in(float t, float tMax, float start, float delta) {
		return start + delta*_in(t/tMax);
	}
	
	private final static float _in(float r) {
		return r*r*r;
	}

	public final static float out(float t, float tMax, float start, float delta) {		
		return start + delta*_out(t/tMax);
	}
	
	private final static float _out(float r) {
		float ir = r - 1.0f;
		return ir*ir*ir + 1.0f;
	}
	
	public final static float inOut(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_in(r*2.0f);
		}
		else {
			e = 0.5f*_out((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float outIn(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_out(r*2.0f);
		}
		else {
			e = 0.5f*_in((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	private final static double _s = 1.70158; 
	
	public final static float inBack(float t, float tMax, float start, float delta) {
		return start + delta*_inBack(t/tMax);
	}
	
	private final static float _inBack(float r) {
		return (float)(java.lang.Math.pow(r, 2.0) * ((_s + 1.0) * r - _s));
	}
	
	public final static float outBack(float t, float tMax, float start, float delta) {		
		return start + delta*_outBack(t/tMax);
	}
	
	private final static float _outBack(float r) {
		double ir = r - 1.0;
		
		return (float)(java.lang.Math.pow(ir, 2.0) * ((_s + 1.0) * ir - _s) + 1.0);
	}
	
	public final static float inOutBack(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_inBack(r*2.0f);
		}
		else {
			e = 0.5f*_outBack((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float outInBack(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_outBack(r*2.0f);
		}
		else {
			e = 0.5f*_inBack((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float inElastic(float t, float tMax, float start, float delta) {
		return start + delta*_inElastic(t/tMax);
	}
	
	private final static double __p = 0.3;
	private final static double __s = __p/4.0; 
	
	private final static float _inElastic(float r) {
		if(r == 0.0f || r == 1.0f) {
			return r;
		}
		
		double ir = r - 1.0;
		
		return (float)(-1 * java.lang.Math.pow(2.0, 10.0*ir) * java.lang.Math.sin((ir-__s)*2*java.lang.Math.PI/__p));
	}
	
	public final static float outElastic(float t, float tMax, float start, float delta) {
		return start + delta*_outElastic(t/tMax);
	}
	
	private final static float _outElastic(float r) {
		if(r == 0.0f || r == 1.0f) {
			return r;
		}
		
		return (float)(-1 * java.lang.Math.pow(2.0, -10.0*r) * java.lang.Math.sin((r-__s)*2*java.lang.Math.PI/__p)) + 1.0f;
	}
	
	public final static float inOutElastic(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_inElastic(r*2.0f);
		}
		else {
			e = 0.5f*_outElastic((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float outInElastic(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_outElastic(r*2.0f);
		}
		else {
			e = 0.5f*_inElastic((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float inBounce(float t, float tMax, float start, float delta) {
		return start + delta*_inBounce(t/tMax);
	}
	
	private final static float _inBounce(float r) {
		return 1.0f - _outBounce(1.0f - r);
	}
	
	public final static float outBounce(float t, float tMax, float start, float delta) {
		return start + delta*_outBounce(t/tMax);
	}
	
	private final static double ___s = 7.5625;
	private final static double ___p = 2.75;
	
	private final static float _outBounce(float r) {
		float l;
		
		if(r < 1.0f/___p) {
			l = (float)(___s * java.lang.Math.pow(r, 2.0));
		}
		else {
			if(r < 2.0f/___p) {
				r -= 1.5f/___p;
				l = (float)(___s * java.lang.Math.pow(r, 2.0) + 0.75);
			}
			else {
				if(r < 2.5f/___p) {
					r -= 2.25f/___p;
					l = (float)(___s * java.lang.Math.pow(r, 2.0) + 0.9375);
				}
				else {
					r -= 2.65f/___p;
					l = (float)(___s * java.lang.Math.pow(r, 2.0) + 0.984375);
				}
			}
		}
		
		return l;
	}
	
	public final static float inOutBounce(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_inBounce(r*2.0f);
		}
		else {
			e = 0.5f*_outBounce((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
	
	public final static float outInBounce(float t, float tMax, float start, float delta) {
		float r = t/tMax;
		
		float e;
		if(r < 0.5f) {
			e = 0.5f*_outBounce(r*2.0f);
		}
		else {
			e = 0.5f*_inBounce((r-0.5f)*2.0f) + 0.5f;
		}
		
		return start + delta*e;
	}
}

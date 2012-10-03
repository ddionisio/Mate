package com.renegadeware.m8.math;

public class CurveKey {
	public enum Continuity {
		Smooth,
		Step
	}
	
	public float position;
	public float value;
	public float tangentIn;
	public float tangentOut;
	public Continuity continuity;
	
	public CurveKey() {
		continuity = Continuity.Smooth;
	}
}

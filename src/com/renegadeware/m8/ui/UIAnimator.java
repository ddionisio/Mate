package com.renegadeware.m8.ui;

public interface UIAnimator {
	public void start();
	public void update(float timeDelta, BaseUI.UIDrawable drawable, int renderOrder, float x, float y);
	public boolean isDone();
}

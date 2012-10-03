package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableRect;

public class RectUI extends BaseUI {
	
	private Color color;
	
	protected final DrawableRect rectDrawable;

	public RectUI() {
		super();
		
		rectDrawable = new DrawableRect();
	}
	
	public void setColor(Color c) {
		rectDrawable.color.set(c);
	}
	
	@Override
	public void load() {
		if(color != null) {
			rectDrawable.color.set(color);
			setAlpha(color.alpha);
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		rectDrawable.color.alpha = alpha;
		rectDrawable.draw(x, y, getWidth()*scaleX, getHeight()*scaleY, rotate, screenScaleX, screenScaleY);
	}
}

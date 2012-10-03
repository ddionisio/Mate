package com.renegadeware.m8.ui;

public class TextFixedSizeUI extends TextUI {
	protected int textAnchorH; //based on baseui anchor
	protected int textAnchorV; //based on baseui anchor
	
	private float textScale;
	
	public TextFixedSizeUI() {
		super();
	}

	@Override
	public void setText(String text) {
		textDrawable.setText(text);
		
		if(textAnchorH == ANCHOR_STRETCH || textAnchorV == ANCHOR_STRETCH) {
			adjustTextScale();
		}
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		if(textAnchorH == ANCHOR_STRETCH || textAnchorV == ANCHOR_STRETCH) {
			adjustTextScale();
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		textDrawable.color.alpha = alpha;
		
		float _x = x, _y = y;
		
		if(textAnchorH == ANCHOR_STRETCH || textAnchorV == ANCHOR_STRETCH) {
			_x += (getWidth()*0.5f - textDrawable.getWidth()*textScale*0.5f)*scaleX;
			_y += (getHeight()*0.5f - textDrawable.getHeight()*textScale*0.5f)*scaleY;
			
			textDrawable.draw(_x, _y, scaleX*textScale, scaleY*textScale, rotate, screenScaleX, screenScaleY);
		}
		else {
			switch(textAnchorH) {
			case ANCHOR_RIGHT:
				_x += (getWidth() - textDrawable.getWidth())*scaleX;
				break;
			case ANCHOR_CENTER:
				_x += (getWidth()*0.5f - textDrawable.getWidth()*0.5f)*scaleX;
				break;
			}
			
			switch(textAnchorV) {
			case ANCHOR_TOP:
				_y += (getHeight() - textDrawable.getHeight())*scaleY;
				break;
			case ANCHOR_CENTER:
				_y += (getHeight()*0.5f - textDrawable.getHeight()*0.5f)*scaleY;
				break;
			}
			
			textDrawable.draw(_x, _y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		}
	}
	
	protected void adjustTextScale() {
		final float w = getWidth();
		final float h = getHeight();
		
		if(textDrawable.getWidth() > w) {
			textScale = w/textDrawable.getWidth();
		}
		else if(textDrawable.getHeight() > h) {
			textScale = h/textDrawable.getHeight();
		}
		else {
			textScale = 1.0f;
		}
	}
}

package com.renegadeware.m8.ui;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableTextLine;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.input.InputXY;

public class BaseButtonUI extends BaseUI {
	protected final static int STATE_INVALID = -1;
	protected final static int STATE_UP = 0;
	protected final static int STATE_DOWN = 1;
	protected final static int STATE_DISABLED = 2;
	
	protected final static int STATE_NUM = 3;
	
	protected String fontId;
	protected float fontPtSize;
	protected Color fontColor;
	protected Color fontColorDisabled;
	
	protected float textPaddingX;
	protected float textPaddingY;
	
	protected String stringId;
	
	//internal data
	protected final DrawableTextLine textDrawable;
	
	protected float textScale;
	
	protected int state;

	public BaseButtonUI() {
		super();
		
		textDrawable = new DrawableTextLine();
		textDrawable.setAlign(DrawableTextLine.ALIGN_CENTER);
		textDrawable.setAlignHeight(DrawableTextLine.ALIGN_CENTER);
		
		inputEnabled = true;
		state = STATE_UP;
	}

	public void setText(String txt) {
		textDrawable.setText(txt);
		
		//set the scale
		float w = getWidth(), h = getHeight();
		if(w == 0 || h == 0) {
			if(w == 0) {
				w = textDrawable.getWidth();
			}
			if(h == 0) {
				h = textDrawable.getHeight();
			}
			
			resize(w, h);
		}
		else {
			adjustTextScale();
		}
	}
	
	public void setDefaultText() {
		if(stringId != null) {
			ContextParameters cp = systemRegistry.contextParameters;
			int id = cp.getResourceId(stringId, null);
			if(id != 0) {
				String str = cp.context.getResources().getString(id);
				setText(str);
			}
		}
	}
	
	@Override
	public void enableInput(boolean enable) {
		super.enableInput(enable);
		if(enable) {
			setState(STATE_UP);
		}
		else {
			setState(STATE_DISABLED);
		}
	}
	
	@Override
	public void load() {
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				textDrawable.setFont(font);
				
				if(fontPtSize > 0.0f) {
					textDrawable.setPointSize(fontPtSize);
				}
				
				if(stringId != null) {
					ContextParameters cp = systemRegistry.contextParameters;
					int id = cp.getResourceId(stringId, null);
					if(id != 0) {
						String str = cp.context.getResources().getString(id);
						textDrawable.setText(str);
					}
					
					//set the scale
					float w = getWidth(), h = getHeight();
					if(w == 0) {
						w = textDrawable.getWidth();
					}
					if(h == 0) {
						h = textDrawable.getHeight();
					}
					
					resize(w, h);
				}
			}
		}
		
		setState(inputEnabled ? state : STATE_DISABLED);
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		adjustTextScale();
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {		
		if(!textDrawable.isEmpty()) {
			textDrawable.color.alpha = alpha;
			textDrawable.draw(x+getWidth()*0.5f*scaleX, y+getHeight()*0.5f*scaleY, scaleX*textScale, scaleY*textScale, rotate, screenScaleX, screenScaleY);
		}
	}

	@Override
	protected void inputTouchPressed(InputXY input) {
		setState(STATE_DOWN);
	}
	
	@Override
	protected void inputTouchReleased(InputXY input) {
		setState(STATE_UP);
	}
	
	@Override
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		if(isInRegion(input.getX(), input.getY(), true)) {
			return true;
		}
		
		setState(STATE_UP);
		
		return false;
	}
	
	protected void setState(int newState) {
		state = newState;
		
		Color fntClr = fontColor;
		
		switch(state) {
		case STATE_DISABLED:
			fntClr = fontColorDisabled;
			break;
		}
		
		//set the font color
		if(textDrawable.getFont() != null) {
			textDrawable.color.set(fntClr != null ? fntClr : Color.WHITE);
		}
	}
	
	/**
	 * Adjust the scale of the string to fit inside the frame
	 */
	protected void adjustTextScale() {
		final float w = getWidth() - textPaddingX*2;
		final float h = getHeight() - textPaddingY*2;
		
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

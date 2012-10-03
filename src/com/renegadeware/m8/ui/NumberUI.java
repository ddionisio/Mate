package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableNumber;
import com.renegadeware.m8.gfx.Font;

public class NumberUI extends BaseUI {
	
	protected String fontId;
	protected float fontPtSize;
	protected int maxDigit;
	protected int number;
	protected char appendChar;
	protected Color color; 
	
	protected DrawableNumber numDrawable;

	public NumberUI() {
		maxDigit = 3;
	}
	
	public void setColor(Color c) {
		if(c == null) {
			if(color != null) {
				numDrawable.color.set(color);
				setAlpha(color.alpha);
			}
		}
		else {
			numDrawable.color.set(c);
			setAlpha(c.alpha);
		}
	}
	
	public void setAppendChar(char c) {
		appendChar = c;
	}
	
	public void setNumber(int num) {
		if(numDrawable == null || numDrawable.getFont() == null) {
			number = num;
		}
		else {
			numDrawable.setNumber(num, appendChar);
			
			resize(numDrawable.getWidth(), numDrawable.getHeight());
		}
	}
	
	public int getNumber() {
		if(numDrawable != null) {
			return numDrawable.getNumber();
		}
		
		return 0;
	}

	@Override
	public void load() {
		numDrawable = new DrawableNumber(maxDigit);
		
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				numDrawable.setFont(font);
				
				setColor(null);
				
				if(fontPtSize > 0.0f) {
					numDrawable.setPointSize(fontPtSize);
				}
			}
		}
		
		setNumber(number);
	}
		
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		numDrawable.color.alpha = alpha;
		numDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}

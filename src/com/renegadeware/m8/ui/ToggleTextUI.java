package com.renegadeware.m8.ui;

import com.renegadeware.m8.ContextParameters;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableTextLine;
import com.renegadeware.m8.gfx.Font;

public class ToggleTextUI extends ToggleUI {
	protected String fontId;
	protected float fontPtSize;
	protected String stringOnId;
	protected String stringOffId;
	protected Color color; 
	protected char preChar;
	protected char postChar;
	
	protected int stringResOnId;
	protected int stringResOffId;
	protected StringBuffer buffer;
	protected final DrawableTextLine textDrawable;

	public ToggleTextUI() {
		super();
		
		textDrawable = new DrawableTextLine();
	}
	
	@Override
	public void load() {
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				textDrawable.setFont(font);
				
				textDrawable.color.set(color);
				setAlpha(textDrawable.color.alpha);
								
				if(fontPtSize > 0.0f) {
					textDrawable.setPointSize(fontPtSize);
				}
			}
		}
		
		final ContextParameters cp = systemRegistry.contextParameters;
		
		int bufLen = 0;
		
		if(preChar != '\0') {
			bufLen++;
		}
		
		if(postChar != '\0') {
			bufLen++;
		}
		
		String str;
		int maxStrLen = 0;
		
		if(stringOnId != null) {
			stringResOnId = cp.getResourceId(stringOnId, null);

			str = cp.context.getResources().getString(stringResOnId);
			if(str != null) {
				maxStrLen = str.length();
			}
		}
		
		if(stringOffId != null) {
			stringResOffId = cp.getResourceId(stringOffId, null);
			str = cp.context.getResources().getString(stringResOffId);
			if(str != null && maxStrLen < str.length()) {
				maxStrLen = str.length();
			}
		}
		
		bufLen += maxStrLen;
		
		buffer = new StringBuffer(bufLen);
		
		onToggleChange();
		
		//resize(textDrawable.getWidth(), textDrawable.getHeight());
	}
	
	@Override
	public void unload() {
		buffer = null;
	}
		
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		textDrawable.color.alpha = alpha;
		textDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}

	@Override
	protected void onToggleChange() {
		buffer.delete(0, buffer.length());
		
		String str;
		
		final ContextParameters cp = systemRegistry.contextParameters;
		
		if(toggle) {
			str = cp.context.getResources().getString(stringResOnId);
		}
		else {
			str = cp.context.getResources().getString(stringResOffId);
		}
		
		if(preChar != '\0') {
			buffer.append(preChar);
		}
		
		if(str != null) {
			buffer.append(str);
		}
		
		if(postChar != '\0') {
			buffer.append(postChar);
		}
		
		textDrawable.setText(buffer.toString());
						
		resize(textDrawable.getWidth(), textDrawable.getHeight());
	}

}

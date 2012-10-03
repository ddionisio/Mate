package com.renegadeware.m8.ui;

import java.util.ArrayList;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableTextLine;
import com.renegadeware.m8.gfx.Font;

public class TextMultiLine extends BaseUI {
	public final static int ALIGN_LEFT = 0;
	public final static int ALIGN_RIGHT = 1;
	public final static int ALIGN_CENTER = 2;
	
	protected String fontId;
	protected float fontPtSize;
	protected String stringId;
	protected Color color;
	protected int align;
	
	protected final DrawableTextLine textDrawable;
	
	protected ArrayList<String> texts;

	public TextMultiLine() {
		textDrawable = new DrawableTextLine();
	}

	public void setColor(Color c) {
		if(c == null) {
			if(color != null) {
				textDrawable.color.set(color);
				setAlpha(color.alpha);
			}
		}
		else {
			textDrawable.color.set(c);
			setAlpha(c.alpha);
		}
	}
	
	public void setAlign(int align) {
		this.align = align;
	}
	
	public ArrayList<String> generateTextLines(String text) {
		ArrayList<String> lines = null;
		
		Font fnt = textDrawable.getFont();
		if(fnt != null) {
			lines = fnt.splitText(text, getWidth(), fontPtSize);
		}
		
		return lines;
	}
	
	public ArrayList<String> generateTextLinesById(int id) {
		return generateTextLines(systemRegistry.contextParameters.context.getResources().getString(id));
	}
			
	public void setText(String text) {
		texts = generateTextLines(text);
		
		resize(getWidth(), _getTextLinesHeight());
	}
	
	public void setText(ArrayList<String> texts) {
		this.texts = texts;
		
		resize(getWidth(), _getTextLinesHeight());
	}
	
	public void setTextById(int id) {
		if(id != 0) {
			setText(systemRegistry.contextParameters.context.getResources().getString(id));
		}
	}
	
	public void setTextById(String id) {
		setTextById(systemRegistry.contextParameters.getResourceId(id, null));
	}
	
	@Override
	public void load() {
		if(fontId != null) {
			Font font = (Font)systemRegistry.fontManager.getByName(fontId);
			if(font != null) {
				textDrawable.setFont(font);
				
				setColor(null);
								
				if(fontPtSize > 0.0f) {
					textDrawable.setPointSize(fontPtSize);
				}
			}
		}
		
		if(stringId != null) {
			setTextById(stringId);
		}
	}
	
	@Override
	public void unload() {
		texts = null;
	}
	
	private float _getTextLinesHeight() {
		float h = 0;
		
		if(texts != null) {
			for(int i = 0; i < texts.size(); i++) {
				h += textDrawable.getHeight();
			}
		}
		
		return h;
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		if(texts == null) {
			return;
		}
		
		textDrawable.color.alpha = alpha;
		
		//start from top
		float w = getWidth();
		float _y = y + getHeight();
		float fntH = textDrawable.getHeight();
		
		for(String text : texts) {
			_y -= fntH;
			
			textDrawable.setText(text);
			
			float txtW = textDrawable.getWidth();
			
			switch(align) {
			case ALIGN_LEFT:
				textDrawable.draw(x, _y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				break;
				
			case ALIGN_RIGHT:
				textDrawable.draw(x + (w - txtW)*scaleX, _y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				break;
				
			case ALIGN_CENTER:
				textDrawable.draw(x + (w*0.5f - txtW*0.5f)*scaleX, _y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				break;
			}
		}
	}
	
}

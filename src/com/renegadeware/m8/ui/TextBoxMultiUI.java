package com.renegadeware.m8.ui;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableGrid;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.GridText;
import com.renegadeware.m8.res.ResourceGroupManager;

public class TextBoxMultiUI extends BaseUI {

	protected String fontId;
	protected int alignment;
	protected float fontPtSize;
	protected Color color;
	
	protected String[] stringIds;
	
	protected Object[] stringParams;
			
	//internal
	protected int curPage;
	
	protected DrawableGrid[] textDrawables;

	public TextBoxMultiUI() {
		super();
	}
	
	/**
	 * Call this before load to specify parameters in the text
	 * @param params
	 */
	public void setTextParams(Object[] params) {
		stringParams = params;
	}
	
	public void setCurPage(int page) {
		curPage = page;
		
		GridText gridText = (GridText)textDrawables[page].grid;
		
		resize(gridText.getWidth(), gridText.getHeight());
	}
	
	@Override
	public void unload() {
		for(DrawableGrid textDrawable : textDrawables) {
			if(textDrawable.grid != null) {
				textDrawable.grid.unload();
				systemRegistry.gridManager.remove(textDrawable.grid);
				textDrawable.grid = null;
			}
		}
		
		textDrawables = null;
	}
	
	@Override
	public void load() {
		
		Font fnt = (Font) systemRegistry.fontManager.getByName(fontId);
		
		GridManager gm = systemRegistry.gridManager;
		
		if(stringIds != null) {
			textDrawables = new DrawableGrid[stringIds.length];
			
			for(int i = 0; i < stringIds.length; i++) {
				GridText gridText = gm.createText(stringIds[i], 
						ResourceGroupManager.InternalResourceGroupName, 
						fnt.id, 
						fontPtSize, getWidth(), alignment, stringParams);
				
				try {
					gridText.load(false);
				} catch(Exception e) {
					DebugLog.e("UI", e.toString(), e);
				}
				
				if(gridText.isLoaded()) {
					DrawableGrid textDrawable = new DrawableGrid();
					
					textDrawable.useColor = true;
					textDrawable.grid = gridText;
					textDrawable.texture = fnt.getTexture(0);
					
					if(color != null) {
						textDrawable.color.set(color);
					}
					
					textDrawables[i] = textDrawable;
				}
			}
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		DrawableGrid textDrawable = textDrawables[curPage];
		if(textDrawable != null) {
			textDrawable.color.alpha = alpha;
			textDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		}
	}

}

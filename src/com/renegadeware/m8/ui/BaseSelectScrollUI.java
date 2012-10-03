package com.renegadeware.m8.ui;

import javax.microedition.khronos.opengles.GL10;

import android.util.FloatMath;

import com.renegadeware.m8.gfx.OpenGLSystem;
import com.renegadeware.m8.input.InputXY;
import com.renegadeware.m8.math.Ease;

public abstract class BaseSelectScrollUI extends BaseUI {
	
	public static interface ScrollItemCallback {
		public boolean onScrollItemIsValid(BaseSelectScrollUI scroller, int index);
		
		public void onScrollItemHighlight(BaseSelectScrollUI scroller, int index);
		
		public void onScrollItemSelected(BaseSelectScrollUI scroller, int index);
	}
	
	protected static final float DRAG_MAX = 12;
	
	protected BaseUI[] items;
	
	protected float itemWidth;
	protected float itemHeight;
	
	protected boolean itemWidthStretch; //if true, item width is resized to width
	protected boolean itemHeightStretch; //if true, item height is resized to height
	
	//TODO: this is pretty hacky...
	protected boolean itemLockScrollEnd; //if true, last item is locked at the end of the list when scrolling
	
	protected float itemPaddingX;
	protected float itemPaddingY;
	
	protected float moveDelay;
	
	protected float unselectAlpha;
	protected float unselectScale;
	
	protected int selectorPositionType; //0 = center, 1 = left/top
	protected float scalerSize; //if 0, uses itemWidth
	
	protected int curIndex;
	
	//internal	
	protected ScrollItemCallback callback;
	
	protected float itemsSize;
	
	protected float curMoveDelay;
	
	protected float curOfs;
	
	protected float startOfs;
	protected float endOfs;
	protected float lastOfs;

	public BaseSelectScrollUI() {
		super();
		
		inputEnabled = true;
		unselectScale = 1.0f;
		unselectAlpha = 1.0f;
	}
	
	public float getItemWidth() {
		return itemWidth;
	}
	
	public float getItemHeight() {
		return itemHeight;
	}
		
	public void setCallback(ScrollItemCallback callee) {
		callback = callee;
	}
	
	public BaseUI[] getItems() {
		return items;
	}
	
	public BaseUI getItem(int ind) {
		return items[ind];
	}
	
	public int getCurItemInd() {
		return curIndex;
	}
	
	public int getNumItems() {
		return items.length;
	}
	
	public BaseUI getCurItem() {
		return items[curIndex];
	}
	
	public void setSelectedItem(int ind, boolean doMove) {
		if(ind >= 0 && ind < items.length) {
			if(doMove) {
				startOfs = getCurOfs(curIndex);
				endOfs = getCurOfs(ind);
				curMoveDelay = 0;
			}
			else {
				curOfs = getCurOfs(ind);
				curMoveDelay = moveDelay;
			}
			
			curIndex = ind;
			
			//let it be known this index is now selected
			if(callback != null) {
				callback.onScrollItemSelected(this, index);
			}
		}
	}
	
	private void _positionItem(BaseUI item) {
		float w = item.getWidth();
		float h = item.getHeight();
		
		//modify size if anchor is stretch
		if(item.anchorH == ANCHOR_STRETCH) {
			w = itemWidth - itemPaddingX*2;
		}
		
		if(item.anchorV == ANCHOR_STRETCH) {
			h = itemHeight - itemPaddingY*2;
		}
						
		item.resize(w, h);
	}

	@Override
	public void load() {
		//load the items
		boolean doArrange = itemWidth > 0 && itemHeight > 0;
		
		for(int i = 0; i < items.length; i++) {
			BaseUI.LoadUI(items[i]);
			
			items[i].index = i;
			
			if(doArrange) {
				_positionItem(items[i]);
			}
		}
				
		if(doArrange) {
			arrangeItems();
			
			setSelectedItem(curIndex, false);
		}
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		boolean doRearrange = false;
		
		if(itemWidthStretch) {
			itemWidth = w;
			doRearrange = true;
		}
		
		if(itemHeightStretch) {
			itemHeight = h;
			doRearrange = true;
		}
		
		if(doRearrange) {
			for(BaseUI item : items) {
				_positionItem(item);
			}
			
			arrangeItems();
			
			setSelectedItem(curIndex, false);
		}
	}
	
	@Override
	public void unload() {
		for(BaseUI item : items) {
			item.reset();
		}
		
		callback = null;
	}
		
	@Override
	public void update(float timeDelta) {		
		//update items
		for(BaseUI item : items) {
			item._update(timeDelta);
		}
		
		//update move
		if(curMoveDelay < moveDelay) {
			curMoveDelay += timeDelta;
			if(curMoveDelay >= moveDelay) {
				curOfs = endOfs;
			}
			else {
				curOfs = Ease.out(curMoveDelay, moveDelay, startOfs, endOfs-startOfs);
			}
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		
		final float w = getWidth(), h = getHeight();
		
		final GL10 gl = OpenGLSystem.getGL();
		gl.glEnable(GL10.GL_SCISSOR_TEST);
		gl.glScissor(
				(int)(x*screenScaleX), 
				(int)(y*screenScaleY), 
				(int)(w*scaleX*screenScaleX), 
				(int)(h*scaleY*screenScaleY));
		
		renderItems(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
		
		gl.glDisable(GL10.GL_SCISSOR_TEST);
	}
	
	@Override
	protected void inputTouchPressed(InputXY input) {
		lastOfs = curOfs;
	}
	
	@Override
	protected void inputTouchReleased(InputXY input) {
		startOfs = curOfs;
		
		int ind = getCurOfsIndex();
		
		if(ind >= 0 && ind < items.length) {
			if(callback != null) {
				//find the valid index
				for(; ind > 0; ind--) {
					if(callback.onScrollItemIsValid(this, ind)) {
						break;
					}
				}
			}
			
			curIndex = ind;
			endOfs = getCurOfs(ind);
			
			if(callback != null) {
				callback.onScrollItemSelected(this, ind);
			}
		}
		else { //invalid index, go to the previous
			endOfs = lastOfs;
		}
		
		curMoveDelay = 0;
	}
	
	@Override
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		curOfs = getNewOfs(input);
		
		//report highlighted item
		if(callback != null) {
			int index = getCurOfsIndex();
			if(index >= 0 && index < items.length) {
				callback.onScrollItemHighlight(this, getCurOfsIndex());
			}
		}
		
		return true;
	}
	
	protected abstract int getCurOfsIndex();
	protected abstract void arrangeItems();
	protected abstract float getCurOfs(int ind);
	protected abstract float getNewOfs(InputXY input);
	protected abstract void renderItems(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha);
}

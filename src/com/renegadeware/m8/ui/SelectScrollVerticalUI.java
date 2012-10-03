package com.renegadeware.m8.ui;

import com.renegadeware.m8.input.InputXY;

public class SelectScrollVerticalUI extends BaseSelectScrollUI {
	
	@Override
	protected int getCurOfsIndex() {
		return (int)((curOfs+itemHeight*0.5f)/itemHeight);
	}

	@Override
	protected void arrangeItems() {
		float _y = 0;
		
		for(BaseUI item : items) {
			item.y = _y;
			
			_y -= itemHeight;
		}
		
		itemsSize = itemHeight*items.length - itemHeight;
		
		if(scalerSize <= 0) {
			scalerSize = itemHeight;
		}
	}

	@Override
	protected float getCurOfs(int ind) {
		return ind*itemHeight;
	}
	
	@Override
	protected float getNewOfs(InputXY input) {
		//return input.getX() - input.getXOnPress();
		float delta = input.getY() - input.getYOnPress();
		
		float newOfs = lastOfs + delta;
		
		float endCap = itemsSize+DRAG_MAX;
		if(itemLockScrollEnd) {
			endCap -= getHeight() - itemHeight;
		}
		
		//cap
		if(newOfs < -DRAG_MAX) {
			newOfs = -DRAG_MAX;
		}
		else if(newOfs > endCap) {
			newOfs = endCap;
		}
		
		return newOfs;
	}

	@Override
	protected void renderItems(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		final float w = getWidth();
		final float h = getHeight();
		
		float midY = selectorPositionType == 0 ? h*0.5f : h - itemHeight*0.5f;
		
		for(int i = 0; i < items.length; i++) {
			BaseUI item = items[i];
			
			final float itmY = midY + item.y + curOfs;
			final float itmHalfH = item.getHeight()*0.5f;
			
			if(itmY-itmHalfH > h) {
				continue;
			}
			else if(itmY+itmHalfH < 0) {
				break;
			}
			
			float delta = item.y + curOfs;
									
			if(delta == 0) {
				item._drawable.setCurAlpha(alpha);
				item._drawable.draw(
						x + (w*0.5f - item.getWidth()*0.5f)*scaleX,
						y + (itmY - itmHalfH)*scaleY, 
						scaleX, scaleY, rotate, screenScaleX, screenScaleY);
			}
			else {
				float t = delta/scalerSize;
				t *= t;
				
				if(t >= 1) {
					item._drawable.setCurAlpha(alpha*unselectAlpha);
					item._drawable.draw(
							x + (w*0.5f - item.getWidth()*0.5f*unselectScale)*scaleX,
							y + (itmY - itmHalfH*unselectScale)*scaleY,  
							scaleX*unselectScale, scaleY*unselectScale, rotate, screenScaleX, screenScaleY);
				}
				else {
					float scale = 1.0f + t*(unselectScale - 1.0f);
					
					item._drawable.setCurAlpha((1.0f + t*(unselectAlpha - 1.0f))*alpha);
					item._drawable.draw(
							x + (w*0.5f - item.getWidth()*0.5f*scale)*scaleX,
							y + (itmY - itmHalfH*scale)*scaleY,
							scaleX*scale, scaleY*scale, rotate, screenScaleX, screenScaleY);
				}
			}
		}
	}
}

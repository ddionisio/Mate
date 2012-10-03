package com.renegadeware.m8.ui;

import com.renegadeware.m8.input.InputXY;

public class SelectScrollHorizontalUI extends BaseSelectScrollUI {
	
	@Override
	protected int getCurOfsIndex() {
		return (int)(((-curOfs)+itemWidth*0.5f)/itemWidth);
	}

	@Override
	protected void arrangeItems() {
		float _x = 0;
		
		for(BaseUI item : items) {
			item.x = _x;
			
			_x += itemWidth;
		}
		
		itemsSize = itemWidth*items.length - itemWidth;
		
		if(scalerSize <= 0) {
			scalerSize = itemWidth;
		}
	}

	@Override
	protected float getCurOfs(int ind) {
		return -ind*itemWidth;
	}
	
	@Override
	protected float getNewOfs(InputXY input) {
		//return input.getX() - input.getXOnPress();
		float delta = input.getX() - input.getXOnPress();
		
		float newOfs = lastOfs + delta;
		
		float endCap = -(itemsSize+DRAG_MAX);
		if(itemLockScrollEnd) {
			endCap += getWidth() - itemWidth;
		}
		
		//cap
		if(newOfs > DRAG_MAX) {
			newOfs = DRAG_MAX;
		}
		else if(newOfs < endCap) {
			newOfs = endCap;
		}
		
		return newOfs;
	}

	@Override
	protected void renderItems(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		final float w = getWidth();
		final float h = getHeight();
		
		float midX = selectorPositionType == 0 ? w*0.5f : itemWidth*0.5f;
		
		for(int i = 0; i < items.length; i++) {
			BaseUI item = items[i];
			
			final float itmX = midX + item.x + curOfs;
			final float itmHalfW = item.getWidth()*0.5f;
			
			if(itmX+itmHalfW < 0) {
				continue;
			}
			else if(itmX-itmHalfW > w) {
				break;
			}
			
			float delta = item.x + curOfs;
									
			if(delta == 0) {
				item._drawable.setCurAlpha(alpha);
				item._drawable.draw(
						x + (itmX - itmHalfW)*scaleX, 
						y + (h*0.5f - item.getHeight()*0.5f)*scaleY, 
						scaleX, scaleY, rotate, screenScaleX, screenScaleY);
			}
			else {
				float t = delta/scalerSize;
				t *= t;
				
				if(t >= 1) {
					item._drawable.setCurAlpha(alpha*unselectAlpha);
					item._drawable.draw(
							x + (itmX - itmHalfW*unselectScale)*scaleX, 
							y + (h*0.5f - item.getHeight()*0.5f*unselectScale)*scaleY, 
							scaleX*unselectScale, scaleY*unselectScale, rotate, screenScaleX, screenScaleY);
				}
				else {
					float scale = 1.0f + t*(unselectScale - 1.0f);
					
					item._drawable.setCurAlpha((1.0f + t*(unselectAlpha - 1.0f))*alpha);
					item._drawable.draw(
							x + (itmX - itmHalfW*scale)*scaleX, 
							y + (h*0.5f - item.getHeight()*0.5f*scale)*scaleY, 
							scaleX*scale, scaleY*scale, rotate, screenScaleX, screenScaleY);
				}
			}
		}
	}
}

package com.renegadeware.m8.ui;

import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.ObjectSprite;

public class SpriteUI extends BaseUI {
	
	protected int spriteAnchorH; //based on UI anchor
	protected int spriteAnchorV; //based on UI anchor
	
	protected final ObjectSprite sprite;
	protected final DrawableBitmap drawable;
	
	protected float spriteOfsX;
	protected float spriteOfsY;

	public SpriteUI() {
		super();
		
		sprite = new ObjectSprite();
		drawable = new DrawableBitmap();
	}
	
	public final ObjectSprite getSprite() {
		return sprite;
	}
	
	@Override
	public void load() {
		resize(getWidth(), getHeight());
	}
	
	@Override
	public void unload() {
		sprite.reset();
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		switch(spriteAnchorH) {
		case ANCHOR_RIGHT:
			spriteOfsX = w;
			break;
		case ANCHOR_CENTER:
			spriteOfsX = w*0.5f;
			break;
		default:
			spriteOfsX = 0;
			break;
		}
		
		switch(spriteAnchorV) {
		case ANCHOR_RIGHT:
			spriteOfsY = h;
			break;
		case ANCHOR_CENTER:
			spriteOfsY = h*0.5f;
			break;
		default:
			spriteOfsY = 0;
			break;
		}
	}
	
	@Override
	protected void update(float timeDelta) {
		sprite.update(timeDelta, null);
		sprite.applyFrame(drawable);
		drawable.anchorX += spriteOfsX;
		drawable.anchorY += spriteOfsY;
	}
	
	@Override
	protected void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		drawable.color.alpha = alpha;
		drawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
	}
}

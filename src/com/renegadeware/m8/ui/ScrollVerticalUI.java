package com.renegadeware.m8.ui;

import javax.microedition.khronos.opengles.GL10;

import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.OpenGLSystem;
import com.renegadeware.m8.gfx.TextureAtlas;
import com.renegadeware.m8.input.InputXY;
import com.renegadeware.m8.util.Util;

public class ScrollVerticalUI extends BaseUI {
	
	static final float DRAG_MAX_HIGHLIGHT = 12;
	
	static final int HIGHLIGHT_POS_TOP = 1;
	static final int HIGHLIGHT_POS_BOTTOM = 2;
	
	/**must start from top transparent to opaque bottom (or black to white)
	   image will be in additive blending mode */
	protected String highlightAtlasId;
	protected String highlightAtlasRef;
	protected Color highlightColor;
	protected float highlightHeight;
	protected float highlightFadeDelay;
	
	protected BaseUI item;
	
	protected float itemPaddingX;
	protected float itemPaddingY;
	
	//internal
	protected float curOfs;
	
	/** HIGHLIGHT_POS_* */
	protected int highlightPos; 
	
	protected float lastOfs;
	
	protected float highlightCurFadeOut;
	protected float highlightAlpha;
	protected float highlightAlphaStart;
	
	protected final DrawableBitmap highlightDrawable;

	public ScrollVerticalUI() {
		super();
		
		highlightDrawable = new DrawableBitmap();
		highlightDrawable.useColor = true;
		highlightDrawable.useBlend = true;
		highlightDrawable.blendSrc = GL10.GL_SRC_ALPHA;
		highlightDrawable.blendDst = GL10.GL_ONE;
		
		inputEnabled = true;
	}
	
	public void resetOffset() {
		curOfs = 0;
		
		highlightCurFadeOut = highlightFadeDelay;
		
		highlightAlpha = 0.0f;
	}
	
	public BaseUI getItem() {
		return item;
	}
	
	private void _adjustHighlightSize(float w, float h) {
		highlightDrawable.width = w;
		highlightDrawable.height = highlightHeight;
	}
	
	@Override
	public void load() {
		//setup highlight
		if(highlightAtlasId != null && highlightAtlasRef != null) {
			TextureAtlas atlas = (TextureAtlas)systemRegistry.textureAtlasManager.getByName(highlightAtlasId);
			if(atlas != null) {
				highlightDrawable.setTextureByAtlas(atlas, highlightAtlasRef);
				
				if(highlightColor != null) {
					highlightDrawable.color.set(highlightColor);
				}
			}
		}
		
		_adjustHighlightSize(getWidth(), getHeight());
		
		//setup item
		if(item != null) {
			BaseUI.LoadUI(item);
			
			//check for stretch anchor, only horizontal, doesn't make sense for vertical
			if(item.anchorH == BaseUI.ANCHOR_STRETCH) {
				float w = getWidth() - itemPaddingX*2.0f;
				
				item.resize(w, item.getHeight());
			}
		}
		
		resetOffset();
	}
	
	@Override
	public void unload() {
		if(item != null) {
			item.reset();
		}
		
		highlightDrawable.texture = null;
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		_adjustHighlightSize(w, h);
	}
	
	@Override
	public void update(float timeDelta) {
		item._update(timeDelta);
		
		if(highlightCurFadeOut < highlightFadeDelay) {
			highlightCurFadeOut += timeDelta;
			if(highlightCurFadeOut > highlightFadeDelay) {
				highlightCurFadeOut = highlightFadeDelay;
			}
			
			highlightAlpha = highlightAlphaStart - (highlightCurFadeOut/highlightFadeDelay)*highlightAlphaStart;
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
		
		item._drawable.setCurAlpha(alpha);
		item._drawable.draw(
				x + (w*0.5f - item.getWidth()*0.5f)*scaleX, 
				y + (h - item.getHeight() - itemPaddingY + curOfs)*scaleY, 
				scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		
		gl.glDisable(GL10.GL_SCISSOR_TEST);
		
		//display highlight
		if(highlightAlpha > 0) {
			float highlightY = 0;
			
			switch(highlightPos) {
			case HIGHLIGHT_POS_TOP:
				if(!highlightDrawable.isFlippedVertical()) {
					highlightDrawable.setFlip(false, true);
				}
				
				highlightY = (h - highlightHeight)*scaleY;
				break;
				
			case HIGHLIGHT_POS_BOTTOM:
				if(highlightDrawable.isFlippedVertical()) {
					highlightDrawable.setFlip(false, true);
				}
				break;
			}
			
			highlightDrawable.color.alpha = highlightAlpha*alpha;
			highlightDrawable.draw(x, y + highlightY, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
		}
	}
	
	@Override
	protected void inputTouchPressed(InputXY input) {
		lastOfs = curOfs;
		highlightCurFadeOut = highlightFadeDelay;
		highlightAlpha = 0.0f;
	}
	
	@Override
	protected void inputTouchReleased(InputXY input) {
		if(highlightAlpha > 0.0f) {
			highlightAlphaStart = highlightAlpha;
			highlightCurFadeOut = 0.0f;
		}
	}
	
	@Override
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		float delta = input.getY() - input.getYOnPress();
		
		curOfs = lastOfs + delta;
		
		if(curOfs < 0) {
			float excessOfs = Util.clamp(curOfs, -DRAG_MAX_HIGHLIGHT, 0.0f);
			highlightAlpha = -excessOfs/DRAG_MAX_HIGHLIGHT;
									
			curOfs = 0;
			
			highlightPos = HIGHLIGHT_POS_TOP;
		}
		else {
			float max = item.getHeight()-getHeight()+itemPaddingY*2;
			float maxExcess = max+DRAG_MAX_HIGHLIGHT*2; 
			if(curOfs > max) {
				float excessOfs = Util.clamp(curOfs, max, maxExcess);
				highlightAlpha = excessOfs/maxExcess;
				
				curOfs = max;
				
				highlightPos = HIGHLIGHT_POS_BOTTOM;
			}
			else {
				highlightAlpha = 0.0f;
			}
		}
		
		return true;
	}
}

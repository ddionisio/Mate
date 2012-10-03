package com.renegadeware.m8.gfx;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.obj.PhasedObject;

/**
 * Note: You'll need to push the drawable manually to render the sprite,
 * ObjectSprite does not store transformation info
 * @author ddionisio
 *
 */
public class ObjectSprite extends PhasedObject {

	protected Sprite sprite;	

	protected Sprite.State curState;
	protected Sprite.Frame curFrame;
	protected int curFrameIndex;
			
	private float frameCount;
	
	private boolean isReverse;
	private boolean isPause;
	private boolean isDone;
	
	private String nextState;

	public ObjectSprite() {
		super();
		
		resetSpriteData();
	}
	
	public ObjectSprite(Sprite sprite, String initialState) {
		super();
		
		assert sprite != null;

		setSprite(sprite, initialState);
	}
	
	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite, String initialState) {
		resetSpriteData();
		
		assert sprite != null;

		this.sprite = sprite;

		if(initialState != null && initialState.length() > 0) {
			setState(initialState);
		}
	}
		
	private void resetSpriteData() {
		sprite = null;

		curState = null;
		curFrame = null;
		curFrameIndex = 0;

		isReverse = false;
		isPause = false;
		isDone = false;

		frameCount = 0.0f;
	}

	@Override
	public void reset() {
		resetSpriteData();
	}

	private void setFrame(int frame) {
		assert curState != null;
		assert frame < curState.frames.length;

		curFrameIndex = frame;

		curFrame = curState.frames[frame];
	}
	
	public final void setNextState(String state) {
		nextState = state;
	}
	
	public final Texture getTexture() {
		return sprite == null ? null : sprite.getTexture();
	}
	
	public final float getSpriteFPS() {
		return sprite == null ? 0.0f : sprite.getFPS();
	}
	
	public final Sprite.Frame getCurrentSpriteFrame() {
		return curFrame;
	}
	
	public final Sprite.State getCurSpriteState() {
		return curState;
	}

	public boolean setState(String state) {
		Sprite.State newState = sprite.getState(state);
		if(newState != null) {
			curState = newState;
			setFrame(0);
			isReverse = false;
			isDone = false;
			frameCount = 0.0f;
		}
		else {
			DebugLog.e("DrawableSprite", "set state not found: "+state);
		}

		return true;
	}

	public void play() {
		isPause = false;
	}

	public void stop() {
		isPause = true;
		isReverse = false;
		isDone = false;
		frameCount = 0.0f;
		setFrame(0);
	}

	public void pause() {
		isPause = true;
	}

	public boolean isPause() {
		return isPause;
	}
	
	/** Is the animation done playing? This is never true for states with repeat type */
	public boolean isDone() {
		return isDone;
	}
	
	public float getCurSpriteFrameCount() {
		return frameCount;
	}

	@Override
	public void update(float timeDelta, BaseObject parent) {
		if(!isPause && !isDone && curState != null && curFrame != null) {
			// accumulate frames
			frameCount += sprite.getFPS()*timeDelta;

			// next frame?
			if(frameCount >= curFrame.numFrames) {
				frameCount = frameCount - curFrame.numFrames;

				int newFrameInd = isReverse ? curFrameIndex-1 : curFrameIndex+1;

				//check to see if new frame is out of bound
				if(newFrameInd == curState.frames.length || newFrameInd < 0) {
					switch(curState.loopMode) {
					case Sprite.LOOP_REPEAT:
						newFrameInd = 0;
						break;

					case Sprite.LOOP_REVERSE:
						if(isReverse) {
							newFrameInd = 0;
							isDone = true;
						}
						else {
							isReverse = true;
							newFrameInd = curState.frames.length-1;
						}
						break;

					case Sprite.LOOP_REVERSE_REPEAT:
						if(isReverse) {
							isReverse = false;
							newFrameInd = 0;
						}
						else {
							isReverse = true;
							newFrameInd = curState.frames.length-1;
						}
						break;

					default:
						newFrameInd = curFrameIndex;
						isDone = true;
						break;
					}
				}

				if(isDone && nextState != null) {
					setState(nextState);
				}
				else if(newFrameInd != curFrameIndex) {
					setFrame(newFrameInd);
				}
			}
		}
	}
	
	/**
	 * Modifies given drawable bitmap with the current frame.  This prepares the drawable to be displayed.
	 * 
	 * @param drawable The drawable that will be modified with sprite's texture, crop, and anchor.
	 */
	public void applyFrame(DrawableBitmap drawable) {
		final int[] c = curFrame.crop;
		
		drawable.texture = sprite.getTexture();

		drawable.setCrop(c[0], c[1], c[2], c[3]);
		
		drawable.width = curFrame.width;
		drawable.height = curFrame.height;
		
		drawable.anchorX = curFrame.offsetX;
		drawable.anchorY = curFrame.offsetY;
	}
}

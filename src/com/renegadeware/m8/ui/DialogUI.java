package com.renegadeware.m8.ui;

import android.util.FloatMath;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.R;
import com.renegadeware.m8.gfx.Color;
import com.renegadeware.m8.gfx.DrawableBitmap;
import com.renegadeware.m8.gfx.DrawableFrame;
import com.renegadeware.m8.gfx.DrawableGrid;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridFrame;
import com.renegadeware.m8.gfx.GridText;
import com.renegadeware.m8.gfx.Texture;
import com.renegadeware.m8.gfx.TextureAtlas;
import com.renegadeware.m8.input.InputXY;
import com.renegadeware.m8.math.Curve;
import com.renegadeware.m8.math.Ease;
import com.renegadeware.m8.res.ResourceGroupManager;
import com.renegadeware.m8.ui.dialog.Actor;
import com.renegadeware.m8.ui.dialog.Chat;

public class DialogUI extends FrameUI {
	/** When you call start(), once the animation ends, this state is called */
	public static final int DIALOG_STATE_STARTED = 1;
	
	/** When a new chat has begun, this is called with given chat index */
	public static final int DIALOG_STATE_CHAT_START = 2;
	
	/** When the last page is done, this is called with given current chat index */
	public static final int DIALOG_STATE_END_PAGE = 3;
	
	/** When you call end(), once the animation ends, this state is called */
	public static final int DIALOG_STATE_ENDED = 4;
	
	public interface DialogListener {
		public void onDialogCallback(DialogUI dialog, int state, int index);
	}
	
	protected String imageAtlasId;
	
	protected String turnPageImageId; //use this as a standalone image
	protected String turnPageAtlasRef; //if imageAtlasId is not null, this is the ref
	protected float turnPageWidth;
	protected float turnPageHeight;
	protected float turnPageBounceAmount;
	
	protected String endPageImageId; //use this as a standalone image
	protected String endPageAtlasRef; //if imageAtlasId is not null, this is the ref
	protected float endPageWidth;
	protected float endPageHeight;
	protected float endPageBlinkDelay;
			
	protected String fontId;
	protected float fontPtSize;
	protected Color fontColor;
	
	protected float portraitWidth;
	protected float portraitHeight;
	
	protected float portraitOffsetX;
	protected float portraitOffsetY;
	
	protected String nameFrameId;
	
	protected String nameFontId;
	protected float nameFontPtSize;
	protected Color nameFontColor;
	
	protected float nameOffsetX;
	protected float nameOffsetY;
	
	protected float charDelay; //char to progressively display per second
	
	protected Actor[] actors;
	
	protected Chat[] chats;
					
	protected int curChatIndex;
	
	//internal
	final static int PORTRAIT_LEFT = 0;
	final static int PORTRAIT_RIGHT = 1;
	final static int PORTRAIT_MIDDLE = 2;
	final static int MAX_PORTRAIT = 3;
	
	float turnPageBounceDelay;
	
	int curSubChatInd;
		
	int curCharNum;
	int curCharMax;
	int curStartLine;
	
	int curActorNum;
			
	boolean readyToTurnPage; //true if we are waiting for input to click to get to the new page
	boolean isLastPage;
	
	float curCharDelay;
	
	int maxLine;
		
	final DrawableGrid textDrawable;
	final DrawableBitmap turnPageDrawable;
	
	final DrawableBitmap endPageDrawable;
	float endPageCurDelay;
	boolean endPageVisible;
	
	final DrawableBitmap[] portraitDrawables;
	
	final DrawableFrame namePlateDrawable;
	final DrawableGrid nameTextDrawable;
	
	final EnterAnimator animEnter;
	final ExitAnimator animExit;
	
	DialogListener listener;

	public DialogUI() {
		super();
		
		textDrawable = new DrawableGrid();
		textDrawable.useColor = true;
		
		turnPageDrawable = new DrawableBitmap();
		turnPageDrawable.useColor = true;
		
		endPageDrawable = new DrawableBitmap();
		endPageDrawable.useColor = true;
		
		namePlateDrawable = new DrawableFrame();
		namePlateDrawable.useColor = true;
		
		nameTextDrawable = new DrawableGrid();
		nameTextDrawable.useColor = true;
						
		portraitDrawables = new DrawableBitmap[MAX_PORTRAIT];
		
		for(int i = 0; i < MAX_PORTRAIT; i++) {
			portraitDrawables[i] = new DrawableBitmap();
			portraitDrawables[i].useColor = true;
		}
		
		curChatIndex = 0;
		
		x = systemRegistry.contextParameters.gameHalfWidth;
		
		inputEnabled = true;
		modal = true;
		anchorV = 2;
		anchorH = 2;
		
		animEnter = new EnterAnimator();
		animExit = new ExitAnimator();
	}
	
	public void setListener(DialogListener l) {
		listener = l;
	}
	
	public void open(UISystem uiSys) {
		uiSys.add(this, animEnter);
	}
	
	public void close() {
		setAnimator(animExit, true);
		_animatorRemoveUIOnEnd = true;
	}
	
	public void setCurrentChat(int index, int subChatIndex) {
		curChatIndex = index;
		
		if(index >= 0 && index < chats.length) {
			curSubChatInd = subChatIndex;
									
			Chat chat = chats[index];
			
			GridText gridText = chat.texts[curSubChatInd];
			
			curStartLine = 0;
												
			textDrawable.grid = gridText;
									
			curCharDelay = 0;
			
			//position the name text and plate based on actors currently active
			int chatterInd = chat.chatterIndex;
			
			//set the name plate
			if(chatterInd != -1) {
				actors[chat.actors[chatterInd].actorIndex].setupNamePlate(namePlateDrawable, nameTextDrawable);
			}
			else {
				nameTextDrawable.grid = null;
			}
			
			//get the current actors talking
			curActorNum = 0;
			if(chat.actors != null) {
				curActorNum = chat.actors.length < MAX_PORTRAIT ? chat.actors.length : MAX_PORTRAIT;
			}
									
			//set the drawables for the portraits
			float w = getWidth();
			for(int i = 0; i < curActorNum; i++) {
				float fade = i == chatterInd ? 1.0f : 0.5f;
				chat.actors[i].setupPortrait(actors, w, fade, portraitDrawables[i]);
			}
			
			_calculatePage();
		}
	}
	
	/**
	 * Force input interaction with dialog
	 */
	public void forceClick() {
		inputTouchReleased(null);
	}
	
	@Override
	public void unload() {
		if(actors != null) {
			int num = actors.length;
			for(int i = 0; i < num; i++) {
				actors[i].unload();
			}
		}
		
		if(chats != null) {
			int num = chats.length;
			for(int i = 0; i < num; i++) {
				chats[i].unload();
			}
		}
		
		super.unload();
	}
	
	@Override
	public void load() {
						
		super.load();
		
		//the font
		Font font = (Font) systemRegistry.fontManager.getByName(fontId);
		
		if(fontColor != null) {
			textDrawable.color.set(fontColor);
			setAlpha(fontColor.alpha);
		}
		
		textDrawable.texture = font.getTexture(0);
		
		//the name font
		Font nameFont = (Font) systemRegistry.fontManager.getByName(nameFontId);
		//either nameFontId is not specified or it's really not found...
		//then just use the font from the textbox
		if(nameFont == null) {
			nameFont = font;
			nameFontPtSize = fontPtSize;
		}
		
		float nameHeight = nameFontPtSize > 0 ? nameFont.getLineHeight()*(nameFontPtSize/nameFont.getPointSize()) : nameFont.getLineHeight();
		
		namePlateDrawable.height = nameHeight;
		
		if(nameFontColor == null) {
			nameTextDrawable.color.set(Color.WHITE);
		}
		else {
			nameTextDrawable.color.set(nameFontColor);
		}
		
		//load nameplate frame
		if(nameFrameId != null) {
			namePlateDrawable.setFrame((GridFrame)systemRegistry.gridManager.getByName(nameFrameId));
		}
		
		//prepare portrait and name texts, plates
		for(int i = 0; i < MAX_PORTRAIT; i++) {
			portraitDrawables[i].width = portraitWidth;
			portraitDrawables[i].height = portraitHeight;
		}
		
		//auto size width to be within the screen
		float w = getWidth(), h = getHeight() < fontPtSize ? fontPtSize : getHeight();
		
		if(w == 0) {
			GridFrame f = frameDrawable.getFrame();
			w = systemRegistry.contextParameters.gameWidth - 4;
			w -= f.getLeftExtend() + f.getRightExtend();
		}
		
		TextureAtlas atlas = imageAtlasId != null && imageAtlasId.length() > 0 ? 
				(TextureAtlas)systemRegistry.textureAtlasManager.getByName(imageAtlasId) : null;
		
		//load turn page texture
		if(atlas != null) {
			turnPageDrawable.setTextureByAtlas(atlas, turnPageAtlasRef);
		}
		else if(turnPageImageId != null) {
			turnPageDrawable.texture = (Texture)systemRegistry.textureManager.getByName(turnPageImageId);
		}
		
		turnPageDrawable.width = turnPageWidth;
		turnPageDrawable.height = turnPageHeight;
		
		if(turnPageBounceAmount == 0) {
			turnPageBounceAmount = turnPageHeight*0.5f;
		}
		
		//load end page texture
		if(atlas != null) {
			endPageDrawable.setTextureByAtlas(atlas, endPageAtlasRef);
		}
		else if(endPageImageId != null) {
			endPageDrawable.texture = (Texture)systemRegistry.textureManager.getByName(endPageImageId);
		}
		
		endPageDrawable.width = endPageWidth;
		endPageDrawable.height = endPageHeight;
				
		final String grp = ResourceGroupManager.InternalResourceGroupName;
		
		//load actors
		if(actors != null) {
			int num = actors.length;
			if(num > 0) {
				for(int i = 0; i < num; i++) {
					try {
						actors[i].load(grp, nameFont, nameFontPtSize);
					} catch(Exception e) {
						DebugLog.e("UI", e.toString(), e);
					}
				}
			}
		}
		
		//load chats
		if(chats != null) {			
			int num = chats.length;
			if(num > 0) {				
				for(int i = 0; i < num; i++) {
					try {
						chats[i].load(grp, font, fontPtSize, w-turnPageWidth);
					} catch(Exception e) {
						DebugLog.e("UI", e.toString(), e);
					}
				}
			}
		}
						
		resize(w, h);
		
		setCurrentChat(curChatIndex, 0);
	}
	
	@Override
	public void resize(float w, float h) {
		super.resize(w, h);
		
		float hExt = super.frameDrawable.getFrame().getTopExtend();
		
		turnPageDrawable.anchorX = w-turnPageWidth;
		endPageDrawable.anchorX = w-endPageWidth;
		
		for(int i = 0; i < MAX_PORTRAIT; i++) {
			portraitDrawables[i].anchorY = h+hExt;
		}
		
		GridFrame nameFrame = namePlateDrawable.getFrame();
		if(nameFrame != null) {
			float nameHExt = nameFrame.getBottomExtend();
			namePlateDrawable.anchorY = h+hExt+nameHExt;
			nameTextDrawable.anchorY = h+hExt+nameHExt;
		}
		
		if(chats != null && chats.length > 0) {
			maxLine = (int)(getHeight()/chats[0].texts[curSubChatInd].getLineHeight());
		}
	}
	
	private void _calculatePage() {
		isLastPage = false;
		
		GridText gridText = chats[curChatIndex].texts[curSubChatInd];
				
		int endLine = curStartLine + maxLine - 1;
		if(endLine >= gridText.getLineCount()) {
			endLine = gridText.getLineCount()-1;
			
			isLastPage = true;
		}
		
		int charStart = gridText.getCharOffsetLine(curStartLine);
		curCharMax = (gridText.getCharOffsetLine(endLine) + gridText.getNumCharsLine(endLine)) - charStart;
		
		if(charDelay == 0) {
			curCharNum = curCharMax;
			
			readyToTurnPage = true;
		}
		else {
			curCharNum = 1;
			
			readyToTurnPage = false;
		}
						
		_updateTextDrawable();
		
		//set the textbox to draw from top
		textDrawable.anchorY = (getHeight() - gridText.getHeight()) + gridText.getLineHeight()*curStartLine;
	}
	
	private void _updateTextDrawable() {
		//set to first line's first character offset
		textDrawable.index = chats[curChatIndex].texts[curSubChatInd].getCharOffsetLine(curStartLine)*6;
		
		//set to number of characters for the page
		textDrawable.count = curCharNum*6;
	}
	
	@Override
	public void update(float timeDelta) {
		//update the char progress only if there's no animation
		if(getAnimator() == null) {
			if(charDelay > 0 && !readyToTurnPage) {
				curCharDelay += timeDelta;
				if(curCharDelay >= charDelay) {
					curCharDelay = 0;
					
					curCharNum++;
					
					readyToTurnPage = curCharNum == curCharMax; 
									
					_updateTextDrawable();
				}
			}
			else {
				if(isLastPage) {
					endPageCurDelay += timeDelta;
					if(endPageCurDelay >= endPageBlinkDelay) {
						endPageVisible = !endPageVisible;
						endPageCurDelay = 0;
					}
				}
				else {
					turnPageBounceDelay += timeDelta*3; //roughly one pi per second
					float s = FloatMath.sin(turnPageBounceDelay); 
					
					turnPageDrawable.anchorY = s*s*turnPageBounceAmount;
				}
			}
		}
	}
	
	@Override
	public void render(float x, float y, float scaleX, float scaleY,
			float rotate, float screenScaleX, float screenScaleY, float alpha) {
		//draw frame
		super.render(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY, alpha);
		//
		
		//render the other elements only if no animation is involved
		if(getAnimator() == null) {			
			//draw portraits
			for(int i = 0; i < curActorNum; i++) {
				DrawableBitmap portraitDrawable = portraitDrawables[i];
				portraitDrawable.color.alpha = alpha;
				portraitDrawable.draw(x + portraitOffsetX*scaleX, y + portraitOffsetY*scaleY, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
			}
			//
			
			//draw name plate and text
			if(nameTextDrawable.grid != null) {
				float ofsX = nameOffsetX*scaleX;
				float ofsY = nameOffsetY*scaleY;
				
				namePlateDrawable.draw(x + ofsX, y + ofsY, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				nameTextDrawable.draw(x + ofsX, y + ofsY, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
			}
			//
			
			//draw dialog text
			if(textDrawable.count > 0) {
				textDrawable.color.alpha = alpha;
				textDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
			}
			//
			
			//draw page flip button
			if(readyToTurnPage) {
				if(isLastPage) {
					if(endPageVisible) {
						endPageDrawable.color.alpha = alpha;
						endPageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
					}
				}
				else {
					turnPageDrawable.color.alpha = alpha;
					turnPageDrawable.draw(x, y, scaleX, scaleY, rotate, screenScaleX, screenScaleY);
				}
			}
			//
		}
	}
	
	@Override
	protected void inputTouchReleased(InputXY input) {
		if(readyToTurnPage) {
			int newStartLine = curStartLine+maxLine;
						
			if(newStartLine >= chats[curChatIndex].texts[curSubChatInd].getLineCount()) {
				//no more page turning, call the callback
				if(listener != null) {
					listener.onDialogCallback(this, DIALOG_STATE_END_PAGE, curChatIndex);
				}
			}
			else {
				curStartLine = newStartLine;
				_calculatePage();
			}
		}
		else {
			curCharNum = curCharMax;
			_updateTextDrawable();
			
			readyToTurnPage = true;
		}
	}
	
	@Override
	protected boolean inputTouchDrag(InputXY input, float distanceSq) {
		return isInRegion(input.getX(), input.getY(), true);
	}
	
	@Override
	protected void animationComplete(UIAnimator anim) {
		if(anim == animEnter) {
			if(listener != null) {
				listener.onDialogCallback(this, DIALOG_STATE_STARTED, 0);
			}
		}
		else if(anim == animExit) {
			if(listener != null) {
				listener.onDialogCallback(this, DIALOG_STATE_ENDED, 0);
			}
		}
	}
	
	class EnterAnimator implements UIAnimator {
		
		private float curDelay;
		private float maxDelay;
		
		public EnterAnimator() {
			maxDelay = 0.35f;
		}

		public void start() {
			curDelay = 0.0f;
		}

		@Override
		public void update(float timeDelta, UIDrawable drawable, int renderOrder, float x, float y) {
			curDelay += timeDelta;
			if(curDelay > maxDelay) {
				curDelay = maxDelay;
			}
			
			//float e = curDelay/maxDelay;
			
			
			float t = Ease.out(curDelay, maxDelay, 0.0f, 1.0f);
			
			drawable.setCurAlpha(t);//e);
			
			systemRegistry.renderSystem.scheduleForDraw(drawable, x, y, t, t, 0.0f, renderOrder);
		}

		@Override
		public boolean isDone() {
			return curDelay == maxDelay;
		}
		
	}
	
	class ExitAnimator implements UIAnimator {
		
		private float curDelay;
		private float maxDelay;
		
		public ExitAnimator() {
			maxDelay = 0.25f;
		}

		public void start() {
			curDelay = 0.0f;
		}

		@Override
		public void update(float timeDelta, UIDrawable drawable, int renderOrder, float x, float y) {
			curDelay += timeDelta;
			if(curDelay > maxDelay) {
				curDelay = maxDelay;
			}
			
			float t = 1.0f - curDelay/maxDelay;

			drawable.setCurAlpha(t);
			systemRegistry.renderSystem.scheduleForDraw(drawable, x, y, t, t, 0.0f, renderOrder);
		}

		@Override
		public boolean isDone() {
			return curDelay == maxDelay;
		}
		
	}
}

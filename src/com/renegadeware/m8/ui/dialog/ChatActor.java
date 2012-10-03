package com.renegadeware.m8.ui.dialog;

import com.renegadeware.m8.gfx.DrawableBitmap;

public final class ChatActor {
	public int actorIndex; // which actor
	public int emoteIndex; //emote index of actor
	public float frameWeight; // position within the frame's horizontal line 0 = left-most, 1 = right-most
	public int anchor;
	public boolean flip;
	
	public void setupPortrait(Actor[] actors, float frameWidth, float fade, DrawableBitmap portrait) {
		Actor actor = actors[actorIndex];
		
		portrait.color.red = portrait.color.green = portrait.color.blue = fade;
		
		portrait.setTextureAutoCrop(actor.emoteTextures[emoteIndex]);
		
		portrait.anchorX = frameWidth*frameWeight;
		
		switch(anchor) {
		case 1: //right
			portrait.anchorX -= portrait.texture.width;
			break;
			
		case 2: //center
			portrait.anchorX -= portrait.texture.width*0.5f;
			break;
		}
		
		if(flip) {
			portrait.setFlip(true, false);
		}
	}
}

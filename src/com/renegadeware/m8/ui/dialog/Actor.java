package com.renegadeware.m8.ui.dialog;

import com.renegadeware.m8.gfx.DrawableFrame;
import com.renegadeware.m8.gfx.DrawableGrid;
import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.GridText;
import com.renegadeware.m8.gfx.Texture;
import com.renegadeware.m8.gfx.TextureManager;
import com.renegadeware.m8.obj.BaseObject;
import com.renegadeware.m8.res.ResourceManager.CreateOrRetrievePair;

public final class Actor {
	public String resGrp;
	
	public String nameStringId;
	public String[] emoteImageIds;
	
	//internal
	public Texture[] emoteTextures;
	public GridText nameText;
	
	public Actor() {
	}
	
	public void unload() {
		final TextureManager tm = BaseObject.systemRegistry.textureManager;
		final GridManager gm = BaseObject.systemRegistry.gridManager;
		
		if(emoteTextures != null) {
			for(int i = 0; i < emoteTextures.length; i++) {
				final Texture txt = emoteTextures[i];
				//only unload and remove the texture if it's within our resource group
				if(txt.getGroup().compareTo(resGrp) == 0) {
					txt.unload();
					tm.remove(txt);
				}
			}
			
			emoteTextures = null;
		}
		
		if(nameText != null) {
			nameText.unload();
			gm.remove(nameText);
			
			nameText = null;
		}
	}
	
	public void load(String grp, Font fnt, float fntPtSize) throws Exception {
		resGrp = grp;
		
		final TextureManager tm = BaseObject.systemRegistry.textureManager;
		final GridManager gm = BaseObject.systemRegistry.gridManager;
		
		//load the name text
		if(nameStringId != null) {
			nameText = gm.createText(nameStringId, grp, fnt.id, fntPtSize, 0, 0, null);
			nameText.load(false);
		}
		
		//load the emotes
		if(emoteImageIds != null) {
			int num = emoteImageIds.length;
			if(num > 0) {
				emoteTextures = new Texture[num];
				
				for(int i = 0; i < num; i++) {
					CreateOrRetrievePair t = tm.createOrRetrieve(tm.getResourceIdByName(emoteImageIds[i]), grp, false, null, null);
					emoteTextures[i] = (Texture)t.res;
					emoteTextures[i].load(false);
				}
			}
		}
	}
	
	public void setupNamePlate(DrawableFrame nameFrameDrawable, DrawableGrid nameTextDrawable) {
		nameTextDrawable.grid = nameText;
		nameTextDrawable.texture = nameText.getFont().getTexture(0);
		nameFrameDrawable.width = nameText.getWidth();
	}
}
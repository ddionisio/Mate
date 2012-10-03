package com.renegadeware.m8.ui.dialog;

import com.renegadeware.m8.gfx.Font;
import com.renegadeware.m8.gfx.GridManager;
import com.renegadeware.m8.gfx.GridText;
import com.renegadeware.m8.obj.BaseObject;

public final class Chat {
	public ChatActor[] actors; //should only be 3 max
	
	public int chatterIndex; //the index within chatters who is currently talking
	
	public String[] stringIds; //required
	
	//internal
	public GridText[] texts;
	
	public Chat() {
		chatterIndex = -1;
	}
					
	public void load(String grp, Font fnt, float fntPtSize, float w) throws Exception {
		final GridManager gm = BaseObject.systemRegistry.gridManager;
		
		int num = stringIds.length;
		
		texts = new GridText[num];
		for(int i = 0; i < num; i++) {
			texts[i] = gm.createText(stringIds[i], grp, fnt.id, fntPtSize, w, 0, null);
			texts[i].load(false);
		}		
	}
	
	public void unload() {
		if(texts != null) {
			final GridManager gm = BaseObject.systemRegistry.gridManager;
			
			int num = stringIds.length;
			for(int i = 0; i < num; i++) {
				texts[i].unload();
				gm.remove(texts[i]);
			}
			
			texts = null;
		}
	}
}
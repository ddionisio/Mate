package com.renegadeware.m8.obj;

import java.util.ArrayList;

public abstract class BaseObjectRegistry extends BaseObject {

	//////////////////////////////////////////////////////////////////////
	// Methods
		
	public void registerForReset(BaseObject object) {
    	final boolean contained = itemsNeedingReset.contains(object);
    	assert !contained;
    	if (!contained) {
    		itemsNeedingReset.add(object);
    	}
    }
    
    @Override
    public void reset() {
    	final int count = itemsNeedingReset.size();
    	for (int x = 0; x < count; x++) {
    		itemsNeedingReset.get(x).reset();
    	}
    }

    private final ArrayList<BaseObject> itemsNeedingReset = new ArrayList<BaseObject>();

}

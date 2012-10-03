/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 
package com.renegadeware.m8.obj;

import java.util.Comparator;

/**
 * A derivation of ObjectManager that sorts its children if they are of type PhasedObject.
 * Sorting is performed on add.
 */
public class PhasedObjectManager extends ObjectManager {
    private final static PhasedObjectComparator sPhasedObjectComparator 
        = new PhasedObjectComparator();
    private boolean mDirty;
    private final PhasedObject mSearchDummy;  // A dummy object allocated up-front for searching by phase.

    public PhasedObjectManager() {
        super();
        mDirty = false;
        getObjects().setComparator(sPhasedObjectComparator);
        getPendingAdditions().setComparator(sPhasedObjectComparator);
        mSearchDummy = new PhasedObject();
    }
    
    public PhasedObjectManager(int arraySize) {
        super(arraySize);
        mDirty = false;
        getObjects().setComparator(sPhasedObjectComparator);
        getPendingAdditions().setComparator(sPhasedObjectComparator);
        mSearchDummy = new PhasedObject();
    }
    
    /**
     * Make sure you know what you're doing, this will affect the find function
     * 
     * @param comparator
     */
    public void setComparator(Comparator<BaseObject> comparator) {
    	if(comparator == null) {
    		getObjects().setComparator(sPhasedObjectComparator);
            getPendingAdditions().setComparator(sPhasedObjectComparator);
    	}
    	else {
    		getObjects().setComparator(comparator);
            getPendingAdditions().setComparator(comparator);
    	}
    }

    @Override
    public void commitUpdates() {
        super.commitUpdates();
        if (mDirty) {
            getObjects().sort(true);
            mDirty = false;
        }
    }
    
    @Override
    public void add(BaseObject object) {
        
        if (object instanceof PhasedObject) {
            super.add(object);
            mDirty = true;
        } else {
            // The only reason to restrict PhasedObjectManager to PhasedObjects is so that
            // the PhasedObjectComparator can assume all of its contents are PhasedObjects and
            // avoid calling instanceof every time.
            assert false : "Can't add a non-PhasedObject to a PhasedObjectManager!";
        }
    }
    
    /**
     * Force update to object order during update
     */
    protected void markDirty() {
    	mDirty = true;
    }
    
    public BaseObject find(int phase) {
        mSearchDummy.setPhase(phase);
        int index = getObjects().find(mSearchDummy, false);
        BaseObject result = null;
        if (index != -1) {
            result = getObjects().get(index);
        } else {
            index = getPendingAdditions().find(mSearchDummy, false);
            if (index != -1) {
                result = getPendingAdditions().get(index);
            }
        }
        return result;
    }

    /** Comparator for phased objects. */
    private static class PhasedObjectComparator implements Comparator<BaseObject>  {
        public int compare(BaseObject object1, BaseObject object2) {
            int result = 0;
            if (object1 != null && object2 != null) {
                result = ((PhasedObject) object1).phase - ((PhasedObject) object2).phase;
            } else if (object1 == null && object2 != null) {
                result = 1;
            } else if (object2 == null && object1 != null) {
                result = -1;
            } 
            return result;
        }
    }
    
}

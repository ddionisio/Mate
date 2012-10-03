package com.renegadeware.m8.util;

import java.util.Comparator;

public final class ObjectComparator implements Comparator<Object> {

	@Override
	public int compare(Object object1, Object object2) {
		if(object1 != null && object2 != null) {
			return object1.equals(object2) ? 0 : 1;
		}
		else if(object1 == null && object2 != null) {
			return 1;
		}
		else if(object2 == null && object1 != null) {
			return -1;
		}
		
		return 0;
	}

}

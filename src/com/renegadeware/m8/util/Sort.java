package com.renegadeware.m8.util;

import java.util.Comparator;

public final class Sort {	
	public static final <T> void insertion(T[] array, int size, Comparator<T> comparator) {
		for(int i = 1; i < size; ++i) {
	        T val = array[i];
	        int j = i;
	        while(j > 0 && comparator.compare(val, array[j-1]) < 0) {
	            array[j] = array[j-1];
	            --j;
	        }
	        array[j] = val;
	    }
	}
	
	static final <T> int partition(T[] array, int f, int l, T pivot, Comparator<T> comparator) {
		int i = f-1, j = l+1;
	    while(true) {
	        while(comparator.compare(pivot, array[--j]) < 0);
	        while(comparator.compare(array[++i], pivot) < 0);
	        if(i < j) {
	            T tmp = array[i];
	            array[i] = array[j];
	            array[j] = tmp;
	        }
	        else {
	            return j;
	        }
	    }
	}
		
	static final <T> void qsortImpl(T[] array, int f, int l, Comparator<T> comparator) {
		while(f+16 < l) {
	        T v1 = array[f], v2 = array[l], v3 = array[(f+l)/2];
	        T median =
	        	comparator.compare(v1, v2) < 0 ?
	            (comparator.compare(v3, v1) < 0 ? v1 : comparator.compare(v2, v3) <= 0 ? v2 : v3) :
	            (comparator.compare(v3, v2) < 0 ? v2 : comparator.compare(v1, v3) <= 0 ? v1 : v3);
	            
	        //int m = partition(array, f, l, median, comparator);
	        int m;
	        int i = f-1, j = l+1;
		    while(true) {
		        while(comparator.compare(median, array[--j]) < 0);
		        while(comparator.compare(array[++i], median) < 0);
		        if(i < j) {
		            T tmp = array[i];
		            array[i] = array[j];
		            array[j] = tmp;
		        }
		        else {
		            m = j;
		            break;
		        }
		    }
	        
	        qsortImpl(array, f, m, comparator);
	        
	        f = m+1;
	    }
	}
	
	//implementation using median hybrid
	public static final <T> void qsort(T[] array, int size, Comparator<T> comparator) {
		qsortImpl(array, 0, size-1, comparator);
		
		//insertion(array, size, comparator);
		for(int i = 1; i < size; ++i) {
	        T val = array[i];
	        int j = i;
	        while(j > 0 && comparator.compare(val, array[j-1]) < 0) {
	            array[j] = array[j-1];
	            --j;
	        }
	        array[j] = val;
	    }
	}
	
	//////////////////////////////////////
	// for no comparator
	static final ObjectComparator defaultComparator = new ObjectComparator();
	
	public static final <T> void insertion(T[] array, int size) {
		insertion(array, size, defaultComparator);
	}
	
	//implementation using median hybrid
	public static final <T> void qsort(T[] array, int size) {
		qsort(array, size, defaultComparator);
	}
}

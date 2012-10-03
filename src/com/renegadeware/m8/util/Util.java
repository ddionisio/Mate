package com.renegadeware.m8.util;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeMap;
import java.util.Iterator;

import com.renegadeware.m8.obj.BaseObject;

import android.content.Context;

public final class Util {
	
	// shortcut ways to reduce types for collections
	
	public static final <K,V> HashMap<K,V> newHashMap() {
		return new HashMap<K, V>();
	}
	
	public static final <K,V> TreeMap<K,V> newTreeMap() {
		return new TreeMap<K, V>();
	}
	
	public static final <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}
	
	public static final <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}
	
	public static final <T> void advanceIterator(Iterator<T> it, int count) {
		for(int i = 0; it.hasNext() && i < count; ++i) {
			it.next();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final <T> T instantiateClass(String name) throws Exception {
		Class<?> c = Class.forName(name);
		return (T) c.newInstance();
	}
	
	public static final Object instantiateClassArray(String name, int len) throws Exception {
		Class<?> c = Class.forName(name);
		return Array.newInstance(c, len);
	}
	
	/**
	 * Converts given number to string by using given array of char.
	 * 
	 * @param num
	 * @param base
	 * @param out
	 * @param outSize
	 * @return number of chars in out
	 */
	public final static int itoa(int num, int base, char[] out, int startInd) {
		if(base < 2 || base > 36) { return 0; }
		int i=startInd, tmp, outSize = out.length;
		char c;
		
		do {
			tmp = num;
			num /= base;
			out[i++] = "zyxwvutsrqponmlkjihgfedcba9876543210123456789abcdefghijklmnopqrstuvwxyz".charAt(35 + (tmp - num*base));
		} while(num != 0 && i < outSize);
						
		if(tmp < 0 && i < outSize) {
			out[i++] = '-';
		}
		
		int count=i; i--;
		int hCount = startInd + (count>>1);
		for(int j = startInd; j < hCount; j++, i--) {
			c = out[j];
			out[j] = out[i];
			out[i] = c;
		}
		
		//reverse
		/*while(j <= count) {
			c = out[i];
			out[i--] = out[j];
			out[j++] = c;
		}*/
		
		return count;
	}
	
	public final static int cellIndex(int row, int col, int numCol) {
		return (row*numCol) + col;
	}
	
	public final static int cellIndexToCol(int index, int numCol) {
		return index%numCol;
	}
	
	public final static int cellIndexToRow(int index, int numCol) {
		return index/numCol;
	}
	
	// Utilities from Replica Island
	
	private static final float EPSILON = 0.0001f;

    public final static boolean close(float a, float b) {
        return close(a, b, EPSILON);
    }

    public final static boolean close(float a, float b, float epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public final static int sign(float a) {
        if (a >= 0.0f) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public final static int clamp(int value, int min, int max) {
        int result = value;
        if (min == max) {
            if (value != min) {
                result = min;
            }
        } else if (min < max) {
            if (value < min) {
                result = min;
            } else if (value > max) {
                result = max;
            }
        } else {
            result = clamp(value, max, min);
        }
        
        return result;
    }
    
    public final static float clamp(float value, float min, float max) {
    	float result = value;
        if (min == max) {
            if (value != min) {
                result = min;
            }
        } else if (min < max) {
            if (value < min) {
                result = min;
            } else if (value > max) {
                result = max;
            }
        } else {
            result = clamp(value, max, min);
        }
        
        return result;
    }
   
    
    public final static int byteArrayToInt(byte[] b, int ofs) {

        // Same as DataInputStream's 'readInt' method
        /*int i = (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) 
                | (b[3] & 0xff));*/
        
        // little endian
        int i = (((b[ofs+3] & 0xff) << 24) | ((b[ofs+2] & 0xff) << 16) | ((b[ofs+1] & 0xff) << 8) 
                | (b[ofs] & 0xff));
    
        return i;
    }
    
    public final static void intToByteArray(int i, byte[] b, int ofs) {
        // little endian
    	b[ofs] = (byte)(i&0xff);
    	b[ofs+1] = (byte)((i&0xff00)>>8);
    	b[ofs+2] = (byte)((i&0xff0000)>>16);
    	b[ofs+3] = (byte)((i&0xff000000)>>24);
    }
    
    public final static float byteArrayToFloat(byte[] b, int ofs) {
        
        // intBitsToFloat() converts bits as follows:
        /*
        int s = ((i >> 31) == 0) ? 1 : -1;
        int e = ((i >> 23) & 0xff);
        int m = (e == 0) ? (i & 0x7fffff) << 1 : (i & 0x7fffff) | 0x800000;
        */
    	
        return Float.intBitsToFloat(byteArrayToInt(b, ofs));
    }
    
    public final static void floatToByteArray(float f, byte[] b, int ofs) {
        int i = Float.floatToIntBits(f);
        intToByteArray(i, b, ofs);
    }
    
    public final static short byteArrayToShort(byte[] b, int ofs) {
        // little endian
        short s = (short)(((b[ofs] & 0xff) << 8) | (b[ofs] & 0xff));
    
        return s;
    }
    
    public final static void shortToByteArray(short s, byte[] b, int ofs) {
        // little endian
    	b[ofs] = (byte)(s&0xff);
    	b[ofs+1] = (byte)((s&0xff00)>>8);
    }
    
    public final static long byteArrayToLong(byte[] b, int ofs) {
        
        // Same as DataInputStream's 'readInt' method
        /*int i = (((b[0] & 0xff) << 24) | ((b[1] & 0xff) << 16) | ((b[2] & 0xff) << 8) 
                | (b[3] & 0xff));*/
        
        // little endian
        long i = (((b[ofs+3] & 0xff) << 24) | ((b[ofs+2] & 0xff) << 16) | ((b[ofs+1] & 0xff) << 8) 
                | (b[ofs] & 0xff));
        
        long i2 = (((b[ofs+7] & 0xff) << 24) | ((b[ofs+6] & 0xff) << 16) | ((b[ofs+5] & 0xff) << 8) 
                | (b[ofs+4] & 0xff));
    
        return ((i2&0xffffffff)<<32) | (i&0xffffffff);
    }
    
    public final static void longToByteArray(long l, byte[] b, int ofs) {
    	intToByteArray((int)(l&0xffffffff), b, ofs);
    	intToByteArray((int)(l>>32), b, ofs+4);
    }
    
    public final static float framesToTime(int framesPerSecond, int frameCount) {
        return (1.0f / framesPerSecond) * frameCount;
    }
    
    public final static int getResourceIdByName(String name) {
    	final Context c = BaseObject.systemRegistry.contextParameters.context;
    	if(c != null) {
    		return c.getResources().getIdentifier(name, null, c.getPackageName());
    	}
    	
    	return 0;
    }
    
    /** Global randomizer, this is initialized in MateActivity's onCreate */
    private static Random rand;
    
    public final static Random getRandom() {
    	return rand;
    }
    
    public final static void initRandom(int seed) {
    	rand = new Random(seed);
    }
    
    public final static void initRandom() {
    	rand = new Random();//71286986890635L);
    }
    
    public final static <T> void shuffleArray(T array[]) {
    	for (int i = 0; i < array.length; i++) {
    	    int randomPosition = rand.nextInt(array.length);
    	    T temp = array[i];
    	    array[i] = array[randomPosition];
    	    array[randomPosition] = temp;
    	}
    }
    
    public final static void shuffleArray(int array[]) {
    	for (int i = 0; i < array.length; i++) {
    	    int randomPosition = rand.nextInt(array.length);
    	    int temp = array[i];
    	    array[i] = array[randomPosition];
    	    array[randomPosition] = temp;
    	}
    }
    
    public final static void shuffleArray(boolean array[]) {
    	for (int i = 0; i < array.length; i++) {
    	    int randomPosition = rand.nextInt(array.length);
    	    boolean temp = array[i];
    	    array[i] = array[randomPosition];
    	    array[randomPosition] = temp;
    	}
    }
    
    public final static void shuffleArray(byte array[]) {
    	for (int i = 0; i < array.length; i++) {
    	    int randomPosition = rand.nextInt(array.length);
    	    byte temp = array[i];
    	    array[i] = array[randomPosition];
    	    array[randomPosition] = temp;
    	}
    }
}

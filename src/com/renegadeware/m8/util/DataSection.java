package com.renegadeware.m8.util;

import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;

import android.graphics.Color;

import com.renegadeware.m8.DebugLog;
import com.renegadeware.m8.math.Vector2;
import com.renegadeware.m8.ui.BaseUI;

public abstract class DataSection implements Iterable<DataSection> {
	
	public static final int FLAG_INCLUDE_WHITESPACE = 0x0000;
	public static final int FLAG_INCLUDE_TRIM_WHITESPACE = 0x0001;
	public static final int FLAG_INCLUDE_APPEND_VECTOR = 0x0000;
	public static final int FLAG_INCLUDE_OVERWRITE_VECTOR = 0x0001;
	
	public class DataSectionIterator implements Iterator<DataSection> {
		public DataSectionIterator() {
			index = 0;
		}
		
		public DataSectionIterator(int index) {
			this.index = 0;
		}
		
		@Override
		public boolean hasNext() {
			return index < countChildren();
		}

		@Override
		public DataSection next() {
			DataSection child = openChild(index);
			index++;
			return child;
		}

		@Override
		public void remove() {
			delChild(openChild(index));
		}
		
		private int index;
	}

	 public DataSectionIterator iterator() {
		 return new DataSectionIterator();
	 }
	 
	 public class SearchIterator implements Iterator<DataSection> {
		 @Override
		 public boolean hasNext() {
			 return index < countChildren();
		 }

		 @Override
		 public DataSection next() {
			 int numChildren = countChildren();
			 while(index < numChildren) {
				 DataSection child = openChild(index);
				 index++;
				 
				 if(child != null && child.sectionName().compareTo(sectionName) == 0) {
					 return child;
				 }
			 }
			 return null;
		 }

		 @Override
		 public void remove() {
		 }
		 
		 private SearchIterator(String sectionName) {
			 this.sectionName = sectionName;
			 index = 0;
		 }

		 private int index;
		 private String sectionName;
	 }
	 
	 public SearchIterator search(String sectionName) {
		 return new SearchIterator(sectionName);
	 }

	 ///////////////////////////////////////////////////
	 // Implements
	 
	 /** This methods returns the number of sections under this one. */
	 public abstract int countChildren();

	 /** This method returns the index of the DataSectionPtr
	  *
	  *  @param data	The DataSectionPtr to return the index of
	  *
	  *  @return The index of the DataSection (-1 if not found)
	  */
	 public int getIndex(DataSection data) {
		 int count = countChildren();
		 
		 for(int i = 0; i < count; i++) {
			 DataSection d = openChild(i);
			 if(d == data) {
				 return i;
			 }
		 }
		 return -1;
	 }

	 /** This method opens a section with the given index. Note that it
	     may return NULL for a valid index if there is an access error. */
	 public abstract DataSection  openChild(int index);

	 /** This method returns the name of the child with the given index. */
	 public String childSectionName(int index) {
		 return openChild(index).sectionName();
	 }

	 /** This method creates a new section directly under the current section.
	     It should be the same type as the parent section, or NULL if not
	     appropriate. */
	 public abstract DataSection newSection(String tag);

	 /** This method allows a new data section to be created at the index specified.
	     It should be the same type as the parent section, or NULL if not
	     appropriate. It is only relevant for XMLSections. index default = -1 */
	 public DataSection insertSection(String tag, int index) { 
		 return null; 
	 }

	 /** This method searches for a new section directly under the current section. */
	 public abstract DataSection findChild(String tag);

	 /** This method deletes a child directly under the current section. */
	 public abstract void delChild(String tag);

	 /** This method deletes a child directly under the current section. */
	 public abstract void delChild(DataSection section);

	 /** This method deletes all children. */
	 public abstract void delChildren();

	 /** This method returns the section's name. */
	 public abstract String sectionName();

	 /** This method returns the approx size in bytes used by this section. */
	 public abstract int bytes();

	 /** This method saves the datasection. */
	 public abstract boolean save(OutputStream out);
	 
	 /** This method returns the data section's parent */
	 public DataSection getParent() { 
		 return null; 
	 }

	 public boolean canPack() { 
		 return true; 
	 }
	 
	 ///////////////////////////////////////////////////
	 // Access methods
	 	 
	 private Object _readObjectFieldArray(String type, int typeInd, DataSection d) throws Exception {
		 Object ret = null;
		 
		 switch(type.charAt(typeInd)) {
			case 'Z':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				boolean[] array = new boolean[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Boolean.parseBoolean(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'D':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				double[] array = new double[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Double.parseDouble(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'F':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				float[] array = new float[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Float.parseFloat(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'I':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				int[] array = new int[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Integer.parseInt(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'J':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				long[] array = new long[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Long.parseLong(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'S':
			{
				String[] data = d.asString(0).split("\\s+");
				int num = data.length;
				
				short[] array = new short[num];
				
				for(int i = 0; i < num; i++) {
					array[i] = Short.parseShort(data[i]);
				}
				
				ret = array;
			}
			break;
			
			case 'L':
			{
				int semiColon = type.indexOf(';');
				
				//check for custom class
				String className = d.getAttributeAsString("class", 0);
				
				if(className == null || className.length() == 0) {
					//if not found, then just take the one from the type name
					className = type.substring(typeInd+1, semiColon);
				}
								
				if(className.compareTo("java.lang.String") == 0) {
					//just split the strings by space
					ret = d.asString(0).split("\\s+");
				}
				else if(className.compareTo("com.renegadeware.m8.math.Vector2") == 0) {
					Vector2[] vecs = new Vector2[d.countChildren()];
					int i = 0;
					for(DataSection subD : d) {
						vecs[i] = subD.asVector2(Vector2.ZERO);
						i++;
					}
					
					ret = vecs;
				}
				else if(className.compareTo("com.renegadeware.m8.gfx.Color") == 0) {
					 //#RRGGBB or #AARRGGBB, 'red', 'blue', etc.
					String[] data = d.asString(0).split("\\s+");
					int num = data.length;
					
					com.renegadeware.m8.gfx.Color[] array = new com.renegadeware.m8.gfx.Color[num];
					
					for(int i = 0; i < num; i++) {
						int clr = Color.parseColor(data[i]);
						array[i] = new com.renegadeware.m8.gfx.Color(clr);
					}
					
					ret = array;
				}
				//TODO: ...special case for ui...we need to fix this later
				else if(className.compareTo("com.renegadeware.m8.ui.BaseUI") == 0) {
					BaseUI[] uis = new BaseUI[d.countChildren()];
					
					int i = 0;
					for(DataSection subD : d) {
						uis[i] = BaseUI.createUIFromDataSection(subD);
						i++;
					}
					
					ret = uis;
				}
				else {
					//hope this works...
					Class<?> klass = Class.forName(className);
					
					Object[] klasses = (Object[]) Array.newInstance(klass, d.countChildren());
					
					int i = 0;
					for(DataSection subD : d) {
						//check to see if there's a specific class to instantiate for this element
						//this is needed for an array of abstract class/interface
						String subClass = subD.getAttributeAsString("class", 0);
						if(subClass != null && subClass.length() > 0) {
							klasses[i] = Util.instantiateClass(subClass);
						}
						else {
							klasses[i] = klass.newInstance();
						}
						
						subD.readObjectFields(klasses[i]);
						i++;
					}
					
					ret = klasses;
				}
			}
			break;
			
			case '[':
			{
				Object[] array=null;// = new Object[d.countChildren()][];
				
				int count = d.countChildren();
				
				if(count > 0) {
					int typeSubInd = typeInd+1;
					
					Object first = _readObjectFieldArray(type, typeInd+1, d.openChild(0));
					array = (Object[])Array.newInstance(first.getClass(), d.countChildren());
					array[0] = first;
					
					for(int i = 1; i < count; i++) {
						DataSection subD = d.openChild(i);
						array[i] = _readObjectFieldArray(type, typeSubInd, subD);
					}
				}
				
				ret = array;
			}
			break;
			
		 }
		 
		 return ret;
	 }
	 
	 public void readObjectFields(Object obj) {
		 Class<?> c = obj.getClass();

		 for(DataSection d : this) {
			 try {
				 //get the field and make sure we can access it
				 Field f = null;
				 
				 try {
					 f = c.getDeclaredField(d.sectionName());
				 } catch(NoSuchFieldException noFieldE) {
					 //search in upper classes until we hit Object
					 boolean fieldFound = false;
					 
					 Class<?> subC = c;
					 
					 while(!fieldFound) {
						 subC = subC.getSuperclass();
						 if(subC == null) {
							 DebugLog.w("DataSection", "Trying to set field "+d.sectionName()+" failed: "+noFieldE.toString());
							 break;
						 }
						 
						 try {
							 f = subC.getDeclaredField(d.sectionName());
							 fieldFound = true;
						 } catch(NoSuchFieldException subNoFieldE) {
							 continue;
						 }
					 }
				 }
				 
				 if(f == null) {
					 continue;
				 }
				 
				 f.setAccessible(true);
				 
				 Class<?> type = f.getType();
				 String typeName = type.getName();

				 if(type.isArray()) {
					 f.set(obj, _readObjectFieldArray(typeName, 1, d));
				 }
				 else if(typeName.compareTo("boolean") == 0) {
					 f.setBoolean(obj, d.asBool());
				 }
				 else if(typeName.compareTo("byte") == 0) {
					 f.setByte(obj, (byte)d.asInt(0));
				 }
				 else if(typeName.compareTo("char") == 0) {
					 f.setChar(obj, d.asString(FLAG_INCLUDE_WHITESPACE).charAt(0));
				 }
				 else if(typeName.compareTo("double") == 0) {
					 f.setDouble(obj, d.asDouble(0));
				 }
				 else if(typeName.compareTo("float") == 0) {
					 f.setFloat(obj, d.asFloat(0));
				 }
				 else if(typeName.compareTo("int") == 0) {
					 f.setInt(obj, d.asInt(0));
				 }
				 else if(typeName.compareTo("long") == 0) {
					 f.setLong(obj, d.asLong(0));
				 }
				 else if(typeName.compareTo("short") == 0) {
					 f.setShort(obj, (short)d.asInt(0));
				 }
				 else if(typeName.compareTo("java.lang.String") == 0) {
					 f.set(obj, d.asString(0));
				 }
				 else if(typeName.compareTo("com.renegadeware.m8.math.Vector2") == 0) {
					 f.set(obj, d.asVector2(Vector2.ZERO));
				 }
				 else if(typeName.compareTo("com.renegadeware.m8.gfx.Color") == 0) {
					 //#RRGGBB or #AARRGGBB, 'red', 'blue', etc.
					 int clr = Color.parseColor(d.asString(0));
					 f.set(obj, new com.renegadeware.m8.gfx.Color(clr));
				 }
				 //TODO: ...special case for ui...we need to fix this later
				 else if(typeName.compareTo("com.renegadeware.m8.ui.BaseUI") == 0) {
					 f.set(obj, BaseUI.createUIFromDataSection(d));
				 }
				 else {
					 //check to see if it has a custom class
					 String customClassName = d.getAttributeAsString("class", 0);
					 if(customClassName != null && customClassName.length() > 0) {
						 Object subObj = Util.instantiateClass(customClassName);
						 d.readObjectFields(subObj);
						 f.set(obj, subObj);
					 }
					 else {
						 //create class, assume that it allows empty constructor
						 Object subObj = type.newInstance();

						 //set the fields of this class from this sub-data's children
						 d.readObjectFields(subObj);

						 f.set(obj, subObj);
					 }
				 }
			 } catch(Exception e) {
				 DebugLog.w("DataSection", "Trying to set field "+d.sectionName()+" failed: "+e.toString());
			 }
		 }
	 }
	 
	 /**
	  * This method opens a section with the name tag specified. It returns a
	  * pointer to the new section that was a subsection of the current. If the
	  * specified subsection was not found, and makeNewSection is true, the new
	  * section will be created. Otherwise this function will return null.
	  *
	  * @param tagPath The path to search for.
	  * @param makeNewSection If true and the section does not already exist, the
	  *			 section is created.
	  *
	  * @return The associated section
	  */
	 public DataSection openSection(String tagPath, boolean makeNewSection) {
		 if(tagPath.length() == 0) {
			 return null;
		 }
		 
		 DataSection child;
		 int pos = tagPath.indexOf('/');
		 
		 // Recurse down the path until we are left with a shingle child section
		 
		 if(0 <= pos && pos < tagPath.length()) {
			 String subPath = tagPath.substring(0, pos);
			 
			 child = findChild(subPath);
			 
			 if(child == null) {
				 if(makeNewSection) {
					 child = newSection(subPath);
				 }
				 
				 if(child == null) {
					 return null;
				 }
			 }
			 
			 return child.openSection(tagPath.substring(pos+1), makeNewSection);
		 }
		 
		 // Find within our list
		 
		 child = findChild(tagPath);
		 
		 if(child == null && makeNewSection) {
			 child = newSection(tagPath);
		 }
		 
		 return child;
	 }

	 /**
	  * This method opens the first sub-section within the section.
	  *
	  * @return A pointer to the subsection. If the specified
	  *	   subsection was not found through its tag, a NULL value is returned
	  *	   instead.
	  */
	 public DataSection openFirstSection() {
		 if(countChildren() > 0) {
			 return openChild(0);
		 }
		 
		 return null;
	 }

	 /** This method opens a vector of sections with the name tag specified. If
	  *  there were no sections that match the tag specified, then the vector is
	  *  left unchanged.
	  *
	  * @param tagPath The name/path of the sections to be read in.
	  * @param dest The vector used for storing the sections.
	  */
	 public void openSections(String tagPath, ArrayList<DataSection> dest) {
		 DataSection child;
		 int pos = tagPath.indexOf('/');
		 
		 if(0 <= pos && pos < tagPath.length()) {
			 child = findChild(tagPath.substring(0, pos));
			 
			 if(child != null) {
				 child.openSections(tagPath.substring(pos+1), dest);
			 }
		 }
		 else {
			 for(DataSection d : this) {
				 if(d.sectionName().compareTo(tagPath) == 0) {
					 dest.add(d);
				 }
			 }
		 }
	 }

	 /**
	  * This method deletes the specified section under the current section.
	  * It can also delete a tag instead of a section. It will fail if the section
	  * does not exist.
	  *
	  * @param tagPath The name/path of the section to be deleted.
	  *
	  * @return True if successful, otherwise false.
	  */
	 public boolean deleteSection(String tagPath) {
		 int tokenBegin = 0;
		 int tokenEnd = -1;
		 
		 DataSection curSection = this;
		 DataSection parent = null;
		 
		 do {
			 tokenBegin = tokenEnd + 1;
			 tokenEnd = tagPath.indexOf('/', tokenBegin);
			 
			 String tag;
			 
			 if(0 <= tokenEnd && tokenEnd < tagPath.length()) {
				 tag = tagPath.substring(tokenBegin, tokenEnd - tokenBegin);
			 }
			 else {
				 tag = tagPath.substring(tokenBegin);
			 }
			 
			 parent = curSection;
			 if(parent != null) {
				 curSection = parent.findChild(tag);
			 }
			 
		 } while(tokenEnd >= 0 && curSection != null);
		 
		 if(parent != null && curSection != null) {
			 parent.delChild(curSection.sectionName());
			 return true;
		 }
		 
		 return false;
	 }

	 /** This methods deletes all sections with the specified name under the current section */
	 public void deleteSections(String tagPath) {
		 while(deleteSection(tagPath));
	 }

	 /////////////////////////////////////////////////////
	 // Methods for reading the value of the DataSection

	 /** This method reads in the value of the DataSection as a boolean. */
	 public boolean asBool() { 
		 return false; 
	 }

	 /** This method reads in the value of the DataSection as an integer. */
	 public int asInt(int defaultVal) { 
		 return defaultVal; 
	 }

	 /** This method reads in the value of the DataSection as a long integer. */
	 public long asLong(long defaultVal ) { 
		 return defaultVal; 
	 }

	 /** This method reads in the value of the DataSection as a float. */
	 public float asFloat(float defaultVal) { 
		 return defaultVal; 
	 }

	 /** This method reads in the value of the DataSection as a double. */
	 public double asDouble(double defaultVal) { 
		 return defaultVal; 
	 }

	 /** This method reads in the value of the DataSection as a String. */
	 public String asString(int flags) { 
		 return ""; 
	 }
	 
	 /** This method reads in the value of the DataSection as a Vector2. */
	 public Vector2 asVector2(Vector2 defaultVal) {
		 return defaultVal.clone();
	 }
	 
	 /////////////////////////////////////////////////////
	 // Methods for writing the value of the DataSection

	 /** This method sets the datasection to a boolean value. */
	 public boolean setBool(boolean value) { 
		 return false; 
	 }

	 /** This method sets the datasection to an integer value. */
	 public boolean setInt(int value) { 
		 return false; 
	 }

	 /** This method sets the datasection to a long value. */
	 public boolean setLong(long value) { 
		 return false; 
	 }

	 /** This method sets the datasection to a floating-point value. */
	 public boolean setFloat(float value) { 
		 return false; 
	 }

	 /** This method sets the datasection to a double value. */
	 public boolean setDouble(double value) { 
		 return false; 
	 }

	 /** This method sets the datasection to a String value. */
	 public boolean setString(String value) { 
		 return false; 
	 }
	 
	 /** This method sets the datasection to a Vector2 value. */
	 public boolean setVector2(Vector2 value) { 
		 return false; 
	 }
	 
	 /////////////////////////////////////////////////////
	 // Methods of reading in values of document elements.

	 /** This method reads in the value of the specified tag as a boolean value. */
	 public boolean readBool(String tagPath) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asBool();
		 }
		 
		 return false;
	 }

	 /** This method reads in the value of the specified tag as an integer value. */
	 public int readInt(String tagPath, int defaultVal) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asInt(defaultVal);
		 }
		 
		 return defaultVal;
	 }

	 /** This method reads in the value of the specified tag as a long integer value. */
	 public long readLong(String tagPath, long defaultVal) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asLong(defaultVal);
		 }
		 
		 return defaultVal;
	 }

	 /** This method reads in the value of the specified tag as a floating-point value. */
	 public float readFloat(String tagPath, float defaultVal) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asFloat(defaultVal);
		 }
		 
		 return defaultVal;
	 }

	 /** This method reads in the value of the specified tag as a double floating-point value. */
	 public double readDouble(String tagPath, double defaultVal) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asDouble(defaultVal);
		 }
		 
		 return defaultVal;
	 }

	 /** This method reads in the value of the specified tag as a string. */
	 public String readString(String tagPath, int flags) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asString(flags);
		 }
		 
		 return "";
	 }
	 
	 /** This method reads in the value of the specified tag as a Vector2 value. */
	 public Vector2 readVector2(String tagPath, Vector2 defaultVal) {
		 DataSection section = openSection(tagPath, false);
		 if(section != null) {
			 return section.asVector2(defaultVal);
		 }
		 
		 return defaultVal;
	 }
	 
	 /////////////////////////////////////////////////////
	 // Methods of writing in values of document elements.

	 /** This method writes a boolean value to the tag specified. */
	 public boolean writeBool(String tagPath, boolean value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setBool(value);
		 }
		 
		 return false;
	 }

	 /** This method writes an integer value to the tag specified. */
	 public boolean writeInt(String tagPath, int value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setInt(value);
		 }
		 
		 return false;
	 }

	 /** This method writes a long value to the tag specified. */
	 public boolean writeLong(String tagPath, long value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setLong(value);
		 }
		 
		 return false;
	 }

	 /** This method writes a floating-point value to the tag specified. */
	 public boolean writeFloat(String tagPath, float value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setFloat(value);
		 }
		 
		 return false;
	 }

	 /** This method writes a double floating-point value to the tag specified. */
	 public boolean writeDouble(String tagPath, double value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setDouble(value);
		 }
		 
		 return false;
	 }

	 /** This method writes a string value to the tag specified. */
	 public boolean writeString(String tagPath, String value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setString(value);
		 }
		 
		 return false;
	 }
	 
	 /** This method writes a Vector2 value to the tag specified. */
	 public boolean writeVector2(String tagPath, Vector2 value) {
		 DataSection section = openSection(tagPath, true);
		 
		 if(section != null) {
			 return section.setVector2(value);
		 }
		 
		 return false;
	 }
	 
	 /////////////////////////////////////////////////////
	 // Methods of reading whole vectors of elements with the same tag.

	 /** This method reads in a vector of bools under the specified tag. */
	 public void readBools(String tagPath, ArrayList<Boolean> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asBool());
			 }
		 }
	 }

	 /** This method reads in a vector of ints under the specified tag. */
	 public void readInts(String tagPath, ArrayList<Integer> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asInt(0));
			 }
		 }
	 }

	 /** This method reads in a vector of longs under the specified tag. */
	 public void readLongs(String tagPath, ArrayList<Long> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asLong(0));
			 }
		 }
	 }

	 /** This method reads in a vector of floats under the specified tag. */
	 public void readFloats(String tagPath, ArrayList<Float> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asFloat(0.0f));
			 }
		 }
	 }

	 /** This method reads in a vector of doubles under the specified tag. */
	 public void readDoubles(String tagPath, ArrayList<Double> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asDouble(0.0));
			 }
		 }
	 }

	 /** This method reads in a vector of strings under the specified tag. */
	 public void readStrings(String tagPath, ArrayList<String> dest, int flags ) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asString(flags));
			 }
		 }
	 }
	 
	 /** This method reads in a vector of Vector2 under the specified tag. */
	 public void readVector2s(String tagPath, ArrayList<Vector2> dest) {
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 dest.add(d.asVector2(null));
			 }
		 }
	 }
	 
	 /////////////////////////////////////////////////////
	 // Methods of writing whole vectors of elements with the same tag.

	 /** This method writes in a vector of bools under the specified tag. */
	 public void writeBools(String tagPath, ArrayList<Boolean> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Boolean val : src) {
			 section.newSection(tag).setBool(val);
		 }
	 }

	 /** This method writes in a vector of ints under the specified tag. */
	 public void writeInts(String tagPath, ArrayList<Integer> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Integer val : src) {
			 section.newSection(tag).setInt(val);
		 }
	 }

	 /** This method writes in a vector of longs under the specified tag. */
	 public void writeLongs(String tagPath, ArrayList<Long> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Long val : src) {
			 section.newSection(tag).setLong(val);
		 }
	 }

	 /** This method writes in a vector of floats under the specified tag. */
	 public void writeFloats(String tagPath, ArrayList<Float> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Float val : src) {
			 section.newSection(tag).setFloat(val);
		 }
	 }

	 /** This method writes in a vector of doubles under the specified tag. */
	 public void writeDoubles(String tagPath, ArrayList<Double> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Double val : src) {
			 section.newSection(tag).setDouble(val);
		 }
	 }

	 /** This method writes in a vector of strings under the specified tag. */
	 public void writeStrings(String tagPath, ArrayList<String> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(String val : src) {
			 section.newSection(tag).setString(val);
		 }
	 }
	 
	 /** This method writes in a vector of Vector2s under the specified tag. */
	 public void writeVector2s(String tagPath, ArrayList<Vector2> src, int flags) {
		 if(flags == FLAG_INCLUDE_OVERWRITE_VECTOR) {
			 deleteSections(tagPath);
		 }
		 
		 SplitTagResult res = splitTagPath(tagPath, false);
		 
		 final DataSection section = res.section;
		 final String tag = res.tag;
		 
		 if(section == null) {
			 return;
		 }
		 
		 for(Vector2 val : src) {
			 section.newSection(tag).setVector2(val);
		 }
	 }
	 
	 ////////////////////////////////////////////
	 // attributes

	 public String getAttributeAsString(String attr, int flags) {
		 return ""; 
	 }

	 public int getAttributeAsInt(String attr, int defaultVal) {
		 return defaultVal; 
	 }

	 public long getAttributeAsLong(String attr, long defaultVal) {
		 return defaultVal; 
	 }

	 public float getAttributeAsFloat(String attr, float defaultVal) {
		 return defaultVal; 
	 }

	 public double getAttributeAsDouble(String attr, double defaultVal) {
		 return defaultVal; 
	 }

	 public Vector2 getAttributeAsVector2(String attr, Vector2 defaultVal) {
		 return defaultVal.clone();
	 }
	 
	 
	 public boolean setAttributeString(String attr, String val) {
		 return false; 
	 }

	 public boolean setAttributeInt(String attr, int val) {
		 return false; 
	 }

	 public boolean setAttributeLong(String attr, long val) {
		 return false; 
	 }

	 public boolean setAttributeFloat(String attr, float val) {
		 return false;
	 }

	 public boolean setAttributeDouble(String attr, double val) {
		 return false;
	 }

	 public boolean setAttributeVector2(String attr, Vector2 val) {
		 return false;
	 }
	 
	 
	 public void setWatcherValues(String path) {
		 
	 }
	 

	 /** recursively copys everything in pSection.
	  * by default, also clear the current section and set the section
	  * name to that of the passed in section */
	 public void copy(DataSection section, boolean modifyCurrent) {
		 if(modifyCurrent) {
			 delChildren();
			 setString(section.asString(0));
		 }
		 
		 for(DataSection d : section) {
			 DataSection newSection = newSection(d.sectionName());
			 newSection.copy(d, true);
		 }
	 }

	 /** Copies all matching sections from pSection, without clearing the current section first */
	 public void copySections(DataSection section, String tag) {
		 for(DataSection d : section) {
			 if(d.sectionName().compareTo(tag) == 0) {
				 DataSection newSection = newSection(d.sectionName());
				 newSection.copy(d, true);
			 }
		 }
	 }

	 /** Copies all sections from pSection, without clearing the current section first */
	 public void copySections(DataSection section) {
		 setString(section.asString(0));
		 
		 for(DataSection d : section) {
			 DataSection newSection = openSection(d.sectionName(), true);
			 
			 newSection.copySections(d);
		 }
	 }

	 /** Compares this DataSection with another. Returns 0 if equal, > 0 if
	  * this DataSection is "greater than" the other and < 0 if this
	  * DataSection is "less than" the other.*/
	 public int compare(DataSection section) {
		 if(section == null) {
			 return 1;
		 }
		 
		 //compare label
		 int diff = sectionName().compareTo(section.sectionName());
		 if(diff != 0) {
			 return diff;
		 }
		 
		 //compare value
		 diff = asString(0).compareTo(section.asString(0));
		 if(diff != 0) {
			 return diff;
		 }
		 
		 //compare children
		 int numChildren = countChildren();
		 diff = numChildren - section.countChildren();
		 if(diff != 0) {
			 return diff;
		 }
		 
		 for(int i = 0; i < numChildren; i++) {
			 diff = openChild(i).compare(section.openChild(i));
			 if(diff != 0) {
				 return diff;
			 }
		 }
		 
		 return 0;
	 }
	 
	 private class SplitTagResult {
		 public DataSection section;
		 public String tag;
	 }
	 
	 /** Helper function used by the array read/write methods. */
	 private SplitTagResult splitTagPath(String tagPath, boolean makeNewSection) {
		 SplitTagResult ret = new SplitTagResult();
		 
		 int pos = tagPath.lastIndexOf('/');

		 // If the tagPath contains no path element, (just a tag),
		 // then the DataSection is ourselves.
		 if(pos < 0 || pos > tagPath.length()) {
			 ret.tag = tagPath;
			 ret.section = this;
		 }
		 else {
			 // Otherwise, find the section that matches the path.
			 ret.section = openSection(tagPath.substring(0, pos), makeNewSection);
			 if(ret.section != null) {
				 ret.tag = tagPath.substring(pos+1);
			 }
		 }

		 return ret;
	 }
}

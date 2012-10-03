package com.renegadeware.m8.gfx;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.renegadeware.m8.res.ManualResourceLoader;
import com.renegadeware.m8.res.Resource;
import com.renegadeware.m8.res.ResourceManager;

/**
 * A 2D rectangular mesh. Can be drawn textured or untextured.
 * This version is modified from the original Grid.java (found in
 * the SpriteText package in the APIDemos Android sample) to support hardware
 * vertex buffers and to insert edges between grid squares for tiling.
 */
public class Grid extends Resource {
	private static final int FLOAT_SIZE = 4;
	private static final int CHAR_SIZE = 2;
		
	public static final int COORD_SIZE = FLOAT_SIZE;
	public static final int COORD_TYPE = GL10.GL_FLOAT;
	
	private static int lastVertBufferIndex = 0;
	        
    private Buffer vertexBuffer;
    private Buffer texCoordBuffer;
    private CharBuffer indexBuffer;
    
    private int vertsCol;
    private int vertsRow;
    private int indexCount;
    private boolean useHardwareBuffers;
    
    // opengl id generated
    private int vertBufferIndex;
    private int indexBufferIndex;
    private int textureCoordBufferIndex;
    
    // these need to be set somewhere
    protected int numRow;
    protected int numCol;
    
    public Grid(int numCol, int numRow, ResourceManager creator, int id, String group) {
		super(creator, id, group, false, null);
		
		this.numRow = numRow;
		this.numCol = numCol;
	}
    
    public Grid(ResourceManager creator, int id, String group,
			boolean isManual, ManualResourceLoader loader) {
		super(creator, id, group, isManual, loader);
		
		//default to 1x1
		numRow = 1;
		numCol = 1;
	}
    
    public void set(int quadX, int quadY, float[][] positions, float[][] uvs) {
        if (quadX < 0 || quadX * 2 >= vertsCol) {
            throw new IllegalArgumentException("quadX");
        }
        if (quadY < 0 || quadY * 2 >= vertsRow) {
            throw new IllegalArgumentException("quadY");
        }
        if (positions.length < 4) {
            throw new IllegalArgumentException("positions");
        }
        if (uvs.length < 4) {
            throw new IllegalArgumentException("quadY");
        }

        int i = quadX * 2;
        int j = quadY * 2;
        
        setVertex(i, j, 		positions[0][0], positions[0][1], positions[0][2], uvs[0][0], uvs[0][1]);
        setVertex(i + 1, j, 	positions[1][0], positions[1][1], positions[1][2], uvs[1][0], uvs[1][1]);
        setVertex(i, j + 1, 	positions[2][0], positions[2][1], positions[2][2], uvs[2][0], uvs[2][1]);
        setVertex(i + 1, j + 1, positions[3][0], positions[3][1], positions[3][2], uvs[3][0], uvs[3][1]);
    }
    
    private void setVertex(int col, int row, float x, float y, float z, float u, float v) {
    	if (col < 0 || col >= vertsCol) {
    		throw new IllegalArgumentException("col");
    	}
    	if (row < 0 || row >= vertsRow) {
    		throw new IllegalArgumentException("row");
    	}

    	final int index = vertsCol * row + col;

    	final int posIndex = index * 3;
    	final int texIndex = index * 2;

    	final FloatBuffer vb = (FloatBuffer)vertexBuffer;
    	final FloatBuffer tb = (FloatBuffer)texCoordBuffer;

    	vb.put(posIndex, x);
    	vb.put(posIndex + 1, y);
    	vb.put(posIndex + 2, z);

    	tb.put(texIndex, u);
    	tb.put(texIndex + 1, v);
    }
    
    public static void beginDrawing(GL10 gl) {
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                        
        lastVertBufferIndex = 0;
                
        gl.glMatrixMode(GL10.GL_TEXTURE);
        //gl.glPushMatrix();
        gl.glLoadIdentity();
    }
    
    public static void endDrawing(GL10 gl) {
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
                
        //gl.glMatrixMode(GL10.GL_TEXTURE);
        //gl.glPopMatrix();
        
        lastVertBufferIndex = 0;
                        
        if (systemRegistry.contextParameters.supportsVBOs) {// gl instanceof GL11) {
        	final GL11 gl11 = (GL11)gl;
        	gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
    
    public void beginDrawingStrips(GL10 gl, boolean useTexture) {
        if (!useHardwareBuffers) {
            gl.glVertexPointer(3, COORD_TYPE, 0, vertexBuffer);
    
            if (useTexture) {
                gl.glTexCoordPointer(2, COORD_TYPE, 0, texCoordBuffer);
            } 
            
        } else {
            final GL11 gl11 = (GL11)gl;
            
            if(lastVertBufferIndex != vertBufferIndex) {
	            lastVertBufferIndex = vertBufferIndex;
	            
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertBufferIndex);
	            gl11.glVertexPointer(3, COORD_TYPE, 0, 0);
	            
	            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, textureCoordBufferIndex);
	            gl11.glTexCoordPointer(2, COORD_TYPE, 0, 0);
	            
	            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, indexBufferIndex);
            }
        }
    }
    
    public void endDrawingStrips(GL10 gl) {
    	if (useHardwareBuffers) {
    		// draw using hardware buffers
            final GL11 gl11 = (GL11)gl;
            
            gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
            gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
            
            lastVertBufferIndex = 0;
    	}
    }
    
    /** Assumes beginDrawingStrips() has been called before this. */
    public void drawStrip(GL10 gl, int startIndex, int indexCount) {
    	int count = indexCount;
    	if (startIndex + indexCount >= this.indexCount) {
    		count = this.indexCount - startIndex;
    	}
    	if (!useHardwareBuffers) {
            gl.glDrawElements(GL10.GL_TRIANGLES, count,
                    GL10.GL_UNSIGNED_SHORT, indexBuffer.position(startIndex));
        } else {
        	GL11 gl11 = (GL11)gl;
            gl11.glDrawElements(GL11.GL_TRIANGLES, count,
                    GL11.GL_UNSIGNED_SHORT, startIndex * CHAR_SIZE);
 
        }
    }
    
    /** Assumes beginDrawingStrips() has been called before this. */
    public void drawAllStrips(GL10 gl) {
    	
    	if (!useHardwareBuffers) {
            gl.glDrawElements(GL10.GL_TRIANGLES, indexCount,
                    GL10.GL_UNSIGNED_SHORT, indexBuffer);
        } else {
        	GL11 gl11 = (GL11)gl;
            gl11.glDrawElements(GL11.GL_TRIANGLES, indexCount,
                    GL11.GL_UNSIGNED_SHORT, 0);
 
        }
    }
                
    public boolean isUsingHardwareBuffers() {
        return useHardwareBuffers;
    }
    
    public boolean isBufferCreated() {
    	return vertexBuffer != null && texCoordBuffer != null;
    }
    
    @Override
	protected void prepareImpl() {
    	//allocate the buffers
    	if(numCol > 0 && numRow > 0 && !isBufferCreated()) {
	    	final int cols = numCol * 2;
	    	final int rows = numRow * 2;
	
	    	if (cols < 0 || cols >= 65536) {
	    		throw new IllegalArgumentException("quadsColumn");
	    	}
	    	if (rows < 0 || rows >= 65536) {
	    		throw new IllegalArgumentException("quadsRow");
	    	}
	    	if (cols * rows >= 65536) {
	    		throw new IllegalArgumentException("quadsColumn * quadsRow >= 32768");
	    	}
	
	    	useHardwareBuffers = false;
	
	    	vertsCol = cols;
	    	vertsRow = rows;
	    	int size = cols * rows;
	
	    	vertexBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 3)
	    	.order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	    	texCoordBuffer = ByteBuffer.allocateDirect(FLOAT_SIZE * size * 2)
	    	.order(ByteOrder.nativeOrder()).asFloatBuffer();
	
	
	    	int quadCount = numCol * numRow;
	
	    	indexCount = quadCount * 6;
	    	indexBuffer = ByteBuffer.allocateDirect(CHAR_SIZE * indexCount)
	    	.order(ByteOrder.nativeOrder()).asCharBuffer();
	
	    	/*
	    	 * Initialize triangle list mesh.
	    	 *
	    	 *     [0]------[1]   [2]------[3] ...
	    	 *      |    /   |     |    /   |
	    	 *      |   /    |     |   /    |
	    	 *      |  /     |     |  /     |
	    	 *     [w]-----[w+1] [w+2]----[w+3]...
	    	 *      |       |
	    	 *
	    	 */
	    	int i = 0;
	    	for (int y = 0; y < numRow; y++) {
	    		final int indexY = y * 2;
	    		for (int x = 0; x < numCol; x++) {
	    			final int indexX = x * 2;
	    			char a = (char) (indexY * vertsCol + indexX);
	    			char b = (char) (indexY * vertsCol + indexX + 1);
	    			char c = (char) ((indexY + 1) * vertsCol + indexX);
	    			char d = (char) ((indexY + 1) * vertsCol + indexX + 1);
	
	    			indexBuffer.put(i++, a);
	    			indexBuffer.put(i++, b);
	    			indexBuffer.put(i++, c);
	
	    			indexBuffer.put(i++, b);
	    			indexBuffer.put(i++, c);
	    			indexBuffer.put(i++, d);
	    		}
	    	}
    	}

    	vertBufferIndex = 0;
    }
    
    @Override
    protected void unprepareImpl() {
    	//clear out the buffers
    	vertexBuffer = null;
        texCoordBuffer = null;
        indexBuffer = null;
    }

	@Override
	protected void loadImpl() {
		//set the buffers to hw
		if (!useHardwareBuffers) {
			GL10 gl = OpenGLSystem.getGL();
			assert gl != null;
			
			if (gl instanceof GL11) {
				GL11 gl11 = (GL11)gl;
                int[] buffer = new int[1];
                
                // Allocate and fill the vertex buffer.
                gl11.glGenBuffers(1, buffer, 0);
                vertBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, vertBufferIndex);
                final int vertexSize = vertexBuffer.capacity() * COORD_SIZE; 
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, vertexSize, 
                        vertexBuffer, GL11.GL_STATIC_DRAW);
                
                // Allocate and fill the texture coordinate buffer.
                gl11.glGenBuffers(1, buffer, 0);
                textureCoordBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 
                        textureCoordBufferIndex);
                final int texCoordSize = 
                    texCoordBuffer.capacity() * COORD_SIZE;
                gl11.glBufferData(GL11.GL_ARRAY_BUFFER, texCoordSize, 
                        texCoordBuffer, GL11.GL_STATIC_DRAW);    
                
                // Unbind the array buffer.
                gl11.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);
                
                // Allocate and fill the index buffer.
                gl11.glGenBuffers(1, buffer, 0);
                indexBufferIndex = buffer[0];
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 
                        indexBufferIndex);
                // A char is 2 bytes.
                final int indexSize = indexBuffer.capacity() * 2;
                gl11.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, indexSize, indexBuffer, 
                        GL11.GL_STATIC_DRAW);
                
                // Unbind the element array buffer.
                gl11.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0);
                
                useHardwareBuffers = true;
                
                assert vertBufferIndex != 0;
                assert textureCoordBufferIndex != 0;
                assert indexBufferIndex != 0;
                assert gl11.glGetError() == 0;
			}
		}
	}

	@Override
	protected void unloadImpl() {
		//remove the buffers from hw
		if (useHardwareBuffers) {
			GL10 gl = OpenGLSystem.getGL();
			assert gl != null;

			if (gl instanceof GL11) {
				GL11 gl11 = (GL11)gl;
				int[] buffer = new int[1];
				
				if(vertBufferIndex != 0) {
					buffer[0] = vertBufferIndex;
					gl11.glDeleteBuffers(1, buffer, 0);
				}

				if(textureCoordBufferIndex != 0) {
					buffer[0] = textureCoordBufferIndex;
					gl11.glDeleteBuffers(1, buffer, 0);
				}

				if(indexBufferIndex != 0) {
					buffer[0] = indexBufferIndex;
					gl11.glDeleteBuffers(1, buffer, 0);
				}
			}

			//invalidate opengl stuff
			vertBufferIndex = 0;
			indexBufferIndex = 0;
			textureCoordBufferIndex = 0;
			useHardwareBuffers = false;
		}		
	}
	
	@Override
	protected void invalidateImpl() {
		vertBufferIndex = 0;
		indexBufferIndex = 0;
		textureCoordBufferIndex = 0;
	}

	@Override
	protected int calculateSize() {
		int capacity = 0;
		if(vertexBuffer != null) {
			capacity += vertexBuffer.capacity();
		}
		if(texCoordBuffer != null) {
			capacity += texCoordBuffer.capacity();
		}
		if(indexBuffer != null) {
			capacity += indexBuffer.capacity();
		}
		return capacity;
	}

}

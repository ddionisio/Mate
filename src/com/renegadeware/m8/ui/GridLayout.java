package com.renegadeware.m8.ui;

import com.renegadeware.m8.DebugLog;

/**
 * Column-Major grid
 * @author ddionisio
 *
 */
public class GridLayout extends BaseLayout {
	
	public int numRow;
	public int numCol;
	
	public float paddingX;
	public float paddingY;
	
	private BaseUI[][] grid;

	public GridLayout() {
	}
	
	public int getNumRow() {
		return numRow;
	}
	
	public int getNumCol() {
		return numCol;
	}
	
	public final int getRow(int index) {
		return index/numCol;
	}
	
	public final int getCol(int index) {
		return index%numCol;
	}
	
	public final int getIndex(int row, int col) {
		return (row*numCol) + col;
	} 
	
	public BaseUI getChild(int row, int col) {
		if(row >= 0 && row < numRow && col >= 0 && col < numCol) {
			return grid[row][col];
		}
		
		return null;
	}
	
	@Override
	protected void initImpl(int numChildren) {
		if(numRow > 0 && numCol > 0) {
			grid = new BaseUI[numRow][numCol];
		}
		else {
			DebugLog.e("GridLayout", "Invalid size value: row = "+numRow+", col = "+numCol);
		}
		setCapacity(numChildren);
	}
	
	@Override
	protected void resetImpl() {
		grid = null;
	}
	
	@Override
	protected void addChildImpl(BaseUI ui) {
		if(ui.index >= 0) {
			int r = getRow(ui.index);
			int c = getCol(ui.index);
			
			if(r >= 0 && r < numRow && c >= 0 && c < numCol) {
				grid[r][c] = ui;
			}
			else {
				DebugLog.e("GridLayout", "index value for "+ui.name+": row = "+r+", col = "+c);
			}
		}
		else {
			//find a suitable place
			for(int r = 0; r < numRow; r++) {
				for(int c = 0; c < numCol; c++) {
					if(grid[r][c] == null) {
						ui.index = getIndex(r, c);
						grid[r][c] = ui;
						return;
					}
				}
			}
		}
	}
	
	@Override
	protected void removeChildImpl(BaseUI ui) {
		if(ui.index >= 0) {
			int r = getRow(ui.index);
			int c = getCol(ui.index);
			
			if(r >= 0 && r < numRow && c >= 0 && c < numCol) {
				grid[r][c] = null;
			}
		}
	}
	
	@Override
	protected void refreshImpl(BaseUI parent) {
		final int count = children.getCount();
		if(count > 0) {
			float width = parent.getWidth();
			float height = parent.getHeight();
			
			float cellWidth = width/numCol;
			float cellHeight = height/numRow;
			
			final Object[] objectArray = children.getArray();
	        for (int i = 0; i < count; i++) {
	        	BaseUI ui = (BaseUI)objectArray[i];
	        	int row = getRow(ui.index);
	        	int col = getCol(ui.index);
	        	
	        	float x = col*cellWidth;
	        	float y = height - (row+1)*cellHeight;
	        	float w = ui.getWidth();
	        	float h = ui.getHeight();
	        	
	        	switch(ui.anchorH) {
	        	case BaseUI.ANCHOR_LEFT:
	        		ui.x = x + paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_RIGHT:
	        		ui.x = x + cellWidth - paddingX;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		ui.x = x + cellWidth*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		ui.x = x + paddingX;
	        		w = cellWidth - paddingX*2;
	        		break;
	        	}
	        	
	        	switch(ui.anchorV) {
	        	case BaseUI.ANCHOR_BOTTOM:
	        		ui.y = y + paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_TOP:
	        		ui.y = y + cellHeight - paddingY;
	        		break;
	        		
	        	case BaseUI.ANCHOR_CENTER:
	        		ui.y = y + cellHeight*0.5f;
	        		break;
	        		
	        	case BaseUI.ANCHOR_STRETCH:
	        		ui.y = y + paddingY;
	        		h = cellHeight - paddingY*2;
	        		break;
	        	}
	        	
	        	ui.resize(w, h);
	        }
		}
	}
}

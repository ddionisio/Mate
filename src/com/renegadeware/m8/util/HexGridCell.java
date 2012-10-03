package com.renegadeware.m8.util;

import android.util.FloatMath;

/**
 * Uniform hexagonal grid cell's metrics utility class.
 * 
 * @author Ruslan Shestopalyuk
 */
public class HexGridCell {
	// references to NEIGHBORS_DI and NEIGHBORS_DJ
	// +y = north, x+ = east
	public static final int NEIGHBOR_INVALID = -1;
	public static final int NEIGHBOR_SOUTH = 0;
	public static final int NEIGHBOR_SOUTH_EAST = 1;
	public static final int NEIGHBOR_NORTH_EAST = 2;
	public static final int NEIGHBOR_NORTH = 3;
	public static final int NEIGHBOR_NORTH_WEST = 4;
	public static final int NEIGHBOR_SOUTH_WEST = 5;
	
	private static final int[] NEIGHBORS_DI = { 0, 1, 1, 0, -1, -1 };
	private static final int[][] NEIGHBORS_DJ = { 
		{ -1, -1, 0, 1, 0, -1 }, { -1, 0, 1, 1, 1, 0 } };
	
	private int x = 0; // cell's left coordinate
	private int y = 0; // cell's top coordinate

	private int col = 0; // cell's horizontal grid coordinate
	private int row = 0; // cell's vertical grid coordinate
	
	public final int side;

	/**
	 * Cell radius (distance from center to one of the corners)
	 */
	public final int radius;
	/**
	 * Cell height
	 */
	public final int height;
	/**
	 * Cell width
	 */
	public final int width;

	public static final int NUM_NEIGHBORS = 6;
	
	public static final int calculateSide(int radius) {
		return radius * 3 / 2;
	}
	
	public static final int calculateHeight(int radius) {
		return (int) (((float) radius) * FloatMath.sqrt(3));
	}
	
	public static final int getOppositeDir(int dir) {
		switch(dir) {
		case NEIGHBOR_NORTH:
			return NEIGHBOR_SOUTH;
		case NEIGHBOR_NORTH_EAST:
			return NEIGHBOR_SOUTH_WEST;
		case NEIGHBOR_SOUTH_EAST:
			return NEIGHBOR_NORTH_WEST;
			
		case NEIGHBOR_SOUTH:
			return NEIGHBOR_NORTH;
		case NEIGHBOR_SOUTH_WEST:
			return NEIGHBOR_NORTH_EAST;
		case NEIGHBOR_NORTH_WEST:
			return NEIGHBOR_SOUTH_EAST;
		}
		
		return dir;
	}
		
	/**
	 * Calculates the width and height of a hex board.
	 * 
	 * @param radius The radius of each hex cell
	 * @param numColumn The number of columns across the board
	 * @param numRow The number of rows across the board
	 * @return an array representing the width and height [0] = width, [1] = height
	 */
	public static final int[] calculateBoardSize(int radius, int numColumn, int numRow) {
		int s = calculateSide(radius);
		int h = calculateHeight(radius);
		
		final int ret[] = {(int) (s*numColumn + radius*0.5), h*numRow};
		
		if(numColumn > 1) {
			ret[1] += h/2;
		}
		
		return ret;
	}
	
	/**
	 * Vertical grid coordinate for the given neighbor.
	 * @param col Reference x-index
	 * @param neighborIdx NEIGHBOR_
	 * @return Horizontal grid coordinate for the given neighbor.
	 */
	public static int getNeighborCol(int col, int neighborIdx) {
		return col + NEIGHBORS_DI[neighborIdx];
	}

	/**
	 * Vertical grid coordinate for the given neighbor.
	 * @param row Reference y-index
	 * @param col Reference x-index; needed to determine row offset 
	 * @param neighborIdx NEIGHBOR_
	 * @return Vertical grid coordinate for the given neighbor.
	 */
	public static int getNeighborRow(int row, int col, int neighborIdx) {
		return row + NEIGHBORS_DJ[col % 2][neighborIdx];
	}
	
	public static int getNeighborDir(int row, int col, int destRow, int destCol) {

		int difRow = destRow - row;
		int difCol = destCol - col;
		
		// check north or south
		if(difCol == 0) {
			if(difRow == -1) {
				return NEIGHBOR_SOUTH;
			}
			else if(difRow == 1) {
				return NEIGHBOR_NORTH;
			}
		}
		else {
			//check west side
			if(difCol == -1) {
				if(col % 2 == 0) {
					if(difRow == -1) {
						return NEIGHBOR_SOUTH_WEST;
					}
					else if(difRow == 0) {
						return NEIGHBOR_NORTH_WEST;
					}
				}
				else {
					if(difRow == 0) {
						return NEIGHBOR_SOUTH_WEST;
					}
					else if(difRow == 1) {
						return NEIGHBOR_NORTH_WEST;
					}
				}
			}
			//check east side
			else if(difCol == 1) {
				if(col % 2 == 0) {
					if(difRow == -1) {
						return NEIGHBOR_SOUTH_EAST;
					}
					else if(difRow == 0) {
						return NEIGHBOR_NORTH_EAST;
					}
				}
				else {
					if(difRow == 0) {
						return NEIGHBOR_SOUTH_EAST;
					}
					else if(difRow == 1) {
						return NEIGHBOR_NORTH_EAST;
					}
				}
			}
		}
		
		return NEIGHBOR_INVALID;
	}

	/**
	 * @param radius Cell radius (distance from the center to one of the corners)
	 */
	public HexGridCell(int radius) {
		this.radius = radius;
		width = radius * 2;
		height = calculateHeight(radius);
		side = calculateSide(radius);

		/*int cdx[] = { radius / 2, side, width, side, radius / 2, 0 };
		cornersDX = cdx;
		int cdy[] = { 0, 0, height / 2, height, height, height / 2 };
		cornersDY = cdy;*/
	}

	/**
	 * @return X coordinate of the cell's top left corner.
	 */
	public int getX() {
		return x;
	}

	/**
	 * @return Y coordinate of the cell's top left corner.
	 */
	public int getY() {
		return y;
	}

	/**
	 * @return X coordinate of the cell's center
	 */
	public int getCenterX() {
		return x + radius;
	}

	/**
	 * @return Y coordinate of the cell's center
	 */
	public int getCenterY() {
		return y + height / 2;
	}

	/**
	 * @return Horizontal grid coordinate for the cell.
	 */
	public int getCol() {
		return col;
	}

	/**
	 * @return Vertical grid coordinate for the cell.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * @return Horizontal grid coordinate for the given neighbor.
	 */
	public int getNeighborCol(int neighborIdx) {
		return col + NEIGHBORS_DI[neighborIdx];
	}

	/**
	 * @return Vertical grid coordinate for the given neighbor.
	 */
	public int getNeighborRow(int neighborIdx) {
		return row + NEIGHBORS_DJ[col % 2][neighborIdx];
	}
	
	public int getNeighborDir(int destRow, int destCol) {
		return getNeighborDir(row, col, destRow, destCol);
	}

	/**
	 * Computes X and Y coordinates for all of the cell's 6 corners, counter-clockwise,
	 * starting from the bottom left.
	 * 
	 * @param cornersX Array to fill in with X coordinates of the cell's corners
	 * @param cornersX Array to fill in with Y coordinates of the cell's corners
	 */
	public void computeCorners(int[] cornersX, int[] cornersY) {
		cornersX[0] = x + radius / 2;
		cornersY[0] = y;
		cornersX[1] = x + side;
		cornersY[1] = cornersY[0];
		cornersX[2] = x + width;
		cornersY[2] = y + height / 2;
		cornersX[3] = cornersX[1];
		cornersY[3] = y + height;
		cornersX[4] = cornersX[0];
		cornersY[4] = cornersY[3];
		cornersX[5] = x;
		cornersY[5] = cornersY[2];
		/*final int cdx[] = { radius / 2, side, width, side, radius / 2, 0 };

		final int cdy[] = { 0, 0, height / 2, height, height, height / 2 };

		for (int k = 0; k < NUM_NEIGHBORS; k++) {
			cornersX[k] = x + cdx[k];
			cornersY[k] = y + cdy[k];
		}*/
	}
	
	public void computeCornersRelative(int[] cornersX, int[] cornersY) {
		cornersX[0] = radius / 2;
		cornersY[0] = 0;
		cornersX[1] = side;
		cornersY[1] = cornersY[0];
		cornersX[2] = width;
		cornersY[2] = height / 2;
		cornersX[3] = cornersX[1];
		cornersY[3] = height;
		cornersX[4] = cornersX[0];
		cornersY[4] = cornersY[3];
		cornersX[5] = 0;
		cornersY[5] = cornersY[2];
	}

	/**
	 * Sets the cell's horizontal and vertical grid coordinates.
	 */
	public void setCellIndex(int col, int row) {
		this.col = col;
		this.row = row;
		x = col * side;
		y = height * (2 * row + (col % 2)) / 2;
	}

	/**
	 * Sets the cell as corresponding to some point inside it (can be used for
	 * e.g. mouse picking).
	 */
	public void setCellByPoint(int x, int y) {
		int ci = (int)FloatMath.floor((float)x/(float)side);
		int cx = x - side*ci;

		int ty = y - (ci % 2) * height / 2;
		int cj = (int)FloatMath.floor((float)ty/(float)height);
		int cy = ty - height*cj;

		if (cx > Math.abs(radius / 2 - radius * cy / height)) {
			setCellIndex(ci, cj);
		} else {
			setCellIndex(ci - 1, cj + (ci % 2) - ((cy < height / 2) ? 1 : 0));
		}
	}
}

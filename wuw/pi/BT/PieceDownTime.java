/**
 * PieceDownTime.java
 * 
 * Copyright 2012
 * This file is part of the WUW (What Users Want) service.
 * 
 * WUW is free software: you can redistribute it and/or modify it under the terms 
 * of the GNU General Public License as published by the Free Software Foundation, 
 * either version 3 of the License, or (at your option) any later version.
 * 
 * WUW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Foobar. 
 * If not, see http://www.gnu.org/licenses/.
 */

package wuw.pi.BT;

/**
 * This class contains information about the started and download times for each 
 * BitTorretn piece
 * @author carvajal-r
 *
 */
class PieceDownTime {

private int piece;
private float startDownTime;
private float endDownTime;


PieceDownTime(int piece, float startDownTime, float endDownTime) {
  this.piece = piece;
  this.startDownTime = startDownTime;
  this.endDownTime = endDownTime;
}


int getPiece() {
  return piece;
}


float getStartDownTime() {
  return startDownTime;
}


float getEndDownTime() {
  return endDownTime;
}


}

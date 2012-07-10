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

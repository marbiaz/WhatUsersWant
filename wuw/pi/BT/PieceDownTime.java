package wuw.pi.BT;


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

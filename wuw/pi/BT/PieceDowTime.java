package wuw.pi.BT;


public class PieceDowTime {

private int piece;
private float startDownTime;
private float endDownTime;


public PieceDowTime(int piece, float startDownTime, float endDownTime) {
  this.piece = piece;
  this.startDownTime = startDownTime;
  this.endDownTime = endDownTime;
}


public int getPiece() {
  return piece;
}


public float getStartDownTime() {
  return startDownTime;
}


public float getEndDownTime() {
  return endDownTime;
}


}

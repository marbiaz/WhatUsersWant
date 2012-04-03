

package wuw.ui;


/**
 * This class implements a set of measures which are used to quantify feedback
 * values used to evaluate the system.
 * 
 * @author Adriana Perez-Espinosa
 * @date 2012 Mar 23
 */
public class Feedback {

public String[] measures;
public double[] values;


public Feedback(String[] measure, double[] values) {
  this.measures = measure;
  this.values = values;
}


public int existMeasure(String measure) {// FIXME: 
  for (int i = 0; i < measures.length; i++) {
    if (measures[i].equals(measure)) {
      return i;
    }
  }
  return -1;
}


public void setValue(int index, double value) { //FIXME: do not expose index!!! search by measure name...
  values[index] = value;
}


public String getMeasure() { //FIXME: this should be in WebUIHandler or WebUIComm....
  String feedback = "";
  for (int i = 0; i < measures.length; i++) {
    feedback = feedback + "&" + measures[i] + "&" + values[i];
  }
  return feedback;
}


}

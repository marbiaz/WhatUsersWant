

package wuw.ui.WebUI;


/**
 * This class implements a set of measures which are used to quantify feedback
 * values used to evaluate the system.
 * 
 * @author Adriana Perez-Espinosa
 * @date 2012 Mar 23
 */
class Feedback {

String[] measures;
double[] values;


Feedback(String[] measure, double[] values) {
  this.measures = measure;
  this.values = values;
}


int existMeasure(String measure) {
  for (int i = 0; i < measures.length; i++) {
    if (measures[i].equals(measure)) {
      return i;
    }
  }
  return -1;
}


void setValue(int index, double value) {
  values[index] = value;
}


String getMeasure() {
  String feedback = "";
  for (int i = 0; i < measures.length; i++) {
    feedback = feedback + "&" + measures[i] + "&" + values[i];
  }
  return feedback;
}


}

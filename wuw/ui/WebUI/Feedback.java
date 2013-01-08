/**
 * Feedback.java
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
 * You should have received a copy of the GNU General Public License along with WUW. 
 * If not, see http://www.gnu.org/licenses/.
 */

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

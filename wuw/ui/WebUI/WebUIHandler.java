/**
 * WebUIHandler.java
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

package wuw.ui.WebUI;


import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;

import wuw.ui.Preference;
import wuw.ui.UIHandler;


/**
 * An instance of this class represent the list of the preferences for one
 * content.
 * 
 * @author Adriana Perez-Espinosa
 * @date 2012 Feb 15
 */
public class WebUIHandler implements UIHandler {


/**
 * Each list of preferences is mapped against the unique ID of a content.
 */
LinkedHashMap<String, LinkedList<Preference>> preferences;
LinkedHashMap<String, Feedback> feedback;


public WebUIHandler() {
  preferences = new LinkedHashMap<String, LinkedList<Preference>>();
  feedback = new LinkedHashMap<String, Feedback>();
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.ui.UIHandler#initPref(java.lang.String, java.lang.String, double,
 * double, double, double)
 */
public void initPref(String contentID, String pref, double start, double end, double step,
    double defaultValue) {
  Preference item = new Preference(pref, start, end, step, defaultValue);
  getContents(contentID).add(item);
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.ui.UIHandler#initPref(java.lang.String, java.lang.String,
 * java.lang.Object[], java.lang.Object)
 */
@Override
public void initPref(String contentID, String pref, Object[] values, Object defaultValue) {
  Preference item = new Preference(pref, values, defaultValue);
  getContents(contentID).add(item);
}


/*
 * (non-Javadoc)
 * 
 * @see wuw.ui.UIHandler#setPref(java.lang.String, java.lang.String,
 * java.lang.Object)
 */
public void setPref(String contentID, String pref, Object value) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    while (itr.hasNext()) {
      aux = itr.next();
      if (aux.getName().equals(pref)) {
        aux.setValue(value);
      }
    }
  } else {
/**/System.err.println("WebUIHandler : ERROR : attempting to set a preference in an"
        + " uninitialized preference list (content ID = " + contentID + ")!!!");
    System.err.flush();
  }

}


/*
 * (non-Javadoc)
 * 
 * @see wuw.ui.UIHandler#getPrefValue(java.lang.String, java.lang.String)
 */
public Object getPrefValue(String contentID, String pref) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Object value = null;
    Preference aux;
    while (itr.hasNext()) {
      aux = itr.next();
      if (aux.getName().equals(pref)) {
        value = aux.getValue();
      }
    }
    return value;
  } else {
/**/System.err.println("WebUIHandler : ERROR : attempting to get a value from an"
        + " uninitialized preference list (content ID = " + contentID + ")!!!");
    System.err.flush();
    return null;
  }
}


/*
 * (non-Javadoc)
 * 
 * @see UIHandler#initFeedback(java.lang.String[], double[])
 */
public void initFeedback(String contentID, String[] measures, double[] values) {
  if (measures.length == values.length) {
    Feedback instance = new Feedback(measures, values);
    feedback.put(contentID, instance);
  } else {
    System.err.println("WebUIHandler : ERROR while intitializing feedback : arguments size mismatch.");
    System.err.flush();
  }
}


/*
 * (non-Javadoc)
 * 
 * @see UIHandler#setFeedback(java.lang.String, double)
 */
public void setFeedback(String contentID, String measure, double value) {
  if (feedback.containsKey(contentID)) {
    Feedback aux;
    aux = feedback.get(contentID);
    if (aux.existMeasure(measure) != -1) {
      aux.setValue(aux.existMeasure(measure), value);
    } else {
      System.err.println("WebUIHandler : ERROR : attempting to set an"
          + " uninitialized measure (measure = " + measure + ")!!!");
      System.err.flush();
    }
  } else {
    System.err.println("WebUIHandler : ERROR : attempting to set in an"
        + " uninitialized Feedback (content ID = " + contentID + ")!!!");
    System.err.flush();
  }

}


// /**
// *
// * @param contentID
// * @param pref
// * @return
// */
// public double getPrefStart(String contentID, String pref) {
// LinkedList<Preference> list = preferences.get(contentID);
// if (list != null) {
// ListIterator<Preference> itr = list.listIterator();
// double start = 0;
// Preference aux;
// while (itr.hasNext()) {
// aux = itr.next();
// if (aux.getName().equals(pref)) {
// start = aux.getStart();
// }
// }
// return start;
// } else {
// /**/System.err.println("WebUIHandler : ERROR : attempting to get a value from an"
// + " uninitialized preference list (content ID = " + contentID + ")!!!");
// return Double.NaN;
// }
// }


/**
 * Function called to initialize the form in the Web Page.
 * 
 * @param contentID
 * @return String with a specific format that contains the name and value of
 *         preferences used By Default Format of the String is "#" is a divider
 *         #NumberPrefereces#Preference1#Preference2#Preference3#.....# Each
 *         Preferences is integrate for 1)
 *         NamePreference,start,end,step,valueDefault 2) NamePreferences,Values
 *         that it can set to, valueDefault each data is separate for the
 *         character "&"
 */
String getPreferences(String contentID) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    int numPref = list.size();
    String answerRequest = "#" + String.valueOf(numPref);
    while (itr.hasNext()) {
      aux = itr.next();
      if (aux.getValues() == null) {
        answerRequest = answerRequest + "#" + aux.getValues() + "&" + aux.getName() + "&"
            + aux.getStart() + "&" + aux.getEnd() + "&" + aux.getStep() + "&";
      } else {
        answerRequest = answerRequest + "#" + aux.getValues() + "&" + aux.getName() + "&"
            + aux.getValues().length + "&";
        for (int i = 0; i < aux.getValues().length; i++) {
          answerRequest = answerRequest + aux.getValues()[i] + "&";
        }

      }
      answerRequest = answerRequest + aux.getValue();
    }
    return answerRequest;
  } else {
/**/System.err.println("WebUIHandler : ERROR : attempting to get a value from an"
        + " uninitialized preference list (content ID = " + contentID + ")!!!");
    return "";
  }
}


/**
 * 
 * @param contentID
 */
void reset(String contentID) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    while (itr.hasNext()) {
      aux = itr.next();
      aux.setValue(aux.getDefaultValue());
    }
  } else {
/**/System.err.println("WebUIHandler : ERROR : attempting to reset preferences in an"
        + " uninitialized preference list (content ID = " + contentID + ")!!!");
  }
}


/**
 * Display the list of Preferences of one Content print the name of preferences,
 * its default value and its value set up.
 */
void displayPreferences(String contentID) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    while (itr.hasNext()) {
      aux = itr.next();
/**/  System.out.println("Name:" + aux.getName() + "; Default value = " + aux.getDefaultVal()
          + "; Current value = " + aux.getValue() + ".");

    }
  } else {
/**/System.out.println("Uninitialized list.");
  }
}


///**
// * @param id
// *          Id of the content
// * @param preferenceList
// *          List of preferences associated to a content
// */
//void addContent(String id, LinkedList<Preference> prefList) {
//  // FIXME : overrides previous list without notifying...
//  preferences.put(id, prefList);
//}


/**
 * @param ID
 *          Identifier of the content to remove
 */
void removeContent(String ID) {
  preferences.remove(ID);
  feedback.remove(ID);
}


void removeAll() {
  if (!preferences.isEmpty()) {
    preferences.clear();
  }
  if (!feedback.isEmpty()) {
    feedback.clear();
  }
}


/**
 * Show the id of the contents in the list
 */
void displayContent() {
  Set<String> keys = preferences.keySet();
  System.out.println("List of Content");
  if (keys.isEmpty()) {
    System.out.println("Empty!");
    return;
  }
  Iterator<String> itrContent = keys.iterator();
  while (itrContent.hasNext()) {
    System.out.println("Content:" + itrContent.next());
  }
}


/**
 * @param ID
 *          Identifier of the content
 * @return true if the content exist in the list false if don't exist
 */
boolean existContent(String ID) {
  return preferences.containsKey(ID) && feedback.containsKey(ID);
}


/**
 * @param idContent
 *          Id of Content
 */
String getFeeback(String idContent) {
  String FeedBack = idContent;
  Feedback aux = feedback.get(idContent);
  FeedBack = FeedBack + aux.getMeasure();
  return FeedBack;
}


private LinkedList<Preference> getContents(String contentID) {
  if (!preferences.containsKey(contentID)) {
    LinkedList<Preference> list = null;
    list = new LinkedList<Preference>();
    preferences.put(contentID, list);
    return list;
  }
  return preferences.get(contentID);
}

}

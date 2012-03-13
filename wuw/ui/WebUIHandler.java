

package wuw.ui;

import java.util.*;


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


WebUIHandler() {
  preferences = new LinkedHashMap<String, LinkedList<Preference>>();
}


private LinkedList<Preference> getContentList(String contentID) {
  if (!preferences.containsKey(contentID)) {
    LinkedList<Preference> list = null;
    list = new LinkedList<Preference>();
    preferences.put(contentID, list);
    return list;
  }
  return preferences.get(contentID);
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
  getContentList(contentID).add(item);
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
  getContentList(contentID).add(item);
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
    return null;
  }
}


/*
 * (non-Javadoc)
 *
 * @see UIHandler#initFeedback(java.lang.String[], double[])
 */
public void initFeedback(String contentID, String[] measures, double[] values) {
  // TODO Auto-generated method stub

}


/*
 * (non-Javadoc)
 *
 * @see UIHandler#setFeedback(java.lang.String, double)
 */
public void setFeedback(String contentID, String measures, double value) {
  // TODO Auto-generated method stub

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
public String getPreferences(String contentID) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    int numPref = preferences.size();
    String answerRequest = String.valueOf(numPref) + "#";
    while (itr.hasNext()) {
      aux = itr.next();
      if (aux.getValues() == null) {
        answerRequest = answerRequest + aux.getName() + "&" + aux.getStart() + "&" + aux.getEnd()
            + "&" + aux.getStep() + "&";
      } else {
        answerRequest = answerRequest + "&" + aux.getName() + "&" + aux.getValues().length + "&";
        for (int i = 0; i < aux.getValues().length; i++) {
          answerRequest = answerRequest + aux.getValues()[i] + "&";
        }

      }
      answerRequest = answerRequest + aux.getValue() + "&" + "#";
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
public void reset(String contentID) {
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
public void displayListPreferences(String contentID) {
  LinkedList<Preference> list = preferences.get(contentID);
  if (list != null) {
    ListIterator<Preference> itr = list.listIterator();
    Preference aux;
    while (itr.hasNext()) {
      aux = itr.next();
      System.out.println("Name:" + aux.getName() + "; Default value = " + aux.getDefaultVal()
          + "; Current value = " + aux.getValue() + ".");

    }
  } else {
/**/System.out.println("Uninitialized list.");
  }
}


/**
 * @param id
 *          Id of the content
 * @param preferenceList
 *          List of preferences associated to a content
 */
public void AddContent(String id, LinkedList<Preference> prefList) {
  // FIXME : overrides previous list without notifying...
  preferences.put(id, prefList);
}


/**
 * @param ID
 *          Identifier of the content to remove
 */
public void removeNodeContent(String ID) {
  // FIXME: after this, containskey(ID) = true, and getValue(ID) = null
  preferences.remove(ID);
}


/**
 * Show the id of the contents in the list
 */
public void DisplayLisContent() {
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
public boolean existContent(String ID) {
  return preferences.containsKey(ID);
}

}

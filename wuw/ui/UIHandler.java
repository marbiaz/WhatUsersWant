

package wuw.ui;


/**
 * This interface defines the services exported by WUW's User Interface module.
 * 
 * @author Marco Biazzini
 * @date 2012 Jan 30
 */
public interface UIHandler {

/**
 * Initialize a numeric preference by specifying its validity range and the step
 * at which possible values must be set.
 * 
 * @param pref
 *          The (unique) name of the preference
 * @param start
 *          The lowest value the preference can be set to
 * @param end
 *          The highest value the preference can be set to
 * @param step
 *          The distance between two subsequent values that the preference can
 *          be set to
 */
public void initPref(String pref, double start, double end, double step);


/**
 * Initialize a preference by specifying all possible values it can be set to.
 * 
 * @param pref
 *          The (unique) name of the preference
 * @param values
 *          The values the preference can be set to
 */
public void initPref(String pref, Object[] values);


/**
 * Set a preference to a given value.
 * 
 * @param pref
 *          The name of the preference to be set
 * @param value
 *          The value the preference is to be set to
 */
public void setPref(String pref, Object value);


/**
 * Retrieve the value of a given preference.
 * 
 * @param pref
 *          The name of the preference
 * @return The value of the preference
 */
public Object getPrefValue(String pref);


/**
 * Initialize the measures displayed to the users as a feedback of the ongoing
 * computation.
 * 
 * @param measures
 *          The names of the measures
 * @param values
 *          The initial values of the measures, whose order matches the
 *          measures' names in <code>measures</code>
 */
public void initFeedback(String[] measures, double[] values);


/**
 * Set a single feedback measure to a given value.
 * 
 * @param measure
 *          The name of the measure to be set
 * @param value
 *          The value to be set
 */
public void setFeedback(String measure, double value);

}

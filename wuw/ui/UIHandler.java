

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
 * @param contentID
 *          The ID of the content which the preference refers to
 * @param pref
 *          The (unique) name of the preference
 * @param start
 *          The lowest value the preference can be set to
 * @param end
 *          The highest value the preference can be set to
 * @param step
 *          The distance between two subsequent values that the preference can
 *          be set to
 * @param defaultValue
 *          The default value of the preference
 */
public void initPref(String contentID, String pref, double start, double end, double step,
    double defaultValue);


/**
 * Initialize a preference by specifying all possible values it can be set to.
 *
 * @param contentID
 *          The ID of the content which the preference refers to
 * @param pref
 *          The (unique) name of the preference
 * @param values
 *          The values the preference can be set to
 * @param defaultValue
 *          The value by Default of the preference
 */
public void initPref(String contentID, String Pref, Object[] values, Object defaultValue);


/**
 * Set a preference to a given value.
 *
 * @param contentID
 *          The ID of the content which the preference refers to
 * @param pref
 *          The name of the preference to be set
 * @param value
 *          The value the preference is to be set to
 */
public void setPref(String contentID, String pref, Object value);


/**
 * Retrieve the value of a given preference.
 *
 * @param contentID
 *          The ID of the content which the preference refers to
 * @param pref
 *          The name of the preference
 * @return The value of the preference
 */
public Object getPrefValue(String contentID, String pref);


/**
 * Initialize the measures displayed to the users as a feedback of the ongoing
 * computation.
 *
 * @param contentID
 *          The ID of the content which the measures refer to
 * @param measures
 *          The names of the measures
 * @param values
 *          The initial values of the measures, whose order matches the
 *          measures' names in <code>measures</code>
 */
public void initFeedback(String contentID, String[] measures, double[] values);


/**
 * Set a single feedback measure to a given value.
 *
 * @param contentID
 *          The ID of the content which the measure refers to
 * @param measure
 *          The name of the measure to be set
 * @param value
 *          The value to be set
 */
public void setFeedback(String contentID, String measure, double value);

}

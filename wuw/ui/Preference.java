

package wuw.ui;

/**
 * A instance of this class identify a one Preference
 *
 * @author Adriana Perez-Espinosa
 * @date 2012 Feb 15
 */
public class Preference {

/**
 * @uml.property name="name"
 */
private final String name;
private Object value;
private final Object defaultValue;
private final double start;
private final double end;
private final double step;
private final Object[] values;


/**
 * Constructor of a preference whose value belongs to a continuous range of
 * values
 *
 * @param name
 *          The (unique) name of the preference
 * @param start
 *          The lowest value the preference can be set to
 * @param end
 *          The highest value the preference can be set to
 * @param step
 *          The distance between two subsequent values that the preference can
 *          be set to
 * @param defaultValue
 *          The value by default of the preference
 */
public Preference(String name, double start, double end, double step, double defaultValue) {
  this.name = name;
  this.start = start;
  this.end = end;
  this.step = step;
  this.value = defaultValue;
  this.defaultValue = defaultValue;
  this.values = null;
}


/**
 * @param name
 *          the (unique) name of the preferences
 * @param values
 *          The values the preference can be set to
 * @param defaultValue
 *          The value of the preference by default
 */
public Preference(String name, Object[] values, Object defaultValue) {
  this.name = name;
  this.values = values;
  this.value = defaultValue;
  this.defaultValue = defaultValue;
  this.start = this.end = this.step = 0;
}


/**
 * @param value
 *          Set the value for the preference
 * @uml.property name="value"
 */
public synchronized void setValue(Object value) {
  this.value = value;
}


/**
 * @return the value of the preference
 */
public synchronized Object getValue() {
  return this.value;
}


/**
 * @return the default value of the preference
 */
public synchronized Object getDefaultValue() {
  return this.defaultValue;
}


/**
 * @return the value of the preference
 */
public synchronized Object[] getValues() {
  return this.values;
}


/**
 * @return the name of the preference
 * @uml.property name="name"
 */
public String getName() {
  return this.name;
}


/**
 * @return the lowest value the preference can be set to
 */
public double getStart() {
  return this.start;
}


/**
 * @return The highest value the preference can be set to
 **/
public double getEnd() {
  return this.end;
}


/**
 * @return The distance between two subsequent values that the preference can be
 *         set to
 */
public double getStep() {
  return this.step;
}


/**
 * @return The value by default of the preference
 */
public Object getDefaultVal() {
  return this.defaultValue;
}

}

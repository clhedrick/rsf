/*
 * Created on 21 Sep 2006
 */
package uk.org.ponder.rsf.evolvers;

import java.util.Date;

import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;

/** The interface to a family of evolvers which allow the input of a date
 * value.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public interface DateInputEvolver {
  /** Construct a date input control, with the binding information specified by 
   * the supplied UIInput, and the initial value supplied as <code>value</code>.
   */
  public UIJointContainer evolveDateInput(UIInput toevolve, Date value);
  /** Construct a date input control, with the initial value taken from the binding
   * information in the supplied UIInput.
   */
  public UIJointContainer evolveDateInput(UIInput toevolve);
}

/*
 * Created on Nov 17, 2005
 */
package uk.org.ponder.rsf.components;

public class UIELBinding extends UIParameter {
  /** The "target" of this binding - an EL bean path that will be written
   * when this binding is received. Includes #{}.
   */
  public String valuebinding;
  /** The r-value of this binding is another EL that will be read from. Only
   * set one out of this field and "objrvalue". Includes #{}.
   */ 
  public String elrvalue;
  /** The r-value of this binding is an Object which will be serialised and
   * reconstructed when the binding is to be applied. Set only one out of this
   * field and <code>elrvalue</code>
   */
  public Object objrvalue;
}

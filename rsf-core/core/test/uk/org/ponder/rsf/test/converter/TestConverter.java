/*
 * Created on 8 Jan 2008
 */
package uk.org.ponder.rsf.test.converter;

import org.junit.Assert;

import uk.org.ponder.rsf.bare.ActionResponse;
import uk.org.ponder.rsf.bare.RenderResponse;
import uk.org.ponder.rsf.bare.ViewWrapper;
import uk.org.ponder.rsf.bare.junit.MultipleRSFTests;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/** Test cases for DataConverter-related issues RSF-47, RSF-57 and RSF-82
 */

public class TestConverter extends MultipleRSFTests {
  
  public TestConverter() {
    contributeRequestConfigLocation("classpath:uk/org/ponder/rsf/test/converter/converter-request-context.xml");
    contributeConfigLocation("classpath:uk/org/ponder/rsf/test/converter/converter-application-context.xml");
  }

  
  public void testBooleanLOR() {
    RenderResponse response = getRequestLauncher().renderView(new SimpleViewParameters(TestProducer3.VIEW_ID));
    ViewWrapper wrapper = response.viewWrapper;
    
    UIForm form = (UIForm) wrapper.queryComponent(new UIForm());
    ActionResponse response2 = getRequestLauncher().submitForm(form, null);
    StringHolder holder = (StringHolder) response2.requestContext.locateBean("stringHolder");
    Assert.assertEquals("effectiveValue", holder.string);
    
    UIBoundBoolean check = (UIBoundBoolean) wrapper.queryComponent(new UIBoundBoolean());
    check.setValue(false);

    ActionResponse response3 = getRequestLauncher().submitForm(form, null);
    StringHolder holder2 = (StringHolder) response3.requestContext.locateBean("stringHolder");
    Assert.assertNull(holder2.string);
    
  }
  
  
  /** Test for DataConverter issue RSF-47, reported in forums at
  * http://ponder.org.uk/rsf/posts/list/183.page
  *
  *<br/>This test case also demonstrates more advanced usage of RSF "full-cycle"
  * tests. It performs 3 full request cycles, firstly one render cycle and then
  * two action cycles, and also demonstrates use of ComponentQueries, modification
  * and submission of a pure component tree.
  */
  
  public void testConverterInapplicable() {
    // Fire an initial request to render the view from TestProducer.java
    RenderResponse response = getRequestLauncher().renderView();
    ViewWrapper wrapper = response.viewWrapper;
    
    UIForm form = (UIForm) wrapper.queryComponent(new UIForm());
    UICommand command = (UICommand) wrapper.queryComponent(new UICommand());
    
    DateHolder defaultHolder = new DateHolder();
    
    // Fire an action request to submit the form with no changes. The UIInput
    // will be submitted with no changes, thus forming an "Inapplicable Value".
    ActionResponse result = getRequestLauncher().submitForm(form, command);
    DateHolder dateHolder = (DateHolder) result.requestContext.locateBean("dateHolder");
    
    Assert.assertEquals(dateHolder.date, defaultHolder.date);
    
    // Now test the correct operation of the DataConverter (DateConverter) in
    // parsing this simulated user input into the required Date object, in a 3rd
    // request.
    String testDate = "2005-05-03"; 
    
    UIInput input = (UIInput) wrapper.queryComponent(new UIInput());
    input.setValue(testDate);
    
    ActionResponse result2 = getRequestLauncher().submitForm(form, command);
    DateHolder dateHolder2 = (DateHolder) result2.requestContext.locateBean("dateHolder");
  
    DateConverter converter = new DateConverter();
    Assert.assertEquals(dateHolder2.date, converter.parse(testDate));
  }
  
  /** Test for RSF-57. The condition is that the form should submit correctly without
   * an attempt to trigger the "ExplosiveConverter".
   */
  // TODO: construct tests for various other DataConverter matching edge cases - 
  // type-based matches and those which expire incomplete etc.
  public void testConverterPathMatch() {
    RenderResponse response = getRequestLauncher().renderView(new SimpleViewParameters(TestProducer2.VIEW_ID));
    ViewWrapper wrapper = response.viewWrapper;
    
    UIForm form = (UIForm) wrapper.queryComponent(new UIForm());
    ActionResponse response2 = getRequestLauncher().submitForm(form, null);
    assertActionError(response2, false);
  }

  
}

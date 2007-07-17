/*
 * Created on Aug 5, 2005
 */
package uk.org.ponder.rsf.processor;

import java.net.MalformedURLException;

import uk.org.ponder.errorutil.ConfigurationException;
import uk.org.ponder.errorutil.CoreMessages;
import uk.org.ponder.errorutil.PermissionException;
import uk.org.ponder.errorutil.ThreadErrorState;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.rsf.flow.errors.SilentRedirectException;
import uk.org.ponder.rsf.flow.errors.ViewExceptionStrategy;
import uk.org.ponder.rsf.state.ErrorStateManager;
import uk.org.ponder.rsf.viewstate.AnyViewParameters;
import uk.org.ponder.rsf.viewstate.ErrorViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.streamutil.write.PrintOutputStream;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * This request-scope bean exists to bracket the creation and operation of the 
 * rendering process, such that any "first" exception causes a redirect to a default
 * view. A "double fault", caught above in the RootHandlerBean, 
 * will render a fatal error message in the raw.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class RenderHandlerBracketer {
  private ViewExceptionStrategy ves;
  private ErrorStateManager errorstatemanager;
  private AnyViewParameters anyviewparams;
  private RenderHandler renderhandler;
  private boolean redirectlevel1 = true;

  public void setViewExceptionStrategy(ViewExceptionStrategy ves) {
    this.ves = ves;
  }
  
  public void setRedirectOnLevel1Error(boolean redirectlevel1) {
    this.redirectlevel1 = redirectlevel1;
  }
  
  public void setErrorStateManager(ErrorStateManager errorstatemanager) {
    this.errorstatemanager = errorstatemanager;
  }
  
  public void setViewParameters(AnyViewParameters viewparams) {
    this.anyviewparams = viewparams;
  }
  // This is a lazy dependency
  public void setRenderHandler(RenderHandler renderhandler) {
    this.renderhandler = renderhandler;
  }

  /** The beanlocator is passed in to allow the late location of the 
   * GetHandlerImpl bean which needs to occur in a controlled exception context.
   */
  public AnyViewParameters handle(PrintOutputStream pos) {
    // An interceptor may have issued a redirect.
    if (!(anyviewparams instanceof ViewParameters)) {
      return anyviewparams;
    }
    ViewParameters viewparams = (ViewParameters) anyviewparams;
    boolean iserrorredirect = viewparams.errorredirect != null;
    // YES, reach into the original request! somewhat bad...
    viewparams.errorredirect = null;
    try {
      if (viewparams instanceof ErrorViewParameters) {
        throw UniversalRuntimeException.accumulate(new MalformedURLException(), 
            "Request for bad view with ID " + viewparams.viewID + " ");
      }
      renderhandler.handle(pos);
    }
    catch (Exception e) {
      // if a request comes in for an invalid view, redirect it onto a default
      ViewParameters redirect = handleLevel1Error(viewparams, e,
          iserrorredirect);
      return redirect;
    }
    finally {
      errorstatemanager.requestComplete();
    }
    // if an exception escapes this block, handler will externally call
    // renderFatalError
    return null;
  }

  // a "Level 1" GET error simply attempts to redirect onto a default
  // view, with errors intact.
  public ViewParameters handleLevel1Error(ViewParameters viewparams,
      Exception e, boolean iserrorredirect) {
    UniversalRuntimeException invest = UniversalRuntimeException.accumulate(e);
    Throwable target = invest.getTargetException();
    boolean issilent = target != null && target instanceof SilentRedirectException; 
    if (!issilent) {
      Logger.log.warn("Exception rendering view: ", e);
    }
   
    if (target != null) {
      Logger.log.warn("Got target exception of " + target.getClass());
    }

    if (target instanceof ConfigurationException || target instanceof Error
        || iserrorredirect || !redirectlevel1) {
      throw invest;
    }

    ViewParameters defaultparameters = ves.handleException(e, viewparams);
    
    // make sure this method is prodded FIRST, because requestComplete 
    // (which would ordinarily allocate the token on seeing an error state) is
    // only called AFTER we visibly return from handle() above.
    if (!issilent) {
      String tokenid = errorstatemanager.allocateOutgoingToken();
      defaultparameters.errortoken = tokenid;
      defaultparameters.errorredirect = "1";
      Logger.log.warn("Error creating view tree - token " + tokenid);
      TargettedMessage newerror = 
        new TargettedMessage(CoreMessages.GENERAL_SHOW_ERROR,  new Object[] { tokenid });
      ThreadErrorState.addMessage(newerror);
    }
    
    return defaultparameters;
  }


}
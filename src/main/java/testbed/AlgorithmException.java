package testbed;

import base.Tools;
import js.base.BasePrinter;
import js.geometry.IPoint;
import js.graphics.AbstractScriptElement;
import js.graphics.PointElement;

import static js.base.Tools.*;

import java.util.List;

/**
 * Exception thrown during algorithm tracing operation
 */
public final class AlgorithmException extends RuntimeException {

  public void setError() {
    mError = true;
  }

  public boolean error() {
    return mError;
  }

  public AlgorithmException(Object... message) {
    mMessages = message;
  }

  @Override
  public String getMessage() {
    parse();
    return mMessage;
  }

  public List<AbstractScriptElement> plotables() {
    parse();
    return mPlotables;
  }

  /**
   * Extract renderable objects, and strings
   */
  private void parse() {
    if (mMessage != null)
      return;
    List<Object> stringables = arrayList();
    mPlotables = arrayList();
    for (Object obj : mMessages) {
      AbstractScriptElement se = null;
      if (obj != null) {
        if (obj instanceof AbstractScriptElement) {
          se = (AbstractScriptElement) obj;
        } else if (obj instanceof IPoint) {
          se = new PointElement(null, (IPoint) obj);
        } else if (!(obj instanceof String))
          todo("convert IRect, IPoint, etc to appropriate objects");
      }
      if (se != null)
        mPlotables.add(se);
      else
        stringables.add(obj);
    }
    BasePrinter prBuffer = new BasePrinter();
    prBuffer.pr(stringables.toArray());
    if (error())
      prBuffer.cr().append(Tools.stackTrace(1, 5, this));
    mMessage = prBuffer.toString();
  }

  private Object[] mMessages;
  private List<AbstractScriptElement> mPlotables;
  private boolean mError;
  private String mMessage;

}

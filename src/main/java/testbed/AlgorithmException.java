/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package testbed;

import js.base.BasePrinter;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.AbstractScriptElement;
import js.graphics.PointElement;
import js.graphics.RectElement;

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
        } else if (obj instanceof IRect) {
          se = new RectElement(null, (IRect) obj);
        } else if (!(obj instanceof String))
          todo("convert IRect, IPoint, etc to appropriate objects;", obj.getClass().getName());
      }
      if (se != null)
        mPlotables.add(se);
      else
        stringables.add(obj);
    }
    BasePrinter prBuffer = new BasePrinter();
    prBuffer.pr(stringables.toArray());
    mMessage = prBuffer.toString();
  }

  private Object[] mMessages;
  private List<AbstractScriptElement> mPlotables;
  private boolean mError;
  private String mMessage;

}

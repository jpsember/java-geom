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
import js.base.Pair;

import static js.base.Tools.*;

import java.util.List;

import geom.AlgRenderable;

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

  public List<Pair<AlgRenderable, Object>> plotables() {
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
      AlgRenderable se =  AlgorithmStepper.sharedInstance().findRendererForObject(obj);
      if (se != null)
        mPlotables.add(pair(se, obj));
      else
        stringables.add(obj);
    }
    BasePrinter prBuffer = new BasePrinter();
    prBuffer.pr(stringables.toArray());
    mMessage = prBuffer.toString();
  }

  private Object[] mMessages;
  private List<Pair<AlgRenderable, Object>> mPlotables;
  private boolean mError;
  private String mMessage;

}

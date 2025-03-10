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

import static js.base.Tools.*;

import java.util.List;

import geom.ParsedAlgItem;

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

  public List<ParsedAlgItem> plotables() {
    parse();
    return mPlotables;
  }

  /**
   * Extract renderable objects, and strings
   */
  private void parse() {
    if (mMessage != null)
      return;

    mPlotables = AlgorithmStepper.sharedInstance().extractRenderables(mMessages);
    
    // Extract the items that didn't have renderables, for printing
    List<Object> nonRenderables = arrayList();
    for (var x : mPlotables) {
      if (x.renderer == null)
        nonRenderables.add(x.object);
    }
    BasePrinter prBuffer = new BasePrinter();
    prBuffer.pr(nonRenderables.toArray());
    mMessage = prBuffer.toString();
  }

  private Object[] mMessages;
  private List<ParsedAlgItem> mPlotables;
  private boolean mError;
  private String mMessage;

}

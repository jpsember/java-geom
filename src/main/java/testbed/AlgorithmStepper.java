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

import js.geometry.IRect;
import js.graphics.AbstractScriptElement;
import js.guiapp.UserOperation;
import js.widget.WidgetManager;

import static geom.GeomTools.*;

import java.awt.*;
import java.awt.geom.*;

import static js.base.Tools.*;
import static testbed.Render.*;
import static testbed.TestBed.*;

public class AlgorithmStepper {

  public static AlgorithmStepper sharedInstance() {
    return sAlgorithmStepper;
  }

  private static AlgorithmStepper sAlgorithmStepper = new AlgorithmStepper();

  public void disable() {
    mDisabled++;
  }

  public void enable() {
    checkState(mDisabled != 0);
    mDisabled--;
  }

  public int step() {
    return mStep;
  }

  /**
   * Execute an algorithm, with optional tracing
   * 
   * @return true if algorithm ran to completion
   */
  public boolean runAlgorithm(TestBedOperation alg) {
    WidgetManager g = widgets();
    mLastException = null;
    mRunning = g.exists(TRACEENABLED) && g.vb(TRACEENABLED);
    mDisabled = 0;
    mStep = 0;
    mStepDecision = null;
    mStepToStopAt = 0;
    if (mRunning)
      mStepToStopAt = g.vi(TRACESTEP);
    if (mStepToStopAt == 0)
      mRunning = false;

    AlgorithmException event = null;
    try {
      alg.runAlgorithm();
    } catch (AlgorithmException traceEvent) {
      event = traceEvent;
    } finally {
      mRunning = false;
    }

    //    if (event != null) {
    //      // if error, save editor buffer for user recall
    //      if (event.error) {
    //        if (Editor.initialized())
    //          Editor.storeErrorItems();
    //      }
    //    }
    mLastException = event;
    return (event == null);
  }

  public void renderAlgorithmResults() {
    if (mLastException != null) {
      plotTrace(mLastException);
    }
  }

  /**
   * Get event that interrupted last algorithm run
   * 
   * @return T, or null if last algorithm ran to completion
   */
  public AlgorithmException lastEvent() {
    return mLastException;
  }

  private void renderElem(AbstractScriptElement elem) {
  }

  /**
   * Display items shown during algorithm, including any added by trace message.
   * 
   * @param evt
   *          trace object thrown during algorithm processing, or null
   */
  private void plotTrace(AlgorithmException tr) {

    pushColor(Color.red);
    pushStroke(STRK_NORMAL);
    for (AbstractScriptElement elem : tr.plotables()) {
      renderElem(elem);
      IRect bounds = elem.bounds().withInset((int) (-5 * getScale()));
      drawRect(bounds);
    }
    pop(2);

    if (widgets().vb(TRACEPLOT)) {
      String msg = tr.getMessage();
      if (!msg.isEmpty()) {
        pushColor(Colors.get(Colors.RED, .32));
        // Modify transform so we ignore the view's page location and scale factor
        Graphics2D g = graphics();
        AffineTransform savedTransform = g.getTransform();
        float scl = 1.8f;
        g.setTransform(AffineTransform.getScaleInstance(scl, scl));
        draw(msg, -20000, 20000, // it will clamp this into range on the bottom left
            TX_BGND | TX_FRAME | TX_CLAMP | 80);
        g.setTransform(savedTransform);
        pop();
      }
    }
    cleanUpRender();
  }

  /**
   * Determine if algorithm tracing is occurring
   * 
   * @return true if so
   */
  public boolean active() {
    return mRunning && mDisabled == 0;
  }

  /**
   * Determine if current step is the stopping point. If this returns false,
   * then the next call to msg() would have no effect
   */
  public boolean update() {
    auxUpdate();
    boolean result = mStepDecision;

    // If decision is false, discard it (but return false),  so subsequent calls to
    // update() or msg() increment the algorithm step and recalculate the decision value
    //
    if (!result)
      mStepDecision = null;
    return result;
  }

  /**
   * If at stopping point of algorithm, throw an exception that will display the
   * message and render elements
   * 
   * Calls update(), if it hasn't already been called, to determine if this is
   * the stopping point
   */
  public void msg(Object... messageAndRenderElements) {

    // If user hasn't called update() yet, do so
    boolean decision = auxUpdate();

    // Discard the decision in case we aren't acting on it
    mStepDecision = null;

    if (decision)
      throw new AlgorithmException(messageAndRenderElements);
  }

  private boolean auxUpdate() {
    if (mStepDecision == null) {
      if (active()) {
        mStep++;
        mStepDecision = (mStep == mStepToStopAt);
      } else
        mStepDecision = false;
    }
    return mStepDecision;
  }

  /**
   * Throw an algorithm error event. Message is always displayed for errors,
   * along with stack trace.
   * 
   * @param s
   *          : Object describing problem
   */
  public void err(Object s) {
    AlgorithmException e = new AlgorithmException(s);
    e.setError();
    throw e;
  }

  public static UserOperation buildStepOper(int i) {
    return new AlgStepOper(i);
  }

  public static UserOperation buildResetOper() {
    return new AlgStepOper(0);
  }

  private int mStepToStopAt;
  private int mStep;
  private Boolean mStepDecision;

  // true if algorithm tracing is enabled
  private boolean mRunning;
  // if > 0, algorithm tracing has been disabled
  private int mDisabled;
  // event that interrupted last algorithm run
  private AlgorithmException mLastException;

}

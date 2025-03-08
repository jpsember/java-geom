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

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static testbed.Colors.*;
import static testbed.Render.*;
import static testbed.TestBed.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Map;

import geom.AlgRenderable;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Polygon;
import js.graphics.AbstractScriptElement;
import js.graphics.PointElement;
import js.graphics.RectElement;
import js.guiapp.UserOperation;
import js.widget.WidgetManager;

public class AlgorithmStepper {

  public static AlgorithmStepper sharedInstance() {
    if (sAlgorithmStepper == null)
      sAlgorithmStepper = new AlgorithmStepper();
    return sAlgorithmStepper;
  }

  private static AlgorithmStepper sAlgorithmStepper;

  @Deprecated
  public void disable() {
    mDisabled++;
  }

  @Deprecated
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
    mActiveStack.clear();
    mActive = true;
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

  /**
   * Display items shown during algorithm, including any added by trace message.
   * 
   * @param evt
   *          trace object thrown during algorithm processing, or null
   */
  private void plotTrace(AlgorithmException tr) {

    pushColor(Color.red);
    pushStroke(STRK_NORMAL);
    for (var plotable : tr.plotables()) {
      plotable.first.render(plotable.second);
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

  public void pushActive(boolean f) {
    if (mActiveStack.size() > 20)
      badState("active stack overflow");
    push(mActiveStack, mActive);
    mActive = f;
  }

  public void popActive() {
    checkState(!mActiveStack.isEmpty(), "active stack underflow");
    mActive = pop(mActiveStack);
  }

  /**
   * Determine if current step is the stopping point. If this returns false,
   * then the next call to msg() would have no effect
   */
  public boolean update() {
    if (!mActive)
      return false;
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
    if (!mActive)
      return;
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

  public void addRenderable(Class klass, AlgRenderable renderer) {
    mRenderableMap.put(klass, renderer);
  }

  private static AlgRenderable sAbstractScriptElementRenderer = (item) -> {
    var elem = (AbstractScriptElement) item;
    IRect bounds = elem.bounds().withInset((int) (-5 * getScale()));
    drawRect(bounds);
  };

  public static AlgRenderable scriptElementRenderer() {
    return sAbstractScriptElementRenderer;
  }

  private AlgorithmStepper() {
    final var r = sAbstractScriptElementRenderer;
    mRenderableMap = hashMap();
    addRenderable(FPoint.class, (item) -> r.render(new PointElement(null, ((FPoint) item).toIPoint())));
    addRenderable(IPoint.class, (item) -> r.render(new PointElement(null, (IPoint) item)));
    addRenderable(IRect.class, (item) -> r.render(new RectElement(null, (IRect) item)));
    addRenderable(Polygon.class, (item) -> renderPoly((Polygon) item));
  }

  public static void renderPoly(Polygon p) {
    // We want the line width to be constant, independent of the zoom factor
    float scale = 1.0f / geomApp().zoomFactor();

    final float radius = 4f * scale;
    pushStroke(STRK_NORMAL);
    pushColor(RED, radius);

    // Determine vertices, if any, involved in vertex being inserted

    IPoint start = null;
    IPoint last = null;
    for (IPoint pt : p.vertices()) {
      fillCircle(pt.toFPoint(), radius);
      if (start == null) {
        start = pt;
      }
      if (last != null) {
        drawLine(last, pt);
      }
      last = pt;
    }

    if (p.numVertices() > 1 && p.isClosed()) {
      drawLine(last, start);
    }
    pop(2);
  }

  private List<Boolean> mActiveStack = arrayList();

  private int mStepToStopAt;
  private int mStep;
  private Boolean mStepDecision;

  // true if algorithm tracing is enabled
  private boolean mRunning;
  // if > 0, algorithm tracing has been disabled
  private int mDisabled;
  private boolean mActive;
  // event that interrupted last algorithm run
  private AlgorithmException mLastException;

  Map<Class, AlgRenderable> mRenderableMap;

  public AlgRenderable findRendererForObject(Object obj) {
    AlgRenderable se = null;
    if (obj != null) {
      if (obj instanceof AlgRenderable) {
        se = (AlgRenderable) obj;
      } else {
        se = mRenderableMap.get(obj.getClass());
        if (se == null) {
          if (obj instanceof AbstractScriptElement) {
            se = scriptElementRenderer();
          }
        }
      }
    }
    return se;
  }

}

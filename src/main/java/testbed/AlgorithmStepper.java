package testbed;

import base.*;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.AbstractScriptElement;
import js.guiapp.UserOperation;

import java.awt.*;
import java.util.*;
import java.awt.geom.*;
import static geom.GeomTools.*;

import static js.base.Tools.*;

public class AlgorithmStepper implements Globals {

  public static AlgorithmStepper sharedInstance() {
    return sAlgorithmStepper;
  }

  private static AlgorithmStepper sAlgorithmStepper = new AlgorithmStepper();

  public void disable() {
    mDisabled++;
  }

  public void enable() {
    mDisabled--;
    Tools.ASSERT(mDisabled >= 0);
  }

  public int step() {
    return mStep;
  }

  /**
   * Execute an algorithm, with optional tracing
   * 
   * @param alg
   *          : algorithm to execute
   * @return true if algorithm ran to completion
   */
  public boolean runAlgorithm(TestBedOperation alg) {
    loadTools();
    mLastException = null;
    mRunning = C.exists(TBGlobals.TRACEENABLED) && C.vb(TBGlobals.TRACEENABLED);
    mDisabled = 0;
    mStep = 0;
    mStepDecision = null;
    mStepToStopAt = 0;
    if (mRunning)
      mStepToStopAt = C.vi(TBGlobals.TRACESTEP);
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
   * Render each item in a collection
   * 
   * @param c
   *          : if not null, collection to render
   * @param color
   *          : if not null, color to override default with
   */
  public void renderAll(Collection c, Color color) {
    renderAll(c, color, -1, -1);
  }

  public void render(Object item) {
    render(item, null, -1, -1);
  }

  /**
   * Render a Traceable item
   * 
   * @param item
   *          : if not null, item to render
   * @param color
   *          : if not null, color to override default with
   */
  public void render(Object item, Color color) {
    render(item, color, -1, -1);
  }

  /**
   * Render a Traceable item
   * 
   * @param item
   *          if not null, item to render
   * @param color
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   */
  public void render(Object item, Color color, int stroke, int markType) {
    Renderable t = make(item, color, stroke, markType);
    if (t != null)
      t.render(color, stroke, markType);
  }

  //  /**
  //   * Attempt to build a Renderable from an object
  //   * @return Renderable, or null if unable to build one
  //   */
  //  public static Renderable make(Object item, Color color, int stroke) {
  //    return make(item, color, stroke, -1);
  //  }
  //  /**
  //   * Attempt to build a Renderable from an object
  //   * @return Renderable, or null if unable to build one
  //   */
  //  public static Renderable make(Object item, Color color) {
  //    return make(item, color, -1, -1);
  //  }
  //  /**
  //   * Attempt to build a Renderable from an object
  //   * @return Renderable, or null if unable to build one
  //   */
  //  public static Renderable make(Object item) {
  //    return make(item, null, -1, -1);
  //  }

  /**
   * Make a renderable and add it to an array for later plotting. Does nothing
   * if array is null, or no renderable can be made.
   * 
   * @param dest
   *          where to store the Renderable
   */
  public static void make(DArray dest, Object item, Color color, int stroke, int markType) {
    if (dest != null) {
      Renderable t = make(item, color, stroke, markType);
      if (t != null)
        dest.add(t);
    }
  }

  /**
   * @see make
   */
  public static void make(DArray dest, Object item, Color color) {
    make(dest, item, color, -1, -1);
  }

  /**
   * @see make
   */
  public static void make(DArray dest, Object item) {
    make(dest, item, null, -1, -1);
  }

  public static void make(DArray dest, String str, Color color, double x, double y, int flags) {
    if (flags == -1)
      flags = TX_BGND | TX_FRAME | TX_CLAMP;

    make(dest, new TraceString(str, color, x, y, flags, 1.0), color);
  }

  /**
   * Attempt to build a Renderable from an object
   * 
   * @param item
   *          object
   * @return Renderable, or null if unable to build one
   */
  public static Renderable make(Object item, Color color, int stroke, int markType) {
    Renderable t = null;
    if (item != null) {
      if (item instanceof Renderable) {
        t = new TraceRenderable((Renderable) item, color, stroke, markType);
        //        t = (Renderable) item;
      } else if (item instanceof IVector) {
        t = new TraceIVector((IVector) item, color, markType);
      } else if (item instanceof LineEqn) {
        t = new TraceLineEqn((LineEqn) item, color, stroke, markType);
      } else if (item instanceof Iterable) {
        t = new TraceIterable((Iterable) item, color, stroke, markType);
      } else if (item instanceof RectangularShape) {
        t = new TraceRect((RectangularShape) item, color, stroke, markType);
      } else if (item instanceof Object[]) {
        t = new TraceIterable(new DArray((Object[]) item), color, stroke, markType);
      }
    }
    return t;
  }

  /**
   * Show a Traceable
   * 
   * @param t
   *          if not null, and a Traceable type, traceable to show
   * @param c
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   * @return empty string
   */
  @Deprecated
  public String show(Object t, Color c, int stroke, int markType) {
    Renderable r = make(t, c, stroke, markType);
    if (r != null) {
      //plotList.add(new PlotItem(r, c, stroke, markType));
    }
    return "";
  }

  /**
   * Render each item in a collection that is a Traceable
   * 
   * @param c
   *          if not null, collection to render
   * @param color
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   */
  public void renderAll(Collection c, Color color, int stroke, int markType) {
    if (c != null)
      for (Iterator it = c.iterator(); it.hasNext();) {
        Object obj = it.next();
        render(obj, color, stroke, markType);
      }
  }

  /**
   * Render each item in an array that is a Traceable
   * 
   * @param array
   *          if not null, array to render
   * @param color
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   */
  public void renderAll(Object[] array, Color color, int stroke, int markType) {
    if (array != null)
      for (int i = 0; i < array.length; i++) {
        Object obj = array[i];
        if (obj == null)
          continue;
        if (obj instanceof Renderable)
          render((Renderable) obj, color, stroke, markType);
      }
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

    V.pushColor(Color.red);
    V.pushStroke(STRK_NORMAL);
    for (AbstractScriptElement elem : tr.plotables()) {
      renderElem(elem);
      IRect bounds = elem.bounds().withInset((int) (-5 * V.getScale()));
      V.drawRect(bounds);
    }
    V.pop(2);

    if (TestBed.plotTraceMessages()) {
      String msg = tr.getMessage();
      if (!msg.isEmpty()) {
        V.pushColor(MyColor.get(MyColor.RED, .32));
        // Modify transform so we ignore the view's page location and scale factor
        Graphics2D g = V.get2DGraphics();
        AffineTransform savedTransform = g.getTransform();
        float scl = 1.8f;
        g.setTransform(AffineTransform.getScaleInstance(scl, scl));
        V.draw(msg, -20000, 20000, // it will clamp this into range on the bottom left
            TX_BGND | TX_FRAME | TX_CLAMP | 80);
        g.setTransform(savedTransform);
        V.pop();
      }
    }
    V.cleanUpRender();
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

  /**
   * Show a collection of Traceable items
   * 
   * @param tList
   *          if not null, collection to show
   * @param c
   *          if not null, color to override default with
   * @return empty string
   */
  public String showAll(Collection tList, Color c) {
    return showAll(tList, c, -1, -1);
  }

  /**
   * Show a collection of Traceable items
   * 
   * @param tList
   *          if not null, collection to show
   * @param c
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   * @return empty string
   */
  public String showAll(Collection tList, Color c, int stroke, int markType) {
    if (tList != null)
      for (Iterator it = tList.iterator(); it.hasNext();) {
        Object obj = it.next();
        show(obj, c, stroke, markType);
      }
    return "";
  }

  /**
   * Show a collection of Traceable items
   * 
   * @param tList
   *          : if not null, collection to show
   * @return empty string
   * @deprecated
   */
  public String showAll(Collection tList) {
    return showAll(tList, null, -1, -1);
  }

  /**
   * Show an array of Traceable items
   * 
   * @param array
   *          if not null, array to show
   * @param c
   *          if not null, color to override default with
   * @return empty string
   */
  public String showAll(Object[] array, Color c) {
    return showAll(array, c, -1, -1);
  }

  /**
   * Show a collection of Traceable items
   * 
   * @param array
   *          if not null, array to show
   * @param c
   *          if not null, color to override default with
   * @param stroke
   *          if >= 0, stroke to override default with
   * @param markType
   *          if >= 0, mark type to override default with
   * @return empty string
   */
  public String showAll(Object[] array, Color c, int stroke, int markType) {
    if (array != null)
      for (int i = 0; i < array.length; i++) {
        Object obj = array[i];
        show(obj, c, stroke, markType);
      }
    return "";
  }

  /**
   * Show an array of Traceable items
   * 
   * @param array
   *          if not null, array of items to show
   * @return empty string
   */
  public String showAll(Object[] array) {
    return showAll(array, null, -1, -1);
  }

  public String show(Object t) {
    return show(t, null, -1, -1);
  }

  public String show(Object t, Color c) {
    return show(t, c, -1, -1);
  }

  // private DArray plotList = new DArray();

  public String show(String str, Color color, FPoint2 loc, int flags, double scale) {
    return show(str, color, loc.x, loc.y, flags, scale);
  }

  public String show(String str, Color color, FPoint2 loc, int flags) {
    return show(str, color, loc, flags, 1.0);
  }

  public String show(String str, Color color, double x, double y, int flags) {
    return show(str, color, x, y, flags, 1.0);
  }

  public String show(String str, Color color, double x, double y, int flags, double scale) {
    if (flags == -1)
      flags = TX_BGND | TX_FRAME | TX_CLAMP;
    return show(new TraceString(str, color, x, y, flags, scale), color);
  }

  //  /**
  //   * Wrapper class for adding color, mark attributes to traceables
  //   */
  //  private static class PlotItem {
  //    public PlotItem(Renderable t, Color c, int stroke, int markType) {
  //      this.item = t;
  //      this.color = c;
  //      this.stroke = stroke;
  //      this.markType = markType;
  //    }
  //
  //    //    public PlotItem(Renderable t) {
  //    //      this(t, null, -1, -1);
  //    //    }
  //    public int stroke;
  //    public int markType;
  //    public Renderable item;
  //    public Color color;
  //  }

  /**
   * Wrapper class for tracing IVectors
   */
  private static class TraceIVector implements Renderable {
    public TraceIVector(IVector v, Color color, int markType) {
      this.v = v;
      this.markType = markType;
      if (color == null)
        color = Color.red;
      this.color = color;
      Tools.ASSERT(v != null);
    }

    private IVector v;
    private int markType;
    private Color color;

    public void render(Color c, int stroke, int markType) {
      if (c == null)
        c = color;
      if (markType < 0)
        markType = this.markType;
      V.pushColor(c);
      V.mark(new FPoint2(v.x(), v.y()), markType);
      V.pop();
    }
  }

  /**
   * Wrapper class for tracing Collections
   */
  private static class TraceIterable implements Renderable {
    public TraceIterable(Iterable c, Color color, int stroke, int markType) {
      this.c = c;
      this.markType = markType;
      this.stroke = stroke;
      this.color = color;
      Tools.ASSERT(c != null);
    }

    public void render(Color c, int stroke, int markType) {
      if (c == null)
        c = color;
      if (stroke < 0)
        stroke = this.stroke;
      if (markType < 0)
        markType = this.markType;
      AlgorithmStepper s = sharedInstance();
      for (Iterator it = this.c.iterator(); it.hasNext();) {
        Object obj = it.next();
        s.render(obj, color, stroke, markType);
      }
    }

    private int stroke;
    private Iterable c;
    private int markType;
    private Color color;
  }

  /**
   * Wrapper class for tracing LineEqn's
   */
  private static class TraceLineEqn implements Renderable {
    public TraceLineEqn(LineEqn v, Color color, int stroke, int markType) {
      this.v = v;
      this.markType = markType;
      this.stroke = stroke;
      if (color == null)
        color = MyColor.cRED;
      this.color = color;

      Tools.ASSERT(v != null);
    }

    private int stroke;
    private LineEqn v;
    private int markType;
    private Color color;

    public void render(Color c, int stroke, int markType) {
      V.pushColor(c, color);
      V.pushStroke(stroke, this.stroke);
      if (markType < 0)
        markType = this.markType;

      IPoint pageSize = editor().getEditorPanel().pageSize();

      double[] t = v.clipToRect(new FRect(0, 0, pageSize.x, pageSize.y));
      if (t != null) {
        FPoint2 p1 = v.pt(t[0]);
        FPoint2 p2 = v.pt(t[1]);

        V.drawLine(p1, p2);
        V.mark(p1, markType);
        V.mark(p2, markType);
      }
      V.pop(2);
    }
  }

  /**
   * Wrapper class for tracing rectangles
   */
  private static class TraceRect implements Renderable {
    public TraceRect(RectangularShape c, Color color, int stroke, int markType) {
      this.c = c;
      this.markType = markType;
      this.stroke = stroke;
      if (color == null)
        color = MyColor.cRED;
      this.color = color;
    }

    public void render(Color c, int stroke, int markType) {
      if (c == null)
        c = color;
      if (stroke < 0)
        stroke = this.stroke;
      if (markType < 0)
        markType = this.markType;
      V.pushColor(c, color);
      V.pushStroke(stroke, this.stroke);
      Rectangle2D r = this.c.getBounds2D();
      double x0 = r.getMinX(), x1 = r.getMaxX(), y0 = r.getMinY(), y1 = r.getMaxY();

      FPoint2 p0 = new FPoint2(x0, y0), p1 = new FPoint2(x1, y0), p2 = new FPoint2(x1, y1),
          p3 = new FPoint2(x0, y1);

      V.drawLine(p0, p1);
      V.drawLine(p1, p2);
      V.drawLine(p2, p3);
      V.drawLine(p3, p0);
      if (markType >= 0) {
        V.mark(p0, markType);
        V.mark(p1, markType);
        V.mark(p2, markType);
        V.mark(p3, markType);
      }
      V.pop(2);
    }

    private int stroke;
    private RectangularShape c;
    private int markType;
    private Color color;
  }

  /**
   * Wrapper class for tracing messages
   */
  private static class TraceString implements Renderable {
    public TraceString(String s, Color color, double x, double y, int flags, double scale) {
      this.str = s;
      if (color == null)
        color = MyColor.cRED;
      this.color = color;
      this.position = new FPoint2(x, y);
      this.flags = flags;
      this.scale = scale;
    }

    public void render(Color c, int stroke, int markType) {
      V.pushColor(c, color);
      V.pushScale(scale);
      V.draw(str, position, flags);
      V.pop(2);
    }

    private String str;
    private int flags;
    private FPoint2 position;
    private Color color;
    private double scale;
  }

  /**
   * Wrapper class for other renderables, for overriding color/stroke/mark attrs
   */
  private static class TraceRenderable implements Renderable {
    public TraceRenderable(Renderable r, Color color, int stroke, int markType) {
      this.r = r;
      this.markType = markType;
      this.stroke = stroke;
      this.color = color;
    }

    public void render(Color c, int stroke, int markType) {
      if (c == null)
        c = this.color;
      if (stroke < 0)
        stroke = this.stroke;
      if (markType < 0)
        markType = this.markType;
      r.render(c, stroke, markType);
    }

    private int stroke;
    private Renderable r;
    private int markType;
    private Color color;
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

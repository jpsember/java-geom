package testbed;

import base.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.*;
import java.util.List;

import static js.base.Tools.*;
import static geom.GeomTools.*;

/**
 * Main view of TestBed applications. It has the short name 'V' to minimize
 * typing.
 */
public class V implements Globals {

  private V() {
  }

  public static void setGraphics(Graphics2D graphics) {
    g = graphics;
    if (graphics == null)
      return;

    TBFont.prepare();
    float z = editor().zoomFactor();
    screenScaleFactor = 2f / z;
    V.setFont(FNT_MEDIUM);
  }

  /**
   * Mark a location with a small 'x'
   */
  public static void mark(FPoint2 pt) {
    mark(pt, MARK_X);
  }

  /**
   * Mark a location
   */
  public static void mark(FPoint2 pt, int markType) {
    mark(pt, markType, 1.0);
  }

  /**
   * Mark a location
   */
  public static void mark(FPoint2 pt, int markType, double scale) {
    double pad = getScale() * scale * .4;
    switch (markType) {
    case MARK_X:
      drawLine(pt.x - pad, pt.y - pad, pt.x + pad, pt.y + pad);
      drawLine(pt.x - pad, pt.y + pad, pt.x + pad, pt.y - pad);
      break;
    default:
    case MARK_DISC:
      fillCircle(pt, pad * 1.5);
      break;
    case MARK_CIRCLE:
      drawCircle(pt, pad * 1.5);
      break;
    case MARK_SQUARE:
      drawRect(pt.x - pad, pt.y - pad, pad * 2, pad * 2);
      break;
    case MARK_FSQUARE:
      fillRect(pt.x - pad, pt.y - pad, pad * 2, pad * 2);
      break;
    case MARK_NONE:
      break;
    }
  }

  /**
   * Draw a string
   */
  public static void draw(String str, FPoint2 loc, int flags) {
    draw(str, loc.x, loc.y, flags);
  }

  /**
   * Save current scaling factor on stack, scale by some factor
   */
  public static void pushScale(double scaleAdj) {
    plotStack.push(new Double(screenScaleFactor));
    screenScaleFactor *= scaleAdj;
    plotStack.push(ST_SCALE);
  }

  private static DArray plotStack = new DArray();

  private static int activeFont;

  /**
   * Set font
   * 
   * @param font
   *          FNT_xx
   */
  public static void setFont(int font) {
    activeFont = font;
    g.setFont(TBFont.getFont(activeFont));
  }

  /**
   * Save current font on stack, set to new
   * 
   * @param font
   *          new font (FNT_xx)
   */
  public static void pushFont(int font) {
    if (font >= 0) {
      plotStack.pushInt(activeFont);
      setFont(font);
    } else {
      plotStack.push(ST_IGNORE);
    }
    plotStack.push(ST_FONT_INDEX);
  }

  public static void pushFont(Font font) {
    checkArgument(font != null);
    // Push {Font, activeFont, "FONT"}
    plotStack.push(g.getFont());
    plotStack.push(activeFont);
    plotStack.push(ST_FONT);
    g.setFont(font);
  }

  /**
   * Pop font from stack
   */
  @Deprecated
  public static void popFont() {
    Integer val = (Integer) popValue(ST_FONT_INDEX);
    if (val != null)
      setFont(val.intValue());
  }

  /**
   * Draw a string (with flags set to zero)
   * 
   * @param str
   * @param x
   * @param y
   *          view coordinates
   */
  public static void draw(String str, double x, double y) {
    draw(str, x, y, 0);
  }

  private static List<String> extractRows(String str, int lineWidth) {
    List<String> strings = arrayList();
    int s = 0;
    int lastSpace = -1;
    int c = 0;
    while (true) {
      char ch = ' ';
      if (c < str.length()) {
        ch = str.charAt(c);
      }
      if (ch == ' ' || ch == '\n') {
        lastSpace = c;
      }

      // If beyond maximum width, back up to last space printed

      if (ch == '\n' || c - s > lineWidth || c == str.length()) {
        if (lastSpace > s || ch == '\n') {
          String ns = str.substring(s, lastSpace);
          strings.add(ns);
          c = lastSpace + 1;
          s = c;
        } else {
          String ns = str.substring(s, c);
          strings.add(ns);
          s = c;
          c++;
        }
      } else {
        c++;
      }
      if (c > str.length())
        break;
    }
    return strings;
  }

  /**
   * Draw a string
   * 
   * @param str
   *          string to draw
   * @param x
   * @param y
   *          view coordinates
   * @param flags
   *          Flags controlling string's appearance. These include:
   * 
   *          <pre>
      TX_LINEWIDTH  if not zero, plots string in multiple rows, breaking at 
                     word boundaries (if possible) so no row has length 
                     greater than this value
      TX_BGND       if set, clears background of string
      TX_FRAME      if set, draws a frame around the string
      TX_CLAMP      if set, clamps coordinates into range of view so entire
                     string is guaranteed to be visible
   *          </pre>
   */
  public static void draw(String str, double x, double y, int flags) {

    int lineWidth = (flags & TX_LINEWIDTH);

    List<String> strings;
    if (lineWidth == 0)
      strings = arrayList(str);
    else
      strings = extractRows(str, lineWidth);
    int maxStrLen = 0;
    for (String s : strings)
      maxStrLen = Math.max(maxStrLen, s.length());

    TBFont f = TBFont.get(activeFont);

    float fsize = (float) f.charWidth();
    float ascent = f.metrics().getAscent();
    float descent = f.metrics().getDescent();

    float textW = maxStrLen * fsize;
    float rowH = (ascent + descent) * .8f;
    float textH = rowH * (strings.size() + .2f);

    float textX = (float) (x - textW * .5f);
    float textY = (float) (y - textH * .5f);

    float pad = 5;

    if ((flags & TX_CLAMP) != 0) {
      // Get the bounds of the editor window, in its own coordinate system
      Rectangle bounds = g.getClipBounds();
      textX = (float) MyMath.clamp(textX, bounds.getMinX() + pad * 2, bounds.getMaxX() - pad * 2 - textW);
      textY = (float) MyMath.clamp(textY, bounds.getMinY() + pad * 2, bounds.getMaxY() - pad * 2 - textH);
    }

    if (flags != 0) {
      textRect.setFrame(textX - pad, textY - pad, textW + pad * 2, textH + pad * 2);
      if ((flags & TX_BGND) != 0) {
        pushColor(Color.white);
        g.fill(textRect);
        pop();
      }
      if ((flags & TX_FRAME) != 0)
        g.draw(textRect);
    }
    int rowNumber = INIT_INDEX;
    for (String s : strings) {
      rowNumber++;
      float ry = textY + rowNumber * rowH + ascent;
      double px = textX;
      if (lineWidth == 0) {
        px = textX + (textW + 1 - s.length() * fsize) * .5;
      }
      g.drawString(s, (float) px, (float) (ry) - 1);
    }
  }

  /**
   * Pop stroke from stack
   */
  @Deprecated
  public static void popStroke() {
    die("no longer supported");
  }

  /**
   * Draw a circle
   * 
   * @param origin
   *          origin of circle
   * @param radius
   *          radius of circle
   */
  public static void drawCircle(FPoint2 origin, double radius) {
    g.draw(
        new Arc2D.Double(origin.x - radius, origin.y - radius, 2 * radius, 2 * radius, 0, 360, Arc2D.CHORD));
  }

  /**
   * Draw a rectangle
   * 
   * @param r
   *          rectangle
   */
  public static void drawRect(FRect r) {
    g.draw(r);
  }

  /**
   * Draw a filled circle (disc)
   * 
   * @param origin
   *          origin of circle
   * @param radius
   *          radius of circle
   */
  public static void fillCircle(FPoint2 origin, double radius) {
    g.fill(
        new Arc2D.Double(origin.x - radius, origin.y - radius, 2 * radius, 2 * radius, 0, 360, Arc2D.CHORD));
  }

  /**
   * Set color
   * 
   * @param c
   */
  public static void setColor(Color c) {
    g.setColor(c);
  }

  /**
   * Pop a number of state attributes
   * 
   * @param count
   *          number to pop
   */
  public static void pop(int count) {
    for (int i = 0; i < count; i++)
      pop();
  }

  /**
   * Pop a state attribute
   */
  public static void pop() {
    if (plotStack.isEmpty())
      throw new IllegalStateException("render stack empty");
    Object tag = plotStack.peek(0);
    if (tag == ST_COLOR) {
      popValue();
      Color nc = (Color) plotStack.pop();
      if (nc != null)
        g.setColor(nc);
    } else if (tag == ST_STROKE) {
      popValue();
      Stroke s = (Stroke) plotStack.pop();
      g.setStroke(s);
    } else if (tag == ST_SCALE) {
      Double val = (Double) popValue(ST_SCALE);
      if (val != null) {
        screenScaleFactor = val.floatValue();
      }
    } else if (tag == ST_FONT_INDEX) {
      popValue();
      Integer val = (Integer) plotStack.pop();
      if (val != null)
        setFont(val.intValue());
    } else if (tag == ST_FONT) {
      // Pop {Font, activeFont, "FONT"}
      popValue();
      activeFont = (Integer) plotStack.pop();
      g.setFont((Font) plotStack.pop());
    } else
      badArg("unsupported stack element:", tag);
  }

  public static void dumpStack() {
    pr("Render state stack:");
    for (Object obj : plotStack) {
      pr(obj, TAB(20), obj.getClass());
    }
  }

  /**
   * Pop color from stack
   */
  @Deprecated
  public static void popColor() {
    throw notSupported();
  }

  private static Object popValue() {
    return plotStack.pop();
  }

  private static Object popValue(Object expectedTag) {
    Object tag = popValue();
    if (tag != expectedTag) {
      throw new IllegalStateException("render stack problem: popped " + tag + ", expected " + expectedTag);
    }
    Object val = plotStack.pop();
    if (val == ST_IGNORE)
      val = null;
    return val;
  }

  /**
   * Get the current graphics context being updated by updateView(), in case we
   * need to manipulate it in ways not provided by this class.
   * 
   * @return Graphics2D
   */
  public static Graphics2D get2DGraphics() {
    return g;
  }

  /**
   * Save current color on stack, set to new
   * 
   * @param c
   *          new color
   */
  public static void pushColor(Color c) {
    pushColor(c, null);
  }

  /**
   * Save current color on stack, set to new
   * 
   * @param c
   *          new color
   */
  public static void pushColor(Color c, Color defaultColor) {
    if (c == null) {
      checkArgument(defaultColor != null);
    }
    plotStack.push(g.getColor());
    plotStack.push(ST_COLOR);
    setColor(c);
  }

  // graphics being updated by updateView()
  private static Graphics2D g;

  /**
   * Draw a line segment
   * 
   * @param p0
   *          first endpoint
   * @param p1
   *          second endpoint
   */
  public static void drawLine(FPoint2 p0, FPoint2 p1) {
    if (false) {
      long start = System.currentTimeMillis();
      drawLine(p0.x, p0.y, p1.x, p1.y);
      long end = System.currentTimeMillis();
      if (end - start > 1) {
        Streams.out.println("drawling line " + p0 + ".." + p1 + " took " + (end - start) + " ms!");
      }
      return;
    }
    drawLine(p0.x, p0.y, p1.x, p1.y);
  }

  /**
   * Draw a pixel as a filled square
   * 
   * @param x
   * @param y
   *          location
   * @param pixelSize
   *          width of square
   */
  public static void drawPixel(double x, double y, double pixelSize) {
    fillRect(x - pixelSize * .5, y - pixelSize * .5, pixelSize, pixelSize);
  }

  /**
   * Draw a pixel as a filled square
   * 
   * @param pt
   *          location
   * @param pixelSize
   *          width of square
   */
  public static void drawPixel(FPoint2 pt, double pixelSize) {
    drawPixel(pt.x, pt.y, pixelSize);
  }

  /**
   * Draw a line segment
   * 
   * @param x0
   * @param y0
   *          first endpoint
   * @param x1
   * @param y1
   *          second endpoint
   */
  public static void drawLine(double x0, double y0, double x1, double y1) {
    Line2D.Double wl = new Line2D.Double();
    wl.setLine(x0, y0, x1, y1);
    g.draw(wl);
  }

  /**
   * Save current stroke on stack, set to new
   */
  public static void pushStroke(int s) {
    pushStroke(s, -1);
  }

  /**
   * Save current stroke on stack, set to new
   */
  public static void pushStroke(int s, int defaultStroke) {
    if (s < 0) {
      s = defaultStroke;
      if (defaultStroke < 0)
        notSupported();
    }
    pushStroke(strokes[s]);
  }

  public static void pushStroke(Stroke stroke) {
    if (stroke == null)
      badArg("null stroke");
    plotStack.push(g.getStroke());
    g.setStroke(stroke);
    plotStack.push(ST_STROKE);
  }

  private static final Object ST_STROKE = "STROKE";
  private static final Object ST_IGNORE = "<no val>";
  private static final Object ST_COLOR = "COLOR";
  private static final Object ST_SCALE = "SCALE";
  private static final Object ST_FONT_INDEX = "FONT_INDEX";
  private static final Object ST_FONT = "FONT ";

  /**
   * Draw a filled rectangle
   * 
   * @param pos
   *          location
   * @param size
   *          size
   */
  public static void fillRect(FPoint2 pos, FPoint2 size) {
    fillRect(pos.x, pos.y, size.x, size.y);
  }

  /**
   * Draw a filled rectangle
   * 
   * @param r
   *          rectangle
   */
  public static void fillRect(FRect r) {
    fillRect(r.x, r.y, r.width, r.height);
  }

  /**
   * Draw a filled rectangle
   * 
   * @param x
   * @param y
   *          location
   * @param w
   *          width
   * @param h
   *          height
   */
  public static void fillRect(double x, double y, double w, double h) {

    final Rectangle2D.Double r = new Rectangle2D.Double();
    r.x = x;
    r.y = y;
    r.width = w;
    r.height = h;
    g.fill(r);
  }

  /**
   * Draw a rectangle
   * 
   * @param x
   * @param y
   *          location
   * @param w
   *          width
   * @param h
   *          height
   */
  public static void drawRect(double x, double y, double w, double h) {
    Rectangle2D.Double r = new Rectangle2D.Double();
    r.x = x;
    r.y = y;
    r.width = w;
    r.height = h;
    g.draw(r);
  }

  private static FRect textRect = new FRect();

  // table of BasicStroke objects for use by application
  private static BasicStroke[] strokes = new BasicStroke[STRK_TOTAL];

  //  // the transform to convert from logic -> view coords
  //  private static AffineTransform logicToViewTF = new AffineTransform(), viewToLogicTF = new AffineTransform();

  // size, in viewspace, of a 1x1 rectangle in logicspace
  //  private static double logPixelSize ; // Not sure necessary

  private static float screenScaleFactor;

  public static float getScale() {
    return screenScaleFactor;
  }

  //private static Dimension physicalSize;
  //  /**
  //   * Get size of view, in physical pixels.  Returns null
  //   * if unknown
  //   * @return size of view, in pixels, or null
  //   */
  //  public static Dimension physicalSize() {
  //    return physicalSize;
  //  }

  //  /**
  //   * Cause view to be repainted
  //   */
  //  public static void repaint() {
  //    repaint(0);
  //  }

  //  /**
  //   * Cause view to be repainted after a delay
  //   * 
  //   * @param tm
  //   *          number of milliseconds
  //   */
  //  public static void repaint(long tm) {
  //    panel.repaint(tm);
  //  }

  //  static Grid grid;

  //  /**
  //   * Return a copy of a point, that has been snapped to the current grid (if it
  //   * is active)
  //   * 
  //   * @param pt
  //   * @return copy of pt, possibly snapped to grid
  //   */
  //  public static FPoint2 snapToGrid(FPoint2 pt) {
  //    if (TestBed.parms.includeGrid && C.vb(TBGlobals.GRIDACTIVE)) {
  //      pt = grid.snap(pt);
  //    } else
  //      pt = new FPoint2(pt);
  //    return pt;
  //  }

  //  static void initGrid() {
  //    setGrid(new SquareGrid());
  //    grid.setSize(10, logicalSize());
  //    if (TestBed.parms.includeGrid)
  //      updateGridSize(C.vi(TBGlobals.GRIDSIZE));
  //  }

  //  static void updateGridSize(int size) {
  //    grid.setSize(size, logicalSize());
  //  }
  //
  static void cleanUpRender() {
    if (!plotStack.isEmpty()) {
      Tools.warn("plot stack not empty");
      plotStack.clear();
    }
  }

}

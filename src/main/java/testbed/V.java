package testbed;

import js.geometry.FPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import static js.base.Tools.*;
import static geom.GeomTools.*;

/**
 * Main view of TestBed applications. It has the short name 'V' to minimize
 * typing.
 */
public class V  {


  // predefined strokes:
  public static final int STRK_NORMAL = 0, STRK_THICK = 1, STRK_THIN = 2, STRK_VERYTHICK = 3,
      STRK_RUBBERBAND = 4, STRK_TOTAL = 5;

  public static final int MARK_X = 0, MARK_DISC = 1, MARK_CIRCLE = 2, MARK_SQUARE = 3, MARK_FSQUARE = 4,
      MARK_NONE = 5;

  /**
   * Text plotting flags
   */
  public static final int //
  // do multiline text with no line > n chars wide?
  TX_LINEWIDTH = 0x00ff
  // clear background?
      , TX_BGND = 0x0100
      // draw frame around text?
      , TX_FRAME = 0x0200
      // clamp into range?
      , TX_CLAMP = 0x0400;

  // Font indexes:
  public static final int FNT_SMALL = 0, FNT_MEDIUM = 1, FNT_LARGE = 2, FNT_ITALIC = 3, FNT_TOTAL = 4;

  private V() {
  }

  public static void setGraphics(Graphics2D graphics) {
    g = graphics;
    if (graphics == null)
      return;

    TBFont.prepare();
    float z = geomApp().zoomFactor();
    sScaleFactor = 2f / z;
    prepareForRender();
    V.setFont(FNT_MEDIUM);
  }

  /**
   * Mark a location with a small 'x'
   */
  public static void mark(FPoint pt) {
    mark(pt, MARK_X);
  }

  /**
   * Mark a location
   */
  public static void mark(FPoint pt, int markType) {
    mark(pt, markType, 1.0);
  }

  /**
   * Mark a location
   */
  public static void mark(FPoint pt, int markType, double scale) {
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
  public static void draw(String str, FPoint loc, int flags) {
    draw(str, loc.x, loc.y, flags);
  }

  /**
   * Save current scaling factor on stack, scale by some factor
   */
  public static void pushScale(double scaleAdj) {
    pushElem(sScaleFactor);
    sScaleFactor *= scaleAdj;
    pushElem(ST_SCALE);
  }

  /**
   * Set font
   * 
   * @param font
   *          FNT_xx
   */
  public static void setFont(int font) {
    sCurrentFontIndex = font;
    g.setFont(TBFont.getFont(sCurrentFontIndex));
  }

  /**
   * Save current font on stack, set to new
   * 
   * @param font
   *          new font (FNT_xx)
   */
  public static void pushFont(int font) {
    if (font >= 0) {
      pushElem(sCurrentFontIndex);
      setFont(font);
    } else {
      pushElem(ST_IGNORE);
    }
    pushElem(ST_FONT_INDEX);
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

    TBFont f = TBFont.get(sCurrentFontIndex);

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
      Rectangle textRect = new Rectangle();
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
   * Draw a circle
   * 
   * @param origin
   *          origin of circle
   * @param radius
   *          radius of circle
   */
  public static void drawCircle(FPoint origin, double radius) {
    g.draw(
        new Arc2D.Double(origin.x - radius, origin.y - radius, 2 * radius, 2 * radius, 0, 360, Arc2D.CHORD));
  }

  public static void drawRect(IRect r) {
    g.drawRect(r.x, r.y, r.width, r.height);
  }

  /**
   * Draw a filled circle (disc)
   * 
   * @param origin
   *          origin of circle
   * @param radius
   *          radius of circle
   */
  public static void fillCircle(FPoint origin, double radius) {
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

  private static void pushElem(Object value) {
    sPlotStateStack.add(value);
  }

  private static <T> T popElem() {
    return (T) js.base.Tools.pop(sPlotStateStack);
  }

  private static <T> T peekElem() {
    return (T) js.base.Tools.peek(sPlotStateStack);
  }

  /**
   * Pop a state attribute
   */
  public static void pop() {
    if (sPlotStateStack.isEmpty())
      throw new IllegalStateException("render stack empty");
    Object tag = peekElem();
    if (tag == ST_COLOR) {
      popElem();
      Color nc = popElem();
      if (nc != null)
        g.setColor(nc);
    } else if (tag == ST_STROKE) {
      popElem();
      Stroke s = popElem();
      g.setStroke(s);
    } else if (tag == ST_SCALE) {
      popElem();
      Double val = popElem();
      if (val != null) {
        sScaleFactor = val.floatValue();
      }
    } else if (tag == ST_FONT_INDEX) {
      popElem();
      Integer val = popElem();
      if (val != null)
        setFont(val.intValue());
    } else if (tag == ST_FONT) {
      // Pop {Font, activeFont, "FONT"}
      popElem();
      sCurrentFontIndex = popElem();
      g.setFont(popElem());
    } else
      badArg("unsupported stack element:", tag);
  }

  public static void dumpStack() {
    pr("Render state stack:");
    for (Object obj : sPlotStateStack) {
      pr(obj, TAB(20), obj.getClass());
    }
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
    pushElem(g.getColor());
    pushElem(ST_COLOR);
    setColor(c);
  }

  /**
   * Draw a line segment
   * 
   * @param p0
   *          first endpoint
   * @param p1
   *          second endpoint
   */
  public static void drawLine(FPoint p0, FPoint p1) {
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
  public static void drawPixel(FPoint pt, double pixelSize) {
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
    pushStroke(sStrokes[s]);
  }

  public static void pushStroke(Stroke stroke) {
    if (stroke == null)
      badArg("null stroke");
    pushElem(g.getStroke());
    g.setStroke(stroke);
    pushElem(ST_STROKE);
  }

  private static final Object ST_STROKE = "STROKE";
  private static final Object ST_IGNORE = "<no val>";
  private static final Object ST_COLOR = "COLOR";
  private static final Object ST_SCALE = "SCALE";
  private static final Object ST_FONT_INDEX = "FONT_INDEX";
  private static final Object ST_FONT = "FONT ";

  //  /**
  //   * Draw a filled rectangle
  //   * 
  //   * @param pos
  //   *          location
  //   * @param size
  //   *          size
  //   */
  //  public static void fillRect(FPoint2 pos, FPoint2 size) {
  //    fillRect(pos.x, pos.y, size.x, size.y);
  //  }

  //  /**
  //   * Draw a filled rectangle
  //   * 
  //   * @param r
  //   *          rectangle
  //   */
  //  public static void fillRect(FRect r) {
  //    fillRect(r.x, r.y, r.width, r.height);
  //  }

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

  public static float getScale() {
    return sScaleFactor;
  }

  static void cleanUpRender() {
    if (!sPlotStateStack.isEmpty()) {
     alert("plot stack not empty");
      sPlotStateStack.clear();
    }
  }

  public static void prepareForRender() {
    sStrokes = new Stroke[STRK_TOTAL];

    // construct strokes so we have uniform thickness despite scaling
    buildStroke(STRK_NORMAL, 1f);
    buildStroke(STRK_THICK, 2f);
    buildStroke(STRK_THIN, .4f);
    buildStroke(STRK_VERYTHICK, 3f);
    {
      float[] dash = new float[2];
      float sc = calcStrokeWidth(8);
      dash[0] = sc;
      dash[1] = sc * .5f;
      sStrokes[STRK_RUBBERBAND] = new BasicStroke(calcStrokeWidth(.4f), BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
    }
  }

  private static float calcStrokeWidth(float width) {
    return width * getScale() * 1.8f;
  }

  private static void buildStroke(int index, float width) {
    float f = calcStrokeWidth(width);
    sStrokes[index] = new BasicStroke(f);
  }

  private static ArrayList<Object> sPlotStateStack = arrayList();
  private static int sCurrentFontIndex;
  private static Stroke[] sStrokes;
  private static float sScaleFactor;
  private static Graphics2D g;

}

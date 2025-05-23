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
package geom;

import static js.base.Tools.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import geom.EditorElement.RenderState;
import geom.gen.ScriptEditState;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.Matrix;
import js.graphics.Paint;
import js.guiapp.UserEvent;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;

import static geom.GeomTools.*;
import static testbed.Render.*;

public class EditorPanel extends JPanel implements MouseListener, MouseMotionListener {

  public EditorPanel() {
    setBackground(new Color(185, 201, 179));
    addMouseListener(this);
    addMouseMotionListener(this);
  }

  @Override
  public void paintComponent(Graphics g) {
    ScriptWrapper script = scriptManager().currentScript();
    //scriptManager().autoZoomOnCurrentScriptIfNec();

    if (!script.isNone()) {
      mGraphics = (Graphics2D) g;
      mCurrentZoom = geomApp().zoomFactor();
      super.paintComponent(g);
      paintContents();
      mGraphics = null;
      mCurrentZoom = null;
    }

    // To avoid having the InfoPanel (or some Swing widgets within it) intercepting keyboard events
    // (that we'd like available for the menu items), always have the editor panel request the focus:
    requestFocus();
  }

  private void determineTransform(IPoint pageSize ) {

    // The page size is the size of the current script's image (if it has one), or
    // the constant (1200, 900) if not.

    Matrix t;
    {
      FPoint fpt = new FPoint(getWidth(), getHeight());
      float zoom = zoomFactor();

      var pageCenter = pageSize.scaledBy(.5f);
      var worldPan = geomApp().panOffset();
      var worldFocus = IPoint.sum(pageCenter, worldPan).toFPoint().scaledBy(zoom);

      FPoint trans = new FPoint(fpt.x / 2 - worldFocus.x, fpt.y / 2 - worldFocus.y);
      t = Matrix.getTranslate(trans);
      t = Matrix.multiply(t, Matrix.getScale(zoom));
    }
    mTransform = t;
    mViewToWorldTransform = mTransform.invert();
  }

  private Matrix mTransform = Matrix.IDENTITY;
  private Matrix mViewToWorldTransform = Matrix.IDENTITY;

  private AffineTransform mSavedTransform;

  /**
   * Save current transform, and replace with one whose origin is at a
   * particular rectangle's origin, and whose scale is 1.0.
   *
   * Returns a rectangle whose size represents the original rectangle's size
   * scaled by the previous scale factor.
   *
   * This is to make precision work on a particular rectangle more painless, so
   * geometric operations with particular pixel counts (and text rendering)
   * doesn't have strange scaling artifacts.
   */
  public FRect pushFocusOn(FRect worldRect) {
    FPoint worldPoint = worldRect.location();
    float sw = worldRect.width * zoomFactor();
    float sh = worldRect.height * zoomFactor();
    FPoint focusView = mTransform.apply(worldPoint);
    mSavedTransform = mGraphics.getTransform();
    Matrix t = Matrix.multiply(mViewToWorldTransform, Matrix.getTranslate(focusView.x, focusView.y));
    mGraphics.transform(t.toAffineTransform());
    return new FRect(0, 0, sw, sh);
  }

  public void popFocus() {
    mGraphics.setTransform(mSavedTransform);
    mSavedTransform = null;
  }

  private void paintContents() {
    ScriptWrapper script = scriptManager().currentScript();
    if (script.isNone())
      return;
    Graphics2D g = mGraphics;
    mSavedTransform = null;

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    IPoint pageSize = geomApp().pageSize();
    determineTransform(pageSize);
    g.transform(mTransform.toAffineTransform());

    geomApp().paintStart(g);

    if (script.hasImage()) {
      g.drawImage(script.image(), 0, 0, null);
    } else {
      if (geomApp().renderPageFrame()) {
        int gray = 192;
        g.setColor(new Color(gray, gray, gray));
        g.fillRect(0, 0, pageSize.x, pageSize.y);
        g.setColor(Color.black);
        g.drawRect(0, 0, pageSize.x, pageSize.y);
      }
    }

    geomApp().paintBackground(g);

    UserOperation op = UserEventManager.sharedInstance().getOperation();

    // Save the current font (and any other stateful Graphics stuff) so
    // that we can pop them after rendering the elements, to restore the old value

    pushFont(FNT_MEDIUM);

    // If no filter is specified, render nominally, but with selected items as selected.
    // Otherwise, non-selected items are rendered disabled.

    IntArray displayedSlots = op.displayedSlotsFilter();
    int filterCursor = 0;
    {
      ScriptEditState state = scriptManager().state();
      int slot = INIT_INDEX;
      for (EditorElement element : state.elements()) {
        slot++;
        boolean selected = IntArray.with(state.selectedElements()).contains(slot);
        RenderState appearance;
        if (displayedSlots == null)
          appearance = selected ? RenderState.SELECTED : RenderState.NOMINAL;
        else {
          appearance = RenderState.DISABLED;
          if (filterCursor != displayedSlots.size()) {
            if (displayedSlots.get(filterCursor) == slot) {
              appearance = RenderState.SELECTED;
              filterCursor++;
            }
          }
        }
        element.render(this, appearance);
      }
    }
    pop();

    op.paint();

    geomApp().paintStop();
  }

  // ------------------------------------------------------------------
  // Mouse-related interfaces
  // ------------------------------------------------------------------

  @Override
  public void mousePressed(MouseEvent ev) {
    generateMouseEvent(ev, UserEvent.CODE_DOWN);
  }

  @Override
  public void mouseReleased(MouseEvent ev) {
    generateMouseEvent(ev, UserEvent.CODE_UP);
  }

  @Override
  public void mouseDragged(MouseEvent ev) {
    generateMouseEvent(ev, UserEvent.CODE_DRAG);
  }

  @Override
  public void mouseMoved(MouseEvent ev) {
    generateMouseEvent(ev, UserEvent.CODE_MOVE);
  }

  @Override
  public void mouseClicked(MouseEvent arg0) {
  }

  @Override
  public void mouseEntered(MouseEvent arg0) {
  }

  @Override
  public void mouseExited(MouseEvent arg0) {
  }

  private void generateMouseEvent(MouseEvent evt, int type) {
    IPoint viewPoint = new IPoint(evt.getX(), evt.getY());
    int modifierFlags = 0;
    if (SwingUtilities.isRightMouseButton(evt))
      modifierFlags |= UserEvent.FLAG_RIGHT;
    if (evt.isAltDown())
      modifierFlags |= UserEvent.FLAG_ALT;
    if (evt.isControlDown())
      modifierFlags |= UserEvent.FLAG_CTRL;
    if (evt.isMetaDown())
      modifierFlags |= UserEvent.FLAG_META;
    if (evt.isShiftDown())
      modifierFlags |= UserEvent.FLAG_SHIFT;

    UserEvent event = new UserEvent(type, mViewToWorldTransform.apply(viewPoint), viewPoint, modifierFlags,
        null);

    if (devMode()) {
      // Note: this doesn't display stack traces in Eclipse in a way that supports clicking
      try {
        UserEventManager.sharedInstance().processUserEvent(event);
      } catch (Throwable t) {
        throw haltProgram(t);
      }
    } else
      UserEventManager.sharedInstance().processUserEvent(event);
  }

  // ------------------------------------------------------------------
  // Rendering
  // ------------------------------------------------------------------

  public float zoomFactor() {
    assertRendering();
    return mCurrentZoom;
  }

  public Graphics2D graphics() {
    assertRendering();
    return mGraphics;
  }

  private Paint mActivePaint = Paint.DEFAULT_INSTANCE;

  public EditorPanel apply(Paint pb) {
    todo("!Refactor to use Render state stack");
    assertRendering();
    mActivePaint = pb.apply(mGraphics);
    return this;
  }

  @Deprecated
  public EditorPanel renderFrame(FRect rect) {
    todo("!<1Refactor to use Render class");
    return renderFrame(rect, 0);
  }

  @Deprecated
  public EditorPanel renderText(String text, float x, float y) {
    alert("!<1renderText is deprecated");
    mGraphics.drawString(text, x, y);
    return this;
  }

  @Deprecated
  public EditorPanel renderFrame(FRect rect, float radius) {
    if (mActivePaint.isFill()) {
      RectangularShape r;
      if (radius > 0) {
        r = new RoundRectangle2D.Double(rect.x, rect.y, rect.width, rect.height, 10, 10);
      } else {
        r = new Rectangle2D.Double(rect.x, rect.y, rect.width, rect.height);
      }
      mGraphics.fill(r);
    } else {
      float widthInPix = mActivePaint.width();
      RectangularShape r;
      if (radius > 0) {
        r = new RoundRectangle2D.Double(rect.x - widthInPix / 2, rect.y - widthInPix / 2,
            rect.width + widthInPix, rect.height + widthInPix, 10, 10);
      } else {
        r = new Rectangle2D.Double(rect.x - widthInPix / 2, rect.y - widthInPix / 2, rect.width + widthInPix,
            rect.height + widthInPix);
      }
      mGraphics.draw(r);
    }
    return this;
  }

  @Deprecated
  public void renderDisc(IPoint location, float radius) {
    alert("!<1this renderDisc is deprecated");
    todo("!Refactor to use Render class");
    renderDisc(location.toFPoint(), radius);
  }

  @Deprecated // Use Render methods
  public void renderDisc(FPoint location, float radius) {
    alert("!<1renderDisc is deprecated");
    render(new Ellipse2D.Float(location.x - radius, location.y - radius, radius * 2, radius * 2));
  }

  public void render(Shape shape) {
    if (mActivePaint.isFill())
      mGraphics.fill(shape);
    else
      mGraphics.draw(shape);
  }

  public void renderCategory(EditorElement element, FRect bounds, RenderState appearance) {
    if (element.properties().category() == null)
      return;
    String categoryString = "" + element.properties().category();
    final float TITLE_OFFSET = 16;

    if (appearance == RenderState.SELECTED) {
      apply(CATEGORY_TEXT_BGND);
      renderFrame(new FRect(bounds.midX() - 8, bounds.y - 60, 48, 54));
    }

    apply(appearance == RenderState.SELECTED ? CATEGORY_TEXT_SELECTED : CATEGORY_TEXT);
    renderText(categoryString, bounds.midX(), bounds.y - TITLE_OFFSET);
  }

  private void assertRendering() {
    if (mGraphics == null)
      badState("method only available while rendering");
  }

  private static final Paint CATEGORY_TEXT = Paint.newBuilder().color(200, 100, 255).bigFont(1.6f).build();
  private static final Paint CATEGORY_TEXT_SELECTED = CATEGORY_TEXT.toBuilder().bigFont(2.2f)
      .color(Color.YELLOW).build();
  private static final Paint CATEGORY_TEXT_BGND = Paint.newBuilder().fill().color(0, 0, 0, 128).build();

  private Graphics2D mGraphics;
  private Float mCurrentZoom;

}

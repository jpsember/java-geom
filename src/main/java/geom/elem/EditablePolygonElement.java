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
 **/
package geom.elem;

import static js.base.Tools.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import geom.EditorPanel;
import geom.EditorElement;
import geom.GeomApp;
import geom.oper.PolygonEditOper;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.MyMath;
import js.geometry.Polygon;
import js.graphics.Paint;
import js.graphics.PolygonElement;
import js.graphics.ScriptElement;
import js.graphics.ScriptUtil;
import js.graphics.gen.ElementProperties;
import js.guiapp.UserEvent;
import js.guiapp.UserOperation;
import testbed.Render;

import static geom.GeomTools.*;
import static testbed.Render.*;

public final class EditablePolygonElement extends PolygonElement implements EditorElement {

  public static final EditablePolygonElement DEFAULT_INSTANCE = new EditablePolygonElement(
      PolygonElement.DEFAULT_INSTANCE.properties(), PolygonElement.DEFAULT_INSTANCE.polygon(), false);

  @Override
  public EditablePolygonElement toEditorElement(ScriptElement obj) {
    PolygonElement elem = (PolygonElement) obj;
    return new EditablePolygonElement(elem.properties(), elem.polygon(), false);
  }

  public EditablePolygonElement(ElementProperties properties, Polygon polygon, boolean curveMode) {
    super(properties, polygon);
    mCurveMode = curveMode;
  }

  @Override
  public boolean contains(int paddingPixels, IPoint pt, boolean isSelected) {
    IRect paddedBounds = bounds().withInset(-paddingPixels);
    return paddedBounds.contains(pt);
  }

  @Override
  public EditablePolygonElement applyTransform(Matrix m) {
    return withPolygon(polygon().applyTransform(m));
  }

  @Override
  public EditablePolygonElement withProperties(ElementProperties properties) {
    return new EditablePolygonElement(properties, polygon(), curveMode());
  }

  @Override
  public EditablePolygonElement validate() {
    Polygon p = polygon();
    if (p.numVertices() < (p.isClosed() ? 3 : 2)) {
      return null;
    }
    return this;
  }

  public EditablePolygonElement withPolygon(Polygon polygon) {
    return new EditablePolygonElement(properties(), polygon, curveMode());
  }

  private ArrayList<IPoint> getVertices(Polygon p) {
    ArrayList<IPoint> lst = arrayList();
    for (IPoint vert : p.vertices())
      lst.add(vert);
    return lst;
  }

  /**
   * Add a point at a particular location, shifting following points to make
   * room; return new polygon
   */
  public EditablePolygonElement withAddPoint(int ptIndex, IPoint point) {
    List<IPoint> v = getVertices(polygon());
    v.add(ptIndex, point);
    return withPolygon(polygon().withVertices(v));
  }

  public EditablePolygonElement withDeletedPoint(int ptIndex) {
    List<IPoint> v = getVertices(polygon());
    v.remove(ptIndex);
    return withPolygon(polygon().withVertices(v));
  }

  public EditablePolygonElement withSetPoint(int ptIndex, IPoint point) {
    checkState(ptIndex >= 0 && ptIndex < polygon().numVertices(), "attempt to store point", ptIndex,
        "for size", polygon().numVertices());
    List<IPoint> v = getVertices(polygon());
    v.set(ptIndex, point);
    return withPolygon(polygon().withVertices(v));
  }

  /**
   * For displaying polygon being edited, a vertex to be added and the position
   * it is to be inserted at
   */
  public EditablePolygonElement withInsertVertex(int position, IPoint vertexOrNull) {

    boolean db = false && alert("logging");

    EditablePolygonElement p = new EditablePolygonElement(properties(), polygon(), curveMode());

    IPoint vt = vertexOrNull;

    // If insert vertex exists, polygon is open, and insert vertex is close to the first vertex, snap to that one.
    //
    if (db)
      alert("trying new snap logic");
    checkState(polygon().isOpen() == p.polygon().isOpen());

    if (polygon().isOpen() && vt != null) {
      var snap = PolygonEditOper.snapToOtherEndpoint(polygon(), position, vt);
      if (snap != null) {
        vt = snap;
        if (DEBUG_POLYEDIT)
          pr("withInsertVertex, snap to other endpoint:", vt);
      }
    }

    p.mInsertVertex = vt;
    p.mInsertPosition = position;
    return p;
  }

  public boolean curveMode() {
    return mCurveMode;
  }

  @Override
  public UserOperation isEditingSelectedObject(GeomApp editor, int slot, UserEvent event) {

    UserOperation ret = null;

    IPoint pt = event.getWorldLocation();
    float tolerance = editor.paddingPixels();
    float toleranceSq = tolerance * tolerance;
    int edElement = -1;
    float closestDistSq = 0;
    for (int v = 0; v < polygon().numVertices(); v++) {
      IPoint vertex = polygon().vertex(v);
      float distSq = MyMath.squaredDistanceBetween(vertex, pt);
      if (distSq >= toleranceSq)
        continue;
      if (edElement < 0 || distSq < closestDistSq) {
        edElement = v;
        closestDistSq = distSq;
      }
    }

    if (edElement >= 0)
      ret = PolygonEditOper.buildEditExistingOper(event, slot, edElement);
    return ret;
  }

  @Override
  public void render(EditorPanel panel, RenderState appearance) {

    // We want the line width to be constant, independent of the zoom factor
    float scale = 1.0f / panel.zoomFactor();

    Paint paint;
    switch (appearance) {
      default:
        throw notSupported();
      case DISABLED:
        paint = PAINT_DISABLED;
        break;
      case NOMINAL:
        // todo("set color based on flavor(?)");
        paint = PAINT_NOMINAL;
        break;
      case SELECTED:
        paint = PAINT_SELECTED;
        break;
    }

    todo("!can we have Render support paints?");
    panel.apply(paint.toBuilder().width(paint.width() * scale));

    // Determine vertices, if any, involved in vertex being inserted

    IPoint pt1 = null;
    IPoint pt2 = null;
    int nPoints = polygon().numVertices();
    if (mInsertVertex != null && nPoints > 0) {
      pt1 = polygon().vertexMod(mInsertPosition - 1);
      if (mInsertPosition < nPoints) {
        pt2 = polygon().vertexMod(mInsertPosition);
      }
    }

    IPoint start = null;
    IPoint last = null;
    for (IPoint pt : polygon().vertices()) {
      if (start == null) {
        start = pt;
        if (DEBUG_POLYEDIT && alert("!highlighting first vertex")) {
          panel.apply(Paint.newBuilder().color(Color.BLUE).width(0.8f * scale));
          panel.renderDisc(pt, 5 * scale);
        }
      }
      if (last != null) {
        if (last == pt1 && pt == pt2) {
          panel.apply(Paint.newBuilder().color(Color.RED).width(0.8f * scale));
          //panel.renderLine(last, pt);
          drawDirectedLineSegment(last, pt, appearance == RenderState.SELECTED);
        } else
          drawDirectedLineSegment(last, pt, appearance == RenderState.SELECTED);
      }
      last = pt;
    }

    if (polygon().isClosed() && start != null && last != null) {
      drawDirectedLineSegment(last, start, appearance == RenderState.SELECTED);
    }
    if (mInsertVertex != null) {
      if (pt1 != null)
        drawDirectedLineSegment(pt1, mInsertVertex, false);
      if (pt2 != null)
        drawDirectedLineSegment(mInsertVertex, pt2, false);

      fillCircle(mInsertVertex, VERTEX_RADIUS * scale);
    }

    if (appearance == RenderState.SELECTED && !curveMode())
      for (IPoint pt : polygon().vertices())
        panel.renderDisc(pt, VERTEX_RADIUS * scale);

    if (ScriptUtil.hasCategory(this)) {
      FRect bounds = panel.pushFocusOn(bounds().toRect());
      panel.renderCategory(this, bounds, appearance);
      panel.popFocus();
    }
  }

  // ------------------------------------------------------------------
  // Constants for rendering
  // ------------------------------------------------------------------

  private static final float VERTEX_RADIUS = 2.5f;

  private static final Paint PAINT_NOMINAL = Paint.newBuilder().width(4).color(119, 52, 235).build();
  private static final Paint PAINT_DISABLED = Paint.newBuilder().width(3).color(119, 52, 235, 64).build();
  private static final Paint PAINT_SELECTED = Paint.newBuilder().width(4).color(255, 0, 0).build();

  private final boolean mCurveMode;
  private IPoint mInsertVertex;
  private int mInsertPosition;

}

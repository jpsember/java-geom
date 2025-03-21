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
package geom.oper;

import static js.base.Tools.*;

import js.geometry.IPoint;
import js.geometry.MyMath;
import js.geometry.Polygon;
import js.guiapp.UserEvent;
import js.guiapp.UserOperation;
import js.json.JSMap;

import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import geom.EditorElement;
import geom.StateTools;
import geom.elem.EditablePolygonElement;
import geom.gen.Command;

import static geom.GeomTools.*;

public class PolygonEditOper extends UserOperation implements UserEvent.Listener {

  public static PolygonEditOper buildAddOper() {
    return new PolygonEditOper(false);
  }

  public static PolygonEditOper buildAddCurveOper() {
    return new PolygonEditOper(true);
  }

  public static PolygonEditOper buildEditExistingOper(UserEvent event, int slot, int vertexIndex) {
    return new PolygonEditOper(event.getWorldLocation(), slot, vertexIndex);
  }

  /**
   * Constructor for adding a new polygon (or curve)
   */
  private PolygonEditOper(boolean curveMode) {
    mAddMode = true;
    mCurveMode = curveMode;
  }

  /**
   * Constructor for editing existing polygon, starting at a vertex
   */
  private PolygonEditOper(IPoint mouseDownLoc, int slot, int vertexIndex) {
    setState(STATE_ADJUST);
    mVertexIndex = vertexIndex;
    mSlot = slot;
    mMouseDownLoc = mouseDownLoc;
  }

  @Override
  public void start() {
    if (mAddMode) {
      mCommand = geomApp().buildCommand("Add Polygon");
      mVertexIndex = 0;
      setState(mCurveMode ? STATE_STARTING_CURVE : STATE_UP);

      boolean open = true;
      Polygon template = Polygon.DEFAULT_INSTANCE.withOpen(open);

      EditablePolygonElement polygon = new EditablePolygonElement(null, template, mCurveMode);

      mSlot = StateTools.addNewElement(mCommand, polygon);
      geomApp().setMouseCursor(Cursor.HAND_CURSOR);

      mMouseOffset = IPoint.ZERO;
    } else {
      mCommand = geomApp().buildCommand("Adjust Polygon");
      EditorElement elem = mCommand.newState().elements().get(mSlot);
      mOriginalElem = (EditablePolygonElement) elem;
      IPoint vertexLoc = mOriginalElem.polygon().vertex(mVertexIndex);
      mMouseOffset = IPoint.difference(vertexLoc, mMouseDownLoc);
    }
  }

  @Override
  public void processUserEvent(UserEvent event) {
    if (!mCurveMode)
      processPolygonUserEvent(event);
    else
      processCurveUserEvent(event);
  }

  private int setState(int newState) {
    if (mState != newState) {
      log("state changing from", stateName(mState), "to", stateName(newState));
      mState = newState;
    }
    return mState;
  }

  private void processPolygonUserEvent(UserEvent event) {
    // alertVerbose();
    if (event.getCode() != UserEvent.CODE_MOVE)
      log("processPolyUserEvent", INDENT, this, CR, event);
    switch (event.getCode()) {

      case UserEvent.CODE_DOWN: {
        if (event.isRight()) {
          geomApp().perform(mCommand);
          event.clearOperation();
          break;
        }

        EditablePolygonElement p = activePolygon();

        IPoint pos = applyMouseOffset(event.getWorldLocation());

        boolean endOperation = false;
        if (snapToOtherEndpoint(p.polygon(), mVertexIndex, pos) != null) {
          if (DEBUG_POLYEDIT)
            pr("snapToOtherEndpoint returned vert, ending oper");
          p = p.withPolygon(p.polygon().withOpen(false));
          endOperation = true;
        } else {
          log("adding point at index", mVertexIndex, pos);
          p = p.withAddPoint(mVertexIndex, pos);
        }
        writeActivePolygon(p);
        if (endOperation)
          event.setOperation(null);
        else
          setState(STATE_ADJUST);
      }
      break;

      case UserEvent.CODE_DRAG: {
        if (mState != STATE_ADJUST)
          break;

        EditablePolygonElement p = activePolygon();

        IPoint pt = applyMouseOffset(event.getWorldLocation());
        log("applied mouse offset to event:", event.getWorldLocation(), "now:", pt);
        var snapTo = snapToOtherEndpoint(p.polygon(), mVertexIndex, pt);
        if (snapTo != null) {
          if (DEBUG_POLYEDIT)
            pr("snapToOtherEndpoint snapping to opposite endpoint");
          log("...snapped to opposite endpoint");
          pt = snapTo;
        }

        p = p.withSetPoint(mVertexIndex, pt);
        writeActivePolygon(p);
      }
      break;

      case UserEvent.CODE_UP: {
        if (mState != STATE_ADJUST)
          break;

        if (DEBUG_POLYEDIT) pr("user event UP, must check if we snapped vertex");

        checkState(activePolygon().polygon().numVertices() > 0, "edit object has no vertices");

        if (event.isRight()) {
          // Delete the vertex
          EditablePolygonElement p = activePolygon().withDeletedPoint(mVertexIndex);
          writeActivePolygon(p);
          event.clearOperation();
          break;
        } else {

          var ap = activePolygon().polygon();
          if (DEBUG_POLYEDIT) pr("ap closed:", ap.isClosed());

          if (ap.isOpen() && ap.numVertices() >= 3) {
            // If first and last vertices are the same, make it a closed polygon
            if (ap.vertex(0).equals(ap.vertexMod(-1))) {
              // Delete the vertex
              EditablePolygonElement p = activePolygon().withDeletedPoint(ap.numVertices() - 1);
              p = p.withPolygon(p.polygon().withOpen(false));
              writeActivePolygon(p);
              event.clearOperation();
              break;
            }
          }

          mVertexIndex = (mVertexIndex + 1);
          if (ap.isClosed())
            mVertexIndex = mVertexIndex % ap.numVertices();
          log("vertex index incr'd to", mVertexIndex);
        }
        setState(STATE_UP);
      }
      break;

      case UserEvent.CODE_MOVE: {
        EditablePolygonElement p = activePolygon();
        p = p.withInsertVertex(mVertexIndex, applyMouseOffset(event.getWorldLocation()));
        writeActivePolygon(p);
        geomApp().perform(mCommand);
      }
      break;
    }
  }

  private void processCurveUserEvent(UserEvent event) {
    //log("processCurveUserEvent", INDENT, this, CR, event);

    switch (event.getCode()) {

      case UserEvent.CODE_DOWN: {
        if (mState == STATE_STARTING_CURVE && !event.isRight()) {
          setState(STATE_ADJUST);
        } else {
          geomApp().perform(mCommand);
          event.clearOperation();
        }
      }
      break;

      case UserEvent.CODE_MOVE: {
        if (mState != STATE_ADJUST)
          break;

        EditablePolygonElement p = activePolygon();

        IPoint pt = applyMouseOffset(event.getWorldLocation());

        if (p.polygon().numVertices() == 0) {
          p = p.withAddPoint(0, pt);
        } else {
          IPoint prevPt = p.polygon().lastVertex();
          if (MyMath.distanceBetween(prevPt, pt) > MIN_CURVE_MOVEMENT_DISTANCE)
            p = p.withAddPoint(p.polygon().numVertices(), pt);
        }
        writeActivePolygon(p);
      }
      break;
    }
  }

  private IPoint applyMouseOffset(IPoint mouseLocation) {
    return IPoint.sum(mouseLocation, mMouseOffset);
  }

  /**
   * Get the polygon being edited
   */
  private EditablePolygonElement activePolygon() {
    return (EditablePolygonElement) mCommand.newState().elements().get(mSlot);
  }

  /**
   * Store new version of polygon being edited
   */
  private void writeActivePolygon(EditablePolygonElement p) {
    StateTools.replaceAndSelectItem(mCommand, mSlot, p);
    geomApp().perform(mCommand);
  }

  @Override
  public void stop() {
    EditablePolygonElement p = activePolygon();
    log("stopping editing:", INDENT, p);
    p = p.withInsertVertex(-1, null);
    writeActivePolygon(p);

    if (mCurveMode) {
      if (p.polygon().isWellDefined()) {
        Polygon poly = p.polygon();

        if (MyMath.distanceBetween(poly.vertex(0), poly.lastVertex()) < 10)
          poly = poly.withOpen(false);

        poly = poly.simplify(0.5f);
        p = p.withPolygon(poly);
        writeActivePolygon(p);
      }
    }

    if (false && alert("extra logging")) {
      EditablePolygonElement p2 = activePolygon();
      if (p2.polygon().isWellDefined() != p.polygon().isWellDefined()) {
        pr("we have a problem, p:", INDENT, p);
        pr("p2:", INDENT, p2);
      }
    }

    if (!p.polygon().isWellDefined()) {
      log("...removing incomplete edit polygon");
      StateTools.remove(mCommand, mSlot);
      geomApp().perform(mCommand);
      geomApp().discardLastCommand();
      return;
    }

    attemptMerge(p);
  }

  private static final float MIN_CURVE_MOVEMENT_DISTANCE = 2.5f;

  private void attemptMerge(EditablePolygonElement p) {
    if (!mCurveMode)
      return;

    Polygon a = p.polygon();
    if (!a.isOpen())
      return;

    // Find another polygon to attempt a merge with.  If there isn't one, attempt merge with ourself.
    List<EditorElement> objects = mCommand.newState().elements();

    EditablePolygonElement mergeCandidate = null;
    int polyCount = 0;
    int bIndex = -1;

    for (int i = 0; i < objects.size(); i++) {
      if (i == mSlot)
        continue;
      EditorElement obj = objects.get(i);
      if (obj.tag() != EditablePolygonElement.TAG)
        continue;
      polyCount++;
      mergeCandidate = (EditablePolygonElement) obj;
      bIndex = i;
    }
    if (polyCount > 1) {
      return;
    }

    EditablePolygonElement closedPolygon = null;

    if (mergeCandidate == null) {
      // If distance between endpoints is near zero, close it
      float dist = MyMath.distanceBetween(p.polygon().vertex(0), p.polygon().lastVertex());
      float curveSize = p.bounds().maxDim() / 3f;
      if (dist > curveSize)
        return;
      closedPolygon = p.withPolygon(p.polygon().withOpen(false));
    } else {
      log("examining merge for slot", bIndex, mSlot);
      EditablePolygonElement pb = mergeCandidate;
      Polygon b = pb.polygon().withOpen(false);
      MergePoly mg = new MergePoly(a, b);
      MergePoly best = mg;
      MergePoly mg2 = new MergePoly(a, b.reverse());
      if (mg2.score() < mg.score())
        best = mg2;
      if (!best.failureReason().isEmpty()) {
        log("failure:", best);
        return;
      }
      log("...replacing pair of polys with merged result");
      closedPolygon = p.withPolygon(best.mergedResult());
    }

    // Discard the 'add polygon' operation, since we're about to merge it; we will
    // want the result of the merge to be the one added to the undo history
    geomApp().discardLastCommand();

    StateTools.remove(mCommand, mSlot);
    if (bIndex >= 0) {
      StateTools.remove(mCommand, bIndex);
      mCommand.description("Merge polygons");
    } else {
      mCommand.description("Close curve polygon");
    }
    StateTools.addNewElement(mCommand, closedPolygon);
    mCommand.mergeDisabled(true);
    geomApp().perform(mCommand);
  }

  /**
   * Determine if an open polygon vertex should be snapped to one of its
   * endpoints to close it
   */
  public static IPoint snapToOtherEndpoint(Polygon polygon, int index, IPoint pt) {
    final float SNAP_VERTEX_DISTANCE = 16;
    final boolean db = false && alert("logging snapToOtherEndpoint");

    if (db) pr("snapToOtherEndpoint, index", index, "pt:", pt, "numVert:", polygon.numVertices());
    IPoint snapTo = null;
    do {

      if (polygon.isClosed())
        break;
      if (polygon.numVertices() < 3)
        break;

      int otherInd;
      if (index == 0) {
        otherInd = polygon.numVertices() - 1;
      } else if (index == polygon.numVertices()) {
        otherInd = 0;
      } else
        break;

      if (db) pr("otherIndex:", otherInd);

      var otherLoc = polygon.vertex(otherInd);
      var otherLocF = otherLoc.toFPoint();
      var currLocF = pt.toFPoint();
      var dist = MyMath.distanceBetween(otherLocF, currLocF);

      var zoom = geomApp().zoomFactor();
      var zoomAdjustedDist = dist * zoom;

      if (db)
        pr("snapToOtherEndpoint, dist from first:", dist, "zoom:", zoom, "adj dist:", zoomAdjustedDist);
      if (zoomAdjustedDist < SNAP_VERTEX_DISTANCE) {
        snapTo = otherLoc;
        if (db)
          pr("...snapping");
      }

    } while (false);
    if (db)
      pr("returning:", snapTo);
    return snapTo;
  }

  // ------------------------------------------------------------------
  // States
  // ------------------------------------------------------------------

  private static List<String> sStateNames = split("up adjust starting", ' ');

  private static String stateName(int state) {
    return sStateNames.get(state);
  }

  private static final int STATE_UP = 0;
  private static final int STATE_ADJUST = 1;
  private static final int STATE_STARTING_CURVE = 2;

  // ------------------------------------------------------------------

  @Override
  public JSMap toJson() {
    JSMap m = super.toJson();
    m.put("add?", mAddMode);
    m.put("curve?", mCurveMode);
    m.put("slot", mSlot);
    m.put("vertex", mVertexIndex);
    m.put("state", stateName(mState));
    if (mMouseOffset != null)
      m.put("mouse_offset", mMouseOffset.toJson());
    return m;
  }

  private EditablePolygonElement mOriginalElem;

  private boolean mAddMode;
  private boolean mCurveMode;
  // amount to add to mouse location to avoid 'jumping' when dragging vertex when starting editing existing
  private IPoint mMouseOffset;
  private IPoint mMouseDownLoc;
  // index of polygon being edited
  private int mSlot;
  private Command.Builder mCommand;

  // Vertex being manipulated
  private int mVertexIndex;
  private int mState;

}

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
package sample;

import geom.EditorElement;
import geom.GeomApp;
import geom.elem.EditablePointElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
import testbed.AlgorithmStepper;
import testbed.TestBedOperation;

import java.util.List;
import java.util.Random;

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static js.geometry.MyMath.*;
import static sample.ClusterGlobals.*;

public class ClusterOper implements TestBedOperation {

  public static final boolean DEBUG_CLUSTER = false && alert("!DEBUG_CLUSTER is true");

  private static final String OPER_ID = "cluster";


  @Override
  public String operId() {
    return OPER_ID;
  }

  private void p2(Object... messages) {
    if (DEBUG_CLUSTER) return;
    pr(messages);
  }

  public void addControls(WidgetManager c) {
    todo("!define constraints on radius of circle in relation to population, tile size");

    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID); //+ ":Bounds");
    {
      c.label("Calculate minimum bounding box of objects").addLabel();

      c.columns(".x");
      c.open("random params");
      {
        c.label("Seed:").addLabel();
        c.withDisplay();
        c.max(100).addSlider(SEED);
        c.label("Count:").addLabel();
        c.max(500).defaultVal(100).addSlider(COUNT);
        c.label("Stickyness:").addLabel();
        c.max(100).defaultVal(20).addSlider(STICKYNESS);
        c.label("Radius:").addLabel();
        c.max(100).defaultVal(20).addSlider(NBR_RAD);


//        c.label("Tile size:").addLabel();
//        c.withDisplay();
//        c.max(120).min(10).addSlider(TILE_SIZE);

        c.spanx();
        c.label("Render tiles").addToggleButton(RENDER_TILES);

        c.label("Zoom:").addLabel();
        c.max(300).addSlider(ZOOM);
      }
      c.close("random params");

      c.columns(".x");
      c.open("listener experiment");
      c.pushListener((x) -> p2("outer listener, id:", x));
      {
        c.label("Slider1:").addLabel();
        c.max(100).listener((x) -> p2("slider1 listener", x)).addSlider("cl_slider_1");
        c.label("Slider2:").addLabel();
        c.max(100).listener((x) -> p2("slider2 listener", x)).defaultVal(12).addSlider("cl_slider_2");
        c.label("Slider3:").addLabel();
        c.max(100).defaultVal(12).addSlider("cl_slider_3");
      }
      c.popListener();
      c.close();
    }
    c.closeTab();
  }

  public void processUserEvent(UserEvent event) {
    if (event.isWidget())
      generate();
  }

  public void runAlgorithm() {
    WidgetManager g = widgets();

    var input = constructInputPoints();

    AlgorithmStepper s = AlgorithmStepper.sharedInstance();

    s.msg("starting algorithm");

    float targZoom;
    {
      var app = geomApp();
      var z = g.vi(ZOOM);
      targZoom = interpolateBetweenScalars(0.8f, 9f, z / 300f);
      //pr("zoom:", z, "current:", app.zoomFactor(), "targ:", targZoom);
      app.setZoomFactor(targZoom);
    }
    var ts = tileSizeForZoom(targZoom);
    pr(ts);

    // If there are no points yet, throw out cached grids
    if (pointsAreNew()) {
      mPointGrid = null;
    }
    if (mPointGrid == null) {
      mPointGrid2 = null;
      var tileSize = ts.tileSize;
      pr("TileSizeParam:",INDENT,ts);
      mPointGrid = new PointGrid(tileSize);
      for (var pt : input) {
        mPointGrid.insert(pt);
      }

    }
  }

  private static int widgetValueHash(String... ids) {
    var sb = new StringBuilder();
    for (var id : ids) {
      sb.append(' ');
      sb.append(widgets().get(id).readValue());
    }
    return sb.toString().hashCode();
  }

  private List<IPoint> constructInputPoints() {
    mPointsAreNewFlag = false;
    var hash = widgetValueHash(SEED, COUNT, STICKYNESS, NBR_RAD, SEED );
    if (mCachedPoints == null || hash != mCachedPointsHashCode) {
      mCachedPointsHashCode = hash;
      mPointsAreNewFlag = true;
      List<IPoint> points = arrayList();
      for (ScriptElement elem : scriptManager().state().elements()) {
        if (elem.is(PointElement.DEFAULT_INSTANCE))
          points.add(elem.location());
        mCachedPoints = points;
      }
    }
    return mCachedPoints;
  }

  private boolean pointsAreNew() {
    return mPointsAreNewFlag;
  }

  private List<IPoint> mCachedPoints;
  private boolean mPointsAreNewFlag;
  private int mCachedPointsHashCode;

  @Override
  public void paintView() {
    if (mPointGrid != null) {
      mPointGrid.render();
    }
  }

  private void generate() {
    WidgetManager g = widgets();
    int seed = g.vi(SEED);
    Random r = new Random(seed + 1);
    int c = g.vi(COUNT);
    List<EditorElement> elemList = arrayList();

    final int PADDING = 3;

    IPoint size = geomApp().pageSize();
    float sx = size.x - 2 * PADDING;
    float sy = size.y - 2 * PADDING;

    var clip = new FRect(PADDING, PADDING, sx, sy);
    FPoint stickyOrigin = null;
    var stickyness = g.vi(STICKYNESS) + 1;
    var stickyRadius = (g.vi(NBR_RAD) / 100.f) * Math.min(size.x, size.y);

    while (elemList.size() < c) {
      FPoint newLoc = null;

      if (stickyOrigin != null && r.nextInt(stickyness) != 0) {
        todo("!why can't I call nextFloat(x)?");
        var newLoc2 =
            pointOnCircle(stickyOrigin, r.nextFloat() * (360f * M_DEG), r.nextFloat() * stickyRadius);
        if (!clip.contains(newLoc2)) {
          continue;
          //  newLoc = newLoc2;
        }
        newLoc = newLoc2;
      }

      if (newLoc == null) {
        newLoc = new FPoint(r.nextFloat() * sx + PADDING, r.nextFloat() * sy + PADDING);
        stickyOrigin = newLoc;
      }

      elemList.add(EditablePointElement.DEFAULT_INSTANCE
          .withLocation(newLoc.toIPoint()));

    }

    Command.Builder b = Command.newBuilder();
    ScriptEditState editState = scriptManager().state();
    // We must discard any selected elements, as they may no longer exist
    b.newState(editState.toBuilder().elements(elemList).selectedElements(null));
    geomApp().perform(b);
    geomApp().performRepaint(GeomApp.REPAINT_EDITOR);
  }


  static class TileSizeParam {
    float zoomFactor;
    float idealTileSize;
    int exponent;
    float param;
    int tileSize;

    @Override
    public String toString() {
      var m = map();
      m.put("zoom", zoomFactor);
      m.put("exponent", exponent);
      m.put("param", param);
      m.put("tile_size", tileSize);
      m.put("ideal_tile_size", idealTileSize);
      return m.prettyPrint();
    }
  }

  /**
   * Determine ideal tile size for a zoom factor
   */
  private TileSizeParam tileSizeForZoom(float zoomFactor) {
    var p = new TileSizeParam();
    p.zoomFactor = zoomFactor;
    p.idealTileSize = Math.max(1, 16f / p.zoomFactor);
    var log2 = Math.log(p.idealTileSize) / Math.log(2);
    checkState(log2 >= 0);
    p.exponent = (int) Math.floor(log2);
    p.param = (float) (log2 - p.exponent);
    p.tileSize = (int) Math.pow(2f, p.exponent);
    return p;
  }


  private PointGrid mPointGrid;
  private PointGrid mPointGrid2;
}

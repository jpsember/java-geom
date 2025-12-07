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
import js.graphics.ImgUtil;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
import testbed.AlgorithmStepper;
import testbed.Render;
import testbed.TestBedOperation;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;
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
    todo("!can't easily save current script");
    todo("!hand editing points doesn't discard cache or trigger redraw reliably");
    todo("!define constraints on radius of circle in relation to population, tile size");

    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID);
    {
      c.label("Calculate minimum bounding box of objects").addLabel();

      c.columns(".x");
      c.open("cluster params");
      {
        c.spanx();
        c.label("Generate").addToggleButton(GENERATE);

        c.label("Seed:").addLabel();
        c.withDisplay();
        c.max(100).addSlider(SEED);
        c.label("Count:").addLabel();
        c.max(500).defaultVal(100).addSlider(COUNT);
        c.label("Stickyness:").addLabel();
        c.max(100).defaultVal(20).addSlider(STICKYNESS);
        c.label("Radius:").addLabel();
        c.max(100).defaultVal(20).addSlider(NBR_RAD);

        c.label("Bgnd image").addToggleButton(RENDER_BGND_IMAGE);
        c.label("Tiles").addToggleButton(RENDER_TILES);
        c.label("Merge").addToggleButton(MERGE);
        c.label("Interpolate").addToggleButton(INTERPOLATE);
        c.spanx();
        c.label("Cache").addToggleButton(CACHE);
        c.label("Zoom:").addLabel();
        c.max(300).addSlider(ZOOM);
        c.label("# Colors:").addLabel();
        c.min(1).max(4).addSlider(NUM_COLORS);

      }
      c.close("cluster params");

    }
    c.closeTab();
  }

  public void processUserEvent(UserEvent event) {
    if (event.isWidget()) {
      if (widgets().vb(GENERATE))
        generate();
    }
  }

  public void runAlgorithm() {
    WidgetManager g = widgets();

    constructInputPoints();

    AlgorithmStepper s = AlgorithmStepper.sharedInstance();

    s.msg("starting algorithm");

    float targZoom;
    {
      var app = geomApp();
      var z = g.vi(ZOOM);
      targZoom = interpolateBetweenScalars(0.8f, 9f, z / 300f);
      app.setZoomFactor(targZoom);
    }
    var ts = tileSizeForZoom(targZoom);

    // If there are no points yet, throw out cached grids
    if (pointsAreNew()) {
      mPointGridCache = hashMap();
    }

    mParam = ts;
    int nc = g.vi(NUM_COLORS);
    for (int i = 0; i<nc; i++) {
      mGridPairs.add( buildGridPair(ts.tileSize,i,nc));
    }

  }

  private GridPair buildGridPair(int tileSize, int colorCode, int maxColors) {
    var p = new GridPair();
    p.mGrid0 = buildGrid(tileSize,   colorCode,maxColors);
    if (widgets().vb(MERGE))
      p.mGrid1 = buildGrid(tileSize * 2,  colorCode,maxColors);
    return p;
  }

  private static int gridCacheKey(int tileSize, int colorCode) {
    return (tileSize << 8) + colorCode;
  }
  private PointGrid buildGrid(int tileSize, int colorCode, int maxColors) {
    if (mPointGridCache == null) mPointGridCache = hashMap();
    var key = gridCacheKey(tileSize, colorCode);

    var result = mPointGridCache.get(key);
    if (result == null) {
      result = new PointGrid(tileSize,colorCode);
      var input = mCachedPoints;
      for (var pt : input) {
        result.insert(pt);
      }
      mPointGridCache.put(key, result);
    }
    return result;
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
    var hash = widgetValueHash(SEED, COUNT, STICKYNESS, NBR_RAD, SEED);

    boolean cacheIsValid =
        widgets().vb(CACHE) &&
            mCachedPoints != null && hash == mCachedPointsHashCode && !widgets().vb(GENERATE);
    if (!cacheIsValid) {
      mCachedPointsHashCode = hash;
      mPointsAreNewFlag = true;
      List<IPoint> points = arrayList();
      for (ScriptElement elem : scriptManager().state().elements()) {
        if (elem.is(PointElement.DEFAULT_INSTANCE))
          points.add(elem.location());
      }
      mCachedPoints = points;
    }
    checkState(mCachedPoints != null);
    return mCachedPoints;
  }

  private boolean pointsAreNew() {
    return mPointsAreNewFlag;
  }

  private List<IPoint> mCachedPoints;
  private boolean mPointsAreNewFlag;
  private int mCachedPointsHashCode;

  private Image mImage;

  @Override
  public void paintView() {
    WidgetManager g = widgets();
    if (g.vb(RENDER_BGND_IMAGE)) {
      if (mImage == null) {
        mImage = ImgUtil.read(new File("background.jpg"));
      }
      Render.graphics().drawImage(mImage, 0, 0, null);
    }

    for (var p : mGridPairs) {
      p.mGrid0.render(mParam.param, p.mGrid1);
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
        var nearbyPoint =
            pointOnCircle(stickyOrigin, r.nextFloat() * (360f * M_DEG), r.nextFloat() * stickyRadius);
        if (!clip.contains(nearbyPoint)) {
          continue;
        }
        newLoc = nearbyPoint;
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
    p.idealTileSize = Math.max(1, 30f / p.zoomFactor);
    var log2 = Math.log(p.idealTileSize) / Math.log(2);
    checkState(log2 >= 0);
    p.exponent = (int) Math.floor(log2);
    p.param = (float) (log2 - p.exponent);
    p.tileSize = (int) Math.pow(2f, p.exponent);
    return p;
  }

  private TileSizeParam mParam;
  private List< GridPair> mGridPairs = arrayList();
 private Map<Integer, PointGrid> mPointGridCache;


  private class GridPair {
    PointGrid mGrid0;
    PointGrid mGrid1;
  }
}

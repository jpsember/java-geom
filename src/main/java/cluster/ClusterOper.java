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
package cluster;

import cluster.gen.QtreeParam;
import cluster.gen.RoadNetwork;
import geom.EditorElement;
import geom.GeomApp;
import geom.elem.EditablePointElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import cluster.gen.PointEvent;
import js.data.IntArray;
import js.file.Files;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.graphics.ImgUtil;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
import sample.RenderItem;
import testbed.AlgorithmStepper;
import testbed.Render;
import testbed.TestBedOperation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static js.geometry.MyMath.*;
import static cluster.ClusterGlobals.*;
import static testbed.Render.*;

public class ClusterOper implements TestBedOperation {

  private static final String OPER_ID = "cluster";

  @Override
  public String operId() {
    return OPER_ID;
  }

  public void addControls(WidgetManager c) {
    todo("!if warning msg > ~90 chars, IDE doesn't make it clickable");

    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID);
    {
      c.label("Demonstrate Point Coalescing with Snap to Topology").addLabel();


      c.columns(".x");
      c.open("cluster params");
      {

        c.label("Zoom:").addLabel();
        c.max(1000).addSlider(ZOOM);

        c.spanx();
        c.label("Representative pts").defaultVal(true).addToggleButton(RENDER_REPRESENTATIVE);

        c.spanx();
        c.label("Snap to topology").defaultVal(true).addToggleButton(SNAP);

        c.spanx();
        c.label("Show tiles").addToggleButton(RENDER_TILES);

        c.spanx();
        c.label("Interpolate").defaultVal(true).addToggleButton(INTERPOLATE);

        c.addHidden(CACHE_GRID, true);

        c.addHidden(SORT_BY_Z, true);
        c.label("# Colors:").addLabel();
        c.min(1).max(4).addSlider(NUM_COLORS);

        c.spanx();
        c.addHidden(RADIUS_FACTOR, 20);

        c.spanx();
        c.addHidden(RENDER_BGND_IMAGE, false);

        c.spanx();
        c.addHidden(RENDER_GRID_POINTS, true);
      }
      c.close("cluster params");

    }
    c.closeTab();
  }

  private void aux(String label, String id) {
    var c = widgets();

    c.label(label).defaultVal(false).addToggleButton(id + "_active");
    c.min(0).max(100).defaultVal(0).addSlider(id);
  }

  private void auxs(String label, String id) {
    var c = widgets();

    c.label(label).defaultVal(false).addToggleButton(id + "_active");
    c.min(-100).max(100).defaultVal(0).addSlider(id);
  }

  public void processUserEvent(UserEvent event) {
  }

  public void runAlgorithm() {

    getTopology();
    WidgetManager g = widgets();

    constructPointEvents();

    AlgorithmStepper s = AlgorithmStepper.sharedInstance();

    s.msg("starting algorithm");

    float targZoom;
    {
      var zoomWidgetValue = g.vi(ZOOM);
      targZoom = interpolateBetweenScalars(1.7f, 12f, zoomWidgetValue / 300f);
      geomApp().setZoomFactor(targZoom);
    }
    var ts = tileSizeForZoom(targZoom);
    mParam = ts;

    mGrid0 = buildGrid(ts.tileSize);
    if (g.vb(INTERPOLATE) /*g.vb(MERGE)*/)
      mGrid1 = buildGrid(ts.tileSize * 2);
  }

  private Map<Integer, Map<Integer, List<FPoint>>> mTileCaches = hashMap();

  private PointGrid buildGrid(int tileSize) {
    var result = mPointGridCache.get(tileSize);
    if (result == null) {
      var ncol = widgets().vi(NUM_COLORS);
      result = new PointGrid(tileSize, ncol, mCachedPoints);
      todo("!assuming older caches are valid");
      var cache = mTileCaches.get(tileSize);
      if (cache == null) {
        cache = hashMap();
        mTileCaches.put(tileSize, cache);
      }
      result.withTileTopologyCache(cache);

      if (widgets().vb(CACHE_GRID))
        mPointGridCache.put(tileSize, result);
      else alert("!not caching grids");
    }
    return result;
  }

  private void constructPointEvents() {
    // Only rebuild point grids if the input point events have changed
    List<PointEvent> points = arrayList();

    // We use a random generator to determine PointEvent colors
    var rand = new Random(1965);

    var nc = widgets().vi(NUM_COLORS);

    for (ScriptElement elem : scriptManager().state().elements()) {
      if (!elem.is(PointElement.DEFAULT_INSTANCE)) continue;
      var b = PointEvent.newBuilder();
      b.colorCode(rand.nextInt(nc));
      b.location(elem.location().toFPoint());
      b.zLoc(rand.nextFloat());
      points.add(b.build());
    }

    var currentHashCode = points.hashCode();

    if (mCachedPoints == null || currentHashCode != mCachedPointsHashCode) {
      mCachedPointsHashCode = currentHashCode;
      mCachedPoints = points;
      clearGridCache();
    }
  }


  private BufferedImage bgndImage() {
    if (mImage == null) {
      mImage = ImgUtil.read(new File("victoria.jpg"));
    }
    return mImage;
  }

  @Override
  public void paintView() {
    WidgetManager g = widgets();
    if (g.vb(RENDER_BGND_IMAGE)) {
      Render.graphics().drawImage(bgndImage(), 0, 0, null);
    }

    renderTopology();

    List<RenderItem> stack = arrayList();

    QuadTree quadTreeForSnap = null;
    if (g.vb(SNAP))
      quadTreeForSnap = quadTree();

    mGrid0.render(mParam.param, mGrid1, stack, quadTreeForSnap);

    float zoomCompensation = getScale();
    var strokeWidth = 1.5f * zoomCompensation;
    var pointSetStroke = new BasicStroke(strokeWidth);
    stroke(pointSetStroke);

    // Sort stacked discs by z
    if (g.vb(SORT_BY_Z))
      stack.sort((o1, o2) -> Float.compare(o1.zSort, o2.zSort));

    boolean first = false;
    for (var ri : stack) {
      color(ri.color);
      fillCircle(ri.origin, ri.radius);
      var boundaryGray = 255;
      // Render a boundary using white, but with the same alpha as the color
      var borderColor = new Color(boundaryGray, boundaryGray, boundaryGray, ri.color.getAlpha());
      color(borderColor);

      // we need to increase the radius so the boundary doesn't overlap
      // the colored interior, otherwise the boundary looks fuzzy (and is not white)
      drawCircle(ri.origin, ri.radius + strokeWidth / 2);
    }
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
    p.idealTileSize = Math.max(1, 120f / p.zoomFactor);
    var log2 = Math.log(p.idealTileSize) / Math.log(2);
    checkState(log2 >= 0);
    p.exponent = (int) Math.floor(log2);
    p.param = (float) (log2 - p.exponent);
    p.tileSize = (int) Math.pow(2f, p.exponent);
    return p;
  }

  private void clearGridCache() {
    mPointGridCache.clear();
  }

  private int mCachedPointsHashCode;
  private List<PointEvent> mCachedPoints;
  private BufferedImage mImage;
  private TileSizeParam mParam;
  private PointGrid mGrid0, mGrid1;
  private final Map<Integer, PointGrid> mPointGridCache = hashMap();


  private void renderTopology() {
    var t = getTopology();
//if (alert("not rendering")) return;

//    float zoomCompensation = getScale();
    var strokeWidth = 0.45f; // * zoomCompensation;
    var pointSetStroke = new BasicStroke(strokeWidth);
    stroke(pointSetStroke);

    color(new Color(111, 76, 138, 128));
    for (var s : t.roadSegments()) {
      drawLine(s.a().toIPoint(), s.b().toIPoint());
    }
  }

  // ----------------------------------------------------------------------------------------------
  // Parsing road network, bus events
  // ----------------------------------------------------------------------------------------------

  private RoadNetwork getTopology() {
    if (mTopology == null) {
      todo("!add ability to load different sample data");

      var d = new File("sample_data");
      Files.assertDirectoryExists(d, "road network and bus events");
      var topology = new File(d, "road_network.csv");

      var nr = new RoadNetworkReader();
      //nr.withMax(5000);
      nr.setGeomColumnName("geom");
      var out = nr.parse(topology);
      mTopology = out.build();
      mQuadTree = constructQuadTreeFromRoadNetwork(mTopology);
    }
    return mTopology;
  }

  private QuadTree quadTree() {
    getTopology();
    return mQuadTree;
  }

  private QuadTree constructQuadTreeFromRoadNetwork(RoadNetwork nw) {
    var segs = IntArray.newBuilder();
    var ps = new PointSet();
    for (var x : nw.roadSegments()) {
      segs.add(ps.add(x.a()));
      segs.add(ps.add(x.b()));
    }
    ps.freeze();

    var param = QtreeParam.newBuilder();
    // These are NOT lat long, but pixels; so set param accordingly
    param.minNodeDimension(1f);

    var q = new QuadTree(param, ps, segs.array());
    return q;
  }


  private RoadNetwork mTopology;
  private QuadTree mQuadTree;


}

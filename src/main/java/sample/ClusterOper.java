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
import geom.gen.cluster.PointEvent;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.MyMath;
import js.graphics.ImgUtil;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
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
import static sample.ClusterGlobals.*;
import static testbed.Render.*;

public class ClusterOper implements TestBedOperation {

  private static final String OPER_ID = "cluster";


  @Override
  public String operId() {
    return OPER_ID;
  }

  public void addControls(WidgetManager c) {
    todo("!can't easily save current script");
    todo("!hand editing points doesn't discard cache or trigger redraw reliably");
    todo("!define constraints on radius of circle in relation to population, tile size");
    todo("!have 'sticky' render things in lines, simulating road segments");
    todo("circle zooming seems out of scale, things remain small when at high zoom factor");


    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID);
    {
      c.label("Calculate minimum bounding box of objects").addLabel();

      c.columns(".x");
      c.open("cluster params");
      {
        c.spanx();
        c.label("Generate").defaultVal(true).addToggleButton(GENERATE);

        c.label("Seed:").addLabel();
        c.withDisplay();
        c.max(100).addSlider(SEED);
        c.label("Count:").addLabel();
        c.max(500).defaultVal(100).addSlider(COUNT);
        c.label("Stickyness:").addLabel();
        c.max(100).defaultVal(20).addSlider(STICKYNESS);
        c.label("Sticky Radius:").addLabel();
        c.max(100).defaultVal(20).addSlider(NBR_RAD);
        c.label("Disc Radius:").addLabel();
        c.max(100).defaultVal(20).addSlider(RADIUS_FACTOR);


        c.label("Bgnd image").defaultVal(true).addToggleButton(RENDER_BGND_IMAGE);
        c.label("Tiles").addToggleButton(RENDER_TILES);
        c.label("Merge").defaultVal(true).addToggleButton(MERGE);
        c.label("Interpolate").defaultVal(true).addToggleButton(INTERPOLATE);
        c.spanx();
        c.label("Sort by z").defaultVal(true).addToggleButton(SORT_BY_Z);
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
    mNumColors = g.vi(NUM_COLORS);

    constructPointEvents();

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

    mParam = ts;

    mGrid0 = buildGrid(ts.tileSize);
    if (widgets().vb(MERGE))
      mGrid1 = buildGrid(ts.tileSize * 2);
  }

  private PointGrid buildGrid(int tileSize) {
    var result = mPointGridCache.get(tileSize);
    if (result == null) {
      result = new PointGrid(tileSize, mNumColors);
      mPointGridCache.put(tileSize, result);
      for (var evt : mCachedPoints) {
        result.insert(evt);
      }
    }
    return result;
  }

  private void constructPointEvents() {
    // Only rebuild point grids if the input point events have changed
    List<PointEvent> points = arrayList();

    // We use a random generator to determine PointEvent colors
    var rand = new Random(1965);

    for (ScriptElement elem : scriptManager().state().elements()) {
      if (!elem.is(PointElement.DEFAULT_INSTANCE)) continue;
      var b = PointEvent.newBuilder();
      b.colorCode(rand.nextInt(mNumColors));
      b.location(elem.location().toFPoint());
      b.zLoc(rand.nextFloat());
      points.add(b.build());
    }

    var currentHashCode = points.hashCode();
    if (mCachedPoints == null || currentHashCode != mCachedPointsHashCode) {
      mCachedPointsHashCode = currentHashCode;
      mCachedPoints = points;
      mPointGridCache = hashMap();
    }
  }

  private int mCachedPointsHashCode;
  private List<PointEvent> mCachedPoints;
  private BufferedImage mImage;

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

    List<RenderItem> stack = arrayList();
    checkState(mGrid0 != null);
    mGrid0.render(mParam.param, mGrid1, stack);

    float zoomCompensation = getScale();
    var pointSetStroke = new BasicStroke(1.5f * zoomCompensation);
    stroke(pointSetStroke);


    // Sort stacked discs by z
    if (g.vb(SORT_BY_Z))
      stack.sort((o1, o2) -> Float.compare(o1.zSort, o2.zSort));

    for (var ri : stack) {
      color(ri.color);
      fillCircle(ri.origin, ri.radius);
    }
  }

  private void generate() {
    WidgetManager g = widgets();
    int seed = g.vi(SEED);
    Random r = new Random(seed + 1);
    int c = g.vi(COUNT);
    List<EditorElement> elemList = arrayList();

    final int PADDING = 0;

    IPoint size = ImgUtil.size(bgndImage());
    float sx = size.x;
    float sy = size.y;

    var clip = new FRect(0, 0, sx, sy);
    FPoint stickyOrigin = null;
    var stickyness = g.vi(STICKYNESS) + 1;
    var stickyRadius = (g.vi(NBR_RAD) / 100.f) * Math.min(size.x, size.y);

    int[] angles = {
        0, 30, 45, 60, 90, 120, 90 + 45,
    };

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
  private PointGrid mGrid0, mGrid1;
  private Map<Integer, PointGrid> mPointGridCache = hashMap();
  private int mNumColors;

}

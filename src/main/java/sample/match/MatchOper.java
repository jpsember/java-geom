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
package sample.match;

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static testbed.Colors.*;
import static testbed.Render.*;
import java.util.List;
import java.util.Random;

import geom.EditorElement;
import geom.GeomApp;
import geom.elem.EditablePointElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
import testbed.*;

public class MatchOper implements TestBedOperation {

  private static final String OPREF = "mt_";

  private static final String OPER_ID = "oper_match" //
      , COUNT = OPREF + "count" //
      , SEED = OPREF + "seed"//
  ;

  @Override
  public String operId() {
    return OPER_ID;
  }

  public void addControls(WidgetManager c) {

    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID + ":Match");
    {
      c.label("Match polylines to database").addLabel();

      c.columns(".x");
      c.open("random params");
      {
        c.label("Seed:").addLabel();
        c.withDisplay();
        c.max(100).addSlider(SEED);
        c.label("Count:").addLabel();
        c.max(100).defaultVal(12).addSlider(COUNT);
      }
      c.close("random params");

      c.columns(".x");
      c.open("listener experiment");
      c.pushListener((x) -> pr("outer listener, id:", x));
      {
        c.label("Slider1:").addLabel();
        c.max(100).listener((x) -> pr("slider1 listener", x)).addSlider(OPREF + "slider_1");
        c.label("Slider2:").addLabel();
        c.max(100).listener((x) -> pr("slider2 listener", x)).defaultVal(12).addSlider(OPREF + "slider_2");
        c.label("Slider3:").addLabel();
        c.max(100).defaultVal(12).addSlider(OPREF + "slider_3");
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

    // Construct algorithm input: a list of polylines

    List<IPoint> points = arrayList();
    for (ScriptElement elem : scriptManager().state().elements()) {
      if (elem.is(PointElement.DEFAULT_INSTANCE))
        points.add(elem.location());
    }

    //    mBounds = null;
    //    mFinalBounds = null;

    AlgorithmStepper s = AlgorithmStepper.sharedInstance();

    // This is an 'unguarded' call to s.msg():
    s.msg("starting algorithm");

    // We don't need to call s.update(), but we can do so, as an optimization, if we 
    // wish to avoid unnecessary calls to s.msg() (i.e., so we avoid constructing an 
    // array of arguments that will just be ignored).
    // 
    // This is a 'guarded' call to s.msg():
    //
    if (s.update())
      s.msg("guarded update call");

    //    for (IPoint pt : points) {
    //      if (mBounds == null) {
    //        mBounds = new IRect(pt.x, pt.y, 0, 0);
    //        s.msg("initial point", pt, mBounds);
    //      } else {
    //        mBounds = mBounds.including(pt);
    //        s.msg("next point", pt, mBounds);
    //      }
    //    }
    //
    //    mFinalBounds = mBounds;
  }

  @Override
  public void paintView() {

    var ds = dset();

    ds.prepareRender();

    var m = ds.geomToViewTransform();

    color(GREEN, 0.8);
    stroke(STRK_THIN);

    var g = graphics();
    var oldTfm = g.getTransform();
    g.transform(m.toAffineTransform());

    int count = 0;
    for (var n : ds.nodes()) {
      if (++count > 20) break;
      FPoint prev = null;
      for (var pt : n.vertices()) {
        if (prev != null) {
          drawLine(prev, pt);
          pr("drawline", prev, "=>", pt);
        }
        prev = pt;
        var h = m.apply(pt.x, pt.y);
        pr("geom->view tfm for:",pt.x,pt.y,"is:",h);
      }
      g.setTransform(oldTfm);
    }

    pr("tfm:", m);

    //    if (mFinalBounds != null) {
    //      stroke(STRK_RUBBERBAND);
    //      color(GREEN, 0.2);
    //      drawRect(mFinalBounds);
    //    }
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
    for (int i = 0; i < c; i++) {
      elemList.add(EditablePointElement.DEFAULT_INSTANCE
          .withLocation(new IPoint(r.nextFloat() * sx + PADDING, r.nextFloat() * sy + PADDING)));
    }

    Command.Builder b = Command.newBuilder();
    ScriptEditState editState = scriptManager().state();
    // We must discard any selected elements, as they may no longer exist
    b.newState(editState.toBuilder().elements(elemList).selectedElements(null));
    geomApp().perform(b);
    geomApp().performRepaint(GeomApp.REPAINT_EDITOR);
  }

  private Dataset dset() {
    if (mDataset != null)
      return mDataset;
    var d = new Dataset();
    d.setVerbose();
    d.prepare();
    mDataset = d;
    return mDataset;
  }

  private Dataset mDataset;
}

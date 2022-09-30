package sample;

import static geom.GeomTools.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Random;

import geom.EditorElement;
import geom.GeomApp;
import geom.elem.EditablePointElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.PointElement;
import js.graphics.ScriptElement;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;
import testbed.*;

public class BoundsOper implements TestBedOperation {

  private static final String OPER_ID = "oper_bounds";

  private static final String SEED = "bo_seed";
  private static final String COUNT = "bo_count";

  @Override
  public String operId() {
    return OPER_ID;
  }

  public void addControls(WidgetManager c) {

    // To demonstrate that the oper id can be different than its UI label, make them distinct:
    //
    c.openTab(OPER_ID + ":Bounds");
    {
      c.label("Calculate minimum bounding box of objects").addLabel();

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
        c.max(100).listener((x) -> pr("slider1 listener", x)).addSlider("exp_slider_1");
        c.label("Slider2:").addLabel();
        c.max(100).listener((x) -> pr("slider2 listener", x)).defaultVal(12).addSlider("exp_slider_2");
        c.label("Slider3:").addLabel();
        c.max(100).defaultVal(12).addSlider("exp_slider_3");
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

    // Construct algorithm input: a list of points 

    List<IPoint> points = arrayList();
    for (ScriptElement elem : scriptManager().state().elements()) {
      if (elem.is(PointElement.DEFAULT_INSTANCE))
        points.add(elem.location());
    }

    mBounds = null;

    AlgorithmStepper s = AlgorithmStepper.sharedInstance();

    // This is an 'unguarded' call to s.msg():
    s.msg("algorithm step 1");

    // We don't need to call s.update(), but we can do so, as an optimization, if we 
    // wish to avoid unnecessary calls to s.msg() (i.e., so we avoid constructing an 
    // array of arguments that will just be ignored).
    // 
    // This is a 'guarded' call to s.msg():
    //
    if (s.update())
      s.msg("guarded update call");

    for (IPoint pt : points) {
      if (mBounds == null) {
        mBounds = new IRect(pt.x, pt.y, 0, 0);
        s.msg("initial point", pt, mBounds);
      } else {
        mBounds = mBounds.including(pt);
        s.msg("next point", pt, mBounds);
      }
    }

    s.msg("algorithm step 2");
    todo("have ability for rendering stuff after algorithm steps have concluded (even if interrupted)");
  }

  @Override
  public void paintView() {
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
    b.newState(editState.toBuilder().elements(elemList));
    geomApp().perform(b);
    geomApp().performRepaint(GeomApp.REPAINT_EDITOR);
  }

  private IRect mBounds;

}

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
import static testbed.TestBedTools.*;

public class BoundsOper implements TestBedOperation, Globals {

  private static final String SEED = "bo_seed";
  private static final String COUNT = "bo_count";

  public void addControls(WidgetManager c) {

    c.tabTitle("Bounds");
    c.open("Bounds tab");
    c.addLabel("Calculate minimum bounding box of objects");

    
    c.open("random params");
    c.max(100).addSlider(SEED);
    c.max(100).defaultVal(12).addSlider(COUNT);
    c.close("random params");
//    {
//      c.open();
//      c.intSlider(SEED, "Seed", "Random number generator seed", 0, 100, 0, 1);
//      c.newColumn();
//      c.intSpinner(COUNT, "Count", "Number to generate", 0, 100, 12, 1);
//      c.close();
//    }
//    c.button(9999, "Hello", "Sample button") //
//        .doubleSlider(9988, "DoubleSlid", "Double slider", 5.2, 10.2, 7.5, 0.1) //
//        .doubleSpinner(9989, "DblSpin", "Double spinner", 5.2, 10.2, 7.5, 0.1) //
//        .textField(9970, "field", "hint", 12, true, "hello") //
//        .textArea(9971, "area", "text area", true, "hello");
//
//    if (false) { // haven't added full support for these yet
//      c.openComboBox(7800, "Combo Box", "This is a combo box", true) //
//       .choice(7805, "choice A") //
//       .choice(7806, "choice B") //
//      .choice(7807, "choice C") //
//       .closeComboBox();
//    }

    c.close("Bounds tab");
  }

  public void processAction(UserEvent event) {
    WidgetManager g = gadg ();
    pr("action:", event);
    
    todo("there ought to be a convenience method for this");
    if (event.getCode() == UserEvent. CODE_WIDGET) {
      generate();
    }
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
    WidgetManager g = gadg ();
     int seed = g.vi(SEED);
    Random r = new Random(seed + 1);
    int c = g.vi(COUNT);
    List<EditorElement> elemList = arrayList();

    final int PADDING = 3;

    IPoint size = editor().getEditorPanel().pageSize();
    float sx = size.x - 2 * PADDING;
    float sy = size.y - 2 * PADDING;
    for (int i = 0; i < c; i++) {
      elemList.add(EditablePointElement.DEFAULT_INSTANCE
          .withLocation(new IPoint(r.nextFloat() * sx + PADDING, r.nextFloat() * sy + PADDING)));
    }

    Command.Builder b = Command.newBuilder();
    ScriptEditState editState = scriptManager().state();
    b.newState(editState.toBuilder().elements(elemList));
    editor().perform(b);
    editor().performRepaint(GeomApp.REPAINT_EDITOR);
  }

  private IRect mBounds;
}

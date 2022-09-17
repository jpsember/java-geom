package sample;

import static geom.GeomTools.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Random;

import geom.EditorElement;
import geom.GeomApp;
import geom.elem.EditableRectElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import js.geometry.FRect;
import js.geometry.IPoint;
import js.geometry.IRect;
import testbed.*;

public class BoundsOper implements TestBedOperation, Globals {

  private static final int SEED = 1800;
  private static final int COUNT = 1801;

  public void addControls() {

    C.sOpenTab("Bounds");
    C.sStaticText("Calculate minimum bounding box of objects");

    {
      C.sOpen();
      C.sIntSlider(SEED, "Seed", "Random number generator seed", 0, 100, 0, 1);
      C.sNewColumn();
      C.sIntSpinner(COUNT, "Count", "Number to generate", 0, 100, 12, 1);
      C.sClose();
    }
    C.sCloseTab();
  }

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      generate();
    }
  }

  public void runAlgorithm() {
    if (T.update())
      T.msg("algorithm step 1");

    //    EdDisc[] obj = SampleMain.getDiscs();
    //    for (EdDisc d : obj) {
    //      if (T.update())
    //        T.msg("disc"+T.show(d));
    //    }

    if (T.update())
      T.msg("algorithm step 2");
  }

  @Override
  public void paintView() {
    pr("paintView not doing anything");
  }

  private void generate() {
    int seed = C.vi(SEED);
    Random r = new Random(seed + 1);
    int c = C.vi(COUNT);
    List<EditorElement> elemList = arrayList();

    final int PADDING = 3;

    IPoint size = editor().getEditorPanel().pageSize();
    float sx = size.x - 2 * PADDING;
    float sy = size.y - 2 * PADDING;
    for (int i = 0; i < c; i++) {
      float rad = 10;
      IRect bounds = new FRect(r.nextFloat() * sx + PADDING, r.nextFloat() * sy + PADDING, rad, rad)
          .toIRect();
      EditableRectElement elem = EditableRectElement.DEFAULT_INSTANCE.withBounds(bounds);
      elemList.add(elem);
    }

    Command.Builder b = Command.newBuilder();
    ScriptEditState editState = scriptManager().state();
    b.newState(editState.toBuilder().elements(elemList));
    editor().perform(b);
    editor().performRepaint(GeomApp.REPAINT_EDITOR);
  }
}

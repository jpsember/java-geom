package sample;

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

import static geom.GeomTools.*;
import static js.base.Tools.*;

public class GeneratorOper implements TestBedOperation, Globals {

  private static final int SEED = 1600;
  private static final int COUNT = 1601;
  private static final int MINRAD = 1602;
  private static final int MAXRAD = 1603;
  private static final int POSITION = 1606;
  private static final int TYPE = 1608;
  private static final int RNDSQR = 1609;
  private static final int CIRCLE = 1610;
  private static final int SPIRAL = 1611;
  private static final int RNDDISC = 1612;
  private static final int SHADOWS = 1613;
  private static final int SQUARE = 1614;
  private static final int SEGS = 1615;

  public void addControls() {
    C.sOpenTab("Gen");
    C.sStaticText("Generates disc sites");
    C.sCheckBox(SQUARE, "Squares", "True to set width & height equal", false);
    {
      C.sOpenComboBox(TYPE, "Pattern", "Select pattern of random discs", false);
      C.sChoice(RNDDISC, "random (disc)");
      C.sChoice(RNDSQR, "random (square)");
      C.sChoice(CIRCLE, "circle");
      C.sChoice(SPIRAL, "spiral");
      C.sChoice(SHADOWS, "bitangents");
      C.sChoice(SEGS, "segments");
      C.sCloseComboBox();
    }
    {
      C.sOpen();
      {
        C.sOpen();
        C.sIntSlider(SEED, "Seed", "Random number generator seed (zero for unseeded)", 0, 100, 0, 1);
        C.sNewColumn();
        C.sIntSpinner(COUNT, "Count", "Number to generate", 0, 100, 12, 1);
        C.sClose();
      }
      {
        C.sOpen();
        C.sIntSlider(MINRAD, "Min Radius", "Minimum radius", 0, 30, 0, 1);
        C.sNewColumn();
        C.sIntSlider(MAXRAD, "Max Radius", "Maximum radius", 0, 250, 50, 1);
        C.sClose();
      }
      {
        C.sOpen();
        C.sIntSlider(POSITION, "Position", "Distance from center (circular)", 0, 200, 30, 1);
        C.sNewColumn();
        C.sClose();
      }

      C.sClose();
    }
    C.sCloseTab();
  }

  public static void generateRandom() {
    genRand(C.vi(TYPE));
  }

  private static void genRand(int type) {
    int seed = C.vi(SEED);
    Random r = seed == 0 ? new Random() : new Random(seed);
    int c = C.vi(COUNT);
    List<EditorElement> elemList = arrayList();

    int radMin = Math.min(C.vi(MINRAD), C.vi(MAXRAD));
    int radMax = Math.max(C.vi(MINRAD), C.vi(MAXRAD));
    float range = (radMax - radMin) ;
    final int PADDING = 3;
    
    IPoint size = editor().getEditorPanel().pageSize();
    float sx = size.x - 2 * PADDING;
    float sy = size.y - 2 * PADDING;
    for (int i = 0; i < c; i++) {
      float rv = r.nextFloat();
      float rad = rv * rv * range + radMin;
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

  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      default:
        if (a.ctrlId / 100 == SEED / 100)
          genRand(C.vi(TYPE));
        break;
      }
    }
  }

  public void runAlgorithm() {
  }

  public void paintView() {
    // Editor.render();
  }

}

package sample;

import js.app.App;
import js.json.JSMap;
import testbed.*;

import static js.base.Tools.*;

public class SampleMain extends TestBed {

  private static final int RANDOM = 4011;//!
  private static final int TYPE = 4013;//!
  private static final int DISC = 4014;//!
  private static final int SQUARE = 4015;//!
  private static final int RECT = 4016;//!
  private static final int POLY = 4017;//!

  public static final int DISCS = 0, SQUARES = 1, RECTS = 2, POLYGONS = 3;

  public static void main(String[] args) {
    loadTools();
    App app = new SampleMain();
    app.startApplication(args);
  }

  @Override
  public boolean hasImageSupport() {
    return false;
  }

  private SampleMain() {
    guiAppConfig() //
        .appName("Sample") //
        .keyboardShortcutRegistry(JSMap.fromResource(this.getClass(), "key_shortcut_defaults.json")) //;
    ;
  }

  // -------------------------------------------------------
  // TestBed overrides
  // -------------------------------------------------------

  @Override
  public void addOperations() {
    addOper(new SimpleOper());
    addOper(new GeneratorOper());
  }

  public static int regionType() {
    return C.vi(TYPE) - DISC;
  }

  @Override
  public void addControls() {
    C.sOpen();
    // C.sCheckBox(RECTS, "Rectangles", null, false);

    {
      C.sOpen();
      C.sButton(RANDOM, "Random", "Generate random discs");

      C.sOpenComboBox(TYPE, "Regions", "Select uncertain regions", false);
      C.sChoice(DISC, "disc");
      C.sChoice(SQUARE, "square");
      C.sChoice(RECT, "rectangle");
      C.sChoice(POLY, "polygon");
      C.sCloseComboBox();
      C.sClose();
    }

    C.sClose();
  }

  //  public void initEditor() {
  //    Editor.addObjectType(EdPolygon.FACTORY);
  //    Editor.addObjectType(EdDisc.FACTORY);
  //    Editor.addObjectType(EdSegment.FACTORY);
  //    Editor.addObjectType(EdPoint.FACTORY);
  //    Editor.addObjectType(EdRect.FACTORY);
  //
  //    Editor.openMenu();
  //    C.sMenuItem(TOGGLEDISCS, "Toggle discs/points", "!^t");
  //    C.sMenuItem(MAKETANGENT, "Set disc tangent", "!^3"); //"!^g");
  //    Editor.closeMenu();
  //  }

  @Override
  public void processAction(TBAction a) {
    if (a.code == TBAction.CTRLVALUE) {
      switch (a.ctrlId) {
      case RANDOM:
        GeneratorOper.generateRandom();
        break;
      }
    }
  }

  //  private static DArray getCandidate(final EdDisc c) {
  //    // find best two candidates for supporting this disc
  //    DArray can = new DArray(discs);
  //    can.sort(new Comparator() {
  //      public int compare(Object arg0, Object arg1) {
  //        EdDisc c1 = (EdDisc) arg0, c2 = (EdDisc) arg1;
  //
  //        double d1 = DiscUtil.itan(c, c1) ? DiscUtil.itanDist(c, c1) : DiscUtil.otanDist(c, c1);
  //        double d2 = DiscUtil.itan(c, c2) ? DiscUtil.itanDist(c, c2) : DiscUtil.otanDist(c, c2);
  //
  //        return (int) Math.signum(d1 - d2);
  //      }
  //    });
  //    return can;
  //  }

  //  /**
  //   * Make highlighted inactive discs tangent to best-fit candidate
  //   */
  //  private void makeTangent() {
  //    DArray a = Editor.editObjects(EdDisc.FACTORY, true, false);
  //    for (int k = 0; k < a.size(); k++) {
  //      EdDisc c = (EdDisc) a.get(k);
  //
  //      // find best two candidates for supporting this disc
  //      DArray can = getCandidate(c);
  //      if (can.size() < 1)
  //        continue;
  //
  //      EdDisc ca = (EdDisc) can.get(0);
  //      boolean ia = DiscUtil.itan(c, ca);
  //      c.setRadius(FPoint2.distance(c.getOrigin(), ca.getOrigin()) + (ia ? ca.getRadius() : -ca.getRadius()));
  //    }
  //  }

  public void setParameters() {
  }

  public void paintView() {
    //    discs = null;
    //    discs2 = null;
    //    rects = null;
    //    rects2 = null;
    //    polygons = null;
    super.paintView();
  }

  //  public static EdPolygon[] getPolygons() {
  //    if (polygons == null) {
  //      DArray a = Editor.readObjects(EdPolygon.FACTORY, false, true);
  //      polygons = new EdPolygon[a.size()];
  //      for (int i = 0; i < a.size(); i++) {
  //        EdPolygon ed = (EdPolygon) a.get(i);
  //        ed = EdPolygon.normalize(ed);
  //        polygons[i] = ed;
  //      }
  //    }
  //    return polygons;
  //  }

  //  public static void perturbDiscs() {
  //    Matrix m = new Matrix(3);
  //    m.setIdentity();
  //
  //    //      Matrix.identity(3, null);
  //    m.translate(-50, -50);
  //    m.rotate(MyMath.radians(1));
  //    m.translate(50, 50);
  //
  //    for (Iterator it = Editor.editObjects(EdDisc.FACTORY, false, false).iterator(); it.hasNext();) {
  //      EdDisc ed = (EdDisc) it.next();
  //      FPoint2 loc = ed.getOrigin();
  //      loc = m.apply(loc, null);
  //      ed.setPoint(0, loc);
  //    }
  //    Editor.unselectAll();
  //  }

  //  public static EdDisc[] getDiscs2() {
  //    getDiscs();
  //    return discs2;
  //  }
  //
  //  public static EdDisc[] getDiscs() {
  //    if (discs == null) {
  //      DArray a = Editor.readObjects(EdDisc.FACTORY, false, true);
  //      DArray b = Editor.readObjects(EdDisc.FACTORY, false, false);
  //
  //      discs = (EdDisc[]) a.toArray(EdDisc.class);
  //      discs2 = (EdDisc[]) b.toArray(EdDisc.class);
  //
  //      for (int i = 0; i < discs2.length; i++) {
  //        discs2[i].clearFlags(DiscUtil.DISC_OVERLAPPING | DiscUtil.DISC_CONTAINED);
  //      }
  //
  //    }
  //    return discs;
  //  }

  //  public static EdObject[] getRegions() {
  //    EdObject[] ret;
  //
  //    switch (regionType()) {
  //    default:
  //      throw new UnsupportedOperationException();
  //    case SampleMain.RECTS:
  //    case SampleMain.SQUARES:
  //      ret = getRects();
  //      break;
  //    case SampleMain.DISCS:
  //      ret = getDiscs();
  //      break;
  //    case SampleMain.POLYGONS:
  //      ret = getPolygons();
  //      break;
  //    }
  //    return ret;
  //  }

  //  public static EdRect[] getRects() {
  //    if (rects == null) {
  //      DArray a = Editor.readObjects(EdRect.FACTORY, false, true);
  //      DArray b = Editor.readObjects(EdRect.FACTORY, false, false);
  //
  //      rects = (EdRect[]) a.toArray(EdRect.class);
  //      rects2 = (EdRect[]) b.toArray(EdRect.class);
  //
  //      for (int i = 0; i < rects2.length; i++) {
  //        rects2[i].clearFlags(DiscUtil.DISC_OVERLAPPING | DiscUtil.DISC_CONTAINED);
  //      }
  //
  //    }
  //    return rects;
  //  }

  //  /**
  //   * Construct a string that uniquely describes a set of EdObjects
  //   * 
  //   * @param obj
  //   * @return
  //   */
  //  public static String getHash(EdObject[] obj) {
  //    StringBuilder sb = new StringBuilder();
  //    for (int i = 0; i < obj.length; i++) {
  //      sb.append(obj[i].getHash());
  //    }
  //    return sb.toString();
  //  }

  //  private static EdDisc[] discs;
  //  private static EdDisc[] discs2;
  //  private static EdRect[] rects;
  //  private static EdRect[] rects2;
  //  private static EdPolygon[] polygons;

}

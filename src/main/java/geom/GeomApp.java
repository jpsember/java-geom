package geom;

import static js.base.Tools.*;

import js.guiapp.GUIApp;
import js.guiapp.OurMenuBar;
//import js.guiapp.UserOperation;

/**
 * A GUIApp that supports editing of geometric objects
 */
public abstract class GeomApp extends GUIApp {

  private static GeomApp sSingleton;

  public GeomApp() {
    checkState(sSingleton == null, "GeomApp already constructed");
    sSingleton = this;
  }

  public static GeomApp singleton() {
    return sSingleton;
  }

  public void addEditMenu(OurMenuBar m) {
    //    m.addMenu("Edit", null);
    //    // The labels will be fetched via getLabelText(), so use placeholders ('_')
    //    addItem("undo", "_", new UndoOper());
    //    addItem("redo", "_", new RedoOper());
    //
    //    m.addSeparator();
    //
    //    addItem("cut", "Cut", new CutOper());
    //    addItem("copy", "Copy", new CopyOper());
    //    addItem("paste", "Paste", new PasteOper());
    //    m.addSeparator();
    //    addItem("select_none", "Select None", new SelectNoneOper());
    //    addItem("select_all", "Select All", new SelectAllOper());
    //    m.addSeparator();
    //    addItem("box_add", "Add Box", new RectAddOper());
    //    addItem("mask_add", "Add Mask", new MaskAddOper());
    //    addItem("pt_add", "Add Point", new PointAddOper());
    //    addItem("polygon_add", "Add Polygon", PolygonEditOper.buildAddOper(this));
    //    addItem("rotation_toggle", "Toggle Rotation", new ToggleRotationOper(this));
    //
    //    {
    //      UserOperation oper = PolygonEditOper.buildAddCurveOper(this);
    //      addItem("curve_add", "Add Curve", oper);
    //      addItem("curve_add2", "Add Curve (2)", oper);
    //    }
    //
    //    //addItem("yolo_merge", "Yolo Merge", new NonMaxSuppressOper());
  }

  public void addViewMenu(OurMenuBar m) {
    //    m.addMenu("View");
    //    addItem("zoom_in", "Zoom In", ZoomOper.buildIn());
    //    addItem("zoom_out", "Zoom Out", ZoomOper.buildOut());
    //    addItem("zoom_reset", "Zoom Reset", ZoomOper.buildReset());
  }

}

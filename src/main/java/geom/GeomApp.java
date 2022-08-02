package geom;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import geom.gen.Command;
import geom.gen.ScriptEditState;
import geom.oper.CommandOper;
import js.graphics.ScriptElement;
import js.graphics.gen.Script;
import js.guiapp.GUIApp;
import js.guiapp.OurMenuBar;
//import js.guiapp.UserOperation;
import js.guiapp.UserEventManager;

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

  // ------------------------------------------------------------------
  // Current script and edit state
  // ------------------------------------------------------------------

  /**
   * Get (immutable) current script state
   */
  public ScriptEditState state() {
    return mState;
  }

  public void setState(ScriptEditState state) {
    mState = state.build();
    if (mScript.defined()) {
      // We have to construct an array of ScriptElements, since we can't
      // just pass an array of EditorElements (even though each element implements ScriptElement)
      List<ScriptElement> elements = new ArrayList<>(mState.elements());
      Script.Builder b = Script.newBuilder();
      b.usage(mScript.data().usage());
      b.items(elements);
      mScript.setData(b.build());
    }
  }

  public ScriptWrapper currentScript() {
    return mScript;
  }

  private ScriptWrapper mScript = ScriptWrapper.DEFAULT_INSTANCE;
  private ScriptEditState mState = ScriptEditState.DEFAULT_INSTANCE;

  // ------------------------------------------------------------------
  // Commands
  // ------------------------------------------------------------------

  /**
   * Construct a command, with the final state initially set to the current
   * state
   */
  public Command.Builder buildCommand(String description) {
    Command.Builder b = Command.newBuilder().description(description);
    b.newState(state());
    return b;
  }

  public void perform(Command command) {
    command = command.build();
    undoManager().record(command);
    setState(command.newState());
  }

  public void perform(CommandOper oper) {
    UserEventManager.sharedInstance().perform(oper);
  }

  /**
   * Delete the last registered command from the undo list, in case the command
   * didn't end up producing anything useful, e.g. an incomplete polygon.
   */
  public void discardLastCommand() {
    undoManager().discardLastCommand();
  }

  public UndoManager undoManager() {
    if (mUndoManager == null)
      mUndoManager = new UndoManager(state());
    return mUndoManager;
  }

  private UndoManager mUndoManager;

  public abstract int paddingPixels();

  /**
   * This is needed for some operations that occur outside of rendering
   * operation
   */
  public abstract float zoomFactor();
  
  
// TODO: consider moving these?
  public static final int REPAINT_EDITOR = (1 << 0);
  public static final int REPAINT_INFO = (1 << 1);
  public static final int REPAINT_ALL = ~0;

}

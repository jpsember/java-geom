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

  private ScriptEditState mState = ScriptEditState.DEFAULT_INSTANCE;

  public ScriptWrapper currentScript() {
    return mScript;
  }

  public void flushScript() {
    mScript.flush();
  }

  private ScriptWrapper mScript = ScriptWrapper.DEFAULT_INSTANCE;

  public void replaceCurrentScriptWith(ScriptWrapper newScript) {
    if (newScript == mScript)
      return;

    // Copy the clipboard from the current script, so we can copy or paste with the new script
    ScriptEditState oldState = mState;
    mScript = newScript;

    // Parse the ScriptElement objects, constructing an appropriate
    // EditorElement for each
    List<EditorElement> editorElements = arrayList();
    for (ScriptElement element : newScript.data().items()) {
      // It is possible that elements are null, if they were unable to be parsed
      if (element == null)
        continue;
      EditorElement parser = EditorElementRegistry.sharedInstance().factoryForTag(element.tag(), false);
      if (parser == null) {
        pr("*** No EditorElement parser found for tag:", quote(element.tag()));
        continue;
      }
      EditorElement elem = parser.toEditorElement(element);
      EditorElement validatedElement = elem.validate();
      if (validatedElement == null) {
        pr("*** failed to validate element:", INDENT, elem);
        continue;
      }
      editorElements.add(validatedElement);
    }

    setState(ScriptEditState.newBuilder() //
        .elements(editorElements)//
        .clipboard(oldState.clipboard())//
    );

    // Discard undo manager, since it refers to a different script
    // TODO: have some support for keeping around multiple undo managers, one for each script,
    // or some finite number of them to keep memory usage down
    mUndoManager = null;
  }

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
  public float zoomFactor() {
    return 1f;
  }

  public void setZoomFactor(float zoom) {
    throw notSupported("setZoomFactor");
  }
  
  public abstract ScriptWrapper getScript();

  public abstract AbstractEditorPanel getEditorPanel();

  // TODO: consider moving these?
  public static final int REPAINT_EDITOR = (1 << 0);
  public static final int REPAINT_INFO = (1 << 1);
  public static final int REPAINT_ALL = ~0;

}

package geom;

import static js.base.Tools.*;

import geom.gen.Command;
import geom.oper.*;
import js.guiapp.GUIApp;
import js.guiapp.MenuBarWrapper;
import js.guiapp.UserEvent;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;
import static geom.GeomTools.*;

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

  @Override
  public UserOperation getDefaultUserOperation() {
    return new DefaultOper();
  }

  // ------------------------------------------------------------------
  // Menu construction methods, can be called from populateMenuBar()
  // ------------------------------------------------------------------

  public void addEditMenu(MenuBarWrapper m) {
    m.addMenu("Edit", null);
    // The labels will be fetched via getLabelText(), so use placeholders ('_')
    addItem("undo", "_", new UndoOper());
    addItem("redo", "_", new RedoOper());

    m.addSeparator();

    addItem("cut", "Cut", new CutOper());
    addItem("copy", "Copy", new CopyOper());
    addItem("paste", "Paste", new PasteOper());
    m.addSeparator();
    addItem("select_none", "Select None", new SelectNoneOper());
    addItem("select_all", "Select All", new SelectAllOper());
    m.addSeparator();
    addItem("box_add", "Add Box", new RectAddOper());
    addItem("pt_add", "Add Point", new PointAddOper());
    addItem("polygon_add", "Add Polygon", PolygonEditOper.buildAddOper());
  }

  public void addViewMenu(MenuBarWrapper m) {
    m.addMenu("View");
    addItem("zoom_in", "Zoom In", ZoomOper.buildIn());
    addItem("zoom_out", "Zoom Out", ZoomOper.buildOut());
    addItem("zoom_reset", "Zoom Reset", ZoomOper.buildReset());
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
    b.newState(scriptManager().state());
    return b;
  }

  public void perform(Command command) {
    command = command.build();
    undoManager().record(command);
    scriptManager().setState(command.newState());
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
      mUndoManager = new UndoManager(scriptManager().state());
    return mUndoManager;
  }

  public void discardUndoManager() {
    mUndoManager = null;
  }

  private UndoManager mUndoManager;

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

  public final int paddingPixels() {
    return (int) (20 / zoomFactor());
  }

  public final EditorPanel getEditorPanel() {
    return mEditorPanel;
  }

  public void constructEditorPanel() {
    mEditorPanel = new EditorPanel();
  }

  // TODO: consider moving these?
  public static final int REPAINT_EDITOR = (1 << 0);
  public static final int REPAINT_INFO = (1 << 1);
  public static final int REPAINT_ALL = ~0;

  @Override
  public void startedGUI() {
    todo("open last script by looking at config file, or an anonymous one");
    ScriptManager.setSingleton(new ScriptManager());
    ScriptManager.singleton().replaceCurrentScriptWith(ScriptWrapper.buildUntitled());
  }

  @Override
  public void userEventManagerListener(UserEvent event) {
    // Avoid repainting if default operation and just a mouse move
    // (though later we may want to render the mouse's position in an info box)
    int repaintFlags = UserEventManager.sharedInstance().getOperation().repaintRequiredFlags(event);
    if (repaintFlags != 0)
      performRepaint(repaintFlags);
    else
      pr("not performing repaint");
  }

  @Override
  public void repaintPanels(int repaintFlags) {
    if (0 != (repaintFlags & REPAINT_EDITOR)) {
      getEditorPanel().repaint();
    }
  }

  private EditorPanel mEditorPanel;

}

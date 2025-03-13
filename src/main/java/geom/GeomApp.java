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
 **/
package geom;

import static js.base.Tools.*;

import java.awt.Graphics2D;
import java.io.File;

import geom.gen.Command;
import geom.gen.ProjectState;
import geom.gen.ScriptEditState;
import geom.oper.*;
import js.base.BasePrinter;
import js.file.Files;
import js.geometry.IPoint;
import js.guiapp.GUIApp;
import js.guiapp.MenuBarWrapper;
import js.guiapp.RecentFiles;
import js.guiapp.UserEvent;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;
import js.widget.WidgetManager;
import testbed.AlgorithmStepper;

import static geom.GeomTools.*;

/**
 * A GUIApp that supports editing of geometric objects
 */
public abstract class GeomApp extends GUIApp {

  /**
   * Widget ids
   */
  public static final String //
      EDITOR_ZOOM = "ed_zoom", //
      EDITOR_PAN_X = "ed_pan_x", // 
      EDITOR_PAN_Y = "ed_pan_y", //
      CURRENT_SCRIPT_FILE = ".script_file",
      SCRIPT_NAME = ".script_name", // nor this?
      MESSAGE = ".message", //
      APP_FRAME = "app_frame"; //

  @Override
  public final String getTitleText() {
    var sm = scriptManager();
    if (isFileBased()) {
      var cs = sm.currentScript();
      if (!cs.isNone())
        return cs.file().getName();
      return "(no scripts open)";
    } else {
      todo("If file-based app, get current file instead");
      if (sm.currentProject().isDefined()) {
        File dir = sm.currentProject().directory();
        return dir.getName();
      }
    }
    return null;
  }

  @Override
  public UserOperation getDefaultUserOperation() {
    return new DefaultOper();
  }

  @Override
  public final void processOptionalArgs() {
    if (cmdLineArgs().hasNextArg()) {
      var f = cmdLineArgs().nextArg();
      mStartFileOrProject = new File(f);
      log(DASHES, "set start file or dir:", INDENT, mStartFileOrProject, VERT_SP);
    }
  }

  private File mStartFileOrProject = Files.DEFAULT;

  /**
   * Return true iff this project has support for images
   */
  public abstract boolean hasImageSupport();

  public IPoint pageSize() {
    return scriptManager().currentScript().assertNotNone().pageSize();
  }

  /**
   * Called from EditorPanel; default implementation does nothing
   */
  public void paintStart(Graphics2D graphics) {
  }

  /**
   * Called from EditorPanel; default implementation does nothing
   */
  public void paintBackground(Graphics2D graphics) {
  }

  /**
   * Called from EditorPanel; default implementation does nothing
   */
  public void paintStop() {
  }

  // ------------------------------------------------------------------
  // Current project
  // ------------------------------------------------------------------

  public final void closeProject() {
    assertProjectBased();
    var sm = scriptManager();
    if (sm.currentProject().isDefault())
      return;
    sm.flushProject();
    widgets().setActive(false);

    sm.setCurrentProject(
        Project.DEFAULT_INSTANCE);
    contentPane().removeAll();
    recentProjects().setCurrentFile(null);
    scriptManager().loadProjectScript();
    discardMenuBar();
    updateTitle();
    performRepaint(REPAINT_ALL);
    notifyProjectListener();
  }

  public final void openProject(File file) {
    var sm = scriptManager();

    assertProjectBased();
    closeProject();

    Project project = new Project(file);

    // If there are recent projects, use their state as the default for this one in case it is a new project

    sm.setCurrentProject(project);
    project.open(recentProjects().getMostRecentFile());

    recentProjects().setCurrentFile(project.directory());
    AppDefaults.sharedInstance().edit().recentFiles(recentProjects().state());
    rebuildFrameContent();
    scriptManager().loadProjectScript();

    updateTitle();
    discardMenuBar();

    // Now that widgets have been built, restore their state
    {
      WidgetManager g = widgets();
      g.setWidgetValues(sm.projectState().widgetStateMap());
      g.setActive(true);
    }

    // Make sure the UI is updated to represent this project's state,
    // and to make sure the keyboard shortcuts work (something to do with focus?)
    //
    performRepaint(REPAINT_ALL);

    notifyProjectListener();
  }

  private void openAppropriateProject() {
    AppDefaults def = AppDefaults.sharedInstance();
    recentProjects().restore(def.read().recentFiles());
    def.edit().recentFiles(recentProjects().state());

    File desiredProjFile = mStartFileOrProject;
    if (Files.empty(desiredProjFile))
      desiredProjFile = recentProjects().getMostRecentFile();
    if (Files.empty(desiredProjFile))
      desiredProjFile = Files.currentDirectory();

    if (!desiredProjFile.isDirectory()) {
      pr("*** No such project directory:", desiredProjFile);
      desiredProjFile = Files.currentDirectory();
    }
    desiredProjFile = Files.absolute(desiredProjFile);
    openProject(desiredProjFile);
  }

  private void openAppropriateFile() {
    AppDefaults def = AppDefaults.sharedInstance();

    recentFiles().restore(def.read().recentFiles());
    def.edit().recentFiles(recentFiles().state());

    // Restore previously opened scripts
    var m = scriptManager();
    for (var f : def.read().openScripts()) {
      if (!f.exists()) continue;
      m.switchToScript(f, true);
    }
    var curr = def.read().currentScript();
    if (Files.nonEmpty(curr) && curr.exists())
      m.switchToScript(curr, true);


    File desiredFile = mStartFileOrProject;

    pr("mStartFileOrProject:", Files.infoMap(mStartFileOrProject));

    if (Files.empty(desiredFile))
      desiredFile = recentFiles().getMostRecentFile();

    if (Files.empty(desiredFile)) {
      todo("maybe create an empty file?");
      return;
    }
    if (!desiredFile.exists()) {
      pr("*** No such file:", desiredFile);
      return;
    }
    desiredFile = Files.absolute(desiredFile);
    scriptManager().openFile(desiredFile);
  }

  @Override
  public final /* final for now */ String getAlertText() {
    if (isProjectBased()) {
      var m = scriptManager();
      if (m.isDefaultProject())
        return "No project selected; open one from the Project menu";
      if (!m.definedAndNonEmpty())
        return "This project is empty! Open another from the Project menu";
    }
    return null;
  }

  public final RecentFiles recentProjects() {
    assertProjectBased();
    return buildRecent();
  }

  public final RecentFiles recentFiles() {
    assertFileBased();
    return buildRecent();
  }

  private RecentFiles buildRecent() {
    if (mRecentFiles == null) {
      mRecentFiles = new RecentFiles();
      if (isProjectBased())
        mRecentFiles.setDirectoryMode();
    }
    return mRecentFiles;
  }


  private RecentFiles mRecentFiles;

  // ------------------------------------------------------------------
  // Menu bar
  // ------------------------------------------------------------------

  @Override
  public final void populateMenuBar(MenuBarWrapper m) {
    if (isProjectBased()) {
      addProjectMenu(m);
      if (scriptManager().definedAndNonEmpty())
        addAdditionalMenus(m);
    } else {
      addAdditionalMenus(m);
    }
  }

  /**
   * Add menus for current open project. Not called if no project is open
   */
  public void addAdditionalMenus(MenuBarWrapper m) {
    addFileMenu(m);
    addEditMenu(m);
    addViewMenu(m);
  }


  public void addProjectMenu(MenuBarWrapper m) {
    m.addMenu("Project");
    addItem("project_open", "Open", new ProjectOpenOper());
    addItem("project_close", "Close", new ProjectCloseOper());
    m.addSubMenu(
        recentProjects().constructMenu("Open Recent", UserEventManager.sharedInstance(), new UserOperation() {
          @Override
          public void start() {
            openProject(recentProjects().getCurrentFile());
          }
        }));
    addItem("project_open_next", "Open Next", new OpenNextProjectOper());
    addItem("project_reopen", "Reopen", new ProjectReopenOper());
  }

  public void addFileMenu(MenuBarWrapper m) {
    m.addMenu("File", null);

    if (isFileBased()) {
      addItem("new_file", "New", new NewFileOper());
      addItem("open_file", "Open", new OpenFileOper());
      addItem("close_file", "Close", new CloseFileOper());

      addItem("open_next_file", "Open Next", new OpenNextScriptOper());
      m.addSeparator();
      addItem("open_set", "Open Set", new LoadScriptSetOper());
      addItem("save_set", "Save Set", new SaveScriptSetOper());
      m.addSeparator();


      UserOperation prevOper = new FileStepOper(-1);
      UserOperation nextOper = new FileStepOper(1);

      addItem("script_step_bwd", "Prev", prevOper);
      addItem("script_step_fwd", "Next", nextOper);
      addItem("script_step_bwd2", "Prev_", prevOper);
      addItem("script_step_fwd2", "Next_", nextOper);
      addItem("script_page_bwd", "Page Bwd", new FileStepOper(-1).withAccel());
      addItem("script_page_fwd", "Page Fwd", new FileStepOper(1).withAccel());
      addItem("script_jump_first", "First", new FileJumpOper(-1));
      addItem("script_jump_last", "Last", new FileJumpOper(1));
    } else {
      UserOperation prevOper = new FileStepOper(-1);
      UserOperation nextOper = new FileStepOper(1);
      UserOperation prevUsedOper = new FileStepUsedOper(-1);
      UserOperation nextUsedOper = new FileStepUsedOper(1);
      addItem("script_step_bwd", "Prev", prevOper);
      addItem("script_step_fwd", "Next", nextOper);
      addItem("script_step_bwd2", "Prev_", prevOper);
      addItem("script_step_fwd2", "Next_", nextOper);
      addItem("script_page_bwd", "Page Bwd", new FileStepOper(-1).withAccel());
      addItem("script_page_fwd", "Page Fwd", new FileStepOper(1).withAccel());
      addItem("script_used_prev", "Prev Used", prevUsedOper);
      addItem("script_used_next", "Next Used", nextUsedOper);
      addItem("script_jump_first", "First", new FileJumpOper(-1));
      addItem("script_jump_last", "Last", new FileJumpOper(1));
    }
  }

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
    addItem("curve_add", "Add Curve", PolygonEditOper.buildAddCurveOper());

  }

  public void addViewMenu(MenuBarWrapper m) {
    m.addMenu("View");
    addItem("zoom_in", "Zoom In", ZoomOper.buildIn());
    addItem("zoom_out", "Zoom Out", ZoomOper.buildOut());
    addItem("zoom_reset", "Zoom Reset", ZoomOper.buildReset());
    addItem("pan_reset", "Pan Reset", PanOper.buildReset());
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

    // If there's a single selected element, render its information to the InfoPanel

    String infoMsg = null;
    ScriptEditState state = scriptManager().state();
    if (state.selectedElements().length == 1) {
      EditorElement elem = state.elements().get(state.selectedElements()[0]);
      infoMsg = elem.infoMessage();
    }
    setInfoMessage(nullToEmpty(infoMsg));
    performPostCommandActions(command.description());
  }

  public void perform(CommandOper oper) {
    UserEventManager.sharedInstance().perform(oper);
    performPostCommandActions(oper);
  }

  /**
   * Perform logic after a command. Default implementation does nothing.
   *
   * Used to re-run the current algorithm (if one is active)
   */
  public void performPostCommandActions(Object... messages) {
    if (verbose())
      log("performPostCommandActions:", BasePrinter.toString(messages));
    AlgorithmStepper alg = AlgorithmStepper.sharedInstance();
    if (alg.active())
      performRepaint(GeomApp.REPAINT_ALL);
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
  public final float zoomFactor() {
    return widgets().vf(EDITOR_ZOOM);
  }

  public final void setZoomFactor(float zoom) {
    widgets().setf(EDITOR_ZOOM, zoom);
  }

  public final IPoint panOffset() {
    return new IPoint(widgets().vi(EDITOR_PAN_X), widgets().vi(EDITOR_PAN_Y));
  }

  public final void setPanOffset(IPoint offset) {
    widgets().seti(EDITOR_PAN_X, offset.x);
    widgets().seti(EDITOR_PAN_Y, offset.y);
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

  @Override
  public final /* for now */ void startedGUI() {
    if (devMode()) {
      // let's crash if there's an uncaught exception in the swing thread 
      exitAppIfException();
    }

    ScriptManager.setSingleton(new ScriptManager());
    if (isProjectBased())
      openAppropriateProject();
    else {
      openAppropriateFile();
      rebuildFrameContent();

      updateTitle();
      discardMenuBar();

      // restore widget states from the current script
      {
        WidgetManager g = widgets();
        todo("restore widget values from current script");

        var sm = scriptManager();
        sm.currentScript().copyWidgetValuesFromScriptToUI();

        g.setActive(true);
      }

      // Make sure the UI is updated to represent this project's state,
      // and to make sure the keyboard shortcuts work (something to do with focus?)
      //
      performRepaint(REPAINT_ALL);
    }
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
    repaintFlags |= mPendingRepaintFlags;
    mPendingRepaintFlags = 0;
    if (0 != (repaintFlags & REPAINT_EDITOR))
      getEditorPanel().repaint();
    if (infoPanel() != null)
      if (0 != (repaintFlags & REPAINT_INFO))
        infoPanel().refresh();
  }

  private EditorPanel mEditorPanel;

  public void constructInfoPanel() {
    mInfoPanel = new InfoPanel();
  }

  public InfoPanel infoPanel() {
    return mInfoPanel;
  }

  private InfoPanel mInfoPanel;

// ------------------------------------------------------------------
// Periodic background tasks (e.g. flushing changes to script)
// ------------------------------------------------------------------

  @Override
  public final void swingBackgroundTask() {

    var m = scriptManager();

    if (isProjectBased()) {
      if (!m.isProjectDefined())
        return;
      m.flushScript();

      // Save any changes to current project, including window bounds
      {
        checkState(m.isProjectDefined());
        m.flushProject();
      }
    } else {
      m.flushScript();
      m.updateAppDefaults();
    }
    AppDefaults.sharedInstance().flush();
  }

  @Override
  public void initWidgets() {
    super.initWidgets();
    todo("move string constants to GeomTools");
    todo("store current open set of scripts in widgets");

    WidgetManager w = widgets();
    // Add widget for persisting frame bounds
    w.add(new AppFrameWidget().setId(APP_FRAME));
    w.addHidden(EDITOR_ZOOM, 1f);
    w.addHidden(EDITOR_PAN_X, 0f);
    w.addHidden(EDITOR_PAN_Y, 0f);
    w.addHidden(CURRENT_SCRIPT_FILE, Files.DEFAULT.getPath());
  }

  public void setInfoMessage(Object... messages) {
    String content = BasePrinter.toString(messages);
    if (infoPanel().setMessage(content))
      mPendingRepaintFlags |= REPAINT_INFO;
  }

  private int mPendingRepaintFlags;
  private ProjectListener mProjectListener;

  public void setProjectListener(ProjectListener listener) {
    mProjectListener = listener;
  }

  private void notifyProjectListener() {
    if (mProjectListener != null)
      mProjectListener.projectActivated(scriptManager().currentProject());
  }

  public void setRenderPageFrame(boolean f) {
    mRenderPageFrame = f;
  }

  boolean renderPageFrame() {
    return mRenderPageFrame;
  }

  private boolean mRenderPageFrame = true;
}

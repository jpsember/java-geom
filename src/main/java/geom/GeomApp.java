package geom;

import static js.base.Tools.*;

import java.io.File;

import geom.gen.Command;
import geom.oper.*;
import js.file.Files;
import js.guiapp.GUIApp;
import js.guiapp.MenuBarWrapper;
import js.guiapp.RecentFiles;
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

  @Override
  public final void processOptionalArgs() {
    if (cmdLineArgs().hasNextArg()) {
      mStartProjectFile = new File(cmdLineArgs().nextArg());
      log(DASHES, "set start project:", INDENT, mStartProjectFile, VERT_SP);
    }
  }

  private File mStartProjectFile = Files.DEFAULT;

  // ------------------------------------------------------------------
  // Current project
  // ------------------------------------------------------------------

  public final Project currentProject() {
    return mCurrentProject;
  }

  public final void closeProject() {
    if (currentProject().isDefault())
      return;
    flushProject();
    mCurrentProject = Project.DEFAULT_INSTANCE;
    removeUIElements();
    recentProjects().setCurrentFile(null);
    scriptManager().replaceCurrentScriptWith(ScriptWrapper.DEFAULT_INSTANCE);
    discardMenuBar();
    updateTitle();
  }

  public final void openProject(File file) {
    closeProject();

    Project project = new Project(file);

    // If there are recent projects, use their state as the default for this one in case it is a new project

    project.open(recentProjects().getMostRecentFile());
    mCurrentProject = project;
    recentProjects().setCurrentFile(project.directory());
    AppDefaults.sharedInstance().edit().recentProjects(recentProjects().state());
    rebuildFrameContent();
    if (infoPanel() != null)
      infoPanel().opening(project);

    scriptManager().replaceCurrentScriptWith(currentProject().script());

    // TODO: restore panel visibilities, etc according to project
    appFrame().setBounds(projectState().appFrame());
    updateTitle();
    discardMenuBar();

    // Make sure the UI is updated to represent this project's state,
    // and to make sure the keyboard shortcuts work (something to do with focus?)
    //
    performRepaint(REPAINT_ALL);
  }

  public final void openAppropriateProject() {
    AppDefaults def = AppDefaults.sharedInstance();
    recentProjects().restore(def.read().recentProjects());
    def.edit().recentProjects(recentProjects().state());

    File desiredProjFile = mStartProjectFile;
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

  public final void flushProject() {
    if (!currentProject().defined())
      return;
    // Store the app frame location, in case it has changed
    projectState().appFrame(appFrame().bounds());
    currentProject().flush();
  }

  public void switchToScript(int index) {
    scriptManager().flushScript();
    if (currentProject().scriptIndex() != index) {
      currentProject().setScriptIndex(index);
      scriptManager().replaceCurrentScriptWith(currentProject().script());
    }
  }

  public ProjectState.Builder projectState() {
    return currentProject().state();
  }

  private Project mCurrentProject = Project.DEFAULT_INSTANCE;

  private void removeUIElements() {
    contentPane().removeAll();
    todo("who owns the EditorPanel?  Do we need to get rid of it here?");
    todo("add support for ControlPanel");
    //mControlPanel = null;
  }

  //  public final void openProject(File file) {
  //    closeProject();
  //
  //    Project project = new Project(file);
  //
  //    // If there are recent projects, use their state as the default for this one in case it is a new project
  //
  //    project.open(recentProjects().getMostRecentFile());
  //    mCurrentProject = project;
  //    recentProjects().setCurrentFile(project.directory());
  //    AppDefaults.sharedInstance().edit().recentProjects(recentProjects().state());
  //    rebuildFrameContent();
  //    mInfoPanel.opening(project);
  //
  //    scriptManager().replaceCurrentScriptWith(currentProject().script());
  //
  //    // TODO: restore panel visibilities, etc according to project
  //    appFrame().setBounds(projectState().appFrame());
  //    updateTitle();
  //    discardMenuBar();
  //
  //    // Make sure the UI is updated to represent this project's state,
  //    // and to make sure the keyboard shortcuts work (something to do with focus?)
  //    //
  //    performRepaint(REPAINT_ALL);
  //  }

  public final RecentFiles recentProjects() {
    if (mRecentProjects == null) {
      mRecentProjects = new RecentFiles();
      mRecentProjects.setDirectoryMode();
    }
    return mRecentProjects;
  }

  private RecentFiles mRecentProjects;

  // ------------------------------------------------------------------
  // Menu bar
  // ------------------------------------------------------------------

  @Override
  public final void populateMenuBar(MenuBarWrapper m) {
    addProjectMenu(m);
    if (currentProject().definedAndNonEmpty()) {
      populateMenuBarForProject(m);
    }
  }

  /**
   * Add menus to for current open project. Not called if no project is open
   */
  public void populateMenuBarForProject(MenuBarWrapper m) {
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
  }

  public void addFileMenu(MenuBarWrapper m) {
    m.addMenu("File", null);
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

  public void constructInfoPanel() {
    mInfoPanel = new InfoPanel();
  }

  public InfoPanel infoPanel() {
    return mInfoPanel;
  }

  private InfoPanel mInfoPanel;
}

package testbed;

import geom.GeomApp;
import geom.ScriptManager;
import js.guiapp.MenuBarWrapper;

import java.awt.*;
import javax.swing.*;
import java.util.List;

import static js.base.Tools.*;

public abstract class TestBed extends GeomApp {

  // ------------------------------------------------------------------
  // Construction
  // ------------------------------------------------------------------

  //  private static void resetFocus() {
  //    todo("requestFocus?  Or just avoid text input?");
  //    //    boolean f = TestBed.getAppContainer().requestFocusInWindow();
  //    //    Streams.out.println("req foc in TestBed app cont=" + f);
  //    //    C.menuPanel().requestFocusInWindow();
  //  }

  // I think this method is unnecessary now that we have Commands and UserOperations...?

  //  /**
  //   * Process an application action; if it is processed, its code may be modified
  //   * or cleared to 0
  //   *
  //   * @param a
  //   *          : TBAction to view and/or modify
  //   */
  //  private void processAction0(TBAction a) {
  //
  //    //     Streams.out.println("processAction "+a);
  //
  //    if (!programBegun) {
  //      a.code = TBAction.NONE;
  //      return;
  //    }
  //
  //    if (a.code != 0 && a.code != TBAction.HOVER) {
  //      resetFocus();
  //
  //      if (false) {
  //        System.out.println("TestBed action() " + a);
  //        if (a.ctrlId != 0)
  //          Streams.out.println("CtrlId= " + a.ctrlId);
  //      }
  //    }
  //
  //    boolean clearAction = false;
  //    boolean clearButUpdate = false;
  //
  //    switch (a.code) {
  //
  //    case TBAction.UPDATETITLE:
  //      todo("set/update title");
  //      //      setExtendedTitle(a.strArg);
  //      //      updateTitle();
  //      break;
  //
  //    case TBAction.CTRLVALUE:
  //      switch (a.ctrlId) {
  //      case TBGlobals.GRIDSIZE:
  //        V.updateGridSize(C.vi(TBGlobals.GRIDSIZE));
  //        break;
  //
  //      case TBGlobals.GLOBALSCALE:
  //        clearButUpdate = true;
  //        break;
  //
  //      //      case TBGlobals.QUIT:
  //      //        exitProgram();
  //      //        break;
  //      //      case TBGlobals.FILLCOLOR: {
  //      //        Color fillColor = new Color(C.vi(TBGlobals.sFILLCOLOR));
  //      //        Color cl = JColorChooser.showDialog(appFrame, "Select background color", fillColor);
  //      //        if (cl != null) {
  //      //          C.seti(TBGlobals.sFILLCOLOR, cl.getRGB() & 0xffffff);
  //      //        }
  //      //      }
  //      //        break;
  //
  //      case TBGlobals.TRACEBWD:
  //      case TBGlobals.TRACEBTNBWD:
  //        C.seti(TBGlobals.TRACESTEP, C.vi(TBGlobals.TRACESTEP) - 1);
  //        break;
  //      case TBGlobals.TRACEFWD:
  //      case TBGlobals.TRACEBTNFWD:
  //        C.seti(TBGlobals.TRACESTEP, C.vi(TBGlobals.TRACESTEP) + 1);
  //        break;
  //      case TBGlobals.BTN_TOGGLECTRLS:
  //      case TBGlobals.BTN_TOGGLECONSOLE: {
  //        JSplitPane sp = (a.ctrlId == TBGlobals.BTN_TOGGLECTRLS) ? spCtrls : spConsole;
  //        int x = sp.getDividerLocation(), x1 = sp.getMaximumDividerLocation();
  //        if (x > x1 || x < 20) {
  //          sp.resetToPreferredSizes();
  //        } else {
  //          sp.setDividerLocation(1.0);
  //        }
  //      }
  //        break;
  //
  //      case TBGlobals.BTN_TOGGLEWORKSPACE:
  //        workFile.setVisible(!workFile.isVisible());
  //        break;
  //
  //      }
  //      break;
  //
  //    case TBAction.ITEMENABLE: {
  //      // call application to determine if this item should
  //      // be enabled.
  //      boolean s = processMenuEnable(a.menuId, a.ctrlId);
  //      // change menu item's state if necessary.
  //      C.get(a.ctrlId).getComponent().setEnabled(s);
  //    }
  //      break;
  //    }
  //
  //    if (clearButUpdate) {
  //      clearAction = true;
  //      updateView();
  //    }
  //    if (clearAction) {
  //      a.code = TBAction.NONE;
  //    }
  //
  //    if (a.code != TBAction.NONE && !operList.isEmpty()) {
  //      oper().processAction(a);
  //    }
  //
  //    // call application-specific handler
  //    processAction(a);
  //
  //    // update the view in case state has changed as a result
  //    // of the main controls
  //    if (a.code != 0)
  //      updateView();
  //
  //    //    // reset the focus?
  //    //    Tools.warn("always resetting focus");
  //    //    resetFocus();
  //  }

  /**
   * Process actions for main controls. Default implementation passes action to
   * current operation
   */
  public void processAction(TBAction a) {
    if (!C.gadgetsActive())
      return;
    try {
      // Don't propagate action if we aren't initialized and displaying a script
      todo("more succinct way to check if script defined?");
      if (ScriptManager.singleton().currentScript().defined())
        oper().processAction(a);
    } catch (TBError e) {
      showError(e.toString());
    }
  }

  public static TestBed singleton() {
    return (TestBed) GeomApp.singleton();
  }

  private void addMenus0() {
  }

  /**
   * Add operations available to the user. Default implementation does nothing.
   * Typical user code (taken from the ConvexHull example):
   * 
   * <pre>
   * // add operations 
   * addOper(GrahamOper.singleton);
   * addOper(JarvisOper.singleton);
   * </pre>
   */
  public void addOperations() {
  }

  /**
   * Add 'global' controls: available to all operations Default implementation
   * does nothing.
   */
  public void addControls(ControlPanel c) {
  }

  private void mainControlScript0( ControlPanel c ) {
   c.checkBox(TBGlobals.CTRLSVISIBLE, null, null, true);

    c.openTabSet(TBGlobals.AUXTABSET);
    {
      c.openTab(TBGlobals.AUXTAB_TRACE, "Trace");
      c.checkBox(TBGlobals.TRACEENABLED, "Enabled", "if true, enables algorithm tracing", true);
      c.checkBox(TBGlobals.TRACEPLOT, "Messages", "plots trace text", true);
      c.intSlider(TBGlobals.TRACESTEP, null, "Highlight individual steps in algorithm", 0, 500, 0, 1);
      c.closeTab();
    }
    c.closeTabSet();
  }

  /**
   * Perform any initialization operations. User should override this method if
   * desired. Default implementation does nothing.
   */
  public void initTestbed() {
  }

  @Override
  public void populateFrame(JPanel parentPanel) {
    constructEditorPanel();
    constructInfoPanel();

    parentPanel.setLayout(new BorderLayout());
    parentPanel.add(getEditorPanel(), BorderLayout.CENTER);
    parentPanel.add(infoPanel(), BorderLayout.SOUTH);

    {
      operList =arrayList();
      C.init();
    }

    parentPanel.add(C.getControlPanel(TBGlobals.CT_MAIN), BorderLayout.LINE_END);
    
    //if (true) {
      ControlPanel c = C.controlPanel();
      c.prepareForGadgets();
      mainControlScript0(c);
      addOperations();
      addControls(c);
      addOperCtrls(c);
      C.finishedGadgets();
//    } else
//    {
//      C.openScript();
//      mainControlScript0();
//      addOperations();
//      addControls();
//      addOperCtrls();
//      String scr = C.closeScript();
//      C.addControls(scr);
//    }

    addMenus0();
    initTestbed();
  }

  // TODO: we need to call readGadgetGUIValues at startup at some point

  //  /**
  //   * Show the application.
  //   * 
  //   * Overridden to retain last window dimensions in configuration file.
  //   */
  //  protected void showApp() {
  //    appFrame.pack();
  //    readGadgetGUIValues();
  //    appFrame.setVisible(true);
  //  }

  /**
   * Process a paintComponent() for the view. Default implementation does
   * nothing
   */
  public void paintView() {
  }

  /**
   * Display an error message dialog within a JOptionPane
   * 
   * @param msg
   *          : the message to display
   */
  public static void showError(String msg) {
    JOptionPane.showMessageDialog(null /* appFrame().frame() */, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  //  protected void exitProgram() {
  //    final boolean db = false;
  //    if (db) {
  //      Streams.out.println("TestBed: exitProgram");
  //    }
  //    if (db) {
  //      Streams.out.println("writeconfig");
  //    }
  //
  //    writeConfigFile();
  //    if (console()) {
  //      if (db) {
  //        Streams.out.println("unset console");
  //      }
  //      C.unsetConsole();
  //    }
  //
  //    if (workFile != null) {
  //      workFile.dispose();
  //      workFile = null;
  //    }
  //    if (db) {
  //      Streams.out.println("calling super");
  //    }
  //
  //    super.exitProgram();
  //  }

  //  static JComponent topLevelContainer() {
  //    return Application.getAppContentPane();
  //  }

  /**
   * Strings for serializing hidden integers, doubles, booleans
   */
  public static final String serInt = " ? i '' -10000 10000 0 ", serDbl = " ? d '' -10000 10000 0 ",
      serBool = " ? c '' ";

  //  /**
  //   * Set path of file last read/saved; for generating EPS, IPE files
  //   * @param f
  //   */
  //  public static void setFilePath(String f) {
  //    fileStats.setPath(f);
  //  //  filePath = f;
  //  }

  //  static String getSpecialSavePath(String orig, String ext) {
  //    String f = orig;
  //    if (f == null || f.length() == 0)
  //      f = fileStats.getPath();
  //    if (f != null)
  //      f = Path.changeExtension(f, ext);
  //    return f;
  //  }

  // true if beginProgram() has been called yet.  If not,
  // we consume any actions without reporting them to the program.
  // private boolean programBegun;

  static int nOpers() {
    return operList.size();
  }

  //  public static void setFileStats(FileStats s) {
  //    fileStats = s;
  //  }
  //
  //  private static FileStats fileStats;

  private static void addOperCtrls( ControlPanel c) {
    if (nOpers() > 0) {
      c.openTabSet (TBGlobals.OPER);
      for (int i = 0; i < nOpers(); i++) {
       //c.prepareForGadgets();
        oper(i).addControls();
       // c.finishedGadgets();
      }
      c.closeTabSet();
    } else {
      c.open();
      c.close();
    }
  }

  public static void addOper(TestBedOperation oper) {
    /**
     * if (operList.isEmpty()) { operList.add(new TestBedOperation() { public
     * void addControls() { C.sOpenTab("Edit"); C.sCloseTab(); }
     * 
     * public void runAlgorithm() { }
     * 
     * public void paintView() { }
     * 
     * public void processAction(TBAction a) { } }); }
     */
    operList.add(oper);
  }

  public static int operNum() {
    return C.vi(TBGlobals.OPER);
  }

  public static TestBedOperation oper() {
    return oper(C.vi(TBGlobals.OPER));
  }

  public static TestBedOperation oper(int n) {
    return  operList.get(n);
  }

  public static boolean plotTraceMessages() {
    return C.vb(TBGlobals.TRACEPLOT);
  }

  // --------- These static members must be initialized by doInit() ----
  private static List<TestBedOperation> operList;

  @Override
  public float zoomFactor() {
    return C.vf(TBGlobals.EDITOR_ZOOM);
  }

  @Override
  public void setZoomFactor(float zoom) {
    C.set(TBGlobals.EDITOR_ZOOM, zoom);
  }

  @Override
  public void paintStart(Graphics2D graphics) {
    V.setGraphics(graphics);
    AlgorithmStepper.sharedInstance().runAlgorithm(oper());
  }

  @Override
  public void paintStop() {
    AlgorithmStepper.sharedInstance().renderAlgorithmResults();
    V.setGraphics(null);
  }

  @Override
  public void populateMenuBarForProject(MenuBarWrapper m) {
    super.populateMenuBarForProject(m);
    m.addMenu("Alg");
    addItem("alg_step_bwd", "Step backward", AlgorithmStepper.buildStepOper(-1));
    addItem("alg_step_fwd", "Step forward", AlgorithmStepper.buildStepOper(1));
    addItem("alg_step_reset", "Reset step", AlgorithmStepper.buildResetOper());
  }

}

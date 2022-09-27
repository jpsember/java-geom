package testbed;

import geom.GeomApp;
import geom.ScriptManager;
import js.guiapp.MenuBarWrapper;

import java.awt.*;
import javax.swing.*;
import java.util.List;

import static js.base.Tools.*;
import static geom.GeomTools.*;

public abstract class TestBed extends GeomApp {

  /**
   * Process actions for main controls. Default implementation passes action to
   * current operation
   */
  public void processAction(TBAction a) {
    if (!gadgetsActive())
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

  @Deprecated
  public static TestBed singleton() {
    return (TestBed) GeomApp.singleton();
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

  private void addMainControls(ControlPanel c) {
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

  @Override
  public void populateFrame(JPanel parentPanel) {
    constructEditorPanel();
    constructInfoPanel();

    parentPanel.setLayout(new BorderLayout());
    parentPanel.add(getEditorPanel(), BorderLayout.CENTER);
    parentPanel.add(infoPanel(), BorderLayout.SOUTH);

    sOperList = arrayList();
    initGadgets();

    ControlPanel c = controlPanel();
    c.prepareForGadgets();
    addMainControls(c);
    addOperations();
    addControls(c);
    addOperCtrls(c);
    c.finishedGadgets();
    parentPanel.add(c, BorderLayout.LINE_END);
  }

  /**
   * Display an error message dialog within a JOptionPane
   * 
   * @param msg
   *          : the message to display
   */
  public static void showError(String msg) {
    JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private static void addOperCtrls(ControlPanel c) {
    if (sOperList.size() > 0) {
      c.openTabSet(TBGlobals.OPER);
      todo("pass in GadgetList to addControls() method");
      for (TestBedOperation oper : sOperList)
        oper.addControls();
      c.closeTabSet();
    } else {
      c.open();
      c.close();
    }
  }

  public static void addOper(TestBedOperation oper) {
    sOperList.add(oper);
  }

  public static int operNum() {
    return gadg ().vi(TBGlobals.OPER);
  }

  public static TestBedOperation oper() {
    return oper(gadg ().vi(TBGlobals.OPER));
  }

  public static TestBedOperation oper(int n) {
    return sOperList.get(n);
  }

  public boolean plotTraceMessages() {
    return gadgets().vb(TBGlobals.TRACEPLOT);
  }

  @Override
  public float zoomFactor() {
    return gadgets().vf(TBGlobals.EDITOR_ZOOM);
  }

  @Override
  public void setZoomFactor(float zoom) {
    gadgets().set(TBGlobals.EDITOR_ZOOM, zoom);
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

  private static List<TestBedOperation> sOperList;

  // ------------------------------------------------------------------
  // Gadgets
  // ------------------------------------------------------------------
  //
//  @Override
//  public void initGadgets() {
//    super.initGadgets();
//
//    GadgetList g = gadgets();
//
//    // Add gadget for persisting frame bounds
//    g.add(new AppFrameGadget().setId(TBGlobals.APP_FRAME));
//    // Add gadget for persisting zoom factor
//    g.addHidden(TBGlobals.EDITOR_ZOOM, 1f);
//    g.addHidden(TBGlobals.CURRENT_SCRIPT_INDEX, 0);
//  }

  public ControlPanel controlPanel() {
    if (mMainControlPanel == null)
      mMainControlPanel = new ControlPanel();
    return mMainControlPanel;
  }

  private ControlPanel mMainControlPanel;

}

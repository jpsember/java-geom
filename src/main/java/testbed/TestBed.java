package testbed;

import geom.GeomApp;
import geom.ScriptManager;
import js.guiapp.MenuBarWrapper;
import js.guiapp.UserEvent;
import js.widget.WidgetManager;

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
  public void processAction(UserEvent a) {
    if (!gadgets().active())
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
   * Add 'global' controls: available to all operations
   * 
   * Default implementation does nothing.
   */
  public void addControls(WidgetManager c) {
  }

  private void addMainControls(WidgetManager c) {
//    c.addToggleButton( TBGlobals.CTRLSVISIBLE, "null, null, true);
    
    c.withTabs(TBGlobals.AUXTABSET);
    {
      c.tabTitle(TBGlobals.AUXTAB_TRACE);
      c.tooltip("if true, enables algorithm tracing");
      c.addToggleButton( TBGlobals.TRACEENABLED, "Enabled" ); //, true);
      c.tooltip( "plots trace text");
      c.addToggleButton(TBGlobals.TRACEPLOT, "Messages"); //, true);
      c.min(0).max(500).stepSize(1).defaultVal(0).addSlider(TBGlobals.TRACESTEP);
      
      //c.intSlider(TBGlobals.TRACESTEP, null, "Highlight individual steps in algorithm", 0, 500, 0, 1);
      
      // we don't need to close individual tabs, since
      // each tab is a single component and didn't require a separate grid to be opened
      
      //c.close("TRACE tab");
//      c.closeTab();
    }
    todo("do we need to close the tab set somehow?");
//    c.closeTabSet();
  }

  @Override
  public void populateFrame(JPanel parentPanel) {
    sOperList = arrayList();

    constructEditorPanel();
    constructInfoPanel();

    parentPanel.setLayout(new BorderLayout());
    parentPanel.add(getEditorPanel(), BorderLayout.CENTER);
    parentPanel.add(infoPanel(), BorderLayout.SOUTH);

    WidgetManager c = gadg();

    c.setPendingContainer(controlPanel());
    c.open("ControlPanel");

    addMainControls(c);
    addOperations();
    addControls(c);
    addOperCtrls(c);
    c.close("ControlPanel");

    parentPanel.add(controlPanel(), BorderLayout.LINE_END);
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

  private static void addOperCtrls(WidgetManager c) {
    if (sOperList.size() > 0) {
      c.withTabs(TBGlobals.OPER)
      ;
      //todo("pass in GadgetList to addControls() method");
      for (TestBedOperation oper : sOperList)  
        oper.addControls();
        
      alert("is close() for tab set necessary?");
      if (false)
      c.close("closeTabSet??"); //c.closeTabSet();
    } else {
      c.open("zero opers");
      c.close("zero opers");
    }
  }

  public static void addOper(TestBedOperation oper) {
    sOperList.add(oper);
  }

  public static int operNum() {
    return gadg().vi(TBGlobals.OPER);
  }

  public static TestBedOperation oper() {
    return oper(gadg().vi(TBGlobals.OPER));
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

  public JPanel controlPanel() {
    if (mMainControlPanel == null) {
      mMainControlPanel = new JPanel();
    }
    return mMainControlPanel;
  }

  private JPanel mMainControlPanel;

}

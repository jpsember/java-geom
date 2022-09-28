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
  @Override
  public void userEventManagerListener(UserEvent event) {
    if (!gadgets().active())
      return;
    try {
      // Don't propagate action if we aren't initialized and displaying a script
      todo("more succinct way to check if script defined?");
      todo("Rename processAction -> processEvent");
      if (ScriptManager.singleton().currentScript().defined())
        oper().processAction(event);
      super.userEventManagerListener(event);
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

    c.openTabSet(TBGlobals.AUXTABSET);
    {
      c.openTab(TBGlobals.AUXTAB_TRACE);
      {
        c.tooltip("if true, enables algorithm tracing");
        c.addToggleButton(TBGlobals.TRACEENABLED, "Enabled", true);
        c.tooltip("plots trace text");
        c.addToggleButton(TBGlobals.TRACEPLOT, "Messages", true);

        {
          c.columns(".x").open("multicolumn subsection");

          c.addLabel("Step:") //
              .tooltip("Highlight individual steps in algorithm") //
              .min(0).max(500).stepSize(1).defaultVal(0).addSlider(TBGlobals.TRACESTEP);
          todo("alg stepper is not propagating events");

          c.close("multicolumn subsection");
        }
      }
      c.closeTab();
    }

    if (alert("another tab")) {
      c.openTab("Beta");
      c.tooltip("an example of a second tab");
      c.addToggleButton("beta_checkbox", "Hello", true);
      c.closeTab();
    }
    c.closeTabSet();
  }

  @Override
  public void populateFrame(JPanel parentPanel) {
    sOperList = arrayList();

    constructEditorPanel();
    constructControlPanel();
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
      c.openTabSet(TBGlobals.OPER);
      for (TestBedOperation oper : sOperList)
        oper.addControls(c);
      c.closeTabSet();
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

  public JPanel controlPanel() {
    return mMainControlPanel;
  }

  private void constructControlPanel() {
    mMainControlPanel = new JPanel();
  }

  private JPanel mMainControlPanel;

}

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
   * Reserved gadget ids
   */
  public static final String //
        OPER = "oper", //
      CTRLSVISIBLE = "ctrls_visible", //
      AUXTABSET = "aux_tabset", //
      AUXTAB_TRACE = "aux_trace", //
      TRACESTEP = "trace_step", //
      TRACEPLOT = "trace_plot", //
      TRACEENABLED = "trace_enabled";

  /**
   * Process actions for main controls. Default implementation passes action to
   * current operation
   */
  @Override
  public void userEventManagerListener(UserEvent event) {
    if (!widgets().active())
      return;
    try {
      // Don't propagate action if we aren't initialized and displaying a script
      todo("more succinct way to check if script defined?");
      if (ScriptManager.singleton().currentScript().defined())
        oper().processUserEvent(event);
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

    c.openTabSet(AUXTABSET);
    {
      c.openTab(AUXTAB_TRACE);
      {
        c.tooltip("if true, enables algorithm tracing");
        c.addToggleButton(TRACEENABLED, "Enabled", true);
        c.tooltip("plots trace text");
        c.addToggleButton(TRACEPLOT, "Messages", true);

        {
          c.columns(".x").open("multicolumn subsection");

          c.addLabel("Step:") //
              .tooltip("Highlight individual steps in algorithm") //
              .min(0).max(500).stepSize(1).defaultVal(0).addSlider(TRACESTEP);

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

    WidgetManager c = widgets();

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
      c.openTabSet(OPER);
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
    return widgets().vi(OPER);
  }

  public static TestBedOperation oper() {
    return oper(widgets().vi(OPER));
  }

  public static TestBedOperation oper(int n) {
    return sOperList.get(n);
  }

  public boolean plotTraceMessages() {
    return widgets().vb(TRACEPLOT);
  }

  @Override
  public float zoomFactor() {
    return widgets().vf(EDITOR_ZOOM);
  }

  @Override
  public void setZoomFactor(float zoom) {
    widgets().set(EDITOR_ZOOM, zoom);
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

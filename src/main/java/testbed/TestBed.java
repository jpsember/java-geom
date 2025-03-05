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
 * 
 **/
package testbed;

import geom.GeomApp;
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
   * Reserved widget ids
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
      if (scriptManager().currentScript().defined())
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
      c.openTab(AUXTAB_TRACE + ":Alg");
      {
        c.tooltip("if true, enables algorithm tracing");
        c.label("Enabled").defaultVal(true).addToggleButton(TRACEENABLED);
        c.tooltip("plots trace text").label("Messages").defaultVal(true).addToggleButton(TRACEPLOT);

        {
          c.columns(".x").open("multicolumn subsection");

          c.label("Step:").addLabel() //
              .tooltip("Highlight individual steps in algorithm") //
              .min(0).max(500).defaultVal(0).addSlider(TRACESTEP);

          c.close("multicolumn subsection");
        }
      }
      c.closeTab();
    }

    // For demonstration purposes, open another tab
    {
      c.openTab("beta:Beta");
      c.tooltip("an example of a second tab");
      c.label("Hello").defaultVal(true).addToggleButton("beta_checkbox");
      c.closeTab();
    }
    c.closeTabSet();
  }

  @Override
  public void populateFrame(JPanel parentPanel) {
    mOperList = arrayList();

    constructEditorPanel();
    constructControlPanel();
    constructInfoPanel();

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
    c.finish();

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

  private void addOperCtrls(WidgetManager c) {
    if (!mOperList.isEmpty()) {
      c.openTabSet(OPER);
      for (TestBedOperation oper : mOperList)
        oper.addControls(c);
      c.closeTabSet();
    } else {
      c.open("zero opers");
      c.close("zero opers");
    }
  }

  public void addOper(TestBedOperation oper) {
    mOperList.add(oper);
  }

  public TestBedOperation oper() {
    String operId = widgets().vs(OPER);
    TestBedOperation oper = null;
    for (TestBedOperation op : mOperList)
      if (op.operId().equals(operId))
        oper = op;
    checkState(oper != null, "oper not found for id:", operId);
    return oper;
  }

  public boolean plotTraceMessages() {
    return widgets().vb(TRACEPLOT);
  }

  @Override
  public void paintStart(Graphics2D graphics) {
    Render.setGraphics(graphics);
    pr("...paintStart");
    AlgorithmStepper.sharedInstance().runAlgorithm(oper());
  }

  @Override
  public void paintBackground(Graphics2D graphics) {
    pr("...paintBackground");
    oper().paintBackground();
  }

  @Override
  public void paintStop() {
    pr("...paintStop");
    AlgorithmStepper.sharedInstance().renderAlgorithmResults();
    oper().paintView();
    Render.setGraphics(null);
  }

  @Override
  public void populateMenuBarForProject(MenuBarWrapper m) {
    super.populateMenuBarForProject(m);
    m.addMenu("Alg");
    addItem("alg_step_bwd", "Step backward", AlgorithmStepper.buildStepOper(-1));
    addItem("alg_step_fwd", "Step forward", AlgorithmStepper.buildStepOper(1));
    addItem("alg_step_reset", "Reset step", AlgorithmStepper.buildResetOper());
  }

  private List<TestBedOperation> mOperList;

  public JPanel controlPanel() {
    return mMainControlPanel;
  }

  private void constructControlPanel() {
    mMainControlPanel = new JPanel();
  }

  private JPanel mMainControlPanel;

}

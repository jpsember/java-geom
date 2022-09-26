package testbed;

import geom.GeomTools;

import javax.swing.*;
import static js.base.Tools.*;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.util.List;

/**
 * Control panel class
 */
public class ControlPanel extends JPanel implements Globals, IScript {

  /**
   * Constructor
   */
  ControlPanel() {
    super(new GridBagLayout());
    loadTools();
    setOpaque(true);

    // add glue panel with all the weight in row 2.
    GC.addGlue(this, 0, 1);
  }

  public void prepareForGadgets() {
    // create an outermost panel, and add it to 
    // this control panel, in the first row
    mStackPanel = new StackPanel(null);
    {
      GC gc = GC.gc(0, 0, 1, 1, 0, 0);
      gc.fill = GC.HORIZONTAL;
      add(mStackPanel.component(), gc);
    }
    mStack = arrayList();
    mHideNextGadget = false;
  }

  public ControlPanel hideNextControl() {
    return hideNextControl(true);
  }

  public ControlPanel hideNextControl(boolean f) {
    if (f)
      mHideNextGadget = true;
    return this;
  }

  public void finishedGadgets() {
    checkState(mStack.isEmpty(), "control stack isn't empty");
    mStack = null;
  }

  public ControlPanel checkBox(int id, String label, String toolTip, boolean defaultValue) {
    hideNextControl(nullOrEmpty(label));
    addControl(new CtCheckBox(id, label, defaultValue, false, parseToolTip(toolTip), null), null);
    return this;
  }

  public ControlPanel intSlider(int id, String label, String toolTip, int minValue, int maxValue,
      int defaultValue, int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, false, toolTip);
    return this;
  }

  public ControlPanel doubleSlider(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, true, toolTip);
    return this;
  }

  public ControlPanel intSpinner(int id, String label, String toolTip, int minValue, int maxValue,
      int defaultValue, int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, false, toolTip);
    return this;
  }

  public ControlPanel doubleSpinner(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, true, toolTip);
    return this;
  }

  public ControlPanel textArea(int id, String label, String toolTip, boolean fixedWidth,
      String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, 0, fixedWidth).setId(id), toolTip);
    return this;
  }

  public ControlPanel textField(int id, String label, String toolTip, int maxLength, boolean fixedWidth,
      String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, maxLength, fixedWidth).setId(id), toolTip);
    return this;
  }

  public ControlPanel openComboBox(int id, String label, String toolTip, boolean asRadio) {
    CtComboBox box = new CtComboBox(label, toolTip, asRadio);
    box.setId(id);
    push(mStack, box);
    addControl(box, toolTip);
    return this;
  }

  public ControlPanel choice(int id, String label) {
    currentComboBox().addItem(id, label);
    return this;
  }

  public ControlPanel closeComboBox() {
    currentComboBox();
    pop(mStack);
    return this;
  }

  public ControlPanel open() {
    return open(null);
  }

  public ControlPanel open(String title) {
    StackPanel prevScope = mStackPanel;
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    prevScope.addItem(mStackPanel.component());
    return this;
  }

  public ControlPanel newColumn() {
    mStackPanel.startNewColumn();
    return this;
  }

  public ControlPanel close() {
    popScope();
    return this;
  }

  public ControlPanel button(int id, String label, String toolTip) {
    addControl(new CtButton(Gadget.createAction(id, label, toolTip, null)).setId(id), null);
    return this;
  }

  public ControlPanel staticText(String text) {
    addControl(new CtLabel(0, text).setId(C.getAnonId()), null);
    return this;
  }

  public ControlPanel openTabSet(int panelId) {
    TabbedPaneGadget g = new TabbedPaneGadget(true);
    g.setId(panelId);
    push(mStack, g);
    return this;
  }

  public ControlPanel closeTabSet() {
    TabbedPaneGadget g = currentTabbedPane();
    mStackPanel.addItem(g.getComponent());
    addControl(g, null);
    pop(mStack);
    return this;
  }

  public ControlPanel openTab(String title) {
    return openTab(0, title);
  }

  public ControlPanel openTab(int tabId, String title) {
    if (title == null)
      title = "<name?>";
    TabbedPaneGadget tabbedPaneGadget = currentTabbedPane();
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    tabbedPaneGadget.addTab(title, tabId, mStackPanel.component());
    return this;
  }

  public ControlPanel closeTab() {
    popScope();
    return this;
  }

  /**
   * Convert a tooltip string to a multi-line tooltip using embedded HTML tags.
   * 
   * @param s
   *          String
   * @return String
   */
  private static String parseToolTip(String s) {
    if (s != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><center>");

      List<String> a = split(s, ' ');
      GeomTools.splitString(s, 50, a);
      for (int i = 0; i < a.size(); i++) {
        if (i > 0)
          sb.append("<br>");
        sb.append(a.get(i));
      }
      sb.append("</center></html>");
      s = sb.toString();
    }
    return s;
  }

  /**
   * Pop scope from stack
   * 
   * @return old scope, the one that has been replaced
   */
  private StackPanel popScope() {
    StackPanel ret = mStackPanel;
    mStackPanel = (StackPanel) pop(mStack);
    return ret;
  }

  private TabbedPaneGadget currentTabbedPane() {
    return (TabbedPaneGadget) last(mStack);
  }

  private CtComboBox currentComboBox() {
    return (CtComboBox) last(mStack);
  }

  private boolean addControl(Gadget c, String toolTip) {
    C.add(c);

    boolean shown = true;
    if (!mHideNextGadget) {
      Component cp = c.getComponent();
      if (cp != null)
        mStackPanel.addItem(cp);
    }
    mHideNextGadget = false;

    if (toolTip != null) {
      JComponent j = (JComponent) c.getComponent();
      if (j == null) {
        alert("JComponent is null for tooltip, c=", c);
      } else
        j.setToolTipText(parseToolTip(toolTip));
    }

    return shown;
  }

  private void addSpinner(int id, String label, double min, double max, double value, double step,
      boolean sliderFlag, boolean withTicks, boolean dbl, String toolTip) {
    if (!(value >= min && value <= max))
      throw new IllegalArgumentException("Spinner/slider initial value not in range");
    addControl(new CtSpinner(label, min, max, value, step, sliderFlag, withTicks, dbl).setId(id), toolTip);
  }

  private List<Object> mStack;
  private StackPanel mStackPanel;
  private boolean mHideNextGadget;

}

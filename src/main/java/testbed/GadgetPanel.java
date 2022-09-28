package testbed;

import static geom.GeomTools.*;

import static js.base.Tools.*;

import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * JPanel containing gadgets, with support for their composition
 */
public class GadgetPanel extends JPanel {

  /**
   * Constructor
   */
  public GadgetPanel() {
    super(new GridBagLayout());

    // add glue panel with all the weight in row 2.
    GC.addGlue(this, 0, 1);
  }

  /**
   * Prepare for adding gadgets to panel (must be balanced by a call to
   * composeEnd())
   */
  public void composeStart() {
    // create an outermost panel, and add it to 
    // this control panel, in the first row
    mStackPanel = new StackPanel(null);
    {
      GC gc = GC.gc(0, 0, 1, 1, 0, 0);
      gc.fill = GC.HORIZONTAL;
      add(mStackPanel.component(), gc);
    }
    mStack = arrayList();
  }

  /**
   * Clean up after adding gadgets to panel
   */
  public void composeEnd() {
    checkState(mStack.isEmpty(), "stack isn't empty");
    mStack = null;
    mStackPanel = null;
  }

  public GadgetPanel checkBox(int id, String label, String toolTip, boolean defaultValue) {
    checkArgument(!nullOrEmpty(label), "label is empty");
    addControl(new CtCheckBox(id, label, defaultValue, false, parseToolTip(toolTip), null), null);
    return this;
  }

  public GadgetPanel intSlider(int id, String label, String toolTip, int minValue, int maxValue,
      int defaultValue, int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, false, toolTip);
    return this;
  }

  public GadgetPanel doubleSlider(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, true, toolTip);
    return this;
  }

  public GadgetPanel intSpinner(int id, String label, String toolTip, int minValue, int maxValue,
      int defaultValue, int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, false, toolTip);
    return this;
  }

  public GadgetPanel doubleSpinner(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, true, toolTip);
    return this;
  }

  public GadgetPanel textArea(int id, String label, String toolTip, boolean fixedWidth,
      String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, 0, fixedWidth).setId(id), toolTip);
    return this;
  }

  public GadgetPanel textField(int id, String label, String toolTip, int maxLength, boolean fixedWidth,
      String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, maxLength, fixedWidth).setId(id), toolTip);
    return this;
  }

  public GadgetPanel openComboBox(int id, String label, String toolTip, boolean asRadio) {
    CtComboBox box = new CtComboBox(label, toolTip, asRadio);
    box.setId(id);
    push(mStack, box);
    addControl(box, toolTip);
    return this;
  }

  public GadgetPanel choice(int id, String label) {
    currentComboBox().addItem(id, label);
    return this;
  }

  public GadgetPanel closeComboBox() {
    currentComboBox();
    pop(mStack);
    return this;
  }

  public GadgetPanel open() {
    return open(null);
  }

  public GadgetPanel open(String title) {
    StackPanel prevScope = mStackPanel;
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    prevScope.addItem(mStackPanel.component());
    return this;
  }

  public GadgetPanel newColumn() {
    mStackPanel.startNewColumn();
    return this;
  }

  public GadgetPanel close() {
    popScope();
    return this;
  }

  public GadgetPanel button(int id, String label, String toolTip) {
    addControl(new CtButton(new GadgetAction(id, label, toolTip, null)).setId(id), null);
    return this;
  }

  public GadgetPanel staticText(String text) {
   // addControl(new CtLabel(0, text).setId(gadg().getAnonId()), null);
    return this;
  }

  public GadgetPanel openTabSet(int panelId) {
    TabbedPaneGadget g = new TabbedPaneGadget(true);
    g.setId(panelId);
    push(mStack, g);
    return this;
  }

  public GadgetPanel closeTabSet() {
    TabbedPaneGadget g = currentTabbedPane();
    mStackPanel.addItem(g.getComponent());
    addControl(g, null);
    pop(mStack);
    return this;
  }

  public GadgetPanel openTab(String title) {
    return openTab(0, title);
  }

  public GadgetPanel openTab(int tabId, String title) {
    if (title == null)
      title = "<name?>";
    TabbedPaneGadget tabbedPaneGadget = currentTabbedPane();
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    tabbedPaneGadget.addTab(title, tabId, mStackPanel.component());
    return this;
  }

  public GadgetPanel closeTab() {
    popScope();
    return this;
  }

  /**
   * Convert a tooltip string to a multi-line tooltip using embedded HTML tags.
   */
  private static String parseToolTip(String s) {
    if (s != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("<html><center>");

      List<String> a = split(s, ' ');
      splitString(s, 50, a);
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
   */
  private void popScope() {
    mStackPanel = (StackPanel) pop(mStack);
  }

  private TabbedPaneGadget currentTabbedPane() {
    return (TabbedPaneGadget) last(mStack);
  }

  private CtComboBox currentComboBox() {
    return (CtComboBox) last(mStack);
  }

  private boolean addControl(Gadget c, String toolTip) {
    throw notSupported();
//    gadg().add(c);
//
//    boolean shown = true;
//    JComponent cp = c.getComponent();
//    if (cp != null)
//      mStackPanel.addItem(cp);
//
//    if (toolTip != null) {
//      JComponent j = (JComponent) c.getComponent();
//      if (j == null) {
//        alert("JComponent is null for tooltip, c=", c);
//      } else
//        j.setToolTipText(parseToolTip(toolTip));
//    }
//
//    return shown;
  }

  private void addSpinner(int id, String label, double min, double max, double value, double step,
      boolean sliderFlag, boolean withTicks, boolean dbl, String toolTip) {
    if (!(value >= min && value <= max))
      throw new IllegalArgumentException("Spinner/slider initial value not in range");
    addControl(new CtSpinner(label, min, max, value, step, sliderFlag, withTicks, dbl).setId(id), toolTip);
  }

  private List<Object> mStack;
  private StackPanel mStackPanel;

}

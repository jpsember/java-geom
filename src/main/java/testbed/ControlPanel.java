package testbed;

import base.*;
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

  /**
   * Add gadget to ControlPanel
   * 
   * @param c
   *          gadget to add
   * @param id
   *          id of gadget being added
   * @param parent
   *          gadget to add to, or 0 if it's the master
   * @return true if control was actually added, or false if it was determined
   *         to be a hidden control (or was skipped)
   */
  private boolean addControl(Gadget c, String toolTip) {
    C.add(c);

    boolean shown = true;
    if (!hideNextControl) {
      Component cp = c.getComponent();
      if (cp != null)
        mStackPanel.addItem(cp);
    }
    hideNextControl = false;

    if (toolTip != null) {
      JComponent j = (JComponent) c.getComponent();
      if (j == null) {
        Tools.warn("JComponent is null for tooltip, c=" + c);
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
    hideNextControl = false;
  }

  public void hideNextControl(boolean f) {
    if (f)
      hideNextControl = true;
  }

  public void finishedGadgets() {
    checkState(mStack.isEmpty(), "control stack isn't empty");
    mStack = null;
  }

  public void sCheckBox(int id, String label, String toolTip, boolean defaultValue) {
    hideNextControl(nullOrEmpty(label));
    addControl(new CtCheckBox(id, label, defaultValue, false, parseToolTip(toolTip), null), null);
  }

  public void sIntSlider(int id, String label, String toolTip, int minValue, int maxValue, int defaultValue,
      int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, false, toolTip);
  }

  public void sDoubleSlider(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, true, false, true, toolTip);
  }

  public void sIntSpinner(int id, String label, String toolTip, int minValue, int maxValue, int defaultValue,
      int stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, false, toolTip);
  }

  public void sDoubleSpinner(int id, String label, String toolTip, double minValue, double maxValue,
      double defaultValue, double stepSize) {
    addSpinner(id, label, minValue, maxValue, defaultValue, stepSize, false, false, true, toolTip);
  }

  public void sTextArea(int id, String label, String toolTip, boolean fixedWidth, String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, 0, fixedWidth).setId(id), toolTip);
  }

  public void sTextField(int id, String label, String toolTip, int maxLength, boolean fixedWidth,
      String defaultValue) {
    addControl(new CtTextFieldNew(label, defaultValue, maxLength, fixedWidth).setId(id), toolTip);
  }

  /**
   * Add a combo box gadget. Must be balanced by a call to sCloseComboBox()
   */
  public void sOpenComboBox(int id, String label, String toolTip, boolean asRadio) {

    // cb <id:int> [<title:label>] <asradio:bool> [<tooltip:label>]
    //       ( {<id:int> <lbl:label>} )

    // int cid = tk.readInt();
    //    String label = tk.readIfLabel();
    //    boolean asRadio = tk.readBoolean();
    //    String toolTip = tk.readIfLabel();
    //    tk.read(T_PAROP);
    CtComboBox box = new CtComboBox(label, toolTip, asRadio);
    box.setId(id);
    push(mStack, box);
    addControl(box, toolTip);
  }

  /**
   * Add a choice to a combobox
   * 
   * @param id
   *          id of choice
   * @param label
   */
  public void sChoice(int id, String label) {
    comboBox().addItem(id, label);
  }

  private CtComboBox comboBox() {
    return (CtComboBox) last(mStack);
  }

  /**
   * Close combo box (previously opened with sOpenComboBox())
   */
  public void sCloseComboBox() {
    comboBox();
    pop(mStack);
  }

  public void sOpen() {
    sOpen(null);
  }

  public void sOpen(String title) {
    StackPanel prevScope = mStackPanel;
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    prevScope.addItem(mStackPanel.component());
  }

  public void sNewColumn() {
    mStackPanel.startNewColumn();
  }

  public void sClose() {
    popScope();
  }

  public void sButton(int id, String label, String toolTip) {
    addControl(new CtButton(Gadget.createAction(id, label, toolTip, null)).setId(id), null);
  }

  public void sStaticText(String text) {
    addControl(new CtLabel(0, text).setId(C.getAnonId()), null);
  }

  public void sOpenTabSet(int panelId) {
    push(mStack, mTabbedPaneGadget);
    mTabbedPaneGadget = new TabbedPaneGadget(true);
    mTabbedPaneGadget.setId(panelId);
  }

  public void sCloseTabSet() {
    mStackPanel.addItem(mTabbedPaneGadget.getComponent());
    addControl(mTabbedPaneGadget, null);
    mTabbedPaneGadget = (TabbedPaneGadget) pop(mStack);
  }

  public void sOpenTab(String title) {
    sOpenTab(0, title);
  }

  public void sOpenTab(int tabId, String title) {
    //    if (id >= TAB_ID_START)
    //      C.sIValue(id);
    if (title == null)
      title = "<name?>";

    //  for (int tabNumber = 0; !tk.readIf(T_PARCL); tabNumber++) {
    // int tabId = tk.readIfInt(tabNumber);

    // String tabLabel = tk.readLabel();
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    mTabbedPaneGadget.addTab(title, tabId, mStackPanel.component());
    //
    //      tk.read(T_PAROP);
    //      processScript();
    //      tk.read(T_PARCL);
    //      popScope();
    //    }
    //    
    //    
    //    C.sLbl(title);
    //    C.sText('(');
    //    sNewLine();
    //
    //    tabPaneCount++;
    //    
  }

  public void sCloseTab() {
    popScope();
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

      List<String> a = arrayList();
      TextScanner.splitString(s, 50, a);
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

  private List<Object> mStack;
  // current panel
  private StackPanel mStackPanel;
  private TabbedPaneGadget mTabbedPaneGadget;
  // private CtComboBox mComboBox;
  // hide next control?
  private boolean hideNextControl;

}

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

  private void addTextField(int id, String label, String value, int maxStrLen, boolean fw, String toolTip) {
    addControl(new CtTextFieldNew(label, value, maxStrLen, fw).setId(id), toolTip);
  }

  private void addComboBox() {
    // cb <id:int> [<title:label>] <asradio:bool> [<tooltip:label>]
    //       ( {<id:int> <lbl:label>} )

    int cid = tk.readInt();
    String label = tk.readIfLabel();
    boolean asRadio = tk.readBoolean();
    String toolTip = tk.readIfLabel();
    tk.read(T_PAROP);

    CtComboBox box = new CtComboBox(label, toolTip, asRadio);
    box.setId(cid);

    while (!tk.readIf(T_PARCL)) {
      int id = tk.readInt();
      String fldLabel = tk.readLabel();
      box.addItem(id, fldLabel);
    }
    addControl(box, toolTip);
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

  /**
   * Process a script to add controls
   * 
   * @param script
   *          script to process
   */
  void processScript(String script) {
    prepareForGadgets();
    tk = new GadgetTokenizer(script);
    processScript();
    tk.read(Token.T_EOF);
    tk = null;
  }

  private int readIdn() {
    int tabId = tk.readIfInt(-1);
    if (tabId < 0)
      tabId = C.getAnonId();
    return tabId;
  }

  public void sCheckBox(int id, String label, String toolTip, boolean defaultValue) {
    hideNextControl(nullOrEmpty(label));
    //
    //    sText("c");
    //    sIValue(id);
    //    sLblnn(label);
    //    sLblnn(toolTip);
    //    sBool(defaultValue);
    //    sNewLine();
    //  }) {
    //    int id = tk.readInt();
    //    String label = tk.readIfLabel();
    //
    //    String toolTip = tk.readIfLabel();
    //    boolean defValue = tk.readIfBool(false);
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
  public   void sOpenComboBox(int id, String label, String toolTip, boolean asRadio) {

    // cb <id:int> [<title:label>] <asradio:bool> [<tooltip:label>]
    //       ( {<id:int> <lbl:label>} )

   // int cid = tk.readInt();
//    String label = tk.readIfLabel();
//    boolean asRadio = tk.readBoolean();
//    String toolTip = tk.readIfLabel();
//    tk.read(T_PAROP);
    checkState(mComboBox == null,"ComboBox is already open");
    CtComboBox box = new CtComboBox(label, toolTip, asRadio);
    box.setId(id);
    mComboBox = box;
addControl(box, toolTip);
  }

  /**
   * Add a choice to a combobox
   * 
   * @param id
   *          id of choice
   * @param label
   */
  public   void sChoice(int id, String label) {
      mComboBox.addItem(id, label);
  }
  
  /**
   * Close combo box (previously opened with sOpenComboBox())
   */
  public   void sCloseComboBox() {
    checkState(mComboBox != null,"no ComboBox open");
   mComboBox = null;
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
    int colWidth = 0; //tk.readIfInt(0);
    addControl(new CtLabel(colWidth, text).setId(C.getAnonId()), null);
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

  private void processScript() {
    while (true) {

      if (tk.eof() || tk.peek(T_PARCL))
        break;

      tk.trace("");
      Token t = tk.read();

      switch (t.id()) {
      default:
        t.exception("unexpected token");

      case T_BUTTON: {
        int id = tk.readInt();
        String label = tk.readLabel();
        String toolTip = tk.readIfLabel();
        addControl(new CtButton(Gadget.createAction(id, label, toolTip, null)).setId(id), null);
      }
        break;

      case T_CHECKBOX: {
        int id = tk.readInt();
        String label = tk.readIfLabel();

        String toolTip = tk.readIfLabel();
        boolean defValue = tk.readIfBool(false);
        addControl(new CtCheckBox(id, label, defValue, false, parseToolTip(toolTip), null), null);
      }
        break;

      case T_COMBOBOX:
        addComboBox();
        break;

      case T_PNL_HTAB: {
        if (tk.trace()) {
          System.out.println(" processing tab set");
        }
        int panelId = readIdn();

        TabbedPaneGadget tb = new TabbedPaneGadget(true);
        tb.setId(panelId);
        mStackPanel.addItem(tb.getComponent());

        for (int tabNumber = 0; !tk.readIf(T_PARCL); tabNumber++) {
          int tabId = tk.readIfInt(tabNumber);

          String tabLabel = tk.readLabel();
          pushScope(null);
          tb.addTab(tabLabel, tabId, mStackPanel.component());

          tk.read(T_PAROP);
          processScript();
          tk.read(T_PARCL);
          popScope();
        }
        addControl(tb, null);
      }
        break;

      case T_PAROP: {
        String title = tk.readIfLabel();
        StackPanel prevScope = mStackPanel;
        push(mStack, mStackPanel);
        mStackPanel = new StackPanel(title);
        prevScope.addItem(mStackPanel.component());
        processScript();
        tk.read(T_PARCL);
        popScope();
      }
        break;

      case T_NEWCOL:
        mStackPanel.startNewColumn();
        break;

      case T_SLIDER_INT:
      case T_SLIDER_DBL:
      case T_SPIN_INT:
      case T_SPIN_DBL: {
        // s [<lbl:label>] <id:int> [<tooltip:label>] <min:int> <max:int> <def:int> <step:int> 
        boolean slider = (t.id(T_SLIDER_INT) || t.id(T_SLIDER_DBL));
        boolean dbl = (t.id(T_SLIDER_DBL) || t.id(T_SPIN_DBL));

        String lbl = tk.readIfLabel();
        int id = tk.readInt();
        String toolTip = tk.readIfLabel();
        double min = tk.readDouble();
        double max = tk.readDouble();
        double val = tk.readDouble();
        double step = tk.readDouble();

        addSpinner(id, lbl, min, max, val, step, slider, false, dbl, toolTip);
      }
        break;

      case T_TRACE:
        tk.setTrace(!tk.trace());
        break;

      case T_LABEL: {
        int colWidth = tk.readIfInt(0);
        addControl(new CtLabel(colWidth, tk.readLabel()).setId(C.getAnonId()), null);
      }
        break;

      case T_TEXTAREA:
      case T_TEXTAREA_FW: {
        // xf <id:int> [<lbl:label>] 0 [<tooltip:label>] 0 <defaultValue:label> 
        int id = tk.readInt();
        String label = tk.readIfLabel();
        int i0 = tk.readInt();
        String toolTip = tk.readIfLabel();
        int i1 = tk.readInt();
        String defVal = tk.readLabel();
        addControl(
            new CtTextArea(label, SwingConstants.CENTER, defVal, i0, i1, t.id(T_TEXTAREA_FW)).setId(id),
            toolTip);
      }
        break;

      case T_HIDDEN:
        hideNextControl = true;
        break;

      case T_TEXTFLD_STR_FW:
      case T_TEXTFLD_STR: {
        // t  [<label:label>] <id:int> [<tooltip:label>] <maxlen:int> <value:label>
        String label = tk.readIfLabel();
        int id = tk.readInt();
        String toolTip = tk.readIfLabel();
        int maxlen = tk.readInt();
        String value = tk.readLabel();
        addTextField(id, label, value, maxlen, t.id(T_TEXTFLD_STR_FW), toolTip);
      }
        break;

      }
    }
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

  private StackPanel pushScope(String title) {
    push(mStack, mStackPanel);
    mStackPanel = new StackPanel(title);
    return mStackPanel;
  }

  private List<Object> mStack;
  // current panel
  private StackPanel mStackPanel;
  private TabbedPaneGadget mTabbedPaneGadget;
  // hide next control?
  private boolean hideNextControl;
  private GadgetTokenizer tk;
  private CtComboBox mComboBox;

}

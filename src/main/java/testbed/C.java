package testbed;

import java.awt.*;

import js.json.JSMap;

import static js.base.Tools.*;

public class C implements Globals {

  private C() {
    loadTools();
  }

  /**
   * Determine if Gadget events should be propagated to listeners (including the
   * project or script record of gadget values). False while user interface is
   * still being constructed
   */
  public static boolean gadgetsActive() {
    return sGadgetsActive;
  }

  public static void setGadgetsActive(boolean state) {
    sGadgetsActive = state;
  }

  private static boolean sGadgetsActive;

  public static void add(Gadget c) {
    mGadgetSet.add(c);
  }

  /**
   * Add a hidden gadget
   */
  public static Gadget addHidden(int id, Object defaultValue) {
    return mGadgetSet.addHidden(id, defaultValue);
  }

  /**
   * Adjust a gadget's enabled state
   * 
   * @param id
   *          id of gadget
   * @param f
   *          true to enable it
   */
  public static void enable(int id, boolean f) {
    mGadgetSet.setEnable(id, f);
  }

  /**
   * Adjust a series of gadget enabled states
   * 
   * @param idList
   *          array of gadget ids
   * @param f
   *          true to enable them
   */
  public static void enable(int[] idList, boolean f) {
    mGadgetSet.setEnable(idList, f);
  }

  /**
   * Get (integer) value of gadget
   * 
   * @param id
   *          id of gadget
   * @return value
   */
  public static int vi(int id) {
    return mGadgetSet.numValue(id).intValue();
  }

  /**
   * Set value of (integer-valued) gadget
   */
  public static int seti(int id, int v) {
    mGadgetSet.setValue(id, v);
    return v;
  }

  /**
   * Get boolean value of gadget
   */
  public static boolean vb(int id) {
    return mGadgetSet.booleanValue(id);
  }

  /**
   * Set boolean value of gadget
   */
  public static boolean setb(int id, boolean boolvalue) {
    mGadgetSet.setValue(id, boolvalue);
    return boolvalue;
  }

  /**
   * Toggle boolean value of gadget
   * 
   * @param id
   *          id of gadget
   * @return new value
   */
  public static boolean toggle(int id) {
    return setb(id, !vb(id));
  }

  static ControlPanel getControlPanel(int pnlId) {
    return ctrlPanels[pnlId];
  }

  /**
   * Get (double) value of gadget
   * 
   * @param id
   *          of gadget
   * @return value
   */
  public static double vd(int id) {
    return mGadgetSet.numValue(id).doubleValue();
  }

  public static float vf(int id) {
    return ((Number) mGadgetSet.get(id).readValue()).floatValue();
  }

  /**
   * Set double value of gadget
   */
  public static double setd(int id, double v) {
    mGadgetSet.setValue(id, v);
    return v;
  }

  /**
   * Set float value of gadget
   */
  public static double set(int id, float v) {
    mGadgetSet.setValue(id, v);
    return v;
  }

  /**
   * Get (string) value of gadget
   */
  public static String vs(int id) {
    return mGadgetSet.stringValue(id);
  }

  static Component getComponent(int id) {
    return mGadgetSet.get(id).getComponent();
  }

  /**
   * Determine if a gadget with a particular id exists
   * 
   * @param id
   *          id of gadget
   * @return true if it exists
   */
  public static boolean exists(int id) {
    return mGadgetSet.exists(id);
  }

  /**
   * Get gadget
   * 
   * @param id
   *          : id of gadget
   * @return Gadget
   */
  static Gadget get(int id) {
    return mGadgetSet.get(id);
  }

  /**
   * Start constructing a new script
   * 
   * @return StringBuilder containing script
   */
  @Deprecated
  static void openScript() {
    script = new StringBuilder();
  }

  /**
   * Close constructed script, return as string
   * 
   * @return String
   */
  @Deprecated
  static String closeScript() {
    String s = script.toString();
    script = null;
    return s;
  }

  /**
   * Hide the next gadget to be added. A hidden gadget can still be read/written
   * to, and its values will be saved, but it just won't show up on the screen.
   */
  public static void sHide() {
    script.append(" ? ");
  }

  /**
   * Optionally hide the next gadget to be added. A hidden gadget can still be
   * read/written to, and its values will be saved, but it just won't show up on
   * the screen.
   * 
   * @param visible
   *          if false, hides next gadget
   * @return visibility state of gadget
   */
  public static boolean sHide(boolean visible) {
    if (!visible)
      sHide();
    return visible;
  }

  /**
   * Add static (immutable) text to panel
   * 
   * @param s
   */
  public static void sStaticText(String s) {
    sText('l');
    sIValue(45);
    sLbl(s);
    sNewLine();
  }

  static void sIValue(int iValue) {
    script.append(' ');
    script.append(iValue);
  }

  static void sDValue(double dValue) {
    script.append(' ');
    script.append(dValue);
  }

  static void sNewLine() {
    script.append('\n');
  }

  static void sBool(boolean v) {
    script.append(v ? " T" : " F");
  }

  /**
   * Open a nested panel within the current panel. Must be balanced by a call to
   * sClose().
   * 
   * @param title
   *          if not null, surrounds panel with title box
   */
  public static void sOpen(String title) {
    // ( <title:label> <script> )
    sNewLine();
    sText('(');
    sLblnn(title);
    sNewLine();
  }

  static char sLastChar() {
    char c = ' ';
    int j = C.script.length();
    while (j > 0) {
      j--;
      c = C.script.charAt(j);
      if (c > ' ')
        break;
    }
    return c;
  }

  /**
   * Open a nested panel within the current panel. Must be balanced by a call to
   * sClose()
   */
  public static void sOpen() {
    sOpen(null);
  }

  private static void sText(char c) {
    script.append(' ');
    script.append(c);
  }

  private static void sText(String s) {
    script.append(' ');
    script.append(s);
  }

 

  /**
   * Close panel (previously opened with sOpen())
   */
  public static void sClose() {
    sText(')');
    sNewLine();
  }

  /**
   * Open a tab set. Must be balanced by a call to sCloseTabSet().
   * 
   * @param id
   *          id of set; reading the value of this gadget will return the
   *          identifier of the currently selected tab
   */
  public static void sOpenTabSet(int id) {
    tabSetCount++;
    // (h  [<tabsetid:int>] { [<tabid:int>] <tablabel:label> ( <gadgets:script> ) } )
    sText("(h");
    if (id >= 0)
      sIValue(id);
  }

  /**
   * Add a tab to the tab set (previously opened with sOpenTabSet()). Must be
   * balanced by a call to sCloseTab(). The id of the tab will be its index
   * within the set.
   * 
   * @param title
   *          title of tab
   */
  public static void sOpenTab(String title) {
    sOpenTab(-1, title);
  }

  /**
   * Add a tab to the tabbed pane (previously opened with sOpenTabSet()). Must
   * be balanced by a call to sCloseTab().
   * 
   * @param id
   *          id of tab; if < TAB_ID_START, ignores this value and uses the
   *          tab's index as the id
   * @param title
   *          title of tab
   */
  public static void sOpenTab(int id, String title) {
    if (tabSetCount == 0)
      throw new IllegalStateException("tab set parity problem");
    sNewLine();
    if (id >= TAB_ID_START)
      C.sIValue(id);
    if (title == null)
      title = "<name?>";
    C.sLbl(title);
    C.sText('(');
    sNewLine();

    tabPaneCount++;
  }

  /**
   * Close tab (previously opened with sOpenTab())
   */
  public static void sCloseTab() {
    if (tabSetCount == 0 || tabPaneCount == 0)
      throw new IllegalStateException("tab set parity problem");
    sText(')');
    sNewLine();
    tabPaneCount--;
  }

  /**
   * Close tab set (previously opened with sOpenTabSet())
   */
  public static void sCloseTabSet() {
    if (tabSetCount == 0)
      throw new IllegalStateException("tab set parity problem");
    tabSetCount--;

    sText(')');
    sNewLine();
  }

  private static void sLblnn(CharSequence label) {
    if (label != null)
      sLbl(label);
  }

  /**
   * Add a label to the script, enclosed within single quotes '....', and with
   * appropriate escape characters
   * 
   * @param label
   *          String, if null, uses empty string
   */
  private static void sLbl(CharSequence label) {
    if (label == null)
      throw new IllegalArgumentException();

    script.append(" '");
    if (label != null) {
      for (int i = 0; i < label.length(); i++) {
        char c = label.charAt(i);
        switch (c) {
        default:
          script.append(c);
          break;
        case '\n':
          script.append("\\n");
          break;
        case '\'':
          script.append("\\'");
          break;
        }
      }
    }
    script.append("'");
  }

  /**
   * Append a string to the script
   * 
   * @param obj
   */
  static void sAppend(Object obj) {
    script.append(obj.toString());
  }

  /**
   * Add a script of controls to the main control panel
   * 
   * @param script
   *          : control script
   */
  @Deprecated
  static void addControls(String script) {
    addControls(script, TBGlobals.CT_MAIN);
  }

  /**
   * Add a script of controls to one of the panels
   * 
   * @param script
   *          : control script
   * @param panel
   *          : CT_xxx
   */
  @Deprecated
  static void addControls(String script, int panel) {
    ctrlPanels[panel].processScript(script);
  }

  public static ControlPanel controlPanel() {
    return ctrlPanels[TBGlobals.CT_MAIN];
  }

  public static void prepareForGadgets() {
    controlPanel().prepareForGadgets();
  }

  public static void finishedGadgets() {
    controlPanel().finishedGadgets();
  }

  /**
   * Get the next anonymous id
   * 
   * @return int
   */
  static int getAnonId() {
    return anonIdBase++;
  }

  public static void readGadgetValuesFromMap(JSMap map) {
    mGadgetSet.setValues(map);
  }

  /**
   * Get JSMap representing widget values
   */
  public static JSMap constructGadgetValueMap() {
    return mGadgetSet.getValues();
  }

  static void init() {
    anonIdBase = TBGlobals.ID_ANON_START;
    ctrlPanels = new ControlPanel[TBGlobals.CT_TOTAL];
    for (int i = 0; i < TBGlobals.CT_TOTAL; i++)
      ctrlPanels[i] = new ControlPanel();
    mGadgetSet = new GadgetList();

    // Add gadget for persisting frame bounds
    C.add(new AppFrameGadget().setId(TBGlobals.APP_FRAME));
    // Add gadget for persisting zoom factor
    C.addHidden(TBGlobals.EDITOR_ZOOM, 1f);
    C.addHidden(TBGlobals.CURRENT_SCRIPT_INDEX, 0);

    script = null;
    tabPaneCount = 0;
    tabSetCount = 0;
  }

  private static int anonIdBase;
  private static ControlPanel[] ctrlPanels;
  private static GadgetList mGadgetSet;
  private static StringBuilder script;
  private static int tabPaneCount;
  private static int tabSetCount;

}

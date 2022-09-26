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
  }

  private static int anonIdBase;
  private static ControlPanel[] ctrlPanels;
  private static GadgetList mGadgetSet;

}

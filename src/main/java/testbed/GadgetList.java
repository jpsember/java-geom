package testbed;

import java.util.*;

import js.json.JSMap;

import static js.base.Tools.*;

public final class GadgetList {

  /**
   * Enable each gadget in a list
   * 
   * @param idList
   *          : list of ids to enable
   * @param state
   *          : enable state to set to
   */
  public void setEnable(int[] idList, boolean state) {
    for (int i = 0; i < idList.length; i++) {
      setEnable(idList[i], state);
    }
  }

  /**
   * Read enable state of gadget
   * 
   * @param id
   *          : gadget id
   * @return true if gadget is enabled
   */
  public boolean enabled(int id) {
    Gadget c = get(id);
    return c.getComponent().isEnabled();
  }

  /**
   * Set enable status of a gadget and its children
   * 
   * @param id
   *          : gadget id
   * @param state
   *          : true to enable, false to disable
   */
  public void setEnable(int id, boolean state) {
    Gadget c = get(id);
    c.getComponent().setEnabled(state);
  }
 
  public Number numValue(int id) {
    return (Number) get(id).readValue();
  }

 
  /**
   * Get value of boolean-valued gadget
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public boolean booleanValue(int id) {
    Boolean result = null;
    Gadget g = get(id);
    if (g != null)
      result = (Boolean) g.readValue();
    if (result == null)
      result = false;
    return result;
  }

  /**
   * Set value of integer-valued gadget
   */
  public void setValue(int id, int v) {
    get(id).writeValue(v);
  }

  /**
   * Set value of boolean-valued gadget
   */
  public void setValue(int id, boolean v) {
    get(id).writeValue(v);
  }

  /**
   * Set value of double-valued gadget
   * 
   * @param id
   *          int
   * @param v
   *          double
   */
  public void setValue(int id, double v) {
    get(id).writeValue(v);
  }

  /**
   * Get value of string-valued gadget
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public String stringValue(int id) {
    return (String) (get(id).readValue());
  }

  /**
   * Set value of string-valued gadget
   * 
   * @param id
   *          int
   * @param v
   *          String
   */
  public void setValue(int id, String v) {
    get(id).writeValue(v);
  }

  private Gadget find(int id) {
    return mGadgetMap.get(id);
  }

  public boolean exists(int id) {
    return (find(id) != null);
  }

  public Gadget get(int id) {
    return find(id);
  }

  public void add(Gadget c) {
    //todo("move this class to Geom package?");
    checkState(!exists(c.getId()));
    mGadgetMap.put(c.getId(), c);
  }

  public Gadget addHidden(int id, Object defaultValue) {
    checkState(!exists(id));
    Gadget g = new HiddenGadget(defaultValue).setId(id);
    add(g);
    return g;
  }

  /**
   * Get JSMap of gadget values
   */
  @Deprecated //merge into caller
  public JSMap getValues() {
    JSMap m = map();
    for (Map.Entry<Integer, Gadget> ent : mGadgetMap.entrySet()) {
      Gadget g = ent.getValue();
      // If it's not a gadget we're interested in retaining the value of, skip.
      if (!g.serialized())
        continue;
      Object v = g.readValue();
      if (v == null)
        continue;
      m.putUnsafe("" + ent.getKey(), v);
    }
    return m;
  }

  @Deprecated //merge setValues()
  public void setValues(JSMap m) {
    for (Map.Entry<String, Object> entry : m.wrappedMap().entrySet()) {
      int id = Integer.parseInt(entry.getKey());
      if (!exists(id))
        continue;
      get(id).writeValue(entry.getValue());
    }
  }

  
  
  
  
  

  /**
   * Adjust a gadget's enabled state
   * 
   * @param id
   *          id of gadget
   * @param f
   *          true to enable it
   */
  @Deprecated // Use setEnable()
  public   void enable(int id, boolean f) {
     setEnable(id, f);
  }

  /**
   * Adjust a series of gadget enabled states
   * 
   * @param idList
   *          array of gadget ids
   * @param f
   *          true to enable them
   */
  @Deprecated // Use enable()
  public  void enable(int[] idList, boolean f) {
     setEnable(idList, f);
  }

  /**
   * Get (integer) value of gadget
   */
  public  int vi(int id) {
    return  numValue(id).intValue();
  }

  /**
   * Set value of (integer-valued) gadget
   */
  public   int seti(int id, int v) {
     setValue(id, v);
    return v;
  }

  /**
   * Get boolean value of gadget
   */
  public   boolean vb(int id) {
    return  booleanValue(id);
  }

  /**
   * Set boolean value of gadget
   */
  public   boolean setb(int id, boolean boolvalue) {
    setValue(id, boolvalue);
    return boolvalue;
  }

  /**
   * Toggle boolean value of gadget
   * 
   * @return new value
   */
  public   boolean toggle(int id) {
    return setb(id, !vb(id));
  }

  /**
   * Get (double) value of gadget
   */
  public   double vd(int id) {
    return numValue(id).doubleValue();
  }

  public   float vf(int id) {
    return ((Number)  get(id).readValue()).floatValue();
  }

  /**
   * Set double value of gadget
   */
  public   double setd(int id, double v) {
    setValue(id, v);
    return v;
  }

  /**
   * Set float value of gadget
   */
  public   double set(int id, float v) {
     setValue(id, v);
    return v;
  }

  /**
   * Get (string) value of gadget
   */
  public   String vs(int id) {
    return  stringValue(id);
  }

//
//  public static ControlPanel DEPRECATEDcontrolPanel() {
//    return mMainControlPanel;
//  }

  /**
   * Get the next anonymous id
   * 
   * @return int
   */
 public int getAnonId() {
    return sAnonIdBase++;
  }

  public   void readGadgetValuesFromMap(JSMap map) {
     setValues(map);
  }

  /**
   * Get JSMap representing widget values
   */
  public   JSMap constructGadgetValueMap() {
    return  getValues();
  }

//  static void initDEPRECATED() {
//    sAnonIdBase = TBGlobals.ID_ANON_START;
//    mMainControlPanel = new ControlPanel();
//    mGadgetSet = new GadgetList();
//
//    // Add gadget for persisting frame bounds
//    C.add(new AppFrameGadget().setId(TBGlobals.APP_FRAME));
//    // Add gadget for persisting zoom factor
//    C.addHidden(TBGlobals.EDITOR_ZOOM, 1f);
//    C.addHidden(TBGlobals.CURRENT_SCRIPT_INDEX, 0);
//  }
//  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  private SortedMap<Integer, Gadget> mGadgetMap = treeMap();
  private   int sAnonIdBase = TBGlobals.ID_ANON_START;
  
  private static class HiddenGadget extends Gadget {

    public HiddenGadget(Object defaultValue) {
      mValue = defaultValue;
    }

    @Override
    public Object readValue() {
      return mValue;
    }

    @Override
    public void writeValue(Object v) {
      checkArgument(v != null);
      mValue = v;
    }

    private Object mValue;
  }
}

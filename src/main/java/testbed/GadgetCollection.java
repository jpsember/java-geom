package testbed;

import java.util.*;

import js.json.JSMap;

import static js.base.Tools.*;

/**
 * A collection of Gadgets
 */
public final class GadgetCollection {

  /**
   * Determine if Gadget events should be propagated to listeners (including the
   * project or script record of gadget values). False while user interface is
   * still being constructed
   */
  public boolean active() {
    return mActive;
  }

  public void setActive(boolean state) {
    mActive = state;
  }

  /**
   * Enable each gadget in a list
   */
  public void setEnable(int[] idList, boolean state) {
    for (int i = 0; i < idList.length; i++) {
      setEnable(idList[i], state);
    }
  }

  /**
   * Read enable state of gadget
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
   * Get (integer) value of gadget
   */
  public int vi(int id) {
    return numValue(id).intValue();
  }

  /**
   * Set value of (integer-valued) gadget
   */
  public int seti(int id, int v) {
    setValue(id, v);
    return v;
  }

  /**
   * Get boolean value of gadget
   */
  public boolean vb(int id) {
    return booleanValue(id);
  }

  /**
   * Set boolean value of gadget
   */
  public boolean setb(int id, boolean boolvalue) {
    setValue(id, boolvalue);
    return boolvalue;
  }

  /**
   * Toggle boolean value of gadget
   * 
   * @return new value
   */
  public boolean toggle(int id) {
    return setb(id, !vb(id));
  }

  /**
   * Get (double) value of gadget
   */
  public double vd(int id) {
    return numValue(id).doubleValue();
  }

  public float vf(int id) {
    return ((Number) get(id).readValue()).floatValue();
  }

  /**
   * Set double value of gadget
   */
  public double setd(int id, double v) {
    setValue(id, v);
    return v;
  }

  /**
   * Set float value of gadget
   */
  public double set(int id, float v) {
    setValue(id, v);
    return v;
  }

  /**
   * Get (string) value of gadget
   */
  public String vs(int id) {
    return stringValue(id);
  }

  /**
   * Allocate another anonymous id
   */
  public int getAnonId() {
    return mAnonIdBase++;
  }

  /**
   * Set gadget values according to a JSMap
   */
  public void writeGadgetValues(JSMap map) {
    for (Map.Entry<String, Object> entry : map.wrappedMap().entrySet()) {
      int id = Integer.parseInt(entry.getKey());
      if (!exists(id))
        continue;
      get(id).writeValue(entry.getValue());
    }
  }

  /**
   * Read gadget values into JSMap
   */
  public JSMap readGadgetValues() {
    JSMap m = map();
    for (Map.Entry<Integer, Gadget> ent : mGadgetMap.entrySet()) {
      Gadget g = ent.getValue();
      Object v = g.readValue();
      if (v != null)
        m.putUnsafe("" + ent.getKey(), v);
    }
    return m;
  }

  private SortedMap<Integer, Gadget> mGadgetMap = treeMap();
  private int mAnonIdBase = TBGlobals.ID_ANON_START;
  private boolean mActive;
}

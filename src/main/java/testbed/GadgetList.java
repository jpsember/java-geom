package testbed;

import java.util.*;

import js.json.JSMap;

import static js.base.Tools.*;

final class GadgetList {

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

  /**
   * Get value of integer-valued gadget
   */
  @Deprecated
  public int intValue(int id) {
    return numValue(id).intValue();
  }

  public Number numValue(int id) {
    return (Number) get(id).readValue();
  }

  /**
   * Get value of double-valued gadget
   */
  @Deprecated
  public double doubleValue(int id) {
    return numValue(id).doubleValue();
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

  public void setValues(JSMap m) {
    for (Map.Entry<String, Object> entry : m.wrappedMap().entrySet()) {
      int id = Integer.parseInt(entry.getKey());
      if (!exists(id))
        continue;
      get(id).writeValue(entry.getValue());
    }
  }

  private SortedMap<Integer, Gadget> mGadgetMap = treeMap();

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

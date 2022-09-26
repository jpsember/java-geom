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
  public int intValue(int id) {
    return numValue(id).intValue();
  }

  public Number numValue(int id) {
    return (Number) get(id).readValue();
  }

  /**
   * Get value of double-valued gadget
   */
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
   * Get string describing object
   * 
   * @return String
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("GadgetList ");
    for (Gadget g : mGadgetMap.values()) {
      sb.append(" " + g.getId() + ":" + g.toString() + "\n");
    }
    return sb.toString();
  }

  private List<Integer> getList(boolean configContext) {
    List<Integer> ret = arrayList();
    for (Gadget g : mGadgetMap.values()) {
      int id = g.getId();

      // skip this; it messes up saving of window locations
      /*
       * if (!configContext) { if (id >= TBGlobals.CONFIGSTART && id <
       * TBGlobals.CONFIGEND) continue; }
       */
      ret.add(id);
    }
    return ret;
  }

  /**
   * Get JSMap of gadget values
   * 
   * @param configContext
   *          true if configuration file, false if editor file
   */
  public JSMap getValues(boolean configContext) {
    todo("some of the gadgets are being stored as strings, which probably doesn't make sense");
    final boolean db = false;

    if (db)
      pr("getValues");

    JSMap m = map();

    List<Integer> idList = getList(configContext);

    Gadget g = null;

    for (int i = 0; i < idList.size(); i++) {
      int id = idList.get(i);
      g = get(id);

      // If it's not a gadget we're interested in retaining the value of, skip.

      //Streams.out.println("id="+id+" value="+g.readValue()+" ser="+g.serialized());
      if (!g.serialized())
        continue;
      Object v = g.readValue();
      if (db)
        pr("value for", id, "is:", v);
      if (v == null)
        continue;
      m.putUnsafe("" + id, v);
    }
    if (db)
      pr("returning:", INDENT, m);
    return m;
  }

  public void setValues(JSMap m) {
    final boolean db = false && alert("debug in effect");
    for (Map.Entry<String, Object> entry : m.wrappedMap().entrySet()) {
      int id = Integer.parseInt(entry.getKey());
      if (db)
        pr("setValue:", id, "->", entry.getValue());
      if (!exists(id)) {
        if (db)
          pr("...doesn't exist");
        continue;
      }
      Gadget g = get(id);
      g.writeValue(entry.getValue());
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

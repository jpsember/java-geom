package testbed;

import java.util.*;

import base.TextScanner;

import static js.base.Tools.*;
import static testbed.IEditorScript.*;

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
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public int intValue(int id) {
    Gadget obj = get(id);
    if (obj == null) {
      return 0;
    }
    Integer iVal;
    Object v = obj.readValue();
    if (v == null)
      iVal = 0;
    else if (v instanceof Integer) {
      iVal = (Integer) v;
    } else {
      iVal = Integer.parseInt(v.toString());
    }
    return iVal;
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
   * 
   * @param id
   *          int
   * @param v
   *          int
   */
  public void setValue(int id, int v) {
    Object val = v;
    Gadget g = get(id);
    if (g.dataType() == Gadget.DT_STRING)
      val = Integer.toString(v);
    g.writeValue(val);
  }

  /**
   * Set value of boolean-valued gadget
   * 
   * @param id
   *          int
   * @param v
   *          boolean
   */
  public void setValue(int id, boolean v) {
    Gadget g = get(id);
    if (g != null)
      g.writeValue(v);
  }

  /**
   * Get value of double-valued gadget
   * 
   * @param id
   *          : id of gadget
   * @return value
   */
  public double doubleValue(int id) {
    final boolean db = false;
    if (db)
      System.out.println("doubleValue id=" + id + " readValue is " + get(id).readValue());
    return ((Double) get(id).readValue());
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
   * Get string describing gadget values
   * 
   * @param configContext
   *          true if configuration file, false if editor file
   * @return string containing values
   */
  public String getValues(boolean configContext) {
    final boolean db = false;

    if (db)
      System.out.println("getValues ");

    StringBuilder sb = new StringBuilder();
    sb.append('[');

    List<Integer> idList = getList(configContext);

    Gadget g = null;
    Object v = null;

    int lastCR = 0;

    for (int i = 0; i < idList.size(); i++) {
      int id = idList.get(i);
      g = get(id);

      // If it's not a gadget we're interested in retaining the value of, skip.

      //Streams.out.println("id="+id+" value="+g.readValue()+" ser="+g.serialized());
      if (!g.serialized())
        continue;
      if (db)
        System.out.println(" attempting to read value for id " + id + ", g=\n" + g);
      v = g.readValue();
      if (db)
        System.out.println(" value " + id + " is " + v);
      if (v == null)
        continue;

      sb.append(g.getId());
      sb.append(' ');
      switch (g.dataType()) {
      case Gadget.DT_BOOL:
        sb.append(f(((Boolean) v)));
        break;
      case Gadget.DT_DOUBLE:
        sb.append(f((Double) v));
        break;
      case Gadget.DT_STRING:
        sb.append(TextScanner.convert((String) v, false, '"'));
        break;
      case Gadget.DT_INT:
        sb.append(v);
        break;
      }
      if (sb.length() - lastCR > 60) {
        sb.append('\n');
        lastCR = sb.length();
      } else
        sb.append(' ');
    }
    sb.append(']');
    if (db)
      System.out.println(" returning\n" + sb.toString());
    return sb.toString();
  }

  private static String f(boolean b) {
    return b ? " T" : " F";
  }

  /**
   * Format a double into a string, without scientific notation
   * 
   * @param v
   *          : value
   * @param iDig
   *          : number of integer digits to display
   * @param fDig
   *          : number of fractional digits to display
   * @return String, with format siiii.fff where s = sign (' ' or '-'), . is
   *         present only if fDig > 0 if overflow, returns s********* of same
   *         size
   */
  private static String f(double v, int iDig, int fDig) {

    StringBuilder sb = new StringBuilder();

    boolean neg = false;
    if (v < 0) {
      neg = true;
      v = -v;
    }

    int[] dig = new int[iDig + fDig];

    boolean overflow = false;

    // Determine which digits will be displayed.
    // Round last digit and propagate leftward.
    {
      double n = Math.pow(10, iDig);
      if (v >= n) {
        overflow = true;
      } else {
        double v2 = v;
        for (int i = 0; i < iDig + fDig; i++) {
          n /= 10.0;
          double d = Math.floor(v2 / n);
          dig[i] = (int) d;
          v2 -= d * n;
        }
        double d2 = Math.floor(v2 * 10 / n);
        if (d2 >= 5) {
          for (int k = dig.length - 1;; k--) {
            if (k < 0) {
              overflow = true;
              break;
            }
            if (++dig[k] == 10) {
              dig[k] = 0;
            } else
              break;
          }
        }
      }
    }

    if (overflow) {
      int nDig = iDig + fDig + 1;
      if (fDig != 0)
        nDig++;
      for (int k = 0; k < nDig; k++)
        sb.append("*");
    } else {

      sb.append(' ');
      int signPos = 0;
      boolean leadZero = false;
      for (int i = 0; i < iDig + fDig; i++) {
        int digit = dig[i]; //(int) d;
        if (!leadZero) {
          if (digit != 0 || i == iDig || (i == iDig - 1 && fDig == 0)) {
            leadZero = true;
            signPos = sb.length() - 1;
          }
        }
        if (i == iDig) {
          sb.append('.');
        }

        if (digit == 0 && !leadZero) {
          sb.append(' ');
        } else {
          sb.append((char) ('0' + digit));
        }
      }
      if (neg)
        sb.setCharAt(signPos, '-');
    }
    return sb.toString();
  }

  private static String f(double f) {
    return f(f, 5, 3);
  }

  /**
   * Parse a sequence of gadget values. Assumes the values are surrounded by '['
   * and ']' tokens.
   *
   * @param tk
   *          Tokenizer
   */
  public void setValues(Tokenizer tk) {
    if (tk.readIf(T_BROP)) {
      outer: while (!tk.readIf(T_BRCL)) {

        // if unexpected boolean, skip
        if (tk.readIf(T_BOOL))
          continue outer;

        int id = tk.readInt();

        if (!exists(id)) {
          tk.read();
          continue;
        }

        Object v = null;
        Gadget g = get(id);
        //pr("restoring widget value for:", id, "type:", g.dataType);
        switch (g.dataType()) {
        case Gadget.DT_BOOL:
          v = tk.readBoolean();
          break;
        case Gadget.DT_DOUBLE:
          v = tk.readDouble();
          break;
        case Gadget.DT_STRING:
          // skip unexpected booleans
          if (tk.peek(T_BOOL))
            continue outer;
          if (tk.peek(T_INT)) {
            v = Integer.toString(tk.readInt());
          } else
            v = tk.readString();
          break;
        case Gadget.DT_INT:
          v = tk.readInt();
          if (id == Gadget.TEST_GADGET) {
            if (alert("special test")) {
              pr("restored value:", v);
              v = 98;
              pr("replacing with:", v);
            }
          }
          break;
        }
        //        Streams.out.println("attempting to write value="+Tools.d(v)+" to gadget "+g);
        //pr("writing value:", v, "to gadget:", g.getId());
        g.writeValue(v);
      }
    }
  }

  private SortedMap<Integer, Gadget> mGadgetMap = treeMap();

}

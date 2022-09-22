package testbed;

import static js.base.Tools.*;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;
import java.util.List;

/**
 * Base class for control gadgets
 */
abstract class Gadget implements Globals, ChangeListener, PropertyChangeListener {

  public static final int TEST_GADGET = 1801;

  public void stateChanged(ChangeEvent changeEvent) {
    loadTools();
    TestBed.singleton().processAction(new TBAction(TBAction.CTRLVALUE, getId()));
  }

  // ------------------------------------------------------
  // PropertyChangeListener interface
  // ------------------------------------------------------

  public void propertyChange(PropertyChangeEvent e) {
    TestBed.singleton().processAction(new TBAction(TBAction.CTRLVALUE, getId()));
  }

  public static final int DT_STRING = 0, DT_INT = 1, DT_DOUBLE = 2, DT_BOOL = 3;

  public int dataType() {
    if (mDataType < 0)
      throw badState("no datatype for:", id);
    return mDataType;
  }

  public void setDataType(int datatype) {
    if (mDataType >= 0)
      badState("datatype already set for:", id);
    mDataType = datatype;
  }

  public List<Integer> children() {
    return mChildIds;
  }

  /**
   * Create an action for the gadget
   */
  public static Action createAction(int id, String label, String toolTip, KeyStroke accel) {
    Action action = new GadgetAction(id, label, toolTip, accel);
    return action;
  }

  /**
   * Get the id assigned to this component
   */
  public int getId() {
    return this.id;
  }

  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the value stored in this component
   * 
   * @return Object : a String, Double, Integer, etc.
   */
  public Object readValue() {
    if (id == TEST_GADGET) {
      pr("readValue, returning:", value);
    }
    return value;
  }

  /**
   * Store a new value to this component
   * 
   * @param v
   *          : a String, Double, Integer, etc.
   */
  public void writeValue(Object v) {
    this.value = v;
  }

  /**
   * Get Swing component associated with gadget
   * 
   * @return Component
   */
  public Component getComponent() {
    return this.component;
  }

  public void setComponent(Component c) {
    this.component = c;
  }

  /**
   * Get GridBagConstraints fill parameter for this gadget
   * 
   * @return int
   */
  public int gcFill() {
    return GridBagConstraints.HORIZONTAL;
  }

  /**
   * Determine if this gadget has a value that needs to be serialized. The
   * default implementation returns true if gadget has a value defined.
   *
   * @return boolean
   */
  public boolean serialized() {
    Object v = readValue();
    return v != null;
  }

  private static class GadgetAction extends MyAction {

    private static final String GADGETID = "GADGETID";

    /**
     * Constructor
     * 
     * @param id
     *          : id of gadget associated with action
     * @param name
     *          : name of action (i.e., menu item label)
     * @param toolTip
     *          : tooltip message
     * @param accel
     *          : keystroke to use as accelerator
     */
    public GadgetAction(int id, String name, String toolTip, KeyStroke accel) {

      super(name);
      putValue(GADGETID, new Integer(id));
      setAccelerator(accel);
      if (toolTip != null) {
        setTooltipText(toolTip, true);
      }
    }

    public int id() {
      return ((Integer) getValue(GADGETID)).intValue();
    }

    public String toString() {
      return "GadgetAction id=" + id() + "\n" + super.toString();
    }

    public void actionPerformed(ActionEvent e) {
      // send application a CTRLVALUE action with the gadget id.
      TestBed.singleton().processAction(new TBAction(TBAction.CTRLVALUE, id()));
    }

  }

  private Component component;
  private int id;
  private Object value;

  // type of data (DT_xxx)
  private int mDataType = -1;
  // list of child controls
  private List<Integer> mChildIds = arrayList();

}

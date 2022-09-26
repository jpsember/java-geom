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

  public static final int DT_STRING = 0, DT_INT = 1, DT_DOUBLE = 2, DT_BOOL = 3;

  public Gadget(int id, int dataType) {
    mId = id;
    todo("can we get rid of the gadget datatype?  Mostly unused");
  }

  public final int getId() {
    return mId;
  }

  public final void stateChanged(ChangeEvent changeEvent) {
    TestBed.singleton().processAction(new TBAction(TBAction.CTRLVALUE, getId()));
  }

  // ------------------------------------------------------
  // PropertyChangeListener interface
  // ------------------------------------------------------

  public final void propertyChange(PropertyChangeEvent e) {
    TestBed.singleton().processAction(new TBAction(TBAction.CTRLVALUE, getId()));
  }

  public final List<Integer> children() {
    return mChildIds;
  }

  /**
   * Create an action for the gadget
   */
  public static Action createAction(int id, String label, String toolTip, KeyStroke accel) {
    Action action = new GadgetAction(id, label, toolTip, accel);
    return action;
  }

  public abstract Object readValue();

  public abstract void writeValue(Object v);

  /**
   * Get Swing component associated with gadget
   */
  public Component getComponent() {
    return mComponent;
  }

  public void setComponent(Component c) {
    mComponent = c;
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
      putValue(GADGETID, id);
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

  private final int mId;
  private Component mComponent;
  // list of child controls
  private List<Integer> mChildIds = arrayList();

}

package testbed;

import static js.base.Tools.*;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import js.guiapp.UserEvent;

import java.beans.*;
import java.util.List;
import static testbed.TestBedTools.*;

/**
 * Base class for control gadgets
 */
abstract class Gadget implements Globals, ChangeListener, PropertyChangeListener {

  public final Gadget setId(int id) {
    todo("the Gadget and action handlers should not be testbed, but instead geom");
    checkState(mId == 0, "already has an id");
    mId = id;
    return this;
  }

  public final int getId() {
    if (mId == 0)
      throw badState("gadget has no id");
    return mId;
  }

  public final void stateChanged(ChangeEvent changeEvent) {
    testBed().processAction(UserEvent.widgetEvent(getId()));
  }

  // ------------------------------------------------------
  // PropertyChangeListener interface
  // ------------------------------------------------------

  public final void propertyChange(PropertyChangeEvent e) {
    testBed().processAction(UserEvent.widgetEvent(getId()));
  }

  public final List<Integer> children() {
    if (mChildIds == null)
      mChildIds = arrayList();
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
   * Get GridBagConstraints fill parameter for this gadget. Default returns
   * HORIZONTAL
   */
  public int gcFill() {
    return GridBagConstraints.HORIZONTAL;
  }

  /**
   * Determine if this gadget has a value that needs to be serialized. The
   * default implementation returns true if gadget has a value defined
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
      testBed().processAction(UserEvent.widgetEvent(id()));
    }

  }

  private int mId;
  private Component mComponent;
  // list of child controls
  private List<Integer> mChildIds;

}

package testbed;

import static js.base.Tools.*;

import java.awt.Component;

import java.util.List;

/**
 * Base class for control gadgets
 */
abstract class Gadget {

  public final Gadget setId(int id) {
    checkState(mId == 0, "already has an id");
    mId = id;
    return this;
  }

  public final int getId() {
    if (mId == 0)
      throw badState("gadget has no id");
    return mId;
  }

  public final List<Integer> children() {
    if (mChildIds == null)
      mChildIds = arrayList();
    return mChildIds;
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
   * Determine if this gadget has a value that needs to be serialized. The
   * default implementation returns true if gadget has a value defined
   */
  public boolean serialized() {
    Object v = readValue();
    return v != null;
  }

  private int mId;
  private Component mComponent;
  // list of child controls
  private List<Integer> mChildIds;

}

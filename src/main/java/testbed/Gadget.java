package testbed;

import static js.base.Tools.*;

import java.awt.Component;

import java.util.List;

/**
 * Abstract class representing a user interface element
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
  public final Component getComponent() {
    return mComponent;
  }

  public final void setComponent(Component c) {
    mComponent = c;
  }

  private int mId;
  private Component mComponent;
  // list of child controls
  private List<Integer> mChildIds;

}

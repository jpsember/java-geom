package testbed;

import static js.base.Tools.*;

@Deprecated
class HiddenGadget extends Gadget {

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
package testbed;

import javax.swing.*;

@Deprecated
class CtCheckBox extends Gadget {

  /**
   * Constructor
   * 
   * @param gl
   *          GadgetList
   * @param id
   *          int
   * @param label
   *          String
   * @param value
   *          boolean
   * @param inMenu
   *          boolean
   * @param toolTip
   *          String
   * @param accel
   *          KeyStroke
   */
  public CtCheckBox(int id, String label, boolean value, boolean inMenu, String toolTip, KeyStroke accel) {
    setId(id);
    Action a = new GadgetAction(id, label, toolTip, accel);

    if (inMenu) {
      setComponent(new JCheckBoxMenuItem());
    } else {
      setComponent(new JCheckBox());
    }
    button().setSelected(value);
    button().setAction(a);
  }

  public void writeValue(Object v) {
    button().setSelected(((Boolean) v).booleanValue());
  }

  public Boolean readValue() {
    return button().isSelected();
  }

  private AbstractButton button() {
    return (AbstractButton) getComponent();
  }

}

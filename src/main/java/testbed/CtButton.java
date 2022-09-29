package testbed;

import javax.swing.JButton;
import javax.swing.Action;

/**
 * Button gadget
 */
@Deprecated
class CtButton extends Gadget {

  public boolean serialized() {
    return false;
  }

  public CtButton(Action a) {
    b = new JButton(a);
    setComponent(b);
  }

  public void writeValue(Object v) {
    b.setText((String) v);
  }

  public Object readValue() {
    return b.getText();
  }

  private JButton b;
}

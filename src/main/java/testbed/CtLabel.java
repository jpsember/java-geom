package testbed;

import base.*;
import javax.swing.*;
import java.awt.*;
import static js.base.Tools.*;

/**
 * Label gadget
 */
class CtLabel extends Gadget {

  public boolean serialized() {
    return false;
  }

  /**
   * Constructor
   * 
   * @param colWidth
   *          0 if not multiline, >0 for max # characters per column for
   *          multiline
   * @param label
   *          String
   */
  public CtLabel(int id, int colWidth, String label) {
    super(id, DT_STRING); // datatype isn't used though
    JComponent c = null;

    if (colWidth == 0) {
      c = new JLabel(label, SwingConstants.CENTER);
    } else {
      DArray a = new DArray();
      TextScanner.splitString(label, colWidth, a);
      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

      for (int i = 0; i < a.size(); i++) {
        JLabel lbl = new JLabel(a.getString(i));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lbl);
      }
      c = p;
    }
    setComponent(c);
  }

  @Override
  public Object readValue() {
    throw notFinished();
  }

  @Override
  public void writeValue(Object v) {
    throw notFinished();
  }

}

package testbed;

import geom.GeomTools;

import javax.swing.*;
import static js.base.Tools.*;

import java.awt.Component;
import java.util.List;

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
  public CtLabel(int colWidth, String label) {
    JComponent c = null;

    if (colWidth == 0) {
      c = new JLabel(label, SwingConstants.CENTER);
    } else {
      List<String> a = arrayList();
      GeomTools.splitString(label, colWidth, a);
      JPanel p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

      for (int i = 0; i < a.size(); i++) {
        JLabel lbl = new JLabel(a.get(i));
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

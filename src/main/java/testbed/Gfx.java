package testbed;

import static js.base.Tools.*;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

class Gfx {
  // border types
  public static final int
  // small padding, no line
  BD_PAD = 0
  // line + small padding
      , BD_LINE_PAD = 1;

  /**
   * Add a border of a particular style to a component
   * 
   * @param c
   *          : JComponent to add border to
   * @param borderType
   *          : type of border, BD_x
   * @param label
   *          : if not null, string to display at top of border
   * @param labelAlignment
   *          : if label defined, this determines its horizontal alignment
   * 
   */
  public static void addBorder(JComponent c, int borderType, String label, int labelAlignment) {
    final Border[] b = {
        // #0: empty, spacing of 2
        BorderFactory.createEmptyBorder(2, 2, 2, 2),
        // #1: lowered etched, interior spacing of 2
        BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)),
        // BorderFactory.createLineBorder(Color.red,3),
    };

    checkArgument(!(label != null && borderType < 0));

    if (label != null) {
      TitledBorder t = BorderFactory.createTitledBorder(b[borderType], label);

      int tbAlign = 0;
      switch (labelAlignment) {
      default:
        tbAlign = TitledBorder.LEFT;
        break;
      case SwingConstants.CENTER:
        tbAlign = TitledBorder.CENTER;
        break;
      case SwingConstants.RIGHT:
        tbAlign = TitledBorder.RIGHT;
        break;
      }
      t.setTitleJustification(tbAlign);
      c.setBorder(t);
    } else {
      if (borderType >= 0) {
        c.setBorder(b[borderType]);
      }
    }
  }

  public static void fillRect(Graphics g, Rectangle r) {
    g.fillRect(r.x, r.y, r.width, r.height);
  }

  public static void showBounds(Graphics g) {
    g.setColor(Color.red);
    drawRect(g, g.getClipBounds());
  }

  public static void drawRect(Graphics g, Rectangle r) {
    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
  }

}

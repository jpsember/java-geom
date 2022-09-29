package testbed;

import java.awt.*;
import javax.swing.*;

@Deprecated
class DEPRECATEDGC extends GridBagConstraints {
  private static DEPRECATEDGC gc = new DEPRECATEDGC();

  public static DEPRECATEDGC gc(int gridX, int gridY, int gridWidth, int gridHeight,
      int weightX, int weightY) {
    gc.gridx = gridX;
    gc.gridy = gridY;
    gc.gridwidth = gridWidth;
    gc.gridheight = gridHeight;
    gc.weightx = weightX;
    gc.weighty = weightY;
    gc.anchor = GridBagConstraints.CENTER;
    gc.fill = GridBagConstraints.BOTH;
    return gc;
  }

  /**
   * Get a 'glue' component that when added as the last row of
   * a GridBagLayout, compresses the other items upward
   * 
   * @return component
   */
  private static Component glue() {
    return new MyGluePanel();
  }

  private static class MyGluePanel extends JPanel {
    public MyGluePanel() {
      this.setPreferredSize(new Dimension(1, 1));
      this.setMinimumSize(new Dimension(1, 1));
    }
  }

  /**
   * Add a 'glue' component to the last row or column of a GridBagLayout
   * to compress other items as much as possible
   * @param Container container to add glue to; must use a GridBagLayout
   * @param gridX 
   * @param gridY grid positions for start of glue
   */

  public static void addGlue(Container component, int gridX, int gridY) {
    DEPRECATEDGC gc = gc(gridX, gridY, DEPRECATEDGC.REMAINDER, DEPRECATEDGC.REMAINDER, 1, 1);
    component.add(glue(), gc);
  }
}

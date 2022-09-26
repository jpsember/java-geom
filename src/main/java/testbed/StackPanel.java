package testbed;

import javax.swing.*;

import static js.base.Tools.*;

import java.awt.Component;
import java.awt.GridBagLayout;

/**
 * Panel that contains a stack of components.
 */
class StackPanel {

  /**
   * Constructor
   */
  public StackPanel(String optionalTitle) {
    mComponent = new JPanel();
    if (!nullOrEmpty(optionalTitle))
      Gfx.addBorder(mComponent, Gfx.BD_LINE_PAD, optionalTitle, SwingConstants.LEFT);

    // create a gridbaglayout to hold the different columns
    mComponent.setLayout(new GridBagLayout());

    // the first row is divided up into arbitrary number of columns,
    // and the vertical weight is zero.

    // the second row spans the panel, and has all the vertical weight; it
    // serves to push the panel contents upward as much as possible
    GC.addGlue(mComponent, 0, 1);
  }

  /**
   * Get the swing component this panel represents
   */
  public Component component() {
    return mComponent;
  }

  /**
   * Start a new column to the right of the previous one
   */
  public void startNewColumn() {
    mCurrentColumn = null;
  }

  /**
   * Add a component to the panel
   */
  public void addItem(Component c) {
    // if no column exists, add one
    if (mCurrentColumn == null)
      createColumn();

    GC gc = GC.gc(0, mCurrentColumnRow, 1, 1, 0, 0);
    mCurrentColumn.add(c, gc);
    mCurrentColumnRow++;
  }

  private void createColumn() {
    mCurrentColumn = new JPanel();
    mCurrentColumn.setLayout(new GridBagLayout());

    {
      GC gc = GC.gc(mCurrentColumnNumber, 0, 1, 1, 1, 0);
      mComponent.add(mCurrentColumn, gc);
    }
    mCurrentColumnNumber++;
    mCurrentColumnRow = 0;

    GC.addGlue(mCurrentColumn, 0, 100);
  }

  // outermost component of this panel
  private final JPanel mComponent;
  // component of current column
  private JPanel mCurrentColumn;
  // current column number, 0..n-1
  private int mCurrentColumnNumber;
  // current row number
  private int mCurrentColumnRow;
}

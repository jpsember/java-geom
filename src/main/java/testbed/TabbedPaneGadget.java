package testbed;

import java.awt.Component;
import java.util.*;
import javax.swing.*;

import static js.base.Tools.*;

@Deprecated
class TabbedPaneGadget extends Gadget {

  /**
   * Read value. Returns the current pane's identifier.
   * 
   * @return Object integer
   */
  public Object readValue() {

    int val = -1;

    JTabbedPane tp = getSet();
    int si = tp.getSelectedIndex();

    // get id of this pane
    val = ids.get(si);
    return val;
  }

  /**
   * Write value: set selected tabbed pane
   * 
   * @param v
   *          integer; if >= ID_BASE, assumes it's an id of an item; otherwise,
   *          assumes it's an index
   */
  public void writeValue(Object v) {
    JTabbedPane tp = getSet();

    int val = ((Number) v).intValue();
    Integer val2 = null;

    if (val >= Globals.TAB_ID_START) {
      val2 = idToIndexMap.get(val);
      if (val2 == null)
        alert("no value", val2, "for", this.getId());
    } else {
      val2 = val;
    }
    if (val2 != null) {
      if (val2.intValue() < 0 || val2.intValue() >= tp.getTabCount()) {
        alert("no such tab:", val2, "for", getId());
      } else
        tp.setSelectedIndex(val2.intValue());
    }
  }

  /**
   * Constructor
   * 
   * @param vertical
   *          true if vertical stack
   * @param tabbed
   *          true if a tab panel
   * @param destContainer
   *          if not null, existing component that will contain this stack's
   *          elements; it is assumed to use GridBagLayout
   */
  public TabbedPaneGadget(boolean vertical) {
    JTabbedPane tabPane = new JTabbedPane(vertical ? JTabbedPane.TOP : JTabbedPane.LEFT);
    tabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
    setComponent(tabPane);
  }

  /**
   * Add a tab to the panel
   * 
   * @param title
   *          title of tab
   * @param pnlId
   *          identifier to assign this tab; if < ID_BASE, uses the tab index as
   *          its id
   * @param component
   */
  public void addTab(String title, int pnlId, Component component) {
    // Determine id of pane.  If it is < 1000, assume it's just an index
    if (pnlId < Globals.TAB_ID_START) {
      pnlId = ids.size();
    }

    titles.add(title);
    idToIndexMap.put(pnlId, ids.size());
    ids.add(pnlId);

    getSet().add(title, component);
  }

  private JTabbedPane getSet() {
    return (JTabbedPane) getComponent();
  }

  // ids of panels
  // maps user ids => pane index
  private Map<Integer, Integer> idToIndexMap = hashMap();

  // titles of items
  private List<String> titles = arrayList();
  // ids of items
  private List<Integer> ids = arrayList();
}

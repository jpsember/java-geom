package testbed;

import static testbed.TestBedTools.*;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import js.guiapp.UserEvent;

class GadgetAction extends MyAction {

  private static final String GADGETID = "GADGETID";

  public GadgetAction(int id, String name, String toolTip, KeyStroke accel) {
    super(name);
    putValue(GADGETID, id);
    setAccelerator(accel);
    if (toolTip != null) {
      setTooltipText(toolTip, true);
    }
  }

  public int id() {
    return ((Integer) getValue(GADGETID)).intValue();
  }

  public String toString() {
    return "GadgetAction id=" + id() + "\n" + super.toString();
  }

  public void actionPerformed(ActionEvent e) {
    testBed().processAction(UserEvent.widgetEvent(id()));
  }

}
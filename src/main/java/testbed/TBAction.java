package testbed;

import static geom.GeomTools.*;
import static js.base.Tools.*;

public class TBAction {

  public static final int NONE = 0,
      // CtButton has been pressed,
      // or valued control has changed
      CTRLVALUE = 9, _UNUSED_ = 999;

  // code for action
  public int code;
  // id of control (if NEWVALUE)
  public int ctrlId;

  public TBAction(int code, int controlId) {
    todo("there is probably another class this can be replaced by; js.guiapp.UserEvent?");
    this.code = code;
    this.ctrlId = controlId;
  }

  public void enable(boolean f) {
    gadg().get(ctrlId).getComponent().setEnabled(f);
  }

  private static final String[] codes = { "NONE", "CTRLVALUE", };

  public String toString() {

    StringBuilder sb = new StringBuilder();
    sb.append("TBAction");

    sb.append("code ");
    sb.append(code);
    if (code >= 0 && code < codes.length) {
      sb.append(' ');
      sb.append(codes[code]);
    }

    sb.append(' ');
    switch (code) {
    case CTRLVALUE:
      sb.append(ctrlId);
      break;
    }

    return sb.toString();
  }
}

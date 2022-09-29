package testbed;

import js.geometry.IRect;
import js.guiapp.GUIApp;
import js.json.JSList;
import js.widget.Widget;

public class AppFrameGadget extends Widget {

  @Override
  public JSList readValue() {
    IRect r = new IRect(GUIApp.sharedInstance().appFrame().frame().getBounds());
    return r.toJson();
  }

  @Override
  public void writeValue(Object v) {
    IRect r = IRect.DEFAULT_INSTANCE.parse(v);
    GUIApp.sharedInstance().appFrame().setBounds(r);
  }

}

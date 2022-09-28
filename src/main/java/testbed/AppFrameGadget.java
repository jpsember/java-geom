package testbed;

import geom.GeomApp;
import js.geometry.IRect;
import js.json.JSList;
import js.widget.Widget;

public class AppFrameGadget extends Widget {

  @Override
  public JSList readValue() {
    IRect r = new IRect(GeomApp.singleton().appFrame().frame().getBounds());
    return r.toJson();
  }

  @Override
  public void writeValue(Object v) {
    IRect r = IRect.DEFAULT_INSTANCE.parse(v);
    GeomApp.singleton().appFrame().setBounds(r);
  }

}

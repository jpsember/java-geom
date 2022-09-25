package testbed;

import static js.base.Tools.*;
import geom.GeomApp;
import js.geometry.IRect;
import js.json.JSList;

public class AppFrameGadget extends Gadget {

  public AppFrameGadget(int id) {
    super(id, DT_IRECT);
    todo("can we get rid of the gadget datatype?  Mostly unused");
  }

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

package geom.oper;

import geom.ScriptWrapper;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.guiapp.UserOperation;

import static js.base.Tools.*;
import static geom.GeomTools.*;

public class AutoZoomOper extends UserOperation {

  @Override
  public boolean shouldBeEnabled() {
    ScriptWrapper script = scriptManager().currentScript();
    if (script.isNone())
      return false;

    // Determine padded bounds of objects
    var objBounds = boundsOfObjects(script.script().items(), 20);
    if (objBounds == null) return false;

    var ep = geomApp().getEditorPanel();
    var editorBounds = new IRect(ep.getBounds());

    pi("editor bounds:", editorBounds);
    pi("obj bounds:", objBounds);


    IPoint pageSize = geomApp().pageSize();
    pi("page size:", pageSize);

    mTargetZoom = Math.min(editorBounds.width / (float) objBounds.width, editorBounds.height / (float) objBounds.height);
    mTargetPan = IPoint.difference(objBounds.midPoint(), pageSize.scaledBy(0.5f));

    var panCurrent = geomApp().panOffset();
    var zoomCurrent = geomApp().zoomFactor();

    pr("current zoom:", zoomCurrent, "pan:", panCurrent);
    pr("desired zoom:", mTargetZoom, "pan:", mTargetPan);

    return !(panCurrent.equals(mTargetPan) && panCurrent.equals(mTargetZoom));
  }

  @Override
  public void start() {
    geomApp().setZoomFactor(mTargetZoom);
    geomApp().setPanOffset(mTargetPan);
  }

  private float mTargetZoom;
  private IPoint mTargetPan;
}

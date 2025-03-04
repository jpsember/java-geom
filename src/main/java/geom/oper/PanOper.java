package geom.oper;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.guiapp.UserEvent;
import js.guiapp.UserEventManager;
import js.guiapp.UserOperation;
import static js.base.Tools.*;
import static geom.GeomTools.*;

public final class PanOper extends UserOperation implements UserEvent.Listener {

  public static UserOperation build(UserEvent initialEvent) {
    return new PanOper(initialEvent);
  }

  public static UserOperation buildReset() {
    return new PanOper(null);
  }

  private PanOper(UserEvent initialEvent) {
    if (initialEvent != null) {
      mInitialDownEvent = initialEvent;
      mOriginalPan = geomApp().panOffset();
    }
  }

  @Override
  public boolean shouldBeEnabled() {
    if (!isReset())
      return true;
    var currentPan = geomApp().panOffset();
    return !currentPan.equals(IPoint.ZERO);
  }

  @Override
  public void start() {
    if (isReset()) {
      geomApp().setPanOffset(IPoint.ZERO);
      todo("!there should be an easier way to do this");
      UserEventManager.sharedInstance().clearOperation();
    }
  }

  @Override
  public void processUserEvent(UserEvent event) {

    switch (event.getCode()) {

    case UserEvent.CODE_DRAG:

    {
      log("DRAG");
      if (mInitialDownEvent == null)
        mInitialDownEvent = event;
      // We calculate the pan based on view coordinates, since they are unaffected
      // by the pan value (which will be constantly changing)

      var zoom = geomApp().zoomFactor();

      // If the user drags the mouse to the right, we want the pan to move to the *left*
      var viewOffset = IPoint.difference(event.getViewLocation(), mInitialDownEvent.getViewLocation())
          .scaledBy(-1);
      var worldOffset = viewOffset.scaledBy(1 / zoom);
      var pan = IPoint.sum(mOriginalPan, worldOffset);

      boolean clamp = false && alert("!clamping pan values");
      if (clamp) {
        final int PAN_MAX = 2000;
        pan.clampTo(new IRect(-PAN_MAX, -PAN_MAX, PAN_MAX, PAN_MAX));
      }

      geomApp().setPanOffset(pan);
    }
      break;

    case UserEvent.CODE_UP:
      event.clearOperation();
      break;
    }
  }

  private boolean isReset() {
    return mInitialDownEvent == null;
  }

  private UserEvent mInitialDownEvent;
  private IPoint mOriginalPan;

}

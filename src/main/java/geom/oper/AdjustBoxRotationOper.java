/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package geom.oper;

import static js.base.Tools.*;

import geom.EditorElement;
import geom.StateTools;
import geom.elem.EditableRectElement;
import geom.gen.Command;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.geometry.MyMath;
import js.graphics.ScriptUtil;
import js.graphics.gen.ElementProperties;
import js.guiapp.UserEvent;
import js.guiapp.UserOperation;
import static geom.GeomTools.*;

public class AdjustBoxRotationOper extends UserOperation implements UserEvent.Listener {

  public AdjustBoxRotationOper(UserEvent event, int slot) {
    loadTools();
    mSlot = slot;
    mMouseDownLoc = event.getWorldLocation();
  }

  @Override
  public void start() {
    log("start");

    mCommand = geomApp().buildCommand("Adjust Box Rotation");
    EditorElement elem = mCommand.newState().elements().get(mSlot);
    mOriginalElem = (EditableRectElement) elem;
    float angle = MyMath.polarAngle(FPoint.difference(mMouseDownLoc.toFPoint(), origin()));
    mAngleOffset = toRadians(ScriptUtil.rotationDegreesOrZero(mOriginalElem)) - toRadians(toDegrees(angle));
  }

  @Override
  public void processUserEvent(UserEvent event) {
    if (event.withLogging())
      log("processUserEvent", event);

    switch (event.getCode()) {

    case UserEvent.CODE_DRAG: {
      FPoint mouseLoc = event.getWorldLocation().toFPoint();
      float newAngle = MyMath.polarAngle(FPoint.difference(mouseLoc, origin()));
      ElementProperties.Builder properties = mOriginalElem.properties().toBuilder();
      properties.rotation(MyMath.clamp(toDegrees(newAngle + mAngleOffset), -85, 85));
      StateTools.replaceAndSelectItem(mCommand, mSlot, mOriginalElem.withProperties(properties));
      geomApp().perform(mCommand);
    }
      break;

    case UserEvent.CODE_UP:
      geomApp().perform(mCommand);
      event.clearOperation();
      break;
    }
  }

  @Override
  public IntArray displayedSlotsFilter() {
    return IntArray.with(mSlot);
  }

  private int toDegrees(float radians) {
    return Math.round(MyMath.normalizeAngle(radians) / MyMath.M_DEG);
  }

  private float toRadians(float degrees) {
    return MyMath.normalizeAngle(degrees * MyMath.M_DEG);
  }

  private FPoint origin() {
    return mOriginalElem.bounds().midPoint().toFPoint();
  }

  private int mSlot;
  private IPoint mMouseDownLoc;
  private Command.Builder mCommand;
  private EditableRectElement mOriginalElem;
  private float mAngleOffset;

}

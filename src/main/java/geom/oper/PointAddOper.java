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

import java.awt.Cursor;

import geom.StateTools;
import geom.elem.EditablePointElement;
import geom.gen.Command;
import js.geometry.IPoint;
import js.guiapp.UserEvent;
import js.guiapp.UserOperation;

import static geom.GeomTools.*;

public class PointAddOper extends UserOperation implements UserEvent.Listener {

  @Override
  public void start() {
    geomApp().setMouseCursor(Cursor.HAND_CURSOR);
  }

  @Override
  public void processUserEvent(UserEvent event) {
    loadTools();
    if (event.withLogging())
      log("processUserEvent", event);

    switch (event.getCode()) {

    case UserEvent.CODE_DOWN: {
      IPoint pos = event.getWorldLocation();
      mOriginalElement = EditablePointElement.DEFAULT_INSTANCE.withLocation(pos);
      mCommand = geomApp().buildCommand("Add Point");
      mSlot = StateTools.addNewElement(mCommand, mOriginalElement);
      geomApp().perform(mCommand);
      geomApp().setMouseCursor(Cursor.CROSSHAIR_CURSOR);
    }
      break;

    case UserEvent.CODE_DRAG: {
      EditablePointElement p = mOriginalElement.withLocation(event.getWorldLocation());
      StateTools.replaceAndSelectItem(mCommand, mSlot, p);
      geomApp().perform(mCommand);
    }
      break;

    case UserEvent.CODE_UP:
      geomApp().perform(mCommand);
      event.clearOperation();
      break;
    }

  }

  private Command.Builder mCommand;
  private int mSlot;
  private EditablePointElement mOriginalElement;

}

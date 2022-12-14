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

import static geom.StateTools.*;

import java.awt.Cursor;
import java.util.List;

import geom.EditorElement;
import geom.gen.Command;
import geom.gen.ScriptEditState;
import js.data.IntArray;
import js.geometry.IPoint;
import js.geometry.Matrix;
import js.guiapp.UserEvent;
import js.guiapp.UserOperation;
import static geom.GeomTools.*;

public class MoveElementsOper extends UserOperation implements UserEvent.Listener {

  public MoveElementsOper(UserEvent initialDownEvent) {
    mDownEventWorldLocation = initialDownEvent.getWorldLocation();
    mCommand = geomApp().buildCommand("");
    mInitialState = mCommand.newState();
    setDescriptionForSelected(mCommand, "Move", mInitialState.selectedElements().length);
    geomApp().setMouseCursor(Cursor.HAND_CURSOR);
  }

  @Override
  public void processUserEvent(UserEvent event) {
    switch (event.getCode()) {
    case UserEvent.CODE_DRAG:
      updateMove(event);
      break;

    case UserEvent.CODE_UP:
      geomApp().perform(mCommand);
      event.clearOperation();
      break;
    }
  }

  @Override
  public IntArray displayedSlotsFilter() {
    return IntArray.with(mCommand.newState().selectedElements());
  }

  private void updateMove(UserEvent event) {
    List<EditorElement> elem = arrayList();
    elem.addAll(mInitialState.elements());
    IPoint translate = IPoint.difference(event.getWorldLocation(), mDownEventWorldLocation);
    Matrix matrix = Matrix.getTranslate(translate);
    todo("!finish implementing better 'dup' logic");
    for (int slot : mInitialState.selectedElements()) {
      EditorElement originalObj = elem.get(slot);
      EditorElement updatedObj = originalObj.applyTransform(matrix);
      elem.set(slot, updatedObj);
    }
    mCommand.newState(mInitialState.toBuilder().elements(elem));
    geomApp().perform(mCommand);
  }

  private IPoint mDownEventWorldLocation;
  private Command.Builder mCommand;
  private ScriptEditState mInitialState;
}

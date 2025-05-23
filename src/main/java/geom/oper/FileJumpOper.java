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
 **/
package geom.oper;

import static js.base.Tools.*;

import js.guiapp.UserOperation;

import geom.Project;

import static geom.GeomTools.*;

public final class FileJumpOper extends UserOperation {

  public FileJumpOper(int direction) {
    loadTools();
    mStepDirection = direction;
  }

  @Override
  public boolean shouldBeEnabled() {
    mNextFileIndex = null;
    var project = scriptManager();
    if (project.definedAndNonEmpty()) {
      mNextFileIndex = (mStepDirection < 0) ? 0 : project.scriptCount() - 1;
    }
    return mNextFileIndex != null;
  }

  @Override
  public void start() {
    if (mNextFileIndex == null)
      return;
    scriptManager().switchToScript(mNextFileIndex);
  }

  private final int mStepDirection;
  private Integer mNextFileIndex;
}

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
package geom;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.json.JSList;
import js.widget.Widget;

import java.awt.*;

import static geom.GeomTools.*;

class AppFrameWidget extends Widget {

  @Override
  public JSList readValue() {
    IRect r = new IRect(geomApp().appFrame().frame().getBounds());
    return r.toJson();
  }

  @Override
  public void writeValue(Object v) {
    IRect r = IRect.DEFAULT_INSTANCE.parse(v);
    if (DEV_FRAME) {
      // Trim the height of the frame to make sure the IDE console is visible
      r = new IRect(r.x, r.y, r.width, Math.min(800, r.height));
    }
    geomApp().appFrame().setBounds(r);
  }

}

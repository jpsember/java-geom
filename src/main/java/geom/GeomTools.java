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
package geom;

import js.app.App;
import js.widget.WidgetManager;
import testbed.ColorWrapper;
import testbed.Colors;
import testbed.StrokeWrapper;

import static js.base.Tools.*;

import java.awt.Color;

public final class GeomTools {

  public static final boolean DEBUG_POLYEDIT = false && alert("!DEBUG_POLYEDIT is true");

  public static GeomApp geomApp() {
    return App.sharedInstance();
  }

  public static boolean devMode() {
    return geomApp().guiAppConfig().devMode();
  }

  public static WidgetManager widgets() {
    return geomApp().widgetManager();
  }

  public static ScriptManager scriptManager() {
    return ScriptManager.singleton();
  }

  /**
   * Wrap an AlgRenderable within one that (temporarily) sets the stroke
   */
  public static Object stroke(int stroke, Object object) {
    return new StrokeWrapper(stroke, object);
  }

  /**
   * Wrap an AlgRenderable within one that (temporarily) sets the color
   */
  public static Object color(Color color, Object object) {
    return new ColorWrapper(color, object);
  }

  public static Object color(int colorId, Object object) {
    return new ColorWrapper(Colors.get(colorId), object);
  }

  public static Object color(int colorId, double shade, Object object) {
    return new ColorWrapper(Colors.get(colorId, shade ), object);
  }

  public static Object alpha(double alpha, Object object) {
    return ColorWrapper.alphaWrapper(alpha, object);
  }
}

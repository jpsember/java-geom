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

import js.app.App;
import js.file.Files;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.graphics.ScriptElement;
import js.gui.gen.GuiAppConfig;
import js.widget.WidgetManager;
import testbed.AlgorithmStepper;
import testbed.ColorWrapper;
import testbed.Colors;
import testbed.FontWrapper;
import testbed.RenderableSegment;
import testbed.RenderableText;
import testbed.StrokeWrapper;

import javax.swing.filechooser.FileFilter;

import static js.base.Tools.*;

import java.awt.Color;
import java.io.File;
import java.util.Collection;

public final class GeomTools {

  public static final boolean DEV_FRAME = true && alert("!DEV_FRAME is in effect");
  public static final boolean DEBUG_INFERZOOM = true && alert("!DEBUG_INFERZOOM is true");

  public static void pi(Object... messages) {
    if (DEBUG_INFERZOOM) pr(insertStringToFront("INFERZOOM===>", messages));
  }

  public static final boolean DEBUG_POLYEDIT = false && alert("!DEBUG_POLYEDIT is true");

  public static final boolean DEBUG_FILEBASED = false && alert("!DEBUG_FILEBASED is true");

  public static final String SCRIPT_SET_EXTENSION = "script_set";


  /**
   * Widget ids
   */
  public static final String //
      EDITOR_ZOOM = "ed_zoom", //
      EDITOR_PAN_X = "ed_pan_x", //
      EDITOR_PAN_Y = "ed_pan_y", //
      CURRENT_SCRIPT_FILE = ".script_file",
      SCRIPT_NAME = ".script_name", // nor this?
      MESSAGE = ".message", //
      APP_FRAME = "app_frame"; //


  public static void df(Object... objects) {
    if (!DEBUG_FILEBASED) return;
    objects = insertStringToFront(">>DBFILEBASED<<:", objects);
    pr(objects);
  }

  public static boolean isProjectBased() {
    return appConfig().projectBased();
  }

  public static boolean isFileBased() {
    return !isProjectBased();
  }

  public static GuiAppConfig.Builder appConfig() {
    return geomApp().guiAppConfig();
  }

  public static void assertProjectBased() {
    if (!isProjectBased())
      badState("not supported for a file-based app");
  }

  public static void assertFileBased() {
    if (isProjectBased())
      badState("not supported for a project-based app");
  }

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

  public static Object font(int fontSize, Object object) {
    return new FontWrapper(fontSize, object);
  }

  public static Object color(int colorId, double shade, Object object) {
    return new ColorWrapper(Colors.get(colorId, shade), object);
  }

  public static Object alpha(double alpha, Object object) {
    return ColorWrapper.alphaWrapper(alpha, object);
  }

  public static AlgRenderable segment(FPoint a0, FPoint a1) {
    return RenderableSegment.with(a0, a1);
  }

  public static RenderableText text(String text) {
    return RenderableText.with(text);
  }

  public static Object textAt(FPoint loc, String text) {
    return RenderableText.with(text).at(loc);
  }

  public static Object textAt(IPoint loc, String text) {
    return RenderableText.with(text).at(loc.toFPoint());
  }

  public static AlgRenderable render(Collection objects) {
    return RenderableCollection.with(objects);
  }

  private static class RenderableCollection implements AlgRenderable {

    public static RenderableCollection with(Collection objects) {
      var c = new RenderableCollection();
      c.mObjects = objects;
      return c;
    }

    @Override
    public void render(Object item) {
      var s = AlgorithmStepper.sharedInstance();
      for (var x : mObjects) {
        var y = s.findRendererForObject(x);
        if (y == null)
          continue;
        y.render(x);
      }
    }

    private Collection<Object> mObjects;
  }

  public static FileFilter filterForExtension(String ext, String description) {
    return new FileFilter() {

      @Override
      public boolean accept(File f) {
        return Files.getExtension(f).equals(ext);
      }

      @Override
      public String getDescription() {
        return description;
      }
    };
  }

  public static IRect boundsOfObjects(Collection<? extends ScriptElement> obj, int padding) {
    IRect allBounds = null;
    for (var item : obj) {
      var b = item.bounds();
      if (allBounds == null) allBounds = b;
      allBounds = allBounds.including(b);
    }
    if (allBounds != null)
      allBounds = allBounds.withInset(-padding);
    return allBounds;
  }
}

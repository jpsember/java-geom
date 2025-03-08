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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.IPoint;
import js.graphics.ImgEffects;
import js.graphics.ImgUtil;
import js.graphics.MonoImageUtil;
import js.json.JSMap;
import js.graphics.ScriptUtil;
import js.graphics.gen.ImageStats;
import js.graphics.gen.MonoImage;
import js.graphics.gen.Script;

import static geom.GeomTools.*;
import static js.graphics.ScriptUtil.*;

/**
 * An enhanced wrapper for a Script object, that supports additional features
 * for ScrEdit, e.g. working with images
 */
public final class ScriptWrapper extends BaseObject {

  public static final ScriptWrapper DEFAULT_INSTANCE = new ScriptWrapper();

  private ScriptWrapper() {
    mScriptFile = Files.DEFAULT;
  }

  public ScriptWrapper(File scriptPath) {
    mScriptFile = scriptPath;
  }

  public boolean isNone() {
    return this == DEFAULT_INSTANCE;
  }

  public boolean defined() {
    return !isNone();
  }

  public boolean isAnonymous() {
    return defined() && Files.empty(mScriptFile);
  }

  @Deprecated
  public static ScriptWrapper buildUntitled() {
    ScriptWrapper w = new ScriptWrapper(Files.DEFAULT);
    return w;
  }

  /**
   * Read the Script from the file, if it hasn't already been read
   */
  public Script script() {
    if (isNone())
      return Script.DEFAULT_INSTANCE;
    if (mScriptData == null) {
      if (isAnonymous())
        mScriptData = Script.DEFAULT_INSTANCE;
      else {
        mScriptData = Files.parseAbstractDataOpt(Script.DEFAULT_INSTANCE, mScriptFile);
      }
    }
    return mScriptData;
  }

  public void setScript(Script data) {
    if (mScriptData != null && mScriptData.widgets().nonEmpty() && data.widgets().isEmpty())
      badState("attempt to set data with no widget map");
    assertNotNone();
    mScriptData = data.build();
  }

  public boolean hasImage() {
    if (isNone())
      return false;
    if (!GeomTools.geomApp().hasImageSupport())
      return false;
    if (mImageFile == null) {
      if (Files.nonEmpty(file())) {
        List<File> imageCandidates = ScriptUtil.findImagePathsForScript(file());
        if (imageCandidates.size() > 1)
          throw badState("Multiple image candidates for script:", file(), INDENT, imageCandidates);
        mImageFile = Files.DEFAULT;
        if (!imageCandidates.isEmpty())
          mImageFile = first(imageCandidates);
      }
    }
    return Files.nonEmpty(mImageFile);
  }

  /**
   * Get script's image; read if necessary
   */
  public BufferedImage image() {
    BufferedImage image = sImageCache.get(imageFile());
    if (image == null)
      image = sImageCache.put(imageFile(), readImage(imageFile()));
    return image;
  }

  /**
   * Read a BufferedImage, with special treatment for .rax files
   */
  private static BufferedImage readImage(File file) {
    if (!Files.getExtension(file).equals(ImgUtil.EXT_RAX))
      return ImgUtil.read(file);

    MonoImage monoImage = ImgUtil.readRax(file);
    ImageStats s = MonoImageUtil.generateStats(monoImage);

    int range = s.range();
    // Don't attempt normalization if there's a strange distribution
    if (range > 500) {
      monoImage = MonoImageUtil.normalizedImageMagick(monoImage, s);
    }
    BufferedImage img = ImgUtil.to8BitRGBBufferedImage(monoImage.size(), monoImage.pixels());
    return ImgEffects.sharpen(img);
  }

  private static ObjectCache<File, BufferedImage> sImageCache = new ObjectCache<>(100);

  public void flush() {
    if (isNone())
      return;

    Script script = script();

    if (ScriptUtil.isUseful(script) || alert("ALWAYS setting useful")) {
      D20("flush script, old widget map:", INDENT, D20Map(script.widgets()));
      storeWidgetValuesWithinScript();
      script = script();

      D20("after updating widget map with new ui:", INDENT, D20Map(script.widgets()));

      String content = DataUtil.toString(script);
      if (alert("!writing pretty printed"))
        content = script.toJson().prettyPrint();

      if (Files.S.writeIfChanged(file(), content)) {
        if (verbose())
          log("flushed changes; new content:", INDENT, script);
      }
    } else {
      if (file().exists()) {
        if (verbose())
          log("deleting useless script:", INDENT, script);
        Files.S.deleteFile(file());
      }
    }
  }

  private void storeWidgetValuesWithinScript() {
    D20("storeWidgetValuesWithinScript");
    var widgetMap = widgets().readWidgetValues();
    D20("widget map:", D20Map(widgetMap));

    todo("don't persist certain values, ie with prefix?");
    var sb = script().toBuilder();
    sb.widgets(widgetMap);
    D20("set script to:", INDENT, D20Map(widgetMap));

    // We have to persist the changes to the cached version
    setScript(sb.build());
  }

  @Deprecated // This should be an instance method for script
  public static void updateUIWithScriptWidgets(Script script) {
    D20("updateUIWithScriptWidgets");
    var wm = script.widgets();
    widgets().setWidgetValues(wm);
    todo("!Do I need to refresh the view?");
  }

  private File imageFile() {
    checkState(hasImage(), "script has no image");
    return mImageFile;
  }

  public File file() {
    assertNotNone();
    return mScriptFile;
  }

  /**
   * Get size of the editor 'page'. If script has an image, return its size;
   * otherwise, some default value
   */
  public IPoint pageSize() {
    if (!hasImage()) {
      return new IPoint(1200, 900);
    }
    return ImgUtil.size(image());
  }

  public ScriptWrapper assertNotNone() {
    if (isNone())
      throw notSupported("not supported for ScriptWrapper = DEFAULT_INSTANCE");
    return this;
  }

  // ------------------------------------------------------------------
  // BaseObject interface
  // ------------------------------------------------------------------

  @Override
  protected String supplyName() {
    if (isNone())
      return "_NONE_";
    return Files.basename(file());
  }

  @Override
  public JSMap toJson() {
    if (isNone())
      return JSMap.DEFAULT_INSTANCE;
    return script().toJson();
  }

  // ------------------------------------------------------------------

  public String debugInfo() {
    StringBuilder sb = new StringBuilder();
    if (isNone())
      sb.append("<none>");
    else if (isAnonymous())
      sb.append("<anonymous>");
    else {
      sb.append(file().getName());
      sb.append(" items:");
      sb.append(script().items().size());
      if (script().metadata() != null) {
        sb.append(" metadata:");
        sb.append(script().metadata().toJson());
      }
    }
    return sb.toString();
  }

  private final File mScriptFile;
  // File containing script's image, or Files.DEFAULT if there is no image
  private File mImageFile;
  private Script mScriptData;

}

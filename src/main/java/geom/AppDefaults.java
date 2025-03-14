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

import java.io.File;

import geom.gen.GeomAppDefaults;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.guiapp.GUIApp;
import js.parsing.RegExp;

public final class AppDefaults extends BaseObject {

  /**
   * Get directory containing all ephemeral configuration files for this app.
   *
   * This includes the project-wide defaults file ("app_defaults.json"), as well
   * as project-specific defaults, which include the path relative to the home
   * directory in the filename
   */
  public static File projectsConfigurationDirectory() {
    if (sProjectsConfigurationDirectory == null) {
      String appName = GUIApp.sharedInstance().name();
      checkArgument(RegExp.patternMatchesString("\\w+", appName), "illegal app name:", appName);
      String modifiedAppName = "app_defaults_" + DataUtil.convertCamelCaseToUnderscores(appName);
      sProjectsConfigurationDirectory = new File(Files.homeDirectory(), "." + modifiedAppName);
      Files.S.mkdirs(sProjectsConfigurationDirectory);
    }
    return sProjectsConfigurationDirectory;
  }

  private static File sProjectsConfigurationDirectory;

  /**
   * Get project file
   */
  public static File projectFileForProject(File projectDirectory) {
    todo("Not supported for file-based apps: projectFileForProject");
    File absProjDir = projectDirectory.getAbsoluteFile();
    File homeDir = Files.homeDirectory();
    File relativeToHome = Files.fileRelativeToDirectory(absProjDir, homeDir);
    String subdirPath = relativeToHome.toString();

    // If it is not within the home directory tree, use the absolute path instead
    if (subdirPath.contains("..")) {
      subdirPath = chompPrefix(absProjDir.toString(), "/");
      alert("project directory is not within home directory; using different prefix:", absProjDir);
    }
    checkArgument(!subdirPath.contains(".."));

    File dir = new File(projectsConfigurationDirectory(), subdirPath);
    Files.S.mkdirs(dir);
    return new File(dir, "project.json");
  }

  // ------------------------------------------------------------------
  // Shared instance
  // ------------------------------------------------------------------

  public static AppDefaults sharedInstance() {
    return sSharedInstance;
  }

  private static final AppDefaults sSharedInstance;

  static {
    sSharedInstance = new AppDefaults();
  }

  // ------------------------------------------------------------------

  private AppDefaults() {
    loadTools();
  }

  public GeomAppDefaults read() {
    log("read");
    // We may be reading this before we've started up the app, i.e. outside of the Swing thread
    if (mDefaults == null) {
      mDefaults = Files.parseAbstractDataOpt(GeomAppDefaults.DEFAULT_INSTANCE, file()).toBuilder();
      if (verbose())
        log("Read GeomAppDefaults from:", file(), INDENT, mDefaults);
    }
    return mDefaults;
  }

  public GeomAppDefaults.Builder edit() {
    read();
    return mDefaults;
  }

  public void flush() {
    log("flushDefaults");
    boolean written = Files.S.writeIfChanged(file(), mDefaults.build().toString());
    if (written)
      log("...changes saved to", file(), INDENT, mDefaults);
  }

  private File file() {
    return new File(projectsConfigurationDirectory(), "app_defaults.json");
  }

  private GeomAppDefaults.Builder mDefaults;
}

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

import java.io.File;
import java.util.*;

import geom.gen.ProjectState;
import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.MyMath;
import js.json.*;
import testbed.TBGlobals;
import js.graphics.ScriptUtil;
import js.graphics.gen.ScriptFileEntry;

import static js.base.Tools.*;
import static geom.GeomTools.*;

public final class Project extends BaseObject {

  public static final Project DEFAULT_INSTANCE = new Project();

  private Project() {
    mDirectory = null;
    mProjectFile = null;
  }

  public Project(File directory) {
    mDirectory = directory;
    mProjectFile = isDefault() ? null : AppDefaults.projectFileForProject(directory);
  }

  /**
   * Read the ProjectState
   * 
   * @param projectForDefaultStateOrNull
   *          If there is no state file yet for this project, and this is not
   *          null and it exists, use this project state as the default
   */
  private ProjectState readProjectState(File projectForDefaultStateOrNull) {
    ProjectState projectState = null;

    {
      File stateFile = mProjectFile;
      if (stateFile.exists()) {
        log("parsing state from existing state file:", stateFile);
        try {
          JSMap m = JSMap.fromFileIfExists(stateFile);
          if (m.containsKey("version")) {
            if (m.getInt("version") != ProjectState.DEFAULT_INSTANCE.version()) {
              badArg("Project version is out of date");
            }
          }
          projectState = Files.parseAbstractDataOpt(ProjectState.DEFAULT_INSTANCE, m);
        } catch (Throwable t) {
          pr("trouble parsing", stateFile, INDENT, t);
        }
      }
    }

    if (projectState == null) {
      log("project state is null");
      if (Files.nonEmpty(projectForDefaultStateOrNull)) {
        File stateFile = AppDefaults.projectFileForProject(projectForDefaultStateOrNull);
        if (verbose())
          log("looking for template state in:", Files.infoMap(projectForDefaultStateOrNull));
        if (stateFile.exists()) {
          if (false) {
            projectState = Files.parseAbstractDataOpt(ProjectState.DEFAULT_INSTANCE, stateFile).toBuilder();
          } else {
            try {
              projectState = Files.parseAbstractDataOpt(ProjectState.DEFAULT_INSTANCE, stateFile).toBuilder();
            } catch (Throwable t) {
              pr("*** Problem parsing project state");
              pr("*** File:", INDENT, Files.infoMap(stateFile));
              pr("content:", INDENT, Files.readString(stateFile));
            }
          }
        }
      }
    }

    if (projectState == null) {
      log("project state is still null, setting to default");
      projectState = ProjectState.DEFAULT_INSTANCE;
    }
    log("returning state:", INDENT, projectState);
    return projectState.build();
  }

  public void open(File projectForDefaultStateOrNull) {
    ensureDefined();
    log("Project.open, dir:", directory(), INDENT, "projectForDefaultStateOrNull:",
        projectForDefaultStateOrNull);

    mProjectState = readProjectState(projectForDefaultStateOrNull).toBuilder();
    buildScriptList();

    // Make sure script index is legal
    //
    int scriptIndex = scriptIndex();
    if (scriptCount() == 0)
      scriptIndex = 0;
    else
      scriptIndex = MyMath.clamp(scriptIndex, 0, scriptCount() - 1);
    log("correcting state script index from", scriptIndex(), "to", scriptIndex);
    setScriptIndex(scriptIndex);
  }

  public boolean definedAndNonEmpty() {
    return defined() && scriptCount() != 0;
  }

  public boolean defined() {
    return !isDefault();
  }

  public boolean isDefault() {
    return mDirectory == null;
  }

  public File directory() {
    return mDirectory;
  }

  private void ensureDefined() {
    if (isDefault())
      badState("Illegal method for default project");
  }

  private void buildScriptList() {
    boolean hasImages = GeomTools.geomApp().hasImageSupport();
    List<ScriptFileEntry> scriptFileList = ScriptUtil.buildScriptList(directory(), hasImages);
    List<ScriptWrapper> scripts = arrayList();

    File scriptsDirectory = ScriptUtil.scriptDirForProject(directory(), hasImages);

    int logCount = 0;
    for (ScriptFileEntry entry : scriptFileList) {
      File scriptFile = new File(scriptsDirectory, entry.scriptName());
      if (logCount++ < 10)
        log("reading script:", entry.scriptName());
      scripts.add(new ScriptWrapper(scriptFile));
    }
    mScripts = scripts;
  }

  // ------------------------------------------------------------------
  // Project state
  // ------------------------------------------------------------------

  public ProjectState.Builder state() {
    ensureDefined();
    return mProjectState;
  }

  public void flush() {
    String newContent = DataUtil.toString(state());
    if (Files.S.writeIfChanged(mProjectFile, newContent)) {
      if (verbose())
        log("...Project changes written:", mProjectFile, INDENT, state());
    }
  }

  private ProjectState.Builder mProjectState = ProjectState.newBuilder();

  // ------------------------------------------------------------------
  // Scripts
  // ------------------------------------------------------------------

  public int scriptIndex() {
    int index = 0;
    if (validGadgets())  
    index = widgets().vi(TBGlobals.CURRENT_SCRIPT_INDEX);
    int count = scriptCount();
    if (index >= count) {
      pr("scriptIndex", index, "exceeds count", count, "!!!!");
      index = (count == 0) ? -1 : 0;
    }
    return index;
  }

  public int scriptCount() {
    ensureDefined();
    return mScripts.size();
  }

  public void setScriptIndex(int index) {
    if (validGadgets())
    widgets().set(TBGlobals.CURRENT_SCRIPT_INDEX, index);
  }

  /**
   * Get the current script
   */
  public ScriptWrapper script() {
    int index = scriptIndex();
    //    int index = gadg().vi(TBGlobals.CURRENT_SCRIPT_INDEX);
    int count = scriptCount();
    if (index < 0 || index >= count)
      return ScriptWrapper.DEFAULT_INSTANCE;
    return mScripts.get(index);
  }

  public ScriptWrapper script(int scriptIndex) {
    return mScripts.get(scriptIndex);
  }

  // ------------------------------------------------------------------

  private final File mDirectory;
  private final File mProjectFile;
  private List<ScriptWrapper> mScripts;

}

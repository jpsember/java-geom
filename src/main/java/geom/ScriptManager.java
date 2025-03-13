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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import geom.gen.ProjectState;
import geom.gen.ScriptEditState;
import js.base.BaseObject;
import js.file.Files;
import js.geometry.MyMath;
import js.graphics.ScriptElement;
import js.graphics.gen.Script;

import static geom.GeomTools.*;
import static js.base.Tools.*;

public class ScriptManager extends BaseObject {

  public static void setSingleton(ScriptManager manager) {
    sSingleton = manager;
  }

  public static ScriptManager singleton() {
    if (sSingleton == null)
      badState("No singleton ScriptManager defined");
    return sSingleton;
  }

  private static ScriptManager sSingleton;

  public void closeFile() {
    var i = scriptIndex();
    checkState(i >= 0);
    flushScript();
    mScripts.remove(i);
    mCurrentScript = ScriptWrapper.DEFAULT_INSTANCE;
  }

  public void openFile(File scriptFile) {
    assertFileBased();
    setCurrentScript(new ScriptWrapper(scriptFile));
  }

  /**
   * Get (immutable) current script state
   */
  public ScriptEditState state() {
    return mState;
  }

  public void setState(ScriptEditState state) {
    StateTools.validate(state);
    mState = state.build();
    if (mCurrentScript.defined()) {
      // We have to construct an array of ScriptElements, since we can't
      // just pass an array of EditorElements (even though each element implements ScriptElement)
      List<ScriptElement> elements = new ArrayList<>(mState.elements());

      // retain the existing widget map by constructing a builder from the existing script

      Script.Builder b = mCurrentScript.script().toBuilder();
      b.usage(mCurrentScript.script().usage());
      b.items(elements);

      // Where are the gui elements?
      mCurrentScript.setScript(b.build());
    }
  }

  public ScriptWrapper currentScript() {
    return mCurrentScript;
  }

  public void flushScript() {
    mCurrentScript.flush();
  }

  /**
   * Set the current script to the current project's script
   */
  public void loadProjectScript() {
    df("loadProjectScript");

    int i = scriptIndex();
    df(" scrIndex:", i);
    if (i >= 0)
      setCurrentScript(script(i));
    else {
      todo("is this the correct thing to do?");
      setCurrentScript(ScriptWrapper.DEFAULT_INSTANCE);
    }
  }

  private void setCurrentScript(ScriptWrapper newScript) {

    // Copy the clipboard from the current script, so we can copy or paste with the new script
    ScriptEditState oldState = mState;
    mCurrentScript = newScript;
    if (mCurrentScript.isNone() || mCurrentScript.isAnonymous()) {
      return;
    }

    // Parse the ScriptElement objects, constructing an appropriate
    // EditorElement for each
    List<EditorElement> editorElements = arrayList();
    for (ScriptElement element : newScript.script().items()) {
      // It is possible that elements are null, if they were unable to be parsed
      if (element == null)
        continue;
      EditorElement parser = EditorElementRegistry.sharedInstance().factoryForTag(element.tag(), false);
      if (parser == null) {
        pr("*** No EditorElement parser found for tag:", quote(element.tag()));
        continue;
      }
      EditorElement elem = parser.toEditorElement(element);
      EditorElement validatedElement = elem.validate();
      if (validatedElement == null) {
        pr("*** failed to validate element:", INDENT, elem);
        continue;
      }
      editorElements.add(validatedElement);
    }

    newScript.copyWidgetValuesFromScriptToUI();

    setState(ScriptEditState.newBuilder() //
        .elements(editorElements)//
        .clipboard(oldState.clipboard())//
    );

    // Discard undo manager, since it refers to a different script
    geomApp().discardUndoManager();
  }


  public void setScripts(List<ScriptWrapper> scripts) {
    // Sort the scripts by filename
    var a = mScripts;
    a.clear();
    a.addAll(scripts);
    a.sort(SCRIPT_FILE_COMPARATOR);
  }

  private static final Comparator<ScriptWrapper> SCRIPT_FILE_COMPARATOR = (a, b) -> {
    return String.CASE_INSENSITIVE_ORDER.compare(a.file().getPath(), b.file().getPath());
  };

  public int scriptCount() {
    todo("Maybe simpler to think of project as an 'auto loaded' list of scripts?");
    ensureDefined();
    return mScripts.size();
  }

  private void ensureDefined() {
    if (isDefaultProject())
      badState("Illegal method for default project");
  }

  @Deprecated // Can we just use isProjectDefined...?
  public boolean isDefaultProject() {
    return isProjectBased() && currentProject().isDefault();
  }

  public boolean isProjectDefined() {
    return !currentProject().isDefault();
  }

  private void setScriptIndex(int index) {
    df("setScriptIndex", index);
    todo("are we sure we are flushing previous one?");

    var scriptFile = Files.DEFAULT;
    if (index >= 0) {
      checkArgument(index < mScripts.size());
      scriptFile = mScripts.get(index).file();
    }
    widgets().sets(GeomApp.CURRENT_SCRIPT_FILE, scriptFile.getPath());

    setCurrentScript(mScripts.get(index));
    todo("!have a listener or something to update the title?");
    geomApp().updateTitle();
  }

  public Project currentProject() {
    assertProjectBased();
    return mCurrentProject;
  }

  public ProjectState.Builder projectState() {
    todo("Not supported projectState for file-based");
    return currentProject().state();
  }


  public void switchToScript(int index) {
    df("switchToScript", index, "from", scriptIndex());
    if (scriptIndex() != index) {
      flushScript();
      setScriptIndex(index);
      scriptManager().loadProjectScript();
    }
  }

  @Deprecated // Would be better to consolidate isDefaultProject, etc
  public boolean definedAndNonEmpty() {
    if (isProjectBased())
      return isProjectDefined() && scriptCount() != 0;
    return scriptCount() != 0;
  }

//  /**
//   * Get the current script
//   */
//  public ScriptWrapper script() {
//    if (isDefaultProject())
//      return ScriptWrapper.DEFAULT_INSTANCE;
//    int index = scriptIndex();
//    int count = scriptCount();
//    if (index < 0 || index >= count)
//      return ScriptWrapper.DEFAULT_INSTANCE;
//    return mScripts.get(index);
//  }

  public ScriptWrapper script(int scriptIndex) {
    return mScripts.get(scriptIndex);
  }


  public int scriptIndex() {
    var path = new File(widgets().vs(GeomApp.CURRENT_SCRIPT_FILE));
    int index = findScript(path);
    var count = scriptCount();
    if (index < 0) {
      todo("what if there are no scripts?");
      checkState(count > 0);
      index = -index - 1;
      if (index >= count)
        index = count - 1;
    }
    return index;
  }

  /**
   * Locate script by filename
   *
   * Returns index, if found, else (-insert_position) - 1 if not
   */
  private /*for now*/ int findScript(File path) {
    // Create a dummy script wrapper that contains the file we're searching for
    var x = new ScriptWrapper(path);
    var slot = Collections.binarySearch(mScripts, x, SCRIPT_FILE_COMPARATOR);
    df("findScript:", path, "count:", mScripts.size(), "slot:", slot);
    return slot;
  }

  public void validateScriptIndex() {
    // Make sure script index is legal
    //
    int scriptIndex = scriptIndex();
    if (scriptCount() == 0)
      scriptIndex = -1;
    else
      scriptIndex = MyMath.clamp(scriptIndex, 0, scriptCount() - 1);
    setScriptIndex(scriptIndex);
  }

//  /**
//   * Create a new, empty script and make it the active one
//   */
//  public void newScript(File path) {
//    int newIndex = mScripts.size();
//    df("newScript, #scripts:", mScripts.size());
//    todo("add support for not-yet-defined file for script wrapper");
//    todo("Will it handle a not-yet-existing file?");
//    mScripts.add(new ScriptWrapper(path));
//    switchToScript(newIndex);
//  }


  public void switchToScript(File file, boolean createIfNotExist) {
    alertVerbose();
    log("switchToScript", file);
//    checkArgument(Files.nonEmpty(file), "switchToScript with null or empty file");

    var slot = findScript(file);
    log("find script slot:", slot);
    if (slot >= 0) {
      flushScript();
      setScriptIndex(slot);
      log("...switched to existing slot");
    } else {
      slot = -1 - slot;
      if (createIfNotExist) {
        Files.assertNonEmpty(file, "can't create empty filename");
        var script = new ScriptWrapper(file);
        log("created new ScriptWrapper for file, inserting to slot", slot);
        mScripts.add(slot, script);
      } else {
        slot = MyMath.clamp(slot, -1, scriptCount() - 1);
      }
      setScriptIndex(slot);
      todo("do we need to tell it to read the script from disk?");
    }

  }

  public void flushProject() {
    todo("Not supported flushProject for file-based");
    if (!currentProject().isDefined())
      return;
    projectState().widgetStateMap(widgets().readWidgetValues());
    currentProject().flush();
  }

  private ScriptEditState mState = ScriptEditState.DEFAULT_INSTANCE;
  private ScriptWrapper mCurrentScript = ScriptWrapper.DEFAULT_INSTANCE;
  private ArrayList<ScriptWrapper> mScripts = arrayList();

  private Project mCurrentProject = Project.DEFAULT_INSTANCE;

  public void setCurrentProject(Project project) {
    mCurrentProject = project;
  }
}

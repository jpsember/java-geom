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
import java.util.List;

import geom.gen.ScriptEditState;
import js.file.Files;
import js.geometry.MyMath;
import js.graphics.ScriptElement;
import js.graphics.gen.Script;

import static geom.GeomApp.CURRENT_SCRIPT_INDEX;
import static geom.GeomTools.*;
import static js.base.Tools.*;

public class ScriptManager {

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
    flushScript();
    mScript = ScriptWrapper.DEFAULT_INSTANCE;
  }

  public void openFile(File scriptFile) {
    assertFileBased();
    //pr("openFile:", scriptFile);
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
    if (mScript.defined()) {
      // We have to construct an array of ScriptElements, since we can't
      // just pass an array of EditorElements (even though each element implements ScriptElement)
      List<ScriptElement> elements = new ArrayList<>(mState.elements());

      // retain the existing widget map by constructing a builder from the existing script

      Script.Builder b = mScript.script().toBuilder();
      b.usage(mScript.script().usage());
      b.items(elements);

      // Where are the gui elements?
      mScript.setScript(b.build());
    }
  }

  public ScriptWrapper currentScript() {
    return mScript;
  }

  public void flushScript() {
    mScript.flush();
  }

  public void clearScript() {
    // Copy the clipboard from the current script, so we can copy or paste with the new script
    mScript = ScriptWrapper.DEFAULT_INSTANCE;
    if (mScript.isNone() || mScript.isAnonymous()) {
      return;
    }
  }

  /**
   * Set the current script to the current project's script
   */
  public void loadProjectScript() {
//    var cp = geomApp().currentProject();
//    var newScript = cp.script();
//
//    if (newScript == mScript) {
//      badState("....project script is already the active script!!!!!!!!!!!!!!!!!!!!!!!!!!");
//      return;
//    }

    int i = scriptIndex();
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
    mScript = newScript;
    if (mScript.isNone() || mScript.isAnonymous()) {
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

  private ScriptEditState mState = ScriptEditState.DEFAULT_INSTANCE;
  private ScriptWrapper mScript = ScriptWrapper.DEFAULT_INSTANCE;


  public void setScripts(List<ScriptWrapper> scripts) {
    mScripts = scripts;
  }

  public int scriptCount() {
    ensureDefined();
    return mScripts.size();
  }

  private void ensureDefined() {
    if (isDefaultProject())
      badState("Illegal method for default project");
  }

  public boolean isDefaultProject() {
    return isProjectBased() && currentProject().isDefault();
  }

  public boolean isProjectDefined() {
    return !currentProject().isDefault();
  }

  public void setScriptIndex(int index) {
    widgets().setf(CURRENT_SCRIPT_INDEX, index);
  }

  public Project currentProject() {
    todo("!move currentProject() out of geomApp and into ScriptManager?");
    return geomApp().currentProject();
  }

  public void switchToScript(int index) {
    todo("Not supported switchToScript for file-based");

    if (scriptIndex() != index) {
      flushScript();
      setScriptIndex(index);
      scriptManager().loadProjectScript();
    }
  }

  public boolean definedAndNonEmpty() {
    return isProjectDefined() && scriptCount() != 0;
  }

  /**
   * Get the current script
   */
  public ScriptWrapper script() {
    if (isDefaultProject())
      return ScriptWrapper.DEFAULT_INSTANCE;
    int index = scriptIndex();
    int count = scriptCount();
    if (index < 0 || index >= count)
      return ScriptWrapper.DEFAULT_INSTANCE;
    return mScripts.get(index);
  }

  public ScriptWrapper script(int scriptIndex) {
    return mScripts.get(scriptIndex);
  }


  public int scriptIndex() {
    todo("!should script index be a script filename instead?");
    int index = widgets().vi(CURRENT_SCRIPT_INDEX);
    int count = scriptCount();
    if (index >= count) {
      if (index > 0)
        pr("scriptIndex", index, "exceeds count", count, "!!!!");
      index = (count == 0) ? -1 : 0;
    }
    return index;
  }


  public void validateScriptIndex() {
    // Make sure script index is legal
    //
    int scriptIndex = scriptIndex();
    if (scriptCount() == 0)
      scriptIndex = 0;
    else
      scriptIndex = MyMath.clamp(scriptIndex, 0, scriptCount() - 1);
    setScriptIndex(scriptIndex);
  }

  /**
   * Create a new, empty script and make it the active one
   */
  public void newScript() {
    int newIndex = mScripts.size();
    todo("add support for not-yet-defined file for script wrapper");
    mScripts.add(new ScriptWrapper(Files.DEFAULT));
    switchToScript(newIndex);
  }

  private List<ScriptWrapper> mScripts = arrayList();

}

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
import java.util.ArrayList;
import java.util.List;

import geom.gen.ScriptEditState;
import js.graphics.ScriptElement;
import js.graphics.gen.Script;

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static js.graphics.ScriptUtil.*;

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
    todo("closeFile");
    die("we need to cleanly discard the current script... or?");
    loadProjectScript(); // was ScriptWrapper.DEFAULT_INSTANCE
  }

  public void openFile(File scriptFile) {
    todo("openFile");
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

  /**
   * Set the current script to the current project's script
   */
  public void loadProjectScript() {
    var cp = geomApp().currentProject();
    D20(VERT_SP, "loadProjectScript; current project:", cp.name());
    var newScript = cp.script();

    if (DEBUG_20)
      todo("read/write controls to scripts");
    if (newScript == mScript) {
      D20("....project script is already the active script!!!!!!!!!!!!!!!!!!!!!!!!!!");
      return;
    }

    // Copy the clipboard from the current script, so we can copy or paste with the new script
    ScriptEditState oldState = mState;
    mScript = newScript;
    if (mScript.isNone() || mScript.isAnonymous()) {
      D20("script is none or anon");
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

    D20("updating ui; new script widgets:", INDENT, D20Map(newScript.script().widgets()));

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

}

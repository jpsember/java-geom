package geom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import geom.gen.ScriptEditState;
import js.graphics.ScriptElement;
import js.graphics.gen.Script;

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
    replaceCurrentScriptWith(ScriptWrapper.DEFAULT_INSTANCE);
  }

  public void openFile(File scriptFile) {
  }

  /**
   * Get (immutable) current script state
   */
  public ScriptEditState state() {
    return mState;
  }

  public void setState(ScriptEditState state) {
    mState = state.build();
    if (mScript.defined()) {
      // We have to construct an array of ScriptElements, since we can't
      // just pass an array of EditorElements (even though each element implements ScriptElement)
      List<ScriptElement> elements = new ArrayList<>(mState.elements());
      Script.Builder b = Script.newBuilder();
      b.usage(mScript.data().usage());
      b.items(elements);
      mScript.setData(b.build());
    }
  }

  public ScriptWrapper currentScript() {
    return mScript;
  }

  public void flushScript() {
    mScript.flush();
  }

  public void replaceCurrentScriptWith(ScriptWrapper newScript) {
    if (newScript == mScript)
      return;

    // Copy the clipboard from the current script, so we can copy or paste with the new script
    ScriptEditState oldState = mState;
    mScript = newScript;
    if (mScript.isNone() || mScript.isAnonymous()) {
      return;
    }

    // Parse the ScriptElement objects, constructing an appropriate
    // EditorElement for each
    List<EditorElement> editorElements = arrayList();
    for (ScriptElement element : newScript.data().items()) {
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

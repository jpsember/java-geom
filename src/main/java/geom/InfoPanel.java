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

import javax.swing.BorderFactory;

import static js.base.Tools.*;

import js.widget.SwingWidgetManager;
import js.widget.Widget;
import js.widget.WidgetManager;
import testbed.ControlPanel;
import testbed.TBGlobals;

import static geom.GeomTools.*;

public class InfoPanel extends ControlPanel {

  private static final boolean GADGETS = false && alert("using gadgets, not widgets");

  public void opening(Project project) {
    if (!todo("!restore widget state map from project somehow")) {
      // mWidgetManager.setStateMap(project.widgetStateMap());
      mWidgetManager.restoreWidgetValues();
    }
  }

  public InfoPanel() {
    if (!GADGETS)
      setBorder(BorderFactory.createRaisedBevelBorder());

    if (GADGETS) {

      prepareForGadgets();

      todo("make these non-persistent");

      textField(TBGlobals.SCRIPT_NAME, "File", "Current script name", 80, false, "script name");
      textField(TBGlobals.MESSAGE, null, null, 80, false, "message");
      finishedGadgets();

    } else {

      WidgetManager m = new SwingWidgetManager();
      // Use the InfoPanel as the outermost container
      m.setPendingContainer(this);

      m.columns(".x").open();
      {
        m.addLabel("Script:");
        mFilePath = m.monospaced().large().addText();
        mMessageField = m.skip().monospaced().addText();
      }
      m.addVertGrow();
      m.close();
      m.setPrepared(true);
      mWidgetManager = m;
    }
  }

  public void refresh() {
    ScriptWrapper script = scriptManager().currentScript();

    String scriptDisplay = "";
    if (!script.isNone()) {
      Project project = editor().currentProject();
      StringBuilder sb = new StringBuilder();
      sb.append(project.scriptIndex());
      sb.append("/");
      sb.append(project.scriptCount() - 1);
      sb.append(" ");
      String nameOnly = script.name();
      sb.append(nameOnly);
      scriptDisplay = sb.toString();
    }
    if (GADGETS)
      gadg().setValue(TBGlobals.SCRIPT_NAME, scriptDisplay);
    else
      mFilePath.setText(scriptDisplay);
  }

  public void setMessage(String text) {
    text = nullToEmpty(text);
    if (text.startsWith("!")) {
      text = "*** " + text.substring(1);
      mErrorTime = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - mErrorTime < 20000)
        return;
    }
    if (GADGETS)
      gadg().setValue(TBGlobals.MESSAGE, text);
    else
      mMessageField.setText(text);
  }

  private WidgetManager mWidgetManager;
  private long mErrorTime;
  private Widget mFilePath;
  private Widget mMessageField;
}

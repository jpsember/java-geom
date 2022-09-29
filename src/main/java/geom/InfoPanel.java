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
import javax.swing.JPanel;

import static js.base.Tools.*;

import js.widget.WidgetManager;

import static geom.GeomTools.*;
import static geom.GeomApp.*;

public class InfoPanel extends JPanel {

  public void opening(Project project) {
    if (!todo("!restore widget state map from project somehow")) {
      // mWidgetManager.setStateMap(project.widgetStateMap());
      // mWidgetManager.restoreWidgetValues();
    }
  }

  public InfoPanel() {

    setBorder(BorderFactory.createRaisedBevelBorder());

    WidgetManager m = widgets();

    // Use the InfoPanel as the root container
    m.setPendingContainer(this);

    m.columns(".x");
    m.open("InfoPanel");
    {
      m.addLabel("Script:");
      m.monospaced().large().addText( SCRIPT_NAME);
      m.skip().monospaced().addText( MESSAGE);
    }
    m.addVertGrow();
    m.close("InfoPanel");
  }

  public void refresh() {
    ScriptWrapper script = scriptManager().currentScript();

    String scriptDisplay = "";
    if (!script.isNone()) {
      Project project = geomApp().currentProject();
      StringBuilder sb = new StringBuilder();
      sb.append(project.scriptIndex());
      sb.append("/");
      sb.append(project.scriptCount() - 1);
      sb.append(" ");
      String nameOnly = script.name();
      sb.append(nameOnly);
      scriptDisplay = sb.toString();
    }
    widgets().setValue( SCRIPT_NAME, scriptDisplay);
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

    widgets().setValue( MESSAGE, text);
  }

  //  private WidgetManager mWidgetManager;
  private long mErrorTime;
  //  private Widget mFilePath;
  //  private Widget mMessageField;
}

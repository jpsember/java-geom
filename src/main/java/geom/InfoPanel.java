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

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import static js.base.Tools.*;

import js.widget.WidgetManager;

import static geom.GeomTools.*;
import static geom.GeomApp.*;

// I'm not sure this needs to be visible outside of the geom package...
class InfoPanel extends JPanel {

  public InfoPanel() {

    setBorder(BorderFactory.createRaisedBevelBorder());

    WidgetManager m = widgets();

    // Use the InfoPanel as the root container
    m.setPendingContainer(this);

    m.columns(".x");
    m.open("InfoPanel");
    {
      m.label("Script:").addLabel();
      m.monospaced().large().addText(SCRIPT_NAME);
      m.skip().monospaced().addText(MESSAGE);
    }
    m.addVertGrow();
    m.close("InfoPanel");
  }

  public void refresh() {

    if (isFileBased()) {
      todo("refresh InfoPanel for File-based projects");
      return;
    }
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
    widgets().sets(SCRIPT_NAME, scriptDisplay);
  }

  /**
   * Returns true if message has changed
   */
  public boolean setMessage(String text) {
    boolean changed = false;
    text = nullToEmpty(text);
    if (text.startsWith("!")) {
      text = "*** " + text.substring(1);
      mErrorTime = System.currentTimeMillis();
    } else {
      if (System.currentTimeMillis() - mErrorTime < 20000)
        return false;
    }
    String oldMessage = widgets().vs(MESSAGE);
    changed = !text.equals(oldMessage);
    if (changed)
      widgets().sets(MESSAGE, text);
    return changed;
  }

  private long mErrorTime;
}

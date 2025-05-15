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
package geom.oper;

import static js.base.Tools.*;

import java.io.File;

import geom.GeomTools;
import geom.ScriptWrapper;
import js.file.Files;
import js.guiapp.SwingUtils;
import js.guiapp.UserOperation;

import static geom.GeomTools.*;

public class NewFileOper extends UserOperation {

  @Override
  public void start() {
    alertVerbose();
    log("start");
    todo("!use directory of last loaded/saved script");
    var f = SwingUtils.displaySaveFileRequester(null, "Name of new script",
        SwingUtils.filenameFilterForExtension(Files.EXT_JSON, null)
    );
    log("save file chooser returned:", INDENT, Files.infoMap(f));
    if (f == null) return;

    f = Files.addExtension(f, "json");
    log("with extension:", f);

    var sm = scriptManager();
    sm.switchToScript(f, true);
  }
}

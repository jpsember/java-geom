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
package sample;

import js.app.App;
import js.file.Files;
import js.graphics.ScriptUtil;
import js.json.JSMap;
import js.widget.WidgetManager;
import sample.match.MatchOper;
import testbed.*;

import static js.base.Tools.*;

import java.io.File;

public class SampleMain extends TestBed {

  public static void main(String[] args) {
    loadTools();

    {
      var f = new File(Files.homeDirectory(), ".geom_sample_project_directory");
      Files.S.setProjectDirectory(f);
    }
    App app = new SampleMain();
    app.startApplication(args);
  }

  private SampleMain() {
    guiAppConfig() //
        .appName("Sample") //
        .keyboardShortcutRegistry(new JSMap(Files.readString(this.getClass(), "key_shortcut_defaults.json")));
  }

  @Override
  public boolean hasImageSupport() {
    return false;
  }

  @Override
  public void addOperations() {
    ScriptUtil.sAllowEmptyScripts = true;
    addOper(new MatchOper());
    addOper(new BoundsOper());
  }

  @Override
  public void addControls(WidgetManager c) {
    c.open("SampleMain controls");
    c.label("This is where app-wide controls get added").addLabel();

    c.listener(this::buttonListener).label("Hello").addButton(".hello_id");

    c.close("SampleMain controls");
  }

  private void buttonListener(String id) {
    pr("Button pressed:", id);
    mPressCount++;
    widgetManager().sets(id, "presses: " + mPressCount);
  }

  private int mPressCount;

}

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

import java.io.File;
import java.util.List;

import geom.Project;

import static js.base.Tools.*;

import js.file.Files;
import js.geometry.MyMath;
import js.guiapp.UserOperation;

import static geom.GeomTools.*;

public class OpenNextProjectOper extends UserOperation {

  @Override
  public void start() {
    var sm = scriptManager();
    // If there's an existing project or recent project, start requester in its parent directory
    //   Project project =  geomApp().currentProject();
    File currentProjectDirectory;
    if (sm.isProjectDefined())
      currentProjectDirectory = sm.currentProject().directory();
    else {
      // Use most recent project (if there is one)
      currentProjectDirectory = geomApp().recentProjects().getMostRecentFile();
    }

    if (Files.empty(currentProjectDirectory))
      return;
    List<File> projectFiles = arrayList();
    for (File f : Files.files(Files.parent(currentProjectDirectory))) {
      if (!f.isDirectory())
        continue;
      projectFiles.add(f);
    }
    if (projectFiles.isEmpty())
      return;

    int currSlot = projectFiles.indexOf(currentProjectDirectory);
    int nextSlot = MyMath.myMod(currSlot + 1, projectFiles.size());
    File nextProjDir = projectFiles.get(nextSlot);


    geomApp().openProject(nextProjDir);
  }
}

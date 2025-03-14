package geom.oper;

import js.file.DirWalk;
import js.file.Files;
import js.guiapp.UserOperation;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;


import static geom.GeomTools.*;
import static js.base.Tools.*;

public class OpenNextScriptOper extends UserOperation {

  @Override
  public boolean shouldBeEnabled() {
    mNextScriptFile = determineNextScriptFile();
    log("next script file:", INDENT, Files.infoMap(mNextScriptFile));
    return mNextScriptFile != null;
  }

  @Override
  public void start() {
    checkState(mNextScriptFile.exists());
    scriptManager().switchToScript(mNextScriptFile, true);
  }

  private File determineNextScriptFile() {
    var sm = scriptManager().currentScript();
    File f = Files.DEFAULT;
    if (!sm.isNone()) f = sm.file();

    var dir = Files.currentDirectory();
    if (Files.nonEmpty(f)) {
      dir = f.getParentFile();
    }
    var dw = new DirWalk(dir).withRecurse(false).withExtensions(Files.EXT_JSON);
    var fl = dw.files();
    int slot = Collections.binarySearch(fl, f, FILE_COMPARATOR);
    if (slot < 0)
      slot = -slot - 1;
    else slot = slot + 1;
    if (slot < fl.size())
      return fl.get(slot);
    return null;
  }


  private static final Comparator<File> FILE_COMPARATOR = (a, b) -> {
    return String.CASE_INSENSITIVE_ORDER.compare(a.getPath(), b.getPath());
  };

  private File mNextScriptFile;
}

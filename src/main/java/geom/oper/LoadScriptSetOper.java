package geom.oper;

import geom.GeomTools;
import js.file.Files;
import js.guiapp.SwingUtils;
import js.guiapp.UserOperation;
import js.json.JSMap;

import java.io.File;

import static geom.GeomTools.filterForExtension;
import static geom.GeomTools.scriptManager;
import static js.base.Tools.*;

public class LoadScriptSetOper extends UserOperation {


  @Override
  public void start() {
    var sm = scriptManager();
    var dir = Files.currentDirectory();
    if (sm.definedAndNonEmpty())
      dir = sm.currentScript().file().getParentFile();

    var f = SwingUtils.displayOpenFileChooser(dir, "Load set of scripts",
        filterForExtension(GeomTools.SCRIPT_SET_EXTENSION, "Script sets"));
    log("load file chooser returned:", INDENT, Files.infoMap(f));
    if (f == null) return;

    var m = JSMap.from(f);
    var lst = m.getList("list");

    sm.closeAllFiles();

    for (var x : lst.asStringList()) {
      var y = new File(x);
      log("openFile:", y);
      sm.switchToScript(y, true);
    }
    if (sm.definedAndNonEmpty())
      sm.switchToScript(0);
  }
}

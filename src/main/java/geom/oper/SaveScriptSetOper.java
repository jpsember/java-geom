package geom.oper;

import geom.GeomTools;
import js.file.Files;
import js.guiapp.SwingUtils;
import js.guiapp.UserOperation;

import java.io.File;

import static geom.GeomTools.filterForExtension;
import static geom.GeomTools.scriptManager;
import static js.base.Tools.*;

public class SaveScriptSetOper extends UserOperation {

  @Override
  public boolean shouldBeEnabled() {
    var sm = scriptManager();
    return sm.definedAndNonEmpty();
  }

  @Override
  public void start() {
    var sm = scriptManager();
    var dir = sm.currentScript().file().getParentFile();

    var f = SwingUtils.displaySaveFileChooser(dir, "Save set of scripts",filterForExtension(GeomTools.SCRIPT_SET_EXTENSION, "Script sets"));
    log("save file chooser returned:", INDENT, Files.infoMap(f));
    if (f == null) return;


    f = Files.addExtension(f, "set");
    log("with extension:", f);

    var m = map();
    var lst = list();
    m.put("list", lst);
    for (var i = 0; i < sm.scriptCount(); i++) {
      lst.add(sm.script(i).file().getPath());
    }
    Files.S.writePretty(f, m);
  }
}

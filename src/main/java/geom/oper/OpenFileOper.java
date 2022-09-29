package geom.oper;

import static js.base.Tools.*;

import java.io.File;

import js.file.Files;
import js.guiapp.SwingUtils;
import js.guiapp.UserOperation;
import static geom.GeomTools.*;

public class OpenFileOper extends UserOperation {

  @Override
  public void start() {
    loadTools();
    File defaultDirectory = null;

    defaultDirectory = Files.ifEmpty(defaultDirectory, Files.currentDirectory());
    File file = SwingUtils.displayOpenFileRequester(Files.parent(defaultDirectory), "Open Script");
    if (Files.empty(file))
      return;

    scriptManager().closeFile();
    scriptManager().openFile(file);
  }
}

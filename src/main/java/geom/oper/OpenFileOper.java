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

    //    // If there's an existing file, start requester in its parent directory
    //    File currentFile = editor().currentScript();
    //        
    //    Project project = seditor().currentProject();
    //    if (project.defined())
    //      defaultDirectory = project.directory();
    //    else {
    //      // Use most recent project (if there is one)
    //      defaultDirectory = seditor().recentProjects().getMostRecentFile();
    //    }
    //
    //    if (!Files.empty(defaultDirectory) && !defaultDirectory.isDirectory())
    //      defaultDirectory = null;

    defaultDirectory = Files.ifEmpty(defaultDirectory, Files.currentDirectory());
    File file = SwingUtils.displayOpenFileRequester(Files.parent(defaultDirectory), "Open Script");
    if (Files.empty(file))
      return;

    editor().closeFile();
    editor().openFile(file);
  }
}

//{
//  private static void doOpen(String f) {
//    final boolean db = false;
//    if (db)
//      Tools.warn("db is on");
//    try {
//
//      stopEdit();
//      unselectAll();
//      Tokenizer tk = null;
//
//      if (f == null) {
//        String s = fileStats.getPath();
//        if (s == null) {
//          s = Path.getUserDir();
//        }
//        IFileChooser ch = Streams.fileChooser();
//        String ns = ch.doOpen("Open file:", s, new PathFilter(TestBed
//            .parms.fileExt));
//        if (ns != null) {
//          f = Path.changeExtension(ns, TestBed.parms.fileExt);
//        }
//      }
//      if (f != null) {
//
//        clearUndo(true);
//
//        // read the new file
//        tk = new Tokenizer(f);
//        if (db) {
//          tk.setEcho(true);
//          tk.setTrace(true);
//        }
//
//        // create a new array to read file into
//        ObjArray dp = new ObjArray();
//
//        C.parseGadgets(tk);
//
//        // read items
//        while (!tk.eof()) {
//          String tag = tk.read(T_WORD).text();
//          Integer iv = (Integer) objTypesMap.get(tag);
//          if (iv == null)
//            throw new TBError("Unrecognized tag: " + tag);
//
//          EdObjectFactory fa = getType(iv.intValue());
//          if (db)
//            Streams.out.println("factory for " + iv + " is " + fa);
//          String wd = tk.read().text();
//          int flags = TextScanner.parseHex(wd, true);
//
//          EdObject eo = fa.parse(tk, flags);
//          if (db)
//            Streams.out.println(" parsed object " + eo + ", type=" + eo.getFactory());
//
//          dp.add(eo);
//        }
//
//        // now that read was ok, replace original
//        items = dp;
//        fileStats.setPath(f);
//
//        updateTitle();
//        TestBed.writeConfigFile();
//
//      }
//      if (tk != null)
//        tk.close();
//
//    } catch (Throwable e) {
//      if (TestBed.parms.debug)
//        Streams.out.println(Tools.d(e));
//      throw new TBError(e);
//    }
//  }
//}

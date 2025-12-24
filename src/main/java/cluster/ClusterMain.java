package cluster;

import js.app.App;
import js.file.Files;
import js.graphics.ScriptUtil;
import js.json.JSMap;
import js.widget.WidgetManager;
import testbed.*;

import static js.base.Tools.*;

import java.io.File;

public class ClusterMain extends TestBed {

  public static void main(String[] args) {
    loadTools();
    Files.setAlternateResourceDir(new File("/Users/home/github_projects/java-geom/src/main/resources"));
    {
      var f = new File(Files.homeDirectory(), ".geom_sample_project_directory");
      Files.S.setProjectDirectory(f);
    }
    App app = new ClusterMain();
    app.startApplication(args);
  }

  private ClusterMain() {
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
    addOper(new ClusterOper());
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

package sample;

import js.app.App;
import js.json.JSMap;
import js.widget.WidgetManager;
import testbed.*;

import static js.base.Tools.*;

public class SampleMain extends TestBed {

  public static void main(String[] args) {
    loadTools();
    App app = new SampleMain();
    app.startApplication(args);
  }

  private SampleMain() {
    guiAppConfig() //
        .appName("Sample") //
        .keyboardShortcutRegistry(JSMap.fromResource(this.getClass(), "key_shortcut_defaults.json")) //;
    ;
  }

  @Override
  public boolean hasImageSupport() {
    return false;
  }

  @Override
  public void addOperations() {
    addOper(new BoundsOper());
  }

  @Override
  public void addControls(WidgetManager c) {
    c.open();
    c.addLabel("This is where app-wide controls get added");
    c.close();
  }

}

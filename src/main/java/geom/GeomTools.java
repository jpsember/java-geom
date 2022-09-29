package geom;

import js.guiapp.GUIApp;
import js.widget.WidgetManager;

public final class GeomTools {

  public static GeomApp geomApp() {
    return GUIApp.sharedInstance();
  }

  public static WidgetManager widgets() {
    return GUIApp.sharedInstance().widgetManager();
  }

  public static ScriptManager scriptManager() {
    return ScriptManager.singleton();
  }

}

package geom;

import static js.base.Tools.*;

public final class GeomTools {

  public static GeomApp editor() {
    return GeomApp.singleton();
  }

  public static ScriptManager scriptManager() {
    return ScriptManager.singleton();
  }

}

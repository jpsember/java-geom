package geom;

import static js.base.Tools.*;

import java.util.List;

import js.guiapp.GUIApp;
import js.widget.WidgetManager;

public final class GeomTools {

  public static GeomApp geomApp() {
    return GUIApp.sharedInstance();
  }

  public static WidgetManager widgets() {
    return GUIApp.sharedInstance().widgetManager();
  }

  /**
   * Temporary while transitioning to Widgets
   */
  public static boolean validGadgets() {
    todo("is validGadgets() required?");
    if (widgets() == null) {
      alert("no gadgets defined");
      return false;
    }
    return true;
  }

  public static ScriptManager scriptManager() {
    return ScriptManager.singleton();
  }

  /**
   * Split a string into substrings at line break positions
   * 
   * @param str
   *          String
   * @param lineWidth
   *          maximum number of characters per row
   * @param lst
   *          substrings are stored here
   * @return int length of longest substring
   */
  public static int splitString(String str, int lineWidth, List<String> lst) {

    int stringStart = 0;
    int lastSpace = -1;

    int cursor = 0;
    int maxStrLen = 0;

    lst.clear();
    while (true) {
      char ch = cursor < str.length() ? str.charAt(cursor) : '\n';
      if (ch <= ' ') {
        lastSpace = cursor;
      }

      // If linefeed, or at maximum width,
      // output a substring from the start of the string to
      // the last space printed (exclusive), or to the maximum
      // width (if no last space exists)

      if (ch == '\n') {
        String ns = str.substring(stringStart, cursor);
        lst.add(ns);
        maxStrLen = Math.max(maxStrLen, ns.length());
        cursor++;
        stringStart = cursor;
      } else if (cursor + 1 - stringStart > lineWidth) {
        if (lastSpace > stringStart) {

          // Consume spaces preceding last space
          int ls = lastSpace - 1;
          while (ls >= stringStart && str.charAt(ls) == ' ') {
            ls--;
          }

          String ns = str.substring(stringStart, ls + 1);
          lst.add(ns);
          maxStrLen = Math.max(maxStrLen, ns.length());

          // Consume spaces following last space
          while (lastSpace + 1 < str.length() && str.charAt(lastSpace + 1) == ' ') {
            lastSpace++;
          }
          stringStart = lastSpace + 1;
          cursor = Math.max(stringStart, cursor);
        } else {
          String ns = str.substring(stringStart, cursor);
          lst.add(ns);
          maxStrLen = Math.max(maxStrLen, ns.length());
          stringStart = cursor;
        }
      } else {
        cursor++;
      }
      if (cursor > str.length()) {
        break;
      }
    }
    return maxStrLen;
  }

}

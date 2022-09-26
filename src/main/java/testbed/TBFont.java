package testbed;

import static js.base.Tools.*;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.List;

class TBFont {

  private static String[] ifonts = { "Monospaced p 12", "Monospaced p 16", "Monospaced b 20", "Times i 16", };

  public static void prepare() {
    if (sFonts == null) {
      sFonts = new TBFont[ifonts.length];
      for (int i = 0; i < ifonts.length; i++)
        sFonts[i] = parse(ifonts[i]);
    }
  }

  public static Font getFont(int index) {
    return get(index).mFont;
  }

  public static TBFont get(int index) {
   checkState(sFonts != null, "TBFont.prepare() not called");
    return sFonts[index];
  }

  /**
   * Parse a string and construct a TBFont. Each string has the format:
   *
   * name style size
   *
   * name is a font name style is [p|i|b] plain/italic/bold size is integer size
   *
   * @param str
   *          String of arguments separated by whitespace
   * @return TBFont
   */
  private static TBFont parse(String str) {
    TBFont f = null;
    int s = Font.PLAIN;
    int sf = 0;

    List<String> words = split(str, ' ');
    String name = words.get(0);
    String style = words.get(1);
    int size = Integer.parseInt(words.get(2));

    for (int i = 0; i < style.length(); i++) {
      switch (style.charAt(i)) {
      case 'p':
        sf = 0;
        break;
      case 'i':
        sf |= Font.ITALIC;
        break;
      case 'b':
        sf |= Font.BOLD;
        break;
      default:
        throw new TBError("TBFont parse problem: " + s);
      }
    }
    f = new TBFont(name, sf, size);

    return f;
  }

  public static Font fixedWidthFont() {
    return fixedWidthFont;

  }

  private static Font fixedWidthFont = new Font("Monospaced", Font.PLAIN, 13);

  public TBFont(String name, int style, int size) {
    mFont = new Font(name, style, size);
    mFontCharWidth = metrics().charWidth(' ');
  }

  public FontMetrics metrics() {
    if (mFontMetrics == null) {
      mFontMetrics = V.get2DGraphics().getFontMetrics(mFont);
    }

    return mFontMetrics;
  }

  public double charWidth() {
    return mFontCharWidth;
  }

  public Font font() {
    return mFont;
  }

  /**
   * Debug utility: show font names
   */
  public static void showFonts() {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] names = ge.getAvailableFontFamilyNames();
    for (int i = 0; i < names.length; i++) {
      System.out.println(names[i]);
    }
  }

  private static TBFont[] sFonts;

  private FontMetrics mFontMetrics;
  private double mFontCharWidth;
  private Font mFont;

}

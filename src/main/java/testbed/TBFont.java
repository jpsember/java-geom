package testbed;

import java.awt.*;
import base.*;
import static js.base.Tools.*;

class TBFont implements IEditorScript {

  private static String[] ifonts = { "Monospaced p 12", "Monospaced p 16", "Monospaced b 20", "Times i 16", };

  public static void prepare() {
    if (fonts == null) {
      loadTools();
      fonts = new TBFont[ifonts.length];
      for (int i = 0; i < ifonts.length; i++) {
        fonts[i] = parse(ifonts[i]);
      }
    }
  }

  public static Font getFont(int index) {
    return get(index).font;
  }

  public static TBFont get(int index) {
    Tools.ASSERT(fonts != null, "TBFont.prepare() not called");
    return fonts[index];
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
    Tokenizer tk = new Tokenizer(str, true);

    //    tk.parse(str, 0);
    String name = tk.read(T_WORD).text();
    String style = tk.read(T_WORD).text();
    int size = tk.readInt();

    //Streams.out.println("TBFont, reading name="+name+", style="+style+", size="+size);

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

  private static Font fixedWidthFont =
      //      new Font("Times",Font.ITALIC,13);
      new Font("Monospaced", Font.PLAIN, 13);

  public TBFont(String name, int style, int size) {
    font = new Font(name, style, size);
    fontCharWidth = metrics().charWidth(' ');
  }

  public FontMetrics metrics() {
    if (fontMetrics == null) {
      fontMetrics = V.get2DGraphics().getFontMetrics(font);
    }

    return fontMetrics;
  }

  public double charWidth() {
    return fontCharWidth;
  }

  public Font font() {
    return font;
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

  // array of fonts
  private static TBFont[] fonts;

  private FontMetrics fontMetrics;
  private double fontCharWidth;
  private Font font;

  public TBFont scaledBy(float scale) {
    float targetFontSize = font.getSize2D() * scale;
    int tf = Math.round(targetFontSize);
    pr("got font scaled by:",scale,"to:",tf);
    return new TBFont(font.getName(), font.getStyle(), tf);
  }
}

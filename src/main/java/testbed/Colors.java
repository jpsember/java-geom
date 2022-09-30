/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package testbed;

import js.geometry.MyMath;
import static js.base.Tools.*;

import java.awt.Color;
import java.util.Map;

/**
 * Manages a set of Color objects, where each base Color has an id, and has a
 * number of shades available (from 0:darkest to 1.0: lightest)
 */
public final class Colors {

  // Color ids
  //
  public static final int WHITE = 0, LIGHTGRAY = 1, GRAY = 2, DARKGRAY = 3, BLACK = 4, RED = 5, PINK = 6,
      ORANGE = 7, YELLOW = 8, GREEN = 9, MAGENTA = 10, CYAN = 11, BLUE = 12, BROWN = 13, PURPLE = 14,
      DARKGREEN = 15, DEFAULT_COLORS = 16;


  private static final int SHADE_LEVELS = 64;

  /**
   * Get color with shade = 0.5
   */
  public static Color get(int id) {
    return get(id, .5);
  }

  /**
   * Get color, indexed by id and a shade 0..1
   */
  public static Color get(int id, double shade) {
    int iLevel = MyMath.clamp((int) (shade * SHADE_LEVELS), 0, SHADE_LEVELS - 1);
    Color[] set = getColorSet(id);
    return set[iLevel];
  }

  private static void add(Color[] set, int shade, double r, double g, double b) {
    set[shade] = construct(r, g, b);
  }

  /**
   * Construct a Color from rgb values, after clamping them
   */
  private static Color construct(double r, double g, double b) {
    return new Color((float) MyMath.clamp(r, 0, 1.0), (float) MyMath.clamp(g, 0, 1.0),
        (float) MyMath.clamp(b, 0, 1.0));
  }

  /**
   * Add a color as a transition between two colors
   * 
   */
  public static void addTransition(int id, Color a, Color b) {
    double accR = a.getRed() / 256.0;
    double accG = a.getGreen() / 256.0;
    double accB = a.getBlue() / 256.0;

    double ri = ((b.getRed() / 256.0) - accR) / SHADE_LEVELS;
    double gi = ((b.getGreen() / 256.0) - accG) / SHADE_LEVELS;
    double bi = ((b.getBlue() / 256.0) - accB) / SHADE_LEVELS;

    Color[] set = addSet(id);
    for (int i = 0; i < SHADE_LEVELS; i++) {
      add(set, i, accR, accG, accB);
      accR += ri;
      accG += gi;
      accB += bi;
    }
  }

  /**
   * Add color
   */
  public static void add(int id, Color c) {
    add(id, c.getRed() / 256.0, c.getGreen() / 256.0, c.getBlue() / 256.0);
  }

  /**
   * Add color
   */
  public static void add(int id, double r, double g, double b) {
    Color[] set = addSet(id);
    for (int i = 0; i < SHADE_LEVELS; i++) {
      double scale = (i * 2) / (double) SHADE_LEVELS;
      double r0 = r * scale, g0 = g * scale, b0 = b * scale;
      double extra = 0;
      if (r0 > 1.0)
        extra += r0 - 1.0;
      if (g0 > 1.0)
        extra += g0 - 1.0;
      if (b0 > 1.0)
        extra += b0 - 1.0;
      r0 += extra * .3;
      g0 += extra * .3;
      b0 += extra * .3;
      add(set, i, r0, g0, b0);
    }
  }

  private static Map<Integer, Color[]> colorsMap() {
    if (sColorsMap == null) {
      sColorsMap = hashMap();
      init();
    }
    return sColorsMap;
  }

  private static void init() {
    add(WHITE, 1, 1, 1);
    add(LIGHTGRAY, 0.75, 0.75, 0.75);
    add(GRAY, 0.50, 0.50, 0.50);
    add(DARKGRAY, 0.25, 0.25, 0.25);
    add(BLACK, 0, 0, 0);
    add(RED, Color.RED);
    add(PINK, Color.PINK);//1, 0.68, 0.68);
    add(ORANGE, Color.ORANGE);
    add(YELLOW, Color.YELLOW);
    add(GREEN, Color.GREEN);
    add(MAGENTA, Color.MAGENTA);
    add(CYAN, Color.CYAN);
    add(BLUE, Color.BLUE);
    add(BROWN, .45, .25, .05); //0.60, 0.40, 0.20);
    add(PURPLE, .516, .125, .94);
    add(DARKGREEN, 0.06, 0.38, 0.06);
  }

  private static Color[] getColorSet(int id) {
    Color[] set = colorsMap().get(id);
    if (set == null)
      badArg("no color with id:", id);
    return set;
  }

  private static Color[] addSet(int id) {
    if (colorsMap().containsKey(id))
      badState("duplicate color, id:", id);
    Color[] set = new Color[SHADE_LEVELS];
    colorsMap().put(id, set);
    return set;
  }

  private static Map<Integer, Color[]> sColorsMap;

}

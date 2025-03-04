package sample.match;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.IRect;
import js.geometry.Matrix;
import js.geometry.MyMath;
import js.json.JSMap;
import js.parsing.DFA;
import js.parsing.Scanner;
import testbed.Render;

import static geom.GeomTools.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import geom.gen.Node;

/**
 * Maintains set of polylines
 */
public class Dataset extends BaseObject {

  //Token Ids generated by 'dev dfa' tool (DO NOT EDIT BELOW)
  private static final int T_WS = 0;
  private static final int T_CR = 1;
  private static final int T_COMMA = 2;
  private static final int T_VALUE = 3;

  private static final int LS_WS = 0;
  private static final int LS_LINESTRING = 1;
  private static final int LS_ENDLINESTRING = 2;
  private static final int LS_NUMBER = 3;
  private static final int LS_COMMA = 4;
  // End of token Ids generated by 'dev dfa' tool (DO NOT EDIT ABOVE)

  public void prepare() {
    loadTools();

    load(new File("sample_project/nanaimo.csv"));

  }

  public void prepareRender() {
    checkState(mNodes != null, "not prepared yet");
    calcTransform();
  }

  public List<Node> nodes() {
    return mNodes;
  }

  public Matrix geomToViewTransform() {
    return mGeometryToView;
  }

  private Matrix calcTransform() {

    // Get the program's page size (the virtual canvas size)
    var pageSize = geomApp().pageSize();

    // Get the bounds of the geometry
    var gb = mGeometryBounds;
    // The scale factor makes sure all boundary appears
    var scale = Math.min(pageSize.x / gb.width, pageSize.y / gb.height);

    // Transform so 0,0 is center of geometry
    var m1 = Matrix.getTranslate(gb.midPoint().scaledBy(-1));
    pr("translate so 0,0 is center of geom:", INDENT, m1);
    // Scale from geom->view
    var m2 = Matrix.getScale(scale);
    pr("scale from geom->view:", INDENT, m2);
    // Transform back so 0,0 is top left of geometry
    pr("pageSize:", pageSize);
    var m3 = Matrix.getTranslate(new FPoint(pageSize.x / 2, pageSize.y / 2));
    pr("transform so 0,0 is top left of geom:", INDENT, m3);

    Matrix mCombined = Matrix.preMultiply(m1, m2, m3);
    pr("combined:", mCombined);

    mGeometryToView = mCombined;
    return mCombined;
  }

  private void load(File f) {
    if (f.equals(mSourceFile))
      return;
    mSourceFile = f;
    var projectDir = Files.parent(f.getAbsoluteFile());
    {
      if (mCsvDfa == null) {
        todo("!the dfas are stored in the project directory for now");
        mCsvDfa = new DFA(JSMap.from(new File(projectDir, "csv.dfa")));
        mLinestringDfa = new DFA(JSMap.from(new File(projectDir, "linestring.dfa")));
      }

      // Parse the csv file
      var s = new Scanner(mCsvDfa, Files.readString(f), T_WS);
      while (s.readIf(T_CR) != null)
        ;
      // skip first row (column headers)
      while (s.readIf(T_CR) == null) {
        s.read();
      }

      List<Node> nodes = arrayList();

      while (s.hasNext()) {
        var b = Node.newBuilder();
        b.id(parseString(s.read(T_VALUE).text()));
        s.read(T_COMMA);
        b.description(parseString(s.read(T_VALUE).text()));
        s.read(T_COMMA);
        parseGeom(b, s.read(T_VALUE).text());
        // Skip any cr's
        while (s.readIf(T_CR) != null)
          ;
        //pr("parsed:", INDENT, b);
        nodes.add(b.build());
      }

      mNodes = nodes;

      mGeometryBounds = calcBounds(nodes);
      log("read nodes:", mNodes.size());
      log("bounds:", mGeometryBounds);
    }

  }

  private FRect calcBounds(List<Node> nodes) {
    FRect bounds = null;
    for (var n : nodes) {
      for (var g : n.vertices()) {
        if (bounds == null)
          bounds = FRect.rectContainingPoints(g, g);
        bounds = bounds.including(g);
      }
    }
    return bounds;
  }

  private String parseString(String text) {
    text = stripQuotes(text);
    // For now, assume there are no escape sequences
    return text;
  }

  private String stripQuotes(String text) {
    if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\""))
      text = text.substring(1, text.length() - 1);
    return text;
  }

  private void parseGeom(Node.Builder b, String text) {
    text = stripQuotes(text);
    var s = new Scanner(mLinestringDfa, text, LS_WS);
    s.read(LS_LINESTRING);
    while (s.readIf(LS_ENDLINESTRING) == null) {
      if (!b.vertices().isEmpty()) {
        s.read(LS_COMMA);
      }
      var longit = parseNumber(s);
      var latit = parseNumber(s);
      b.vertices().add(new FPoint(longit, latit));
    }
  }

  private float parseNumber(Scanner s) {
    var t = s.read(LS_NUMBER).text();
    return Float.parseFloat(t);
  }

  private File mSourceFile;
  private List<Node> mNodes;
  private DFA mCsvDfa, mLinestringDfa;
  private FRect mGeometryBounds;
  private Matrix mGeometryToView;

}

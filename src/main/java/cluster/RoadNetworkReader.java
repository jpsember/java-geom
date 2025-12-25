package cluster;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.FPoint;
import js.geometry.Matrix;
import js.parsing.DFA;
import js.parsing.Lexer;
import cluster.gen.RoadSegment;
import cluster.gen.RoadNetwork;

import java.io.File;
import java.util.List;

import static js.base.Tools.*;
import static cluster.LineStringTokens.*;
import static cluster.MatchUtil.*;

public class RoadNetworkReader extends BaseObject {

  public RoadNetwork parse(File databaseFile) {
    try {
      checkArgument(Files.nonEmpty(databaseFile), "database file is empty");
      prepareDfas();
      return parseCsv(databaseFile);
    } catch (Throwable e) {
      pr("*** Error parsing file:", databaseFile);
      throw e;
    }
  }

  private String mGeomColumnName;

  public void setGeomColumnName(String name) {
    mGeomColumnName = name;
  }

  private RoadNetwork parseCsv(File databaseFile) {
    var p = new CsvReader();
    p.parse(databaseFile);

    checkArgument(mGeomColumnName != null, "no geometry column name specified");
    var geomColumn = p.findColumn(mGeomColumnName);
    checkArgument(geomColumn >= 0, "can't find geometry column with name 'geom'");

    List<RoadSegment> nodeBuffer = arrayList();

    for (var row : p.rows()) {
      parseGeometry(row.get(geomColumn), nodeBuffer);
    }

    checkArgument(!nodeBuffer.isEmpty(), "no geometry found");
    var out = RoadNetwork.newBuilder();
    out.roadSegments(nodeBuffer);
    var bnds = determineBounds(out);

    pr(determineExtremalPoints(out));
    // Transform all the points

    FPoint scl = new FPoint(GEO_TO_PIXEL_SCALE_FACTOR, GEO_TO_PIXEL_SCALE_FACTOR);
    FPoint translate = bnds.midPoint().negate();
    var tfm = Matrix.preMultiply(Matrix.getTranslate(translate), Matrix.getScale(scl.x, scl.y));
    var i = INIT_INDEX;
    for (var pt : out.roadSegments()) {
      i++;
      var b = pt.toBuilder();
      b.a(tfm.apply(b.a()));
      b.b(tfm.apply(b.b()));
      out.roadSegments().set(i, b.build());
    }
    out.translate(translate);
    out.scale(scl);

//    pr("after transform:",CR,determineExtremalPoints(out));

    return out;
  }


  private static void parseGeometry(String text, List<RoadSegment> target) {
    prepareDfas();
    text = stripQuotes(text);
    var s = new Lexer(sLinestringDfa).withSkipId(LS_WS).withText(text);
    s.read(LS_LINESTRING);

    FPoint prevPt = null;
    while (!s.readIf(LS_ENDLINESTRING)) {
      s.readIf(LS_COMMA);
      var longit = parseNumber(s);
      var latit = parseNumber(s);
      var newPt = new FPoint(longit, latit);
      if (prevPt != null) {
        target.add(RoadSegment.newBuilder().a(prevPt).b(newPt).build());
      }
      prevPt = newPt;
    }
  }

  private static float parseNumber(Lexer s) {
    var t = s.read(LS_NUMBER).text();
    return Float.parseFloat(t);
  }

  // ------------------------------------------------------------------
  // DFA
  // ------------------------------------------------------------------

  private static void prepareDfas() {
    if (sLinestringDfa == null) {
      sLinestringDfa = DFA.parse(
          "{\"graph\":[0,9,1,80,1,97,1,1,77,1,-20,0,1,76,1,-97,0,1,49,9,-116,0,1,48,1,77,0,1,45,1,65,0,1,44,1,63,0,1,41,1,54,0,1,32,1,47,0,1,1,1,32,1,47,0,3,1,1,41,1,61,0,3,0,5,0,0,2,1,49,9,-116,0,1,48,1,77,0,4,2,2,69,1,101,1,112,0,1,46,1,91,0,0,1,1,48,10,98,0,4,2,2,69,1,101,1,112,0,1,48,10,98,0,0,2,1,48,10,-123,0,2,43,1,45,1,126,0,0,1,1,48,10,-123,0,4,1,1,48,10,-123,0,4,3,1,48,10,-116,0,2,69,1,101,1,112,0,1,46,1,91,0,0,1,1,73,1,-90,0,0,1,1,78,1,-83,0,0,1,1,69,1,-76,0,0,1,1,83,1,-69,0,0,1,1,84,1,-62,0,0,1,1,82,1,-55,0,0,1,1,73,1,-48,0,0,1,1,78,1,-41,0,0,1,1,71,1,-34,0,0,2,1,32,1,-34,0,1,40,1,-22,0,2,0,0,1,1,85,1,-13,0,0,1,1,76,1,-6,0,0,1,1,84,1,1,1,0,1,1,73,1,8,1,0,1,1,76,1,15,1,0,1,1,73,1,22,1,0,1,1,78,1,29,1,0,1,1,69,1,36,1,0,1,1,83,1,43,1,0,1,1,84,1,50,1,0,1,1,82,1,57,1,0,1,1,73,1,64,1,0,1,1,78,1,71,1,0,1,1,71,1,78,1,0,2,1,32,1,78,1,1,40,1,90,1,0,1,1,40,1,-22,0,0,1,1,79,1,104,1,0,1,1,73,1,111,1,0,1,1,78,1,118,1,0,1,1,84,1,-34,0],\"token_names\":\"WS LINESTRING ENDLINESTRING NUMBER COMMA\",\"version\":\"$2\"}"
      );
    }
  }

  private static DFA sLinestringDfa;

}

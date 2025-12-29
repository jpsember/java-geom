package cluster;

import js.base.Pair;
import js.file.DirWalk;
import js.file.Files;
import js.geometry.*;
import js.json.JSMap;
import js.parsing.RegExp;
import cluster.gen.RoadSegment;
import cluster.gen.RoadNetwork;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static js.base.Tools.*;

public final class MatchUtil {

  public static final boolean ISSUE_12 = false && alert("Issue 12 is in effect");
  public static final boolean ISSUE_13 = false && alert("Issue 13 (segment_matching inspection) is in effect");

  public static final boolean ISSUE_RUST = false && alert("ISSUE_RUST is in effect");
//  public static final Comparator<? super Node> NODE_COMPARATOR = new Comparator<Node>() {
//    @Override
//    public int compare(Node o1, Node o2) {
//      int diff = Integer.compare(o1.vertices().size(), o2.vertices().size());
//      if (diff != 0) return diff;
//      for (int i = 0; i < o1.vertices().size(); i++) {
//        var vi = o1.vertices().get(i);
//        var vj = o2.vertices().get(i);
//        diff = Float.compare(vi.x, vj.x);
//        if (diff != 0) return diff;
//        diff = Float.compare(vi.y, vj.y);
//        if (diff != 0) return diff;
//      }
//      return 0;
//    }
//  };

  public static void ru(Object... messages) {
    if (ISSUE_RUST)
      pc(insertStringToFront(">>", messages));
  }

  public static void rf(Object... messages) {
    if (ISSUE_RUST)
      pr(insertStringToFront(">>", messages));
  }

  public static void p13(Object... messages) {
    if (!ISSUE_13) return;
    pr(insertStringToFront("ISSUE_13--->", messages));
  }

  // This is the scale factor to convert longitude/latitude values to world 'pixels'
  //
  public static final float GEO_TO_PIXEL_SCALE_FACTOR = 30_000;

//  public static Node polygonToNode(Polygon polygon) {
//    var b = Node.newBuilder();
//    b.vertices(arrayList());
//    for (var ipt : polygon.vertices()) {
//      b.vertices().add(ipt.toFPoint());
//    }
//    return b.build();
//  }

//  public static Polygon nodeToPolygon(Node n) {
//    List<IPoint> ipts = arrayList();
//    for (var fpt : n.vertices()) {
//      ipts.add(fpt.toIPoint());
//    }
//    return new Polygon(ipts, true);
//  }


  //  public static Node polygonToNode(IPoint[] vertices) {
//    var n = Node.newBuilder();
//    n.vertices(arrayList());
//    for (var pt : vertices)
//      n.vertices().add(pt.toFPoint());
//    return n.build();
//  }
//
  public static FRect bounds(RoadSegment n) {
    return FRect.rectContainingPoints(n.a(), n.b());
  }

  public static FRect calcBounds(Collection<RoadSegment> nodes) {
    List<FPoint> points = collectPoints(nodes);
//
////    FRect b = null;
//    for (var nn : nodes) {
//      points.add(nn.a());
//      points.add(nn.b());
////      FRect b2 = bounds(nn);
////      if (b == null)
////        b = b2;
////      else b = b.including(b2);
//    }
    return FRect.rectContainingPoints(points);
  }

  public static FRect determineBounds(RoadNetwork.Builder input) {
    var nodes = input.roadSegments();
    checkArgument(!nodes.isEmpty());
//    if (nodes.isEmpty()) return;

    var b = calcBounds(nodes);
    input.origin(b.location());
    input.size(b.size());
    return b;
  }


  public static JSMap determineExtremalPoints(RoadNetwork nw) {
    FPoint xmin = null;
    FPoint xmax = null;
    FPoint ymin = null;
    FPoint ymax = null;

    for (var seg : nw.roadSegments()) {
      for (int pass = 0; pass < 2; pass++) {
        var pt = pass == 0 ? seg.a() : seg.b();
        if (xmin == null) {
          xmin = pt;
          xmax = pt;
          ymin = pt;
          ymax = pt;
        }
        if (xmin.x > pt.x)
          xmin = pt;
        if (xmax.x < pt.x)
          xmax = pt;


        if (ymin.y > pt.y)
          ymin = pt;
        if (ymax.y < pt.y)
          ymax = pt;
      }
    }

    return map().put("", "Extremal points").putNumbered("# segs", nw.roadSegments().size()).putNumbered("x min", xmin).putNumbered("x max", xmax)
        .putNumbered("y min", ymin).putNumbered("y max", ymax);
  }


  public static FPoint normalizeGeoLoc(FPoint longLat, RoadNetwork nodeSet) {
    return new FPoint((longLat.x - nodeSet.origin().x) * GEO_TO_PIXEL_SCALE_FACTOR,
        (longLat.y - nodeSet.origin().y) * GEO_TO_PIXEL_SCALE_FACTOR);
  }

  public static RoadNetwork normalize(RoadNetwork input) {

    todo("avoid normalizing, we are already dealing with pixels at this point");
    var out = input.build().toBuilder();

    determineBounds(out);

    var i = INIT_INDEX;
    for (var n : out.roadSegments()) {
      i++;
      var b = n.toBuilder();
      b.a(normalizeGeoLoc(b.a(), out));
      b.b(normalizeGeoLoc(b.b(), out));
      out.roadSegments().set(i, b.build());
    }


    // Scale the size up as well
    out.size(input.size().scaledBy(GEO_TO_PIXEL_SCALE_FACTOR));
    return out.build();
  }

//  public static NodeSet applyPolylineSimplify(NodeSet in, int mPolylineSimplify) {
//    var out = in.build().toBuilder();
//
//    if (mPolylineSimplify > 0) {
//      out.nodes().clear();
//      // Apply simplification
//      for (var n : in.nodes()) {
//        var b = n.toBuilder();
//        applySimplification(b, mPolylineSimplify);
//        out.nodes().add(b.build());
//      }
//    }
//    return out.build();
//  }


//  private static void applySimplification(Node.Builder b, int factor) {
//    if (b.vertices().size() < 3)
//      return;
//    if (factor != 0) {
//      var p = Polygon.fromVertices(b.vertices(), true);
//      var s2 = p.simplify(factor / 2f);
//      b.vertices(intToFloat(s2.vertices()));
//    }
//  }

  private static List<FPoint> intToFloat(IPoint[] input) {
    List<FPoint> out = arrayList();
    for (var x : input)
      out.add(x.toFPoint());
    return out;
  }

  public static JSMap debugInfo(RoadNetwork s) {
    var s2 = s.build().toBuilder();
    var count = s2.roadSegments().size();
    s2.roadSegments().clear();
    var m = s2.toJson();
    m.put("nodes", count);
    return m;
  }

//  public static FRect expandBounds(FRect boundsOrNull, Node node) {
//    var nb = bounds(node);
//    if (boundsOrNull == null) {
//      return nb;
//    }
//    return boundsOrNull.including(nb);
//  }

//  public static FRect expandBounds(FRect bounds, Collection<Node> nodes) {
//    for (var n : nodes) {
//      bounds = expandBounds(bounds, n);
//    }
//    return bounds;
//  }

  public static List<RoadSegment> transformNodes(Collection<RoadSegment> input, Matrix tfm) {
    List<RoadSegment> out = arrayList();
    for (var n : input) {
      out.add(transformNode(n, tfm));
    }
    return out;
  }

  public static RoadSegment transformNode(RoadSegment input, Matrix tfm) {
    var b = input.toBuilder();
    b.a(tfm.apply(b.a()));
    b.b(tfm.apply(b.b()));
//    List<FPoint> pts = arrayList();
//    for (var pt : input.vertices()) {
//      pts.add(tfm.apply(pt));
//    }
//    b.vertices(pts);
    return b.build();
  }

//  public static NodeSet filterInvalid(NodeSet input) {
//    var out = input.build().toBuilder();
//    out.nodes().clear();
//    for (var n : input.nodes()) {
//      if (n.vertices().size() < 2) {
//        continue;
//      }
//      out.nodes().add(n);
//    }
//    return out.build();
//  }

  public static List<FPoint> collectPoints(Collection<RoadSegment> nodes) {
    List<FPoint> out = arrayList();
    for (var n : nodes) {
      out.add(n.a());
      out.add(n.b());
//      out.addAll(n.vertices());
    }
    return out;
  }

  public static Pair<Matrix, FRect> calcPixelToAlignedWorldSpaceTransform(Collection<RoadSegment> nodes) {
    List<FPoint> pts = collectPoints(nodes);
    checkArgument(nonEmpty(pts), "no points found");
    var pixBounds = FRect.rectContainingPoints(pts);
    // Add some padding on the right and bottom in case the bounds are degenerate.
    pixBounds = new FRect(pixBounds.x, pixBounds.y, Math.max(10, pixBounds.width),
        Math.max(10, pixBounds.height));

    checkArgument(pixBounds.width != 0 && pixBounds.height != 0, "bounding rect is degenerate for:", nodes);

    var m = Matrix.preMultiply(Matrix.getTranslate(pixBounds.location().negate()),
        Matrix.getScale(1f / GEO_TO_PIXEL_SCALE_FACTOR));

    return pair(m, pixBounds);
  }

  public static String abbrev(String s) {
    int maxLen = 6;
    if (s.length() >= maxLen)
      return s.substring(0, maxLen);
    return s;
  }

//  public static Node assertValid(Node node, String message) {
//    if (!valid(node))
//      badArg("node has too few vertices (" + message + ")", node);
//    return node;
//  }

//  public static boolean valid(Node node) {
//    return node.vertices().size() >= 2;
//  }

  private static Pattern TRIM_PAT = RegExp.pattern("\\d+\\.\\d+");

  public static String trimDoubles(String txt) {
    var m = TRIM_PAT.matcher(txt);
    StringBuilder sb = new StringBuilder();
    int cursor = 0;
    while (cursor < txt.length()) {
      var r = m.find(cursor);
      if (r) {
        var s = m.start();
        var e = m.end();
        sb.append(txt.substring(cursor, s));
        var subs = txt.substring(s, e);
        int q = subs.indexOf('.');
        int maxQ = Math.min(q + 6, subs.length());
        sb.append(subs.substring(0, maxQ));
        cursor = e;
      } else {
        sb.append(txt.substring(cursor));
        break;
      }
    }
    if (sb.length() == txt.length())
      return txt;
    var result = sb.toString();
    return result;
  }

  public static String parseString(String text) {
    checkArgument(!text.startsWith("\""), "quotes ought to be removed before this:", text);
    text = stripQuotes(text);
    // For now, assume there are no escape sequences
    return text;
  }

  public static String stripQuotes(String text) {
    if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\""))
      text = text.substring(1, text.length() - 1);
    return text;
  }

  /**
   * Delete files within a directory that have particular names ("name") or
   * extensions (":ext")
   */
  public static void cleanDirectory(File dir, String... namesOrExtensions) {
    if (!dir.exists())
      return;
    List<String> exts = arrayList();
    List<String> names = arrayList();
    for (var expr : namesOrExtensions) {
      var justExt = chompPrefix(expr, ":");
      if (justExt != expr)
        exts.add(justExt);
      else
        names.add(expr);
    }
    var dw = new DirWalk(dir).withRecurse(true); // We want to clean any subdirectories as well
    for (var f : dw.files()) {
      var nm = f.getName();
      var ext = Files.getExtension(nm);
      if (exts.contains(ext) || names.contains(nm)) {
        Files.S.deleteFile(f);
      }
    }
  }

  public static String pct(double a, double b) {
    if (b == 0.0) return "div0";
    return String.format("%.1f", (a / b) * 100);
  }


//  /**
//   * Write NodeSet to csv file
//   */
//  public static void writeToCsv(List<Node> nodes, List<String> columnNames, File destFile) {
//    checkArgument(Files.getExtension(destFile).equals(Files.EXT_CSV), "expected csv extension for:", destFile);
//    p13("writeToCsv, destFile:", destFile);
//
//    var w = new CsvWriter();
//    for (var cn : columnNames)
//      w.addColumn(cn);
//    w.doneColumns();
//
//    for (var n : nodes) {
//      for (var c : n.originalColumnContents()) {
//        w.add(c);
//      }
//      w.doneRow();
//    }
//    Files.S.writeString(destFile, w.close());
//  }


//  public static String generateGeometry(Node node) {
//    checkArgument(node.vertices().size() >= 2, "too few vertices");
//    var sb = new StringBuilder();
//    // "LINESTRING(-123.99787629392873 49.211193070827704,-123.99849595871265 49.211889715843995,-123.99849595871265 49.21315118318739)"
//    sb.append("LINESTRING(");
//
//    var i = INIT_INDEX;
//    for (var v : node.vertices()) {
//      i++;
//      if (i != 0) {
//        sb.append(',');
//      }
//      sb.append(v.x);
//      sb.append(' ');
//      sb.append(v.y);
//    }
//    sb.append(")");
//    return sb.toString();
//  }


  /**
   * Determine if a line segment (u...v) intersects box b
   */
  public static boolean segmentIntersectsBox(float bx, float by, float bw, float bh, float ux, float uy, float vx, float vy) {

    var dx = vx - ux;
    var dy = vy - uy;
    var dxs = dx * dx;
    var dys = dy * dy;

    var sqLength = dxs + dys;
    var EPS = 1e-7;
    var isPoint = sqLength < EPS * EPS;
    boolean isect;


    if (isPoint) {
      isect = ux >= bx && ux <= bx + bw && uy >= by && uy <= by + bh;
    } else {

      // See https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
      // the section "Given two points on each line segment"

      float x2, x3, y3, x4, y4, y3b, y4b, t, u;
      final var y1 = 0;
      final var y2 = 0;

      if (dxs <= dys) {
        // The (abs) slope of the segment is >= 1; see if segment intersects the horizontal edges of the box

        // transform to the box's origin by subtracting bx, by

        x2 = bw; // i.e. (bx + bw) - bx
        //
        x3 = ux - bx;
        y3 = uy - by;
        //
        x4 = vx - bx;
        y4 = vy - by;
        //
        // For the top edge, transform to the top left point (bx, by+bh)
        y3b = uy - (by + bh);
        y4b = vy - (by + bh);
      } else {
        // The (abs) slope of the segment is < 1; see if segment intersects the vertical edges of the box,
        // by flipping box and segment around the x=y axis
        x2 = bh;
        x3 = uy - by;
        y3 = ux - bx;
        x4 = vy - by;
        y4 = vx - bx;

        y3b = ux - (bx + bw);
        y4b = vx - (bx + bw);
      }

      t = ((  - x3) * (y3 - y4) - (  - y3) * (x3 - x4)) //
          /    //---------------------------------------------
          ((  - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

      // There's a friggin negative sign here
      u = -((  - x2) * (  - y3) - (y1 - y2) * (  - x3))   //
          /    //---------------------------------------------
          ((  - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4));

      isect = (t >= 0 && t <= 1) && (u >= 0 && u <= 1);

      if (!isect) {

        // Check the opposite side of the box

        var tb = ((  - x3) * (y3b - y4b) - (  - y3b) * (x3 - x4))  //
            /    //---------------------------------------------
            (( - x2) * (y3b - y4b) - (y1 - y2) * (x3 - x4));

        var ub = -((  - x2) * ( - y3b) - (y1 - y2) * (  - x3)) //
            /    //---------------------------------------------
            ((  - x2) * (y3b - y4b) - (y1 - y2) * (x3 - x4));

        isect = tb >= 0 && tb <= 1 && ub >= 0 && ub <= 1;
      }
    }
    return isect;
  }


}

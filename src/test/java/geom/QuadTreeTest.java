package geom;

import cluster.PointSet;
import cluster.QuadTree;
import cluster.gen.QtreeParam;
import js.base.Pair;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.geometry.MyMath;
import js.json.JSList;
import js.testutil.MyTestCase;
import org.junit.Test;

import java.util.List;

import static js.base.Tools.*;

public class QuadTreeTest extends MyTestCase {


  @Test
  public void segint() {
    isect(100, 20, -10, 40, 12);
  }

  @Test
  public void segint2() {
    isect(100, 50 - 2 * 10, 0 - 3 * 10, 50 + 4 * 10, 0 + 6 * 10);
  }

  @Test
  public void segBoxInt() {
    var bx = 40;
    var by = 20;
    var bw = 80;
    var bh = 60;

    int[] segs = {
        // No
        0, 0, 50, 0, //
        0, 0, 0, 50, //
        40, 0, 40, 19, //

        99, 99, 99, 99,
        // Yes
        40, 0, 40, 20, //
        30, 15, 50, 25, //
        20, 30, 40, 30, //
        20, 30, 60, 30, //
        100, 19, 130, 21,

    };

    var m = map();
    boolean expected = false;

//    m.putNumbered("NO");
    boolean problem = false;
    for (int i = 0; i < segs.length; i += 4) {
      var ux = segs[i];
      var uy = segs[i + 1];
      var vx = segs[i + 2];
      var vy = segs[i + 3];
      if (ux == 99) {
        expected = true;
        continue;
      }
      var r = segIsectBox(bx, by, bw, bh, ux, uy, vx, vy);
      var correct = r == expected;
      if (!correct) problem = true;
      m.putNumbered(list().add(ux).add(uy).add(vx).add(vy).add(correct ? (expected ? "Y" : "N") : "****"));
    }
    assertMessage(m);
    checkState(!problem);
  }

  private void isect(float x2, float x3, float y3, float x4, float y4) {
    var t = ((-x3) * (y3 - y4) - (-y3) * (x3 - x4)) / (-x2 * (y3 - y4));
    var m = map();
    m.put("x2", x2).put("x3", x3).put("y3", y3).put("y4", y4);
    m.put("t", t);
    var xi = t * x2;
    m.put("xi", xi);

    float[] param = new float[2];
    MyMath.linesIntersection(0, 0, x2, 0, x3, y3, x4, y4, param);
    m.put("tm", param[0]);

    assertMessage(m);
  }

  private static boolean segIsectBox(float bx, float by, float bw, float bh, float ux, float uy, float vx, float vy) {

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

      float x2, x3, y3, x4, y4, y3b, y4b, t0, t1;
      if (dxs <= dys) {
        // The (abs) slope of the segment is >= 1; see if segment intersects the horizontal edges of the box
        x2 = bw;
        x3 = ux - bx;
        x4 = vx - bx;
        y3 = uy - by;
        y4 = vy - by;
        y3b = uy - (by + bh);
        y4b = vy - (by + bh);
      } else {
        // The (abs) slope of the segment is < 1; see if segment intersects the vertical edges of the box,
        // by flipping box and segment around the x=y axis
        x2 = bh;
        x3 = uy - by;
        x4 = vy - by;
        y3 = ux - bx;
        y4 = vx - bx;

        y3b = ux - (bx + bw);
        y4b = vx - (bx + bw);
      }
      t0 = ((-x3) * (y3 - y4) - (-y3) * (x3 - x4)) / (-x2 * (y3 - y4));

      isect = t0 >= 0 && t0 <= 1;
      if (!isect) {
        var t0b = ((-x3) * (y3b - y4b) - (-y3b) * (x3 - x4)) / (-x2 * (y3b - y4b));
        isect = t0b >= 0 && t0 <= 1;
      }
    }
    return isect;
  }

  /// /    float x3, float y3, float x4, float y4) {
//      var t = ((-x3) * (y3 - y4) - (-y3) * (x3 - x4)) / (-x2 * (y3 - y4));
//      var m = map();
//      m.put("x2", x2).put("x3", x3).put("y3", y3).put("y4", y4);
//      m.put("t", t);
//      var xi = t * x2;
//      m.put("xi", xi);
  @Test
  public void seg1() {
    genSegments(1);
    genOut();
  }

  @Test
  public void seg1_no_rewrite() {
    param().disableRewrite(true);
    genSegments(1);
    genOut();
  }

  @Test
  public void seg4() {
    genSegments(4);
    genOut();
  }

  @Test
  public void zeroSegments() {
    genSegments(0);
    genOut();
  }

  @Test
  public void zeroLengthSegments() {
    addSeg(20, 30, 20, 30);
    addSeg(60, 70, 60, 70);
    genOut();
  }

  @Test
  public void manyCopiesSameSeg2() {
    skipQueries();
    int count = 20;
    for (int i = 0; i < count; i++)
      addSeg(20, 30, 20 + 6, 30 + 2);
    for (int i = 0; i < count; i++)
      addSeg(60, 70, 60 + 2, 70 + 3);
    genOut();
  }

  @Test
  public void manyCopies() {
    skipQueries();
    int count = 10;
    for (int i = 0; i < count; i++)
      addSeg(20, 30, 20 + 10, 30 - 2);
    genOut();
  }

  @Test
  public void manyCopiesSameSeg() {
    skipQueries();
    int count = 20;
    for (int i = 0; i < count; i++)
      addSeg(20, 30, 20 + 10, 30 - 2);
    for (int i = 0; i < count; i++)
      addSeg(60, 70, 60 + 2, 70 + 9);
    genOut();
  }

  @Test
  public void manyCopiesSameSegSmallDim() {
    param()
        .minNodeDimension(1e-1f);
    manyCopiesSameSeg();
  }

  @Test
  public void seg10() {
    genSegments(10);
    genOut();
  }


  @Test
  public void seg100() {
    genSegments(100);
    genOut();
  }

  private void genOut() {
    var t = genTree();

    var m = map();

    m.put("info", t.auxInfo());
    //m.put("tree", t.toJson());

    if (!mSkipQueries)
      for (var seg : mSegments) {
        var p0 = seg.first;
        var p1 = seg.second;
        var b = FRect.rectContainingPoints(p0, p1);
        var m2 = map();
        m2.put("bounds", b.toJson());
        var result = t.findSegments(b);
        checkArgument(result.length != 0, "result was empty, should have contained at least the segment", p0, p1);
        m2.put("result", JSList.with(result));
        m.putNumbered(m2);
      }

    assertMessage(m.prettyPrint());
  }

  private void genSegments(int count) {
    var clip = new FRect(new FPoint(100, 100));

    var target = mSegments.size() + count;
    while (mSegments.size() < target) {
      var p0 = new FPoint(10f + random().nextFloat() * 90f, 10f + random().nextFloat() * 90f);
      p0 = p0.toIPoint().toFPoint();
      var wedge = 30;
      var dir = random().nextInt((int) (360.0 / wedge)) * wedge * MyMath.M_DEG;
      var p1 = MyMath.pointOnCircle(p0, dir, 75f * random().nextFloat() * random().nextFloat());
      p1 = p1.toIPoint().toFPoint();
      var bnd = FRect.rectContainingPoints(p0, p1);
      if (!clip.contains(bnd))
        continue;
      addSeg(p0, p1);
    }
  }

  private void addSeg(double x0, double y0, double x1, double y1) {
    addSeg(new FPoint(x0, y0), new FPoint(x1, y1));
  }

  private void addSeg(FPoint p0, FPoint p1) {
    mSegments.add(pair(p0, p1));
  }

  private QuadTree genTree() {
    if (mTree == null) {
      mTree = new QuadTree(param(), pointSet(), segmentEndpoints());
      if (verbose()) mTree.setVerbose();
      mParam = null;
      mTree.prepare();
    }
    return mTree;
  }


  private int[] segmentEndpoints() {
    pointSet();
    return mSegmentIds;
  }

  private PointSet pointSet() {
    if (mPointSet == null) {
      var ps = new PointSet();
      var ids = IntArray.newBuilder();
      for (var seg : mSegments) {
        var pt = seg.first;
        var id = ps.add(pt);
        ids.add(id);

        pt = seg.second;
        id = ps.add(pt);
        ids.add(id);
      }
      mPointSet = ps.freeze();
      mSegmentIds = ids.array();
    }
    return mPointSet;
  }

  private QtreeParam.Builder param() {
    return checkNotNull(mParam, "params no longer available");
  }

  private void skipQueries() {
    mSkipQueries = true;
  }

  private List<Pair<FPoint, FPoint>> mSegments = arrayList();
  private QtreeParam.Builder mParam = QtreeParam.newBuilder().minNodeDimension(1f);
  private int[] mSegmentIds = null;
  private PointSet mPointSet;
  private QuadTree mTree;
  private boolean mSkipQueries;
}

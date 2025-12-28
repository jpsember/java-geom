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
  public void wtf() {
    // Investigating floating point stability problem (subtracting floats with small values produces zero)
    float y = 28.0f;
    float h = 1.9073486E-6f;

    var midy = y + h * .5f;
    pr("midy:" + midy, "h: " + h, "top:" + (midy - y), "bot:" + (y + h - midy));
  }


  @Test
  public void seg1() {
    genSegments(1);
    genOut();
  }

  @Test
  public void seg1_no_rewrite() {
    param().disableFruitlessRewrite(true);
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

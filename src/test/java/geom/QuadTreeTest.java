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
import js.json.JSMap;
import js.testutil.MyTestCase;
import org.junit.Test;

import java.util.List;

import static cluster.MatchUtil.*;
import static js.base.Tools.*;

public class QuadTreeTest extends MyTestCase {


  @Test
  public void segmentBoxIntersection() {
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
        90, 19, 122, 24,

    };
    auxSegBox(40, 20, 80, 60, segs);
  }


  /**
   * Given a box and a set of segments, perform the segment/box intersection algorithm,
   * and generate a json summary of the results
   *
   * Segs is an array of 4n integers, where each 4 are the segment x1,y1,x2,y2.
   * It assumes the first bunch of segments do *not* intersect the box.
   *
   * The special value 99,99,99,99 indicates that the following segments *do* intersect
   * the box.
   */
  private void auxSegBox(int bx, int by, int bw, int bh, int[] segs) {

    var m = map();
    boolean expectedIsectResult = false;

    boolean problem = false; // true if any mismatch occurred

    for (int i = 0; i < segs.length; i += 4) {
      var ux = segs[i];
      if (ux == 99) {
        expectedIsectResult = true;
        continue;
      }

      var uy = segs[i + 1];
      var vx = segs[i + 2];
      var vy = segs[i + 3];

      var r = segmentIntersectsBox(bx, by, bw, bh, ux, uy, vx, vy);
      var correct = r == expectedIsectResult;
      if (!correct) problem = true;
      m.putNumbered(list().add(ux).add(uy).add(vx).add(vy).add(correct ? (expectedIsectResult ? "Y" : "N") : "****"));
    }
    assertMessage(m);
    checkState(!problem);
  }


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



  private void auxSer( ) {
    var name =  name();
    int j = 0;
    while (!Character.isDigit(name.charAt(j) ))j++;
    int numSegs = Integer.parseInt(name.substring(j));

    for (int i = 0; i<500; i++) {
      int seed = mStartSeed + i;
      pr(VERT_SP,"seed:",seed);
      reset();
      resetSeed(seed);
      ser(""+i, numSegs);
      pr(VERT_SP,"seed:",seed,VERT_SP);
    }
    assertGenerated();
  }


  @Test
  public void serialize1() {
    startSeed(119);
    auxSer( );
  }

  @Test
  public void serialize2() {
    auxSer( );
  }
  @Test
  public void serialize3() {
    auxSer();
  }


  @Test
  public void serialize4() {rv();
    auxSer();
  }

  @Test
  public void serialize5() {
    startSeed(119);
    auxSer();
  }

  @Test
  public void serialize8() {
    auxSer();
  }

  @Test
  public void serialize20() {
    auxSer();
  }

  @Test
  public void serialize100() {
    auxSer();
  }


  @Test
  public void wtf() {
    resetSeed(5000);
    genSegments(5);
    genOut();
  }

  @Test
  public void wtf2() {
    FRect a = new FRect(18, 32, 33, 43);
    FRect b = new FRect(51, 32, 33, 43);
    var p1 = new FPoint(18, 52);
    var p2 = new FPoint(21, 47);
    var ia = segmentIntersectsBox(a, p1, p2);
    checkState(ia);
//    bounds:
//    (   18         32         66         43      )
//    seg:     18.0000     52.0000     21.0000     47.0000
//    left: (   18         32         33         43      )
//    rigt: (   51         32         33         43      )
//    (wtf:) ------------ tearDown
  }

//  @Test
//  public void segr() {
//    int i = 5000;
//    for (int j=0; j<100; j++,i++) {
//

  /// /      mParam = QtreeParam.newBuilder().minNodeDimension(1f);
  /// /      mSegments.clear();
  /// /      mSegmentIds = null;
  /// /      mPointSet = null;
  /// /      mTree = null;
  /// /      mSkipQueries = false;
//
//      pr("seed:",i);
//      resetSeed(i);
//      genSegments(5);
//      genOut();
//      break;
//    }
//  }
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
      performQueries(t, m);

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
      log("adding seg:",p0,"==>",p1);
      addSeg(p0, p1);
    }
  }

  private void ser(String prefix, int count) {
    genSegments(count);

    var t = genTree();
    pr(DASHES,VERT_SP,"Attempting to serialize:",INDENT,t);

    var m1 = t.serialize();
    log("serialized:", INDENT, m1);
    var m2 = map();
    performQueries(t, m2);
    var t2 = QuadTree.deserialize(m1);
    pr(DASHES,VERT_SP,"Deserialized:",INDENT,t2,VERT_SP);
    var m3 = map();
    performQueries(t2, m3);
    var m4 = map();
    m4.put("A orig", m2);
    m4.put("B deser", m3);

    generateMessage("res_"+prefix+"_.json", m4.prettyPrint());
    checkState(m2.equals(m3));
  }

  private void performQueries(QuadTree t, JSMap m) {
    for (var seg : mSegments) {
      var p0 = seg.first;
      var p1 = seg.second;
      var b = FRect.rectContainingPoints(p0, p1);
      pr("QUERYING bounds:",b);
      var m2 = map();
      m2.put("bounds", b.toJson());
      var result = t.findSegments(b);
      checkArgument(result.length != 0, "result was empty, should have contained at least the segment", p0, p1);
      m2.put("result", JSList.with(result));
      m.putNumbered(m2);
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

  private void reset() {
    mSegments.clear();
    mSegmentIds = null;
    mPointSet = null;
    mTree = null;
    mParam = QtreeParam.newBuilder().minNodeDimension(1f);
  }

  private void startSeed(int n) {
    mStartSeed = n;
  }
  private List<Pair<FPoint, FPoint>> mSegments = arrayList();
  private QtreeParam.Builder mParam = QtreeParam.newBuilder().minNodeDimension(1f);
  private int[] mSegmentIds = null;
  private PointSet mPointSet;
  private QuadTree mTree;
  private boolean mSkipQueries;
  private int mStartSeed = 100;
}

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
  public void seg1() {
    genSegments(1);
    genOut();
  }

  @Test
  public void seg4() {
    genSegments(4);
    genTree();
    genOut();
  }

  private void genOut() {

    var m = map();
    for (var seg : mSegments) {
      var p0 = seg.first;
      var p1 = seg.second;
      var b = FRect.rectContainingPoints(p0, p1);
      var m2 = map();
      m2.put("bounds", b.toJson());
      var result = genTree().findCandidates(b);
      if (!alert("reenable this check"))
        checkArgument(result.length != 0, "result was empty, should have contained at least the segment", p0, p1);
      m2.put("result", JSList.with(result));
      m.putNumbered(m2);
    }
    assertMessage(m.prettyPrint());
  }

  private void genSegments(int count) {
    int n = 0;
    var clip = new FRect(new FPoint(100, 100));

    var target = mSegments.size() + count;
    while (mSegments.size() < target) {
      var p0 = new FPoint(10f + random().nextFloat() * 90f, 10f + random().nextFloat() * 90f);
      p0 = p0.toIPoint().toFPoint();
      var dir = random().nextInt(4) * 45 * MyMath.M_DEG;
      var p1 = MyMath.pointOnCircle(p0, dir, 75f * random().nextFloat() * random().nextFloat());
      p1 = p1.toIPoint().toFPoint();
      var bnd = FRect.rectContainingPoints(p0, p1);
      if (!clip.contains(bnd))
        continue;
      mSegments.add(pair(p0, p1));
    }
  }

  private QuadTree genTree() {
    if (mTree == null)
      mTree = new QuadTree(mParam, pointSet(), segmentEndpoints());
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

  private List<Pair<FPoint, FPoint>> mSegments = arrayList();
  private QtreeParam.Builder mParam = QtreeParam.newBuilder();
  private int[] mSegmentIds = null;
  private PointSet mPointSet;
  private QuadTree mTree;
}

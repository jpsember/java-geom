package cluster;

import js.base.BaseObject;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.RoadNetwork;
import cluster.gen.QtreeParam;

import java.util.List;

import static js.base.Tools.*;

public class QuadTree extends BaseObject {
  private static final boolean db = false && alert("Verbose is on in QuadTree");


//  public RoadSegment worldToPixelSpace(RoadSegment node) {
//    var b = node.build().toBuilder();
////    List<FPoint> pixelVertices = arrayList();
////    for (var worldPt : b.vertices()) {
//    {
//      var worldPt = b.a();
//      var np = new FPoint((worldPt.x - mOrigin.x) * GEO_TO_PIXEL_SCALE_FACTOR,
//          (worldPt.y - mOrigin.y) * GEO_TO_PIXEL_SCALE_FACTOR);
//      b.a(np);
//    }
//    {
//      var worldPt = b.b();
//      var np = new FPoint((worldPt.x - mOrigin.x) * GEO_TO_PIXEL_SCALE_FACTOR,
//          (worldPt.y - mOrigin.y) * GEO_TO_PIXEL_SCALE_FACTOR);
//      b.b(np);
//
//    }
//
////      pixelVertices.add(np);
////    }
////    b.vertices(pixelVertices);
//    return b.build();
//  }

  public QuadTree(RoadNetwork nodeSet, QtreeParam paramOrNull) {
    checkArgument(nodeSet.roadSegments().size() != 0, "node set is empty");
    mParam = nullTo(paramOrNull, QtreeParam.DEFAULT_INSTANCE).build();
    mPointSet = new PointSet();

//    // Determine bounds of all the points
//    List<FPoint> pts = arrayList();
//    for (var rs : nodeSet.roadSegments()) {
//      pts.add(rs.a());
//      pts.add(rs.b());
//      }
//    mRootBounds =
//    FRect.rectContainingPoints(pts);

    // Add all points to the point set, and to the quad tree

    mRoot = new QNode();
    var seg =  IntArray.newBuilder();
    mRoot.mSegments = seg;
todo("replace node intarray builder with immutable at some point");

    List<FPoint> allFPoints = arrayList();
    for (var rs : nodeSet.roadSegments()) {
      var a = rs.a();
      var b = rs.b();
      allFPoints.add(a);
      allFPoints.add(b);
      int i0 = mPointSet.add(a);
      int i1 = mPointSet.add(b);
      seg.add(i0);
      seg.add(i1);
    }

    mRootBounds = FRect.rectContainingPoints(allFPoints);
    mRoot = splitNodeSet(mRoot, mRootBounds);
  }
//
//  private void prepare() {
//    if (mRoot == null) {
//      var p1 = mNodeSet.roadSegments().get(0).a();
//      if (db)
//        checkArgument(p1.x < 20000, "improperly transformed nodeset:", p1);
//
//      construct(mNodeSet);
//      // We can throw out the node set since it is no longer needed
//      mNodeSet = null;
//    }
//  }

  public int[] findCandidates(FRect inputBounds) {

    if (db)
      pr("inputBounds:", INDENT, inputBounds);

    mQueryResultSet.clear();
    mQueryInputBounds = inputBounds;
    mDepth = 0;
    auxFind(mRoot, mRootBounds);

    todo("sort the results to remove duplicates");
    return mQueryResultSet.array();
//
//    List<RoadSegment> out = arrayList();
//    out.addAll(mQueryResultSet);
//
////    if (alert("sorting candidates")) {
////      out.sort(NODE_COMPARATOR);
////    }
//    if (db)
//      pr("candidates:", out);
//    return out;
  }

  private void auxFind(QNode qNode, FRect bounds) {
    if (db)
      pr(TAB(mDepth * 3), "bounds:", bounds, "queryB:", mQueryInputBounds);

    if (!mQueryInputBounds.intersects(bounds)) {
      if (db)
        pr("....doesn't intersect");
      return;
    }
    mDepth++;

    if (qNode.left() != null || qNode.right() != null) {
      boolean splitDimension = bounds.width > bounds.height;
      float s = splitCoordinate(splitDimension, bounds);
      var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
      if (qNode.left() != null)
        auxFind(qNode.left(), recurseBounds[0]);
      if (qNode.right() != null)
        auxFind(qNode.right(), recurseBounds[1]);
    } else {
      var segmentEndpointIds = qNode.mSegments.array();
      for (int i = 0; i < segmentEndpointIds.length; i+=2) {
        var id0 = segmentEndpointIds[i];
        var id1 = segmentEndpointIds[i+1];
        var p0 = mPointSet.get(id0);
        var p1 = mPointSet.get(id1);
        var segmentBounds = FRect.rectContainingPoints(p0,p1);
         if (mQueryInputBounds.intersects(segmentBounds)) {
          mQueryResultSet.add(id0);
          mQueryResultSet.add(id1);
        }
      }
    }
    mDepth--;
  }

  private static class QNode {

    private QNode mLeftChild, mRightChild; // Pointers to child nodes

    // If this is a leaf node, this will contain the segments, as pairs of start+end endpoint ids
    private IntArray mSegments;

    QNode() {

    }

    QNode left() {
      return mLeftChild;
    }

    QNode right() {
      return mRightChild;
    }

    IntArray segments() {
     return mSegments;
    }

    void addSegment(int endpointId0, int endpointId1) {
      var b = (IntArray.Builder)mSegments;
      b.add(endpointId0);
      b.add(endpointId1);
    }
//    FRect calculateBounds(PointSet ps) {
//      checkArgument(population() != 0);
//      List<FPoint> pts = arrayList();
//      var sz = mSegments.size();
//      for (int j = 0; j < sz; j+=2) {
//        int p0 = mSegments.get(j);
//        int p1 = mSegments.get(j+1);
//        var pt0 = ps.get(p0);
//        var pt1 = ps.get(p1);
//        pts.add(pt0);
//        pts.add(pt1);
//      }
//      return FRect.rectContainingPoints(pts);
//    }

    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    public JSMap toJson() {
      var m = map();
      m.put("", "QNode " + debugIndex);
      m.put("pop", population());
      if (mLeftChild != null)
        m.put("cLeft", mLeftChild.debugIndex);
      if (mRightChild != null)
        m.put("cRight", mRightChild.debugIndex);
      return m;
    }

//    public void addPolylines(Collection<RoadSegment> nodes) {
//      if (mNodes == null)
//        mNodes = arrayList();
//      mNodes.addAll(nodes);
//    }

    // Returns the number of polylines stored in this node (not in the rest of the subtree though)
    public int population() {
      if (mSegments == null)
        return 0;
      return mSegments.size() / 2;
    }
//
//    public void addPolyline(RoadSegment n) {
//      if (mNodes == null)
//        mNodes = arrayList();
//      mNodes.add(n);
//    }

    public void discardPolylines() {

      mSegments = null;
//      mNodes = null;
    }

    public void setLeftChild(QNode child) {
      mLeftChild = child;
    }

    public void setRightChild(QNode child) {
      mRightChild = child;
    }

    public void assertUseful() {
      todo("do a useful check");
//      if (mLeftChild == null || mRightChild == null && mNodes == null)
//        badState("QuadTree node is not useful:", INDENT, toJson());
    }

    private static int sDebugIndex;
    private int debugIndex;
  }

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------
//
//  /**
//   * Construct the QuadTree
//   */
//  private void construct(RoadNetwork nodeSet) {
//    QNode.sDebugIndex = 100;
//    // Build a leaf QNode that contains all the nodes, to be recursively split
//    var qn = new QNode();
//    qn.addPolylines(nodeSet.roadSegments());
//    mDepth = 0;
//
//    // Calculate the bounds of all of the nodes, and pass that into the split node method
//
//    var bounds = qn.calculateBounds();
//    mRootBounds = bounds;
//    mRoot = splitNodeSet(qn, bounds);
//  }

  private QNode splitNodeSet(final QNode node, FRect bounds) {
todo("this does NOT need to return anything, as input node doesn't change");


    // If there are only a few polylines in this node, don't split it further
    if (node.population() <= mParam.maxNodeCapacity()) {
      return node;
    }

    // Construct new nodes for the left and right children

    var qL = new QNode();
    var qR = new QNode();
    var inputPop = node.population();

    boolean splitDimension = bounds.width > bounds.height;

    final float s = splitCoordinate(splitDimension, bounds);
    {
      var segs = node.segments().array();
      for (int i = 0; i < segs.length; i+=2) {
        var id0 = segs[i];
        var id1 = segs[i+1];
        var pt0 = mPointSet.get(id0);
        var pt1 = mPointSet.get(id1);
      var segmentBounds = FRect.rectContainingPoints(pt0,pt1);
        // Add segment to each child node that it intersects
        var isect = calcIntersectFlags(splitDimension, s, segmentBounds);
        var isectL = (isect & 1) != 0;
        var isectR = (isect & 2) != 0;
        if (isectL) {
          qL.addSegment(id0,id1);
        }
        if (isectR) {
          qR.addSegment(id0,id1);
        }
      }
//      for (var n : node.polylines()) {
//        var b = MatchUtil.bounds(n);
//
//
//      }

      // We need a recursion stopping criterion to avoid infinite descent for large intermingled polylines
      // that can't be separated by further subdivisions

      var leftPop = qL.population();
      var rightPop = qR.population();
      var max = Math.max(leftPop, rightPop);
      var min = Math.min(leftPop, rightPop);

      // TODO: if the same segments is added many times, this might recurse forever
      todo("add unit test for many copies of the same segment");
      if (max == inputPop && min != 0) {
        return node;
      }
    }

    var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);

    // The polylines have been moved to the child nodes, so get rid of ours
    node.discardPolylines();
    mDepth++;
    checkState(mDepth < 50, "recurse depth limit exceeded");

    if (qL.population() != 0) {
      var x = splitNodeSet(qL, recurseBounds[0]);
      todo("assert useful ONLY if it undergoes splitting");
      if (x != qL)
        x.assertUseful();
      node.setLeftChild(x);
    }
    if (qR.population() != 0) {
      var x = splitNodeSet(qR, recurseBounds[1]);
//      if (x != qR)
//        x.assertUseful();
      node.setRightChild(x);
    }
    mDepth--;

    return node;
  }

  private static float splitCoordinate(boolean splitDimension, FRect bounds) {
    return splitDimension ? bounds.midX() : bounds.midY();
  }

  private static FRect[] calcSubdivisionBounds(boolean splitDimension, FRect inb, float splitc) {
    FRect[] out = new FRect[2];
    if (splitDimension) {
      out[0] = new FRect(inb.x, inb.y, splitc - inb.x, inb.height);
      out[1] = new FRect(splitc, inb.y, inb.endX() - splitc, inb.height);
    } else {
      out[0] = new FRect(inb.x, inb.y, inb.width, splitc - inb.y);
      out[1] = new FRect(inb.x, splitc, inb.width, inb.endY() - splitc);
    }
    return out;
  }

  private int calcIntersectFlags(boolean splitDimension, float splitCoordinate, FRect objectBounds) {
    int f = 0;
    if ((splitDimension ? objectBounds.x : objectBounds.y) <= splitCoordinate)
      f |= 1;
    if ((splitDimension ? objectBounds.endX() : objectBounds.endY()) >= splitCoordinate)
      f |= 2;
    return f;
  }

//  private RoadNetwork mNodeSet;
//  private FPoint mOrigin;
  private QNode mRoot;
  private FRect mRootBounds;

  // Used for recursing during constructing Quadtree

  private FRect mQueryInputBounds;
  private IntArray.Builder mQueryResultSet = IntArray.newBuilder();
  private int mDepth;
  private QtreeParam mParam;
  private PointSet mPointSet;
}

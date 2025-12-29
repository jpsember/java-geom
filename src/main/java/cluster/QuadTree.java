package cluster;

import js.base.BaseObject;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.QtreeParam;
import js.json.JSObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static cluster.MatchUtil.*;

import static js.base.Tools.*;

public class QuadTree extends BaseObject {

  public QuadTree(QtreeParam paramOrNull, PointSet pointSet, int[] segmentEndpointPairs) {
    todo("!remove unused parameters, e.g. stopping");
    todo("!assign a unique id number to each leaf node, for more compact serialization; but not necessary if done at serialization stage");
    checkArgument(!pointSet.mutable(), "PointSet must be frozen");
    mSegmentEndpointPairs = segmentEndpointPairs;
    mParam = nullTo(paramOrNull, QtreeParam.DEFAULT_INSTANCE).build();
    mPointSet = pointSet;
  }

  @Override
  public JSMap toJson() {
    var m = super.toJson();
    if (mRoot == null) return m;
    var m2 = auxDump(mRoot, mRootBounds);
    m.put("tree", m2);
    return m;
  }

  private JSObject auxDump(QNode node, FRect bounds) {
    if (node.isLeaf()) {
      var x = list();
      x.add("w").add(bounds.width).add("h").add(bounds.height);
      x.add("p").add(node.population());
      return x;
    } else {
      var m = map();
      if (node.left() != null || node.right() != null) {
        boolean splitDimension = bounds.width > bounds.height;
        float s = splitCoordinate(splitDimension, bounds);
        var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
        if (node.left() != null) {
          var m2 = auxDump(node.left(), recurseBounds[0]);
          m.put("L", m2);
        }
        if (node.right() != null) {
          var m2 = auxDump(node.right(), recurseBounds[1]);
          m.put("R", m2);
        }
      }
      return m;
    }
  }

  public void prepare() {
    if (mRoot != null) return;
    // Add all points to the point set, and to the quad tree
    mRoot = new QNode(true);
    var seg = mRoot.segments().toBuilder(); // it's already a builder, so it will just return mSegments

    var segmentEndpointPairs = mSegmentEndpointPairs;
    var pointSet = mPointSet;
    List<FPoint> allFPoints = arrayList();
    for (int i = 0; i < segmentEndpointPairs.length; i += 2) {
      var id0 = segmentEndpointPairs[i + 0];
      var id1 = segmentEndpointPairs[i + 1];
      var a = pointSet.get(id0);
      var b = pointSet.get(id1);
      allFPoints.add(a);
      allFPoints.add(b);
      seg.add(id0).add(id1);
    }

    var bounds = FRect.DEFAULT_INSTANCE;
    if (!allFPoints.isEmpty())
      bounds =
          FRect.rectContainingPoints(allFPoints);
    mRootBounds = bounds;
    log("root bounds:", mRootBounds);
    splitNodeSet(0, mRoot, mRootBounds);

    todo("!have more sophisticated seg intersects box calculation");

    if (!mParam.disableRewrite()) {
      mPreCull = subtreeNodeCount(mRoot);
      Map<IntArray, QNode> uniqueLeafMap = hashMap();
      mRoot = rewriteTree(mRoot, uniqueLeafMap);
      mPostCull = subtreeNodeCount(mRoot);
    }
    // Discard things no longer required
    mSegmentEndpointPairs = null;
  }

  public JSMap auxInfo() {
    var m = map();
    m.put("height", auxHeight(mRoot));
    if (mPreCull != 0)
      m.put("nodes_before_cull", mPreCull).put("nodes_post_cull", mPostCull);


    {
      int leafCount = 0;
      Set<IntArray> set = hashSet();
      int sumOfSegmentListCounts = 0;

      // Determine number of distinct leaf nodes
      List<QNode> stack = arrayList();
      push(stack, mRoot);
      while (!stack.isEmpty()) {
        var n = pop(stack);
        if (!n.isLeaf()) {
          if (n.left() != null) push(stack, n.left());
          if (n.right() != null) push(stack, n.right());
        } else {
          var arr = n.segments().build();
          sumOfSegmentListCounts += arr.size() / 2;
          leafCount++;
          set.add(arr);
        }
      }
      m.put("leaf count", leafCount).put("leaf unique", set.size());
      if (leafCount != 0)
        m.put("leaf list avg #segs", sumOfSegmentListCounts / (float) leafCount);
    }

    return m;
  }

  private int auxHeight(QNode node) {
    if (node == null)
      return 0;
    return 1 + Math.max(auxHeight(node.left()), auxHeight(node.right()));
  }

  public int[] findSegments(FRect inputBounds) {
    log("findSegments, inputBounds:", INDENT, inputBounds);
    prepare();
    mQueryResultPairs.clear();
    mQueryInputBounds = inputBounds;
    auxFind(0, mRoot, mRootBounds);

    var b = IntArray.newBuilder();
    for (var key : mQueryResultPairs) {
      int pt0 = (int) (key >> 32);
      int pt1 = key.intValue();
      b.add(pt0);
      b.add(pt1);
    }
    return b.array();
  }

  private static boolean rectsTouch(FRect a, FRect b) {
    return (a.x <= b.endX() && a.endX() >= b.x && a.y <= b.endY() && a.endY() >= b.y);
  }

  private void auxFind(int depth, QNode qNode, FRect bounds) {
    if (verbose())
      log(TAB(depth * 2), "bounds:", bounds);

    if (!rectsTouch(mQueryInputBounds, bounds)) {
      log("....doesn't intersect");
      return;
    }

    if (!qNode.isLeaf()) {
      boolean splitDimension = bounds.width > bounds.height;
      float s = splitCoordinate(splitDimension, bounds);
      var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
      if (qNode.left() != null)
        auxFind(1 + depth, qNode.left(), recurseBounds[0]);
      if (qNode.right() != null)
        auxFind(1 + depth, qNode.right(), recurseBounds[1]);
    } else {
      var segmentEndpointIds = qNode.mSegments.array();
      for (int i = 0; i < segmentEndpointIds.length; i += 2) {
        var id0 = segmentEndpointIds[i];
        var id1 = segmentEndpointIds[i + 1];
        var p0 = mPointSet.get(id0);
        var p1 = mPointSet.get(id1);

        // We are LIBERAL in what we return.  If the query bounds touches the
        // segment bounds, include it.

        var segmentBounds = FRect.rectContainingPoints(p0, p1);
        if (rectsTouch(mQueryInputBounds, segmentBounds)) {
          mQueryResultPairs.add((((long) id0) << 32) | id1);
        }
      }
    }
  }

  private static class QNode {

    private QNode mLeftChild, mRightChild; // Pointers to child nodes

    // Storage for start+end endpoint ids, if this is a leaf node; otherwise, null
    private IntArray mSegments;

    QNode(boolean leafNode) {
      if (leafNode)
        mSegments = IntArray.newBuilder();
    }

    boolean isLeaf() {
      return mSegments != null;
    }

    QNode left() {
      return mLeftChild;
    }

    QNode right() {
      return mRightChild;
    }

    IntArray segments() {
      checkArgument(mSegments != null);
      return mSegments;
    }

    void addSegment(int endpointId0, int endpointId1) {
      var b = (IntArray.Builder) segments();
      b.add(endpointId0);
      b.add(endpointId1);
    }


    void convertToInterior(QNode newLeft, QNode newRight) {
      todo("can we instead construct a fresh node?");
      checkArgument(isLeaf());
      checkArgument(newLeft != null || newRight != null);
      mSegments = null;
      mLeftChild = newLeft;
      mRightChild = newRight;
    }

    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    public JSMap toJson() {
      var m = map();
      m.put("pop", population());
      if (mLeftChild != null)
        m.put("cLeft", true); //mLeftChild.debugIndex);
      if (mRightChild != null)
        m.put("cRight", true); //mRightChild.debugIndex);
      return m;
    }

    // Returns the number of polylines stored in this node (not in the rest of the subtree though)
    public int population() {
      return segments().size() / 2;
    }

    public void setLeftChild(QNode child) {
      mLeftChild = child;
    }

    public void setRightChild(QNode child) {
      mRightChild = child;
    }

  }

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------

  private void splitNodeSet(int depth, final QNode node, FRect bounds) {
    log("splitNodeSet, pop:", node.population(), "bounds:", bounds, "depth:", depth);

    checkArgument(node.isLeaf());
    // If there are only a few elements in this node (or none), don't split it further
    if (node.population() <= mParam.targetNodeMaxPop()) {
      log("...population too low, doing nothing");
      return;
    }

    boolean splitDimension = bounds.width > bounds.height;

    // If split dimension is less than the min, do no futher splitting
    float unsplitSize = splitDimension ? bounds.width : bounds.height;

    if (unsplitSize <= mParam.minNodeDimension()) {
      log("...reached minimum node dimension, doing nothing");
      return;
    }

    final float s = splitCoordinate(splitDimension, bounds);

    // Construct new nodes for the left and right children

    var qL = new QNode(true);
    var qR = new QNode(true);

    var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
    var boundsLeft = recurseBounds[0];
    var boundsRight = recurseBounds[1];

    log("...split dimension:", splitDimension, "coordinate:", s);
    {
      var segs = node.segments().array();
      for (int i = 0; i < segs.length; i += 2) {
        var id0 = segs[i];
        var id1 = segs[i + 1];
        checkArgument(id0 > 0, id0);
        checkArgument(id1 > 0, id1);
        var pt0 = mPointSet.get(id0);
        var pt1 = mPointSet.get(id1);


        // Add segment to each child node that it intersects


        // During this tree construction operation, we want to be more strict
        // about whether a segment touches the node bounding box.

        // If the bounding boxes don't touch, then no;
        // otherwise, if a segment endpoint touches the box, then yes;
        // otherwise, yes iff the segment intersects one of the box sides

        // We are more liberal (for efficiency) during the tree query operation.

        if (true || !alert("not doing new way yet")) {
          int isectCount = 0;
          if (segmentIntersectsBox(boundsLeft, pt0, pt1)) {
            qL.addSegment(id0, id1);
            isectCount++;
          }
          if (segmentIntersectsBox(boundsRight, pt0, pt1)) {
            qR.addSegment(id0, id1);
            isectCount++;
          }
          if (isectCount == 0) {
            pr("bounds:", INDENT, bounds);
            pr("seg:", pt0, pt1);
            pr("left:", boundsLeft);
            pr("rigt:", boundsRight);
            die("segment did NOT intersect either child node:", bounds, INDENT, pt0, pt1, CR, "left:");
          }
        } else {
          var segmentBounds = FRect.rectContainingPoints(pt0, pt1);

          todo("calcIntersectFlags is kind of clunky");

          var isect = calcIntersectFlags(splitDimension, s, segmentBounds);
          var isectL = (isect & 1) != 0;
          var isectR = (isect & 2) != 0;

          if (isectL) {
            // Do stricter check
            if (boxTouchesSegment(boundsLeft, pt0, pt1))
              qL.addSegment(id0, id1);
          }
          if (isectR) {
            if (boxTouchesSegment(boundsRight, pt0, pt1))
              qR.addSegment(id0, id1);
          }
        }
      }
    }
    checkState(depth < 50, "recurse depth limit exceeded");

    // We are turning this leaf node into an interior node, which means
    // 1) one or more children get added
    // 2) the segments are discarded (they were moved)

    QNode newLeft = null;
    QNode newRight = null;

    if (qL.population() != 0) {
      log("recurse, left bounds:", boundsLeft);
      splitNodeSet(1 + depth, qL, boundsLeft);
      newLeft = qL;
    }
    if (qR.population() != 0) {
      log("recurse, right bounds:", boundsRight);
      splitNodeSet(1 + depth, qR, boundsRight);
      newRight = qR;
    }

    node.convertToInterior(newLeft, newRight);
  }

  // We will assume that the bounding box of the segment touches the query box
  public /*for testing*/ static boolean boxTouchesSegment(FRect box, FPoint p0, FPoint p1) {
    if (box.contains(p0)) {
      return true;
    }
    if (box.contains(p1))
      return true;

    todo("check if box edge intersects segment");

    // If segment touches a box side, then it will touch both a horizontal and vertical side
    // (perhaps at the box corner).
    //
    // This lets us check only segments that intersect at at least 45 degrees to avoid precision problems

    todo("finish this code");
    if (true) return true;
    return false;
  }

  private int subtreeNodeCount(QNode parent) {
    if (parent == null) return 0;
    return 1 + subtreeNodeCount(parent.left()) + subtreeNodeCount(parent.right());
  }

  /**
   * Rewrite a subtree, eliminating splits that yield identical sets of segments in each of the immediate (leaf) children
   *
   * @param parent root of subtree, or null
   * @return root of (possibly rewritten) subtree
   */
  private static QNode rewriteTree(QNode parent, Map<IntArray, QNode> uniqueLeafMap) {
    //pr("rewriteTree:",INDENT,parent,"parent is null:",parent == null);
    if (parent == null) return null;
    if (parent.isLeaf()) {

      // Convert the segment list to an immutable version
      var seg = parent.segments();
      seg = seg.build();

      // If there is already a leaf node in the map with this set of segments, return it instead
      var existing = uniqueLeafMap.get(seg);
      if (existing != null) return existing;

      todo("The 'freezing' of the segments should be done before rewriting, or at least not to confuse what is happening here");

      parent.mSegments = seg;
      uniqueLeafMap.put(seg, parent);
      return parent;
    }

    // Rewrite each child
    var left = rewriteTree(parent.left(), uniqueLeafMap);
    var right = rewriteTree(parent.right(), uniqueLeafMap);

    // If two children exist, are both leaf nodes, and have identical segments, return either one of these as the new parent;
    // otherwise, return a new node with these children
    if (left != null && right != null && left.isLeaf() && right.isLeaf() && left.mSegments.equals(right.mSegments)) {
      return left;
    }

    var newP = new QNode(false);
    newP.setLeftChild(left);
    newP.setRightChild(right);
    return newP;
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

  private QNode mRoot;
  private FRect mRootBounds;

  // Used for recursing during constructing Quadtree

  private FRect mQueryInputBounds;
  private Set<Long> mQueryResultPairs = hashSet();
  private QtreeParam mParam;
  private PointSet mPointSet;
  private int[] mSegmentEndpointPairs;
  private int mPreCull, mPostCull;
}

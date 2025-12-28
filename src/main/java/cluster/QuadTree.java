package cluster;

import js.base.BaseObject;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.QtreeParam;
import js.json.JSObject;

import java.util.List;
import java.util.Set;

import static js.base.Tools.*;

public class QuadTree extends BaseObject {

  public QuadTree(QtreeParam paramOrNull, PointSet pointSet, int[] segmentEndpointPairs) {
    todo("!remove unused parameters, e.g. stopping");
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
    mRoot = new QNode();
    var seg = IntArray.newBuilder();
    mRoot.mSegments = seg;

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
    todo("!maybe recycle nodes that are equivalent");

    if (!mParam.disableFruitlessRewrite()) {
      mPreCull = subtreeNodeCount(mRoot);
      mRoot = undoFruitlessSplits(mRoot);
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



    if (true) {

      int leafCount = 0;
      Set<IntArray> set = hashSet();

      // Determine number of distinct leaf nodes
      List<QNode> stack = arrayList();
      push(stack, mRoot);
      while (!stack.isEmpty()) {
        var n = pop(stack);
        if (!n.isLeaf()) {
          if (n.left() != null)push(stack,n.left());
          if (n.right() != null)    push(stack,n.right());
        } else {
          var arr =  n.segments().build();
          if (arr.isEmpty()) continue;
          leafCount++;
          var wasNew = set.add(arr);
          if (!wasNew)
            pr("segment list already exists:",INDENT,arr);
        }
      }
      m.put("leaf count",leafCount).put("leaf unique",set.size());
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

    if (qNode.left() != null || qNode.right() != null) {
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
        var segmentBounds = FRect.rectContainingPoints(p0, p1);
        if (rectsTouch(mQueryInputBounds, segmentBounds)) {
          mQueryResultPairs.add((((long) id0) << 32) | id1);
        }
      }
    }
  }

  private static class QNode {

    private QNode mLeftChild, mRightChild; // Pointers to child nodes

    // Storage for start+end endpoint ids, if this is a leaf node; otherwise, an empty array
    private IntArray mSegments;

    QNode() {
      mSegments = IntArray.newBuilder();
    }

    boolean isLeaf() {
      return mLeftChild == null && mRightChild == null;
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
      var b = (IntArray.Builder) mSegments;
      b.add(endpointId0);
      b.add(endpointId1);
    }

    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    public JSMap toJson() {
      var m = map();
//      m.put("", "QNode " + debugIndex);
      m.put("pop", population());
      if (mLeftChild != null)
        m.put("cLeft", true); //mLeftChild.debugIndex);
      if (mRightChild != null)
        m.put("cRight", true); //mRightChild.debugIndex);
      return m;
    }


    // Returns the number of polylines stored in this node (not in the rest of the subtree though)
    public int population() {
      return mSegments.size() / 2;
    }

    public void discardSegments() {
      mSegments = IntArray.DEFAULT_INSTANCE;
    }

    public void setLeftChild(QNode child) {
      mLeftChild = child;
    }

    public void setRightChild(QNode child) {
      mRightChild = child;
    }

    public void trimSegmentList() {
      mSegments = mSegments.build();
    }
  }

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------

  private void splitNodeSet(int depth, final QNode node, FRect bounds) {

    log("splitNodeSet, pop:", node.population(), "bounds:", bounds, "depth:", depth);

    node.trimSegmentList();

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

    var qL = new QNode();
    var qR = new QNode();

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
        var segmentBounds = FRect.rectContainingPoints(pt0, pt1);
        // Add segment to each child node that it intersects
        var isect = calcIntersectFlags(splitDimension, s, segmentBounds);
        var isectL = (isect & 1) != 0;
        var isectR = (isect & 2) != 0;

        if (isectL) {
          qL.addSegment(id0, id1);
        }
        if (isectR) {
          qR.addSegment(id0, id1);
        }
      }
    }

    var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);

    // The segments have been moved to the child nodes, so get rid of ours
    node.discardSegments();

    checkState(depth < 50, "recurse depth limit exceeded");

    if (qL.population() != 0) {
      log("recurse, left bounds:", recurseBounds[0]);
      splitNodeSet(1 + depth, qL, recurseBounds[0]);
      node.setLeftChild(qL);
    }
    if (qR.population() != 0) {
      log("recurse, right bounds:", recurseBounds[1]);
      splitNodeSet(1 + depth, qR, recurseBounds[1]);
      node.setRightChild(qR);
    }
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
  private static QNode undoFruitlessSplits(QNode parent) {
    if (parent == null) return null;
    if (parent.isLeaf()) return parent;

    // Rewrite each child
    var left = undoFruitlessSplits(parent.left());
    var right = undoFruitlessSplits(parent.right());

    // If two children exist, are both leaf nodes, and have identical segments, return either one of these as the new parent;
    // otherwise, return a new node with these children
    if (left != null && right != null && left.isLeaf() && right.isLeaf() && left.mSegments.equals(right.mSegments)) {
      return left;
    }

    var newP = new QNode();
    newP.discardSegments();
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

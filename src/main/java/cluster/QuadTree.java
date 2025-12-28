package cluster;

import js.base.BaseObject;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.QtreeParam;

import java.util.List;
import java.util.Set;

import static js.base.Tools.*;

public class QuadTree extends BaseObject {

  public QuadTree(QtreeParam paramOrNull, PointSet pointSet, int[] segmentEndpointPairs) {
    checkArgument(!pointSet.mutable(), "PointSet must be frozen");
    mSegmentEndpointPairs = segmentEndpointPairs;
    mParam = nullTo(paramOrNull, QtreeParam.DEFAULT_INSTANCE).build();
    mPointSet = pointSet;
  }

  public void prepare() {
    if (mRoot != null) return;
    // Add all points to the point set, and to the quad tree
    mRoot = new QNode();
    var seg = IntArray.newBuilder();
    mRoot.mSegments = seg;
    todo("replace node intarray builder with immutable at some point");

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
    pr("root bounds:", z(mRootBounds));
    splitNodeSet(0, mRoot, mRootBounds);

//    if (!alert("skipping cull"))
    todo("fruitless splits doesn't do anything");
    todo("have more sophisticated seg intersects box calculation");

//    if (!alert("skipping fruitless splits"))
    {
      mPreCull = subtreeNodeCount(mRoot);
      mRoot = undoFruitlessSplits(mRoot);
      mPostCull = subtreeNodeCount(mRoot);
    }
    // Discard things no longer required
    mSegmentEndpointPairs = null;
  }

  public JSMap auxInfo() {
    return map().put("before_cull", mPreCull).put("post_cull", mPostCull).put("height", auxHeight(mRoot) - 1);
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

    public void discardPolylines() {
      mSegments = IntArray.DEFAULT_INSTANCE;
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

  private static final int MAX_RECURSE_DEPTH = 100;

  private void splitNodeSet(int depth, final QNode node, FRect bounds) {

    log("splitNodeSet, pop:", node.population(), "bounds:", bounds, "depth:", depth);

    // If node is empty, splitting is trivially unnecessary
    if (node.population() == 0) return;

    // If there are only a few elements in this node, don't split it further
    todo("!but does this have any effect on the size or runtime?");
    if (node.population() <= mParam.targetNodeMaxPop()) {
      log("...population too low, doing nothing");
      return;
    }

    boolean splitDimension = bounds.width > bounds.height;

    // If split dimension is less than the min, do no futher splitting
    float unsplitSize = splitDimension ? bounds.width : bounds.height;

    pr("depth:", depth, "unsplitSize:", unsplitSize, "minNodeDim:", mParam.minNodeDimension());
    if (unsplitSize <= mParam.minNodeDimension()) {
      log("...reached minimum node dimension, doing nothing");
      return;
    }

    final float s = splitCoordinate(splitDimension, bounds);
    pr("unsplitSize:", unsplitSize, "bounds:", z(bounds));

    if (splitDimension) {
      var left = s - bounds.x;
      var right = bounds.endX() - s;
      pr("left:", "" + left, "right:", "" + right);
      pr("bounds:", z(bounds));
      checkArgument(Math.min(left, right) * 2 >= mParam.minNodeDimension());
    } else {
      var bot = s - bounds.y;
      var top = bounds.endY() - s;
      pr("bottom:", "" + bot, "top:", "" + top);
      pr("bounds:", z(bounds));
      checkArgument(Math.min(bot, top) * 2 >= mParam.minNodeDimension());
    }

    // Construct new nodes for the left and right children

    var qL = new QNode();
    var qR = new QNode();
    var inputPop = node.population();


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

//        log("...segment bounds:", segmentBounds, "isect L:", isectL, "R:", isectR);
        if (isectL) {
          qL.addSegment(id0, id1);
        }
        if (isectR) {
          qR.addSegment(id0, id1);
        }
      }


      // We need a recursion stopping criterion to avoid infinite descent for large intermingled polylines
      // that can't be separated by further subdivisions

      var leftPop = qL.population();
      var rightPop = qR.population();
      var max = Math.max(leftPop, rightPop);
      var min = Math.min(leftPop, rightPop);

      todo("this kind of check is probably no longer necessary, if we are going to 'undo' fruitless subdivisions");
      // TODO: if the same segments is added many times, this might recurse forever
      log("leftPop:", leftPop, "rightPop:", rightPop);
      if (!alert("skipping this step") && max == inputPop && min != 0) {
        log("....stop criterion reached, stopping");
        return;
      }
    }

    var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);

    if (recurseBounds[0].maxDim() * 2 < mParam.minNodeDimension()

        || recurseBounds[1].maxDim() * 2 < mParam.minNodeDimension()
    ) die("min node dim is:", "" + mParam.minNodeDimension(), "but recursebounds:", INDENT,

        z(recurseBounds[0]), CR, z(recurseBounds[1]), CR, "orig:", CR, z(bounds));
//
//    if (unsplitSize <= mParam.minNodeDimension()) {


    // The polylines have been moved to the child nodes, so get rid of ours
    node.discardPolylines();
    checkState(depth < MAX_RECURSE_DEPTH, "recurse depth limit exceeded");

    if (qL.population() != 0) {
      log("recurse, left bounds:", recurseBounds[0]);
      pr("splitLeft, orig b:", bounds.size(), "rec:", recurseBounds[0].size());
      splitNodeSet(1 + depth, qL, recurseBounds[0]);
      node.setLeftChild(qL);
    }
    if (qR.population() != 0) {
      log("recurse, right bounds:", recurseBounds[1]);
      pr("splitRigt, orig b:", bounds.size(), "rec:", recurseBounds[1].size());
      splitNodeSet(1 + depth, qR, recurseBounds[1]);
      node.setRightChild(qR);
    }
  }

  private static String z(FRect r) {
    return "x:" + r.x + " y:" + r.y + " w:" + r.width + " h:" + r.height;
  }

  private int subtreeNodeCount(QNode parent) {
    if (parent == null) return 0;
    return 1 + subtreeNodeCount(parent.left()) + subtreeNodeCount(parent.right());
  }

  private static String ci(QNode q) {
    if (q == null) return "-";
    if (q.isLeaf())
      return "leaf:" + q.population();
    return "intr";
  }

  private static QNode undoFruitlessSplits(QNode parent) {
    // if (alert("disabling fruitless split stuff")) return parent;
    if (parent == null) return parent;

    {
      if (parent.isLeaf())
        pr("undoFruitless, leaf, pop:", parent.population());
      else {
        pr("undoFruitless, children:", ci(parent.left()), ci(parent.right()));
      }
    }
    var left = undoFruitlessSplits(parent.left());
    var right = undoFruitlessSplits(parent.right());

    // If two children exist, are both leaf nodes, and have identical segments, return either one of these as the new parent;
    // otherwise, return a new node with these children
    if (left != null && right != null && left.isLeaf() && right.isLeaf() && left.mSegments.equals(right.mSegments)) {
      return left;
    }
    var newP = new QNode();
    newP.discardPolylines();
    ;
    newP.setLeftChild(left);
    newP.setRightChild(right);
    return newP;
//    if ()
////    int numKids = (left != null ? 1 : 0) + (right != null ? 1 : 0);
////    checkState(numKids != 1);
//    if (numKids == 0) return parent;
//    if (!left.isLeaf() || !right.isLeaf())
//      return parent;
//    if (left.mSegments.equals(right.mSegments)) {
//      return left;
//    }
//    return parent;
  }


  private static float splitCoordinate(boolean splitDimension, FRect bounds) {
    float result;
    if (splitDimension) {
      result = bounds.midX();
      checkEps(result - bounds.x);
      checkEps(bounds.endX() - result);
    } else {
      result = bounds.midY();
      checkEps(result - bounds.y);
      checkEps(bounds.endY() - result);
    }
    return result;
//    return splitDimension ? bounds.midX() : bounds.midY();
  }

  private static float checkEps(float value) {
    if (value < 1e-8)
      badArg("value is too close to zero:", "" + value);
    return value;
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

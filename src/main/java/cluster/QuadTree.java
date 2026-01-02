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

  private QuadTree(QtreeParam paramOrNull, PointSet pointSet) {
    mParam = nullTo(paramOrNull, QtreeParam.DEFAULT_INSTANCE).build();
    mPointSet = pointSet;
  }

  public QuadTree(QtreeParam paramOrNull, PointSet pointSet, int[] segmentEndpointPairs) {
    this(paramOrNull, pointSet);
    checkArgument(!pointSet.mutable(), "PointSet must be frozen");
    mSegmentEndpointPairs = segmentEndpointPairs;

  }

  public PointSet pointSet() {
    return mPointSet;
  }

  public void prepare() {
    if (mRoot != null) return;
    // Add all points to the point set, and to the quad tree
    mRoot = new QNode();
    var seg = mRoot.segments().toBuilder(); // it's already a builder, so it will just return mSegments

    var segmentEndpointPairs = mSegmentEndpointPairs;
    var pointSet = mPointSet;
    List<FPoint> allFPoints = arrayList();
    for (int i = 0; i < segmentEndpointPairs.length; i += 2) {
      var id0 = segmentEndpointPairs[i /*+ 0*/];
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
    mRoot = splitNode(mRoot, 0, mRootBounds);

    if (!mParam.disableRewrite()) {
      mPreCull = subtreeNodeCount(mRoot);
      Map<IntArray, QNode> uniqueLeafMap = hashMap();
      mRoot = rewriteTree(mRoot, uniqueLeafMap);
      mPostCull = subtreeNodeCount(mRoot);
    }
    // Discard things no longer required
    mSegmentEndpointPairs = null;
  }

  /**
   * Find all segments intersecting a square centered at a query point
   *
   * @param radius half the width of the square
   */
  public int[] findSegments(FPoint queryPoint, float radius) {
    var bounds = new FRect(queryPoint, queryPoint).withInset(-radius);
    return findSegments(bounds);
  }

  public int[] findSegments(FRect inputBounds) {
    log("findSegments, inputBounds:", INDENT, inputBounds);
    prepare();
    mQueryResultPairs.clear();
    mQueryInputBounds = inputBounds;
    auxFind(0, mRoot, mRootBounds);

    log("number of segs found:", mQueryResultPairs.size());
    var b = IntArray.newBuilder();
    for (var key : mQueryResultPairs) {
      int pt0 = (int) (key >> 32);
      int pt1 = key.intValue();
      b.add(pt0);
      b.add(pt1);
    }
    return b.array();
  }

  private void auxFind(int depth, QNode qNode, FRect bounds) {
    if (!rectsTouch(mQueryInputBounds, bounds)) {
      return;
    }

    if (!qNode.isLeaf()) {
      boolean splitDimension = bounds.width > bounds.height;
      float s = splitCoordinate(splitDimension, bounds);
      var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
      for (int childIndex = 0; childIndex < 2; childIndex++) {
        var child = qNode.child(childIndex);
        if (child != null)
          auxFind(1 + depth, child, recurseBounds[childIndex]);
      }
    } else {
      var segmentEndpointIds = qNode.segments().array();
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


  private static boolean rectsTouch(FRect a, FRect b) {
    // I can't use the FRect.intersects function, since it may fail with vertical or horizontal
    // road segments (which can produce zero-area rects)
    return (a.x <= b.endX() && a.endX() >= b.x && a.y <= b.endY() && a.endY() >= b.y);
  }

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------

  private QNode splitNode(final QNode node, int depth, FRect bounds) {
    checkArgument(node.isLeaf());
    log("splitNodeSet, pop:", node.population(), "bounds:", bounds, "depth:", depth);

    // If there are only a few elements in this node (or none), don't split it further
    if (node.population() <= mParam.targetNodeMaxPop()) {
      log("...population too low, doing nothing");
      return node;
    }

    boolean splitDimension = bounds.width > bounds.height;

    // If split dimension is less than the min, do no futher splitting
    float unsplitSize = splitDimension ? bounds.width : bounds.height;

    if (unsplitSize <= mParam.minNodeDimension()) {
      log("...reached minimum node dimension, doing nothing");
      return node;
    }

    final float s = splitCoordinate(splitDimension, bounds);

    // Construct new nodes for the left and right children

    var qL = new QNode();
    var qR = new QNode();

    var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
    var boundsLeft = recurseBounds[0];
    var boundsRight = recurseBounds[1];

    log("...split dimension:", splitDimension, "coordinate:", s);
    {
      var segs = node.segments().array();
      for (int i = 0; i < segs.length; i += 2) {
        var id0 = segs[i];
        var id1 = segs[i + 1];
        var pt0 = mPointSet.get(id0);
        var pt1 = mPointSet.get(id1);


        // Add segment to each child node that it intersects

        // During this tree construction operation, we want to be more strict
        // about whether a segment touches the node bounding box.

        // If the bounding boxes don't touch, then no;
        // otherwise, if a segment endpoint touches the box, then yes;
        // otherwise, yes iff the segment intersects one of the box sides

        // We are more liberal (for efficiency) during the tree query operation.

        int isectCount = 0;
        if (segmentIntersectsBox(boundsLeft, pt0, pt1)) {
          qL.addSegment(id0, id1);
          isectCount++;
        }
        if (segmentIntersectsBox(boundsRight, pt0, pt1)) {
          qR.addSegment(id0, id1);
          isectCount++;
        }
        checkState(isectCount != 0);
      }
    }

    // Construct an interior node to replace this node

    QNode newLeft = null;
    QNode newRight = null;
    if (qL.population() != 0) {
      newLeft = splitNode(qL, 1 + depth, boundsLeft);
    }
    if (qR.population() != 0) {
      newRight = splitNode(qR, 1 + depth, boundsRight);
    }
    return new QNode(newLeft, newRight);
  }

  /**
   * Rewrite a subtree, eliminating splits that yield identical sets of segments in each of the immediate (leaf) children.
   * Also, convert the leaf node segment lists to immutable (non-builder) arrays
   *
   * @param rootNode root of subtree, or null
   * @return root of (possibly rewritten) subtree
   */
  private static QNode rewriteTree(QNode rootNode, Map<IntArray, QNode> uniqueLeafMap) {
    if (rootNode == null) return null;

    if (rootNode.isLeaf()) {
      // Convert the segment list to an immutable version
      rootNode.freeze();
      var seg = rootNode.segments();

      // If there is already a leaf node in the map with this set of segments, return it instead
      var existing = uniqueLeafMap.get(seg);
      if (existing != null) return existing;

      rootNode.setDebugId(50 + uniqueLeafMap.size());
      uniqueLeafMap.put(seg, rootNode);
      return rootNode;
    }

    // Rewrite each child
    var left = rewriteTree(rootNode.left(), uniqueLeafMap);
    var right = rewriteTree(rootNode.right(), uniqueLeafMap);

    // If two children exist, are both leaf nodes, and have identical segments, return either one of these as the new parent;
    // otherwise, return a new node with these children
    if (left != null && right != null && left.isLeaf() && right.isLeaf() && left.segments().equals(right.segments())) {
      return left;
    }

    return new QNode(left, right);
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

  private final Set<Long> mQueryResultPairs = hashSet();
  private final QtreeParam mParam;
  private final PointSet mPointSet;

  private QNode mRoot;
  private FRect mRootBounds;
  private FRect mQueryInputBounds;
  private int[] mSegmentEndpointPairs;
  private int mPreCull, mPostCull;

  // ----------------------------------------------------------------------------------------------
  // Serialization
  // ----------------------------------------------------------------------------------------------

  public JSMap serialize() {

    todo("!document format of serialized nodes");

    // Perform a post-order tree traversal (https://en.wikipedia.org/wiki/Tree_traversal)
    // so that when an interior node needs to be serialized (or deserialized), its
    // child nodes have already been serialized (resp. deserialized)

    List<QNode> traversal = arrayList();
    postOrderTraversal(mRoot, traversal);

    // The traversal may include *several copies* of certain leaf nodes, if they were
    // child nodes of more than one internal node

    // Assign unique (nonzero) serialization ids to each *unique* node;
    int nextId = 1;
    for (var n : traversal) {
      if (n.serializationId() != 0)
        continue;
      n.setSerializationId(nextId);
      nextId++;
    }

    var encodedNodes = IntArray.newBuilder();

    // Serialize the nodes, skipping those that have already been serialized
    int lastSerializedId = 0;
    for (var n : traversal) {
      if (n.serializationId() <= lastSerializedId)
        continue;
      lastSerializedId = n.serializationId();

      if (!n.isLeaf()) {
        encodedNodes.add(nodeIdOrZero(n.left()));
        encodedNodes.add(nodeIdOrZero(n.right()));
      } else {
        var segs = n.segments();
        int numSeg = segs.size() / 2;
        encodedNodes.add(-numSeg - 1);
        for (int i = 0; i < segs.size(); i++) {
          encodedNodes.add(segs.get(i));
        }
      }
    }

    var m = map();
    m.put(SER_KEY_NODES, encodedNodes.toJson());
    m.put(SER_KEY_ROOT_BOUNDS, mRootBounds.toJson());
    m.put(SER_KEY_POINTS, mPointSet.serialize());
    m.put(SER_KEY_PARAM, mParam.toJson());
    return m;
  }

  private void postOrderTraversal(QNode subtree, List<QNode> result) {
    if (subtree == null) return;
    postOrderTraversal(subtree.left(), result);
    postOrderTraversal(subtree.right(), result);
    result.add(subtree);
  }


  private static int nodeIdOrZero(QNode nodeOrNull) {
    if (nodeOrNull == null) return 0;
    var id = nodeOrNull.serializationId();
    checkState(id != 0);
    return id;
  }

  public static String SER_KEY_NODES = "nodes";
  public static String SER_KEY_ROOT_BOUNDS = "root_bounds";
  public static String SER_KEY_POINTS = "points";
  public static String SER_KEY_PARAM = "param";

  public static QuadTree deserialize(JSMap m) {
    var nodes = m.getList(SER_KEY_NODES).asIntArray();
    List<QNode> constructedNodes = arrayList();

    var i = 0;
    while (i < nodes.length) {
      var x = nodes[i++];
      if (x < 0) {  // It's a leaf node
        int numSegs = -x - 1;
        var q = new QNode();
        for (int j = 0; j < numSegs; j++) {
          var endPointA = nodes[i++];
          var endPointB = nodes[i++];
          q.addSegment(endPointA, endPointB);
        }
        constructedNodes.add(q);
      } else {
        constructedNodes.add(new QNode(optNode(constructedNodes, x), optNode(constructedNodes, nodes[i++])));
      }
    }
    var rootBounds = FRect.DEFAULT_INSTANCE.parse(m.getList(SER_KEY_ROOT_BOUNDS));
    var points = PointSet.deserialize(m.getList(SER_KEY_POINTS));
    var param = m.getUnsafe(SER_KEY_PARAM);
    var params = QtreeParam.DEFAULT_INSTANCE.parse(param);
    var qt = new QuadTree(params, points);
    qt.mRoot = last(constructedNodes);
    qt.mRootBounds = rootBounds;
    return qt;
  }

  private static QNode optNode(List<QNode> list, int id) {
    if (id == 0) return null;
    return list.get(id - 1);
  }

  // ----------------------------------------------------------------------------------------------
  // Logging and debugging
  // ----------------------------------------------------------------------------------------------

  @Override
  public JSMap toJson() {
    var m = super.toJson();
    if (mRoot == null) return m;
    m.put("tree", auxDump(mRoot, mRootBounds));
    return m;
  }

  private JSObject auxDump(QNode node, FRect bounds) {
    if (node.isLeaf()) {
      var m = map();
      if (node.debugId() != 0)
        m.put("x", node.debugId());

      var x = list();
      for (int j : node.segments().array())
        x.add(j);
      m.put("segs", x);
      return m;
    } else {
      var m = map();
      if (node.debugId() != 0)
        m.put("x", node.debugId());
      if (node.left() != null || node.right() != null) {
        boolean splitDimension = bounds.width > bounds.height;
        float s = splitCoordinate(splitDimension, bounds);
        var recurseBounds = calcSubdivisionBounds(splitDimension, bounds, s);
        for (int i = 0; i < 2; i++) {
          var child = node.child(i);
          if (child != null) {
            var m2 = auxDump(child, recurseBounds[i]);
            m.put(i == 0 ? "L" : "R", m2);
          }
        }
      }
      return m;
    }
  }


  private int subtreeNodeCount(QNode parent) {
    if (parent == null) return 0;
    return 1 + subtreeNodeCount(parent.left()) + subtreeNodeCount(parent.right());
  }

  public JSMap auxInfo() {
    var m = map();
    m.put("height", treeHeight(mRoot));
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

  private int treeHeight(QNode node) {
    if (node == null)
      return 0;
    return 1 + Math.max(treeHeight(node.left()), treeHeight(node.right()));
  }
}

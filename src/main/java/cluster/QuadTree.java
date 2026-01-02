package cluster;

import js.base.BaseObject;
import js.data.IntArray;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.QtreeParam;
import js.json.JSObject;

import java.util.Arrays;
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
    todo("!assign a unique id number to each leaf node, for more compact serialization; but not necessary if done at serialization stage");
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
//    log("findSegments, inputBounds:", INDENT, inputBounds);
    prepare();
    mQueryResultPairs.clear();
    mQueryInputBounds = inputBounds;
    auxFind(0, mRoot, mRootBounds);

//    log("number of segs found:", mQueryResultPairs.size());
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
    // I can't use the FRect.intersects function, since it may fail with vertical or horizontal
    // road segments (which can produce zero-area rects)
    return (a.x <= b.endX() && a.endX() >= b.x && a.y <= b.endY() && a.endY() >= b.y);
  }

  private void auxFind(int depth, QNode qNode, FRect bounds) {
//    if (verbose())
//      log(TAB(depth * 2), "bounds:", bounds);

    //pr("checking if:", mQueryInputBounds, CR, "touches:", bounds);
    if (!rectsTouch(mQueryInputBounds, bounds)) {
//      log("....doesn't intersect");
      return;
    }
//    pr("...yes");

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

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------

  private QNode splitNode(final QNode node, int depth, FRect bounds) {
    log("splitNodeSet, pop:", node.population(), "bounds:", bounds, "depth:", depth);

    checkArgument(node.isLeaf());
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
      }
    }
    checkState(depth < 50, "recurse depth limit exceeded");

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

      rootNode.mDebugId = 50 + uniqueLeafMap.size();
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

  private void postOrderTraversal(QNode subtree, List<QNode> result) {
    if (subtree == null) return;
    pr("postOrderTraversal, node:", INDENT, subtree.toJson());
    postOrderTraversal(subtree.left(), result);
    postOrderTraversal(subtree.right(), result);
    pr("...adding node:", INDENT, subtree.toJson());
    result.add(subtree);
  }

  public JSMap serialize() {

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
      pr("..........ASSIGNING UNIQUE ID:", nextId, "TO:", INDENT, n);
      nextId++;
    }

    var serializedNodeInts = IntArray.newBuilder();
    QNode forAssertionOnlylastNodeSerialized = null;


    pr(VERT_SP, "now serializing nodes");
    // Serialize the nodes, skipping those that have already been serialized
    int lastSerializedId = 0;
    for (var n : traversal) {
      pr("ser id:", n.serializationId(), INDENT, n);
      if (n.serializationId() <= lastSerializedId)
        continue;
      lastSerializedId = n.serializationId();
      pr("...updating serializedId to", lastSerializedId);
      forAssertionOnlylastNodeSerialized = n;


      if (!n.isLeaf()) {
        serializedNodeInts.add(nodeIdOrZero(n.left()));
        serializedNodeInts.add(nodeIdOrZero(n.right()));
      } else {
        var segs = n.segments();
        int numSeg = segs.size() / 2;
        serializedNodeInts.add(-numSeg - 1);
        for (int i = 0; i < segs.size(); i++) {
          serializedNodeInts.add(segs.get(i));
        }
      }
    }
    checkState(forAssertionOnlylastNodeSerialized == mRoot);

    pr("serialized nodes to:", INDENT, serializedNodeInts);

    var m = map();
    m.put(SER_KEY_NODES, serializedNodeInts.toJson());
    m.put(SER_KEY_ROOT_BOUNDS, mRootBounds.toJson());
    m.put(SER_KEY_POINTS, mPointSet.serialize());
    m.put(SER_KEY_PARAM, mParam.toJson());
    return m;
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
    pr(VERT_SP, "deserialize:", INDENT, m);
    var nodes = m.getList(SER_KEY_NODES).asIntArray();

    var i = 0;

    List<QNode> constructedNodes = arrayList();

    while (i < nodes.length) {
      pr("next ints:",nodes[i],nodes[i+1]);
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
        var y = nodes[i++];
        pr("getting nodes for x:",x,"and y:",y);
        var q = new QNode(optNode(constructedNodes,x), optNode(constructedNodes,y));
        constructedNodes.add(q);
      }
    }
    QNode root = last(constructedNodes);


//
//    var interiorCount = nodes[i++];
//    pr("deserializing interior nodes, count:",interiorCount);
//
//    for (int j = 0; j < interiorCount; j++) {
//      show("left,right", nodes, i);
//      var leftId = nodes[i++];
//      var rightId = nodes[i++];
//
//      // This is to be an internal node, but temporarily represent it as a leaf node,
//      // with a single segment, whose endpoints are the ids of the left and right child
//      var q = new QNode();
//      q.addSegment(leftId, rightId);
//      constructedNodes.add(q);
//    }
//
//    var leafCount = nodes[i++];
//    pr("deserializing leaf nodes, count:",leafCount);
//    for (int j = 0; j < leafCount; j++) {
//      show("numsegs", nodes, i);
//      int numSegs = nodes[i++];
//      var q = new QNode();
//      for (int k = 0; k < numSegs; k++) {
//        show("point ids", nodes, i);
//        var p1 = nodes[i++];
//        var p2 = nodes[i++];
//        q.addSegment(p1, p2);
//      }
//      constructedNodes.add(q);
//    }
//
//    // Now replace the (temporarily) leaf nodes with interior nodes
//    for (int j = 0; j < interiorCount; j++) {
//      var q = constructedNodes.get(j);
//
//      pr("...converting temporary leaf node back to interior node");
//
//      QNode[] children = new QNode[2];
//      for (int k = 0; k < 2; k++) {
//        var id = q.segments().get(k);
//        pr("...id:",id);
//        if (id != 0)
//          children[k] = constructedNodes.get(id - 1);
//      }
//
//      var interiorNode = new QNode(children[0], children[1]);
//      constructedNodes.set(j, interiorNode);
//      pr("converted leaf back to interior node:",INDENT,interiorNode);
//    }

    var rootBounds = FRect.DEFAULT_INSTANCE.parse(m.getList(SER_KEY_ROOT_BOUNDS));
    var points = PointSet.deserialize(m.getList(SER_KEY_POINTS));
    var param = m.getUnsafe(SER_KEY_PARAM);
    var params = QtreeParam.DEFAULT_INSTANCE.parse(param);
    var qt = new QuadTree(params, points);
    qt.mRoot = root;
    qt.mRootBounds = rootBounds;
    return qt;
  }

  private static void show(String prompt, int[] n, int offset) {
    pr("reading:", prompt, Arrays.copyOfRange(n, offset, n.length));
  }

  private static QNode optNode(List<QNode> list, int id) {
    if (id == 0) return null;
    return list.get(id-1);
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
      if (node.mDebugId != 0)
        m.put("x", node.mDebugId);

      var x = list();
//      if (node.mDebugId != 0)
//        x.add("x").add( node.mDebugId);
//      x.add("w").add(bounds.width).add("h").add(bounds.height);
      //m.put("pop", node.population());
      for (int j : node.segments().array())
        x.add(j);
      m.put("segs", x);
      return m;
    } else {
      var m = map();
      if (node.mDebugId != 0)
        m.put("x", node.mDebugId);
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

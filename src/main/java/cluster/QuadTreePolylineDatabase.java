package cluster;

import js.base.BaseObject;
import js.geometry.FPoint;
import js.geometry.FRect;
import js.json.JSMap;
import cluster.gen.Node;
import cluster.gen.NodeSet;
import cluster.gen.QtreeParam;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static cluster.MatchUtil.*;
// import static geom.MatchUtil.*;

import static js.base.Tools.*;

public class QuadTreePolylineDatabase extends BaseObject {
  private static final boolean db = false && alert("Verbose is on in QuadTree");


  public Node worldToPixelSpace(Node node) {
    var b = node.build().toBuilder();
//    List<FPoint> pixelVertices = arrayList();
//    for (var worldPt : b.vertices()) {
    {
      var worldPt = b.a();
      var np = new FPoint((worldPt.x - mOrigin.x) * GEO_TO_PIXEL_SCALE_FACTOR,
          (worldPt.y - mOrigin.y) * GEO_TO_PIXEL_SCALE_FACTOR);
      b.a(np);
    }
    {
      var worldPt = b.b();
      var np = new FPoint((worldPt.x - mOrigin.x) * GEO_TO_PIXEL_SCALE_FACTOR,
          (worldPt.y - mOrigin.y) * GEO_TO_PIXEL_SCALE_FACTOR);
      b.b(np);

    }

//      pixelVertices.add(np);
//    }
//    b.vertices(pixelVertices);
    return b.build();
  }

  public QuadTreePolylineDatabase(NodeSet nodeSet, QtreeParam paramOrNull) {
//    nodeSet = filterInvalid(nodeSet);
    checkArgument(nodeSet.nodes().size() != 0, "node set is empty");
    mParam = nullTo(paramOrNull, QtreeParam.DEFAULT_INSTANCE).build();
    mOrigin = nodeSet.origin();
    mNodeSet = MatchUtil.normalize(nodeSet);
  }

  private void prepare() {
    if (mRoot == null) {
      var p1 = mNodeSet.nodes().get(0).a();
      if (db)
        checkArgument(p1.x < 20000, "improperly transformed nodeset:", p1);

      construct(mNodeSet);
      // We can throw out the node set since it is no longer needed
      mNodeSet = null;
    }
  }

  public List<Node> findCandidates(Node pixelInputPolyline, int padding) {
    prepare();

    var inputBounds = MatchUtil.bounds(pixelInputPolyline).withInset(-padding);

    if (db)
      pr("inputBounds:", INDENT, inputBounds);

    mQueryResultSet.clear();
    mQueryInputBounds = inputBounds;
    mDepth = 0;
    auxFind(mRoot, mRootBounds);

    // We stored the collected candidates in a set, to remove duplicates.  This is leveraging the
    // datagen-produced data class for Node, which supplies hashcode/equals etc.

    // Copy the remaining (unique) elements to an array for output.

    List<Node> out = arrayList();
    out.addAll(mQueryResultSet);

//    if (alert("sorting candidates")) {
//      out.sort(NODE_COMPARATOR);
//    }
    if (db)
      pr("candidates:", out);
    return out;
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
      for (var c : qNode.polylines()) {
        var cr = MatchUtil.bounds(c);
        if (mQueryInputBounds.intersects(cr)) {
          mQueryResultSet.add(c);
        }
      }
    }
    mDepth--;
  }

  private static class QNode {

    private QNode mLeftChild, mRightChild; // Pointers to child nodes

    // If this is a leaf node, this will contain the polylines; at present, these are "Node"s, but
    // maybe later we'll use a more optimized datastructure
    private List<Node> mNodes;

    QNode() {

    }

    QNode left() {
      return mLeftChild;
    }

    QNode right() {
      return mRightChild;
    }

    List<Node> polylines() {
      todo("rename this to nodes?");
      return mNodes;
    }

    FRect calculateBounds() {
      checkArgument(population() != 0);
      return MatchUtil.calcBounds(mNodes);
    }

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

    public void addPolylines(Collection<Node> nodes) {
      if (mNodes == null)
        mNodes = arrayList();
      mNodes.addAll(nodes);
    }

    // Returns the number of polylines stored in this node (not in the rest of the subtree though)
    public int population() {
      if (mNodes == null)
        return 0;
      return mNodes.size();
    }

    public void addPolyline(Node n) {
      if (mNodes == null)
        mNodes = arrayList();
      mNodes.add(n);
    }

    public void discardPolylines() {
      mNodes = null;
    }

    public void setLeftChild(QNode child) {
      mLeftChild = child;
    }

    public void setRightChild(QNode child) {
      mRightChild = child;
    }

    public void assertUseful() {
      if (mLeftChild == null || mRightChild == null && mNodes == null)
        badState("QuadTree node is not useful:", INDENT, toJson());
    }

    private static int sDebugIndex;
    private int debugIndex;
  }

  //-------------------------------------------------------------------------
  // Construction
  //-------------------------------------------------------------------------

  /**
   * Construct the QuadTree
   */
  private void construct(NodeSet nodeSet) {
    QNode.sDebugIndex = 100;
    // Build a leaf QNode that contains all the nodes, to be recursively split
    var qn = new QNode();
    qn.addPolylines(nodeSet.nodes());
    mDepth = 0;

    // Calculate the bounds of all of the nodes, and pass that into the split node method

    var bounds = qn.calculateBounds();
    mRootBounds = bounds;
    mRoot = splitNodeSet(qn, bounds);
  }

  private QNode splitNodeSet(final QNode node, FRect bounds) {

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

      for (var n : node.polylines()) {
        var b = MatchUtil.bounds(n);

        // Add polyline to each child node that it intersects
        var isect = calcIntersectFlags(splitDimension, s, b);
        var isectL = (isect & 1) != 0;
        var isectR = (isect & 2) != 0;
        if (isectL) {
          qL.addPolyline(n);
        }
        if (isectR) {
          qR.addPolyline(n);
        }
      }

      // We need a recursion stopping criterion to avoid infinite descent for large intermingled polylines
      // that can't be separated by further subdivisions

      var leftPop = qL.population();
      var rightPop = qR.population();
      var max = Math.max(leftPop, rightPop);
      var min = Math.min(leftPop, rightPop);

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
      if (x != qL)
        x.assertUseful();
      node.setLeftChild(x);
    }
    if (qR.population() != 0) {
      var x = splitNodeSet(qR, recurseBounds[1]);
      if (x != qR)
        x.assertUseful();
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

  private NodeSet mNodeSet;
  private FPoint mOrigin;
  private QNode mRoot;
  private FRect mRootBounds;

  // Used for recursing during constructing Quadtree

  private FRect mQueryInputBounds;
  private Set<Node> mQueryResultSet = hashSet();
  private int mDepth;
  private QtreeParam mParam;
}

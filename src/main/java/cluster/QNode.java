package cluster;

import js.data.IntArray;
import js.json.JSMap;

import static js.base.Tools.*;

public class QNode {

  private QNode mLeftChild, mRightChild; // Pointers to child nodes

  // Storage for start+end endpoint ids, if this is a leaf node; otherwise, null
  private IntArray mSegments;

  private int mSerializationId;

  public void setSerializationId(int id) {
    mSerializationId = id;
  }

  public int serializationId() {
    return mSerializationId;
  }

  /**
   * Construct a leaf node
   */
  QNode() {
    mSegments = IntArray.newBuilder();
  }

  /**
   * Construct an interior node
   */
  QNode(QNode left, QNode right) {
    checkArgument(left != null || right != null);
    mLeftChild = left;
    mRightChild = right;
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

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  public JSMap toJson() {
    var m = map();
    m.put("pop", population());
    if (mLeftChild != null)
      m.put("cLeft", true);
    if (mRightChild != null)
      m.put("cRight", true);
    return m;
  }

  // Returns the number of segments stored in this leaf node
  public int population() {
    return segments().size() / 2;
  }

  public void freeze() {
    mSegments = segments().build();
  }
}

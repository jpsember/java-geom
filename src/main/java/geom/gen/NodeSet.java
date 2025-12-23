package geom.gen;

import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.FPoint;
import js.json.JSList;
import js.json.JSMap;

public class NodeSet implements AbstractData {

  public List<Node> nodes() {
    return mNodes;
  }

  public FPoint origin() {
    return mOrigin;
  }

  public FPoint size() {
    return mSize;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "nodes";
  protected static final String _1 = "origin";
  protected static final String _2 = "size";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSList j = new JSList();
      for (Node x : mNodes)
        j.add(x.toJson());
      m.put(_0, j);
    }
    m.putUnsafe(_1, mOrigin.toJson());
    m.putUnsafe(_2, mSize.toJson());
    return m;
  }

  @Override
  public NodeSet build() {
    return this;
  }

  @Override
  public NodeSet parse(Object obj) {
    return new NodeSet((JSMap) obj);
  }

  private NodeSet(JSMap m) {
    mNodes = DataUtil.parseListOfObjects(Node.DEFAULT_INSTANCE, m.optJSList(_0), false);
    {
      mOrigin = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mOrigin = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mSize = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mSize = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof NodeSet))
      return false;
    NodeSet other = (NodeSet) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mNodes.equals(other.mNodes)))
      return false;
    if (!(mOrigin.equals(other.mOrigin)))
      return false;
    if (!(mSize.equals(other.mSize)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      for (Node x : mNodes)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mOrigin.hashCode();
      r = r * 37 + mSize.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected List<Node> mNodes;
  protected FPoint mOrigin;
  protected FPoint mSize;
  protected int m__hashcode;

  public static final class Builder extends NodeSet {

    private Builder(NodeSet m) {
      mNodes = DataUtil.mutableCopyOf(m.mNodes);
      mOrigin = m.mOrigin;
      mSize = m.mSize;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public NodeSet build() {
      NodeSet r = new NodeSet();
      r.mNodes = DataUtil.immutableCopyOf(mNodes);
      r.mOrigin = mOrigin;
      r.mSize = mSize;
      return r;
    }

    public Builder nodes(List<Node> x) {
      mNodes = (x == null) ? new ArrayList(0) : x;
      return this;
    }

    public Builder origin(FPoint x) {
      mOrigin = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder size(FPoint x) {
      mSize = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final NodeSet DEFAULT_INSTANCE = new NodeSet();

  private NodeSet() {
    mNodes = DataUtil.emptyList();
    mOrigin = FPoint.DEFAULT_INSTANCE;
    mSize = FPoint.DEFAULT_INSTANCE;
  }

}

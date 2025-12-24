package cluster.gen;

import js.data.AbstractData;
import js.geometry.FPoint;
import js.json.JSMap;

public class Node implements AbstractData {

  public FPoint a() {
    return mA;
  }

  public FPoint b() {
    return mB;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "a";
  protected static final String _1 = "b";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mA.toJson());
    m.putUnsafe(_1, mB.toJson());
    return m;
  }

  @Override
  public Node build() {
    return this;
  }

  @Override
  public Node parse(Object obj) {
    return new Node((JSMap) obj);
  }

  private Node(JSMap m) {
    {
      mA = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mA = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mB = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mB = FPoint.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof Node))
      return false;
    Node other = (Node) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mA.equals(other.mA)))
      return false;
    if (!(mB.equals(other.mB)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mA.hashCode();
      r = r * 37 + mB.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected FPoint mA;
  protected FPoint mB;
  protected int m__hashcode;

  public static final class Builder extends Node {

    private Builder(Node m) {
      mA = m.mA;
      mB = m.mB;
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
    public Node build() {
      Node r = new Node();
      r.mA = mA;
      r.mB = mB;
      return r;
    }

    public Builder a(FPoint x) {
      mA = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder b(FPoint x) {
      mB = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final Node DEFAULT_INSTANCE = new Node();

  private Node() {
    mA = FPoint.DEFAULT_INSTANCE;
    mB = FPoint.DEFAULT_INSTANCE;
  }

}

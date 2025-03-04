package geom.gen;

import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.FPoint;
import js.json.JSList;
import js.json.JSMap;

public class Node implements AbstractData {

  public String id() {
    return mId;
  }

  public String description() {
    return mDescription;
  }

  public List<FPoint> vertices() {
    return mVertices;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "id";
  protected static final String _1 = "description";
  protected static final String _2 = "vertices";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mId);
    m.putUnsafe(_1, mDescription);
    {
      JSList j = new JSList();
      for (FPoint x : mVertices)
        j.add(x.toJson());
      m.put(_2, j);
    }
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
    mId = m.opt(_0, "");
    mDescription = m.opt(_1, "");
    mVertices = DataUtil.parseListOfObjects(FPoint.DEFAULT_INSTANCE, m.optJSList(_2), false);
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
    if (!(mId.equals(other.mId)))
      return false;
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(mVertices.equals(other.mVertices)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mId.hashCode();
      r = r * 37 + mDescription.hashCode();
      for (FPoint x : mVertices)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mId;
  protected String mDescription;
  protected List<FPoint> mVertices;
  protected int m__hashcode;

  public static final class Builder extends Node {

    private Builder(Node m) {
      mId = m.mId;
      mDescription = m.mDescription;
      mVertices = DataUtil.mutableCopyOf(m.mVertices);
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
      r.mId = mId;
      r.mDescription = mDescription;
      r.mVertices = DataUtil.immutableCopyOf(mVertices);
      return r;
    }

    public Builder id(String x) {
      mId = (x == null) ? "" : x;
      return this;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
    }

    public Builder vertices(List<FPoint> x) {
      mVertices = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final Node DEFAULT_INSTANCE = new Node();

  private Node() {
    mId = "";
    mDescription = "";
    mVertices = DataUtil.emptyList();
  }

}

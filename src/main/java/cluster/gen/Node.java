package cluster.gen;

import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.FPoint;
import js.json.JSList;
import js.json.JSMap;

public class Node implements AbstractData {

  @Deprecated
  public String id() {
    return mId;
  }

  @Deprecated
  public String description() {
    return mDescription;
  }

  public FPoint a() {
    return mA;
  }

  public FPoint b() {
    return mB;
  }

  @Deprecated
  public List<FPoint> vertices() {
    return mVertices;
  }

  @Deprecated
  public List<String> originalColumnContents() {
    return mOriginalColumnContents;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "id";
  protected static final String _1 = "description";
  protected static final String _2 = "a";
  protected static final String _3 = "b";
  protected static final String _4 = "vertices";
  protected static final String _5 = "original_column_contents";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mId);
    m.putUnsafe(_1, mDescription);
    m.putUnsafe(_2, mA.toJson());
    m.putUnsafe(_3, mB.toJson());
    {
      JSList j = new JSList();
      for (FPoint x : mVertices)
        j.add(x.toJson());
      m.put(_4, j);
    }
    {
      JSList j = new JSList();
      for (String x : mOriginalColumnContents)
        j.add(x);
      m.put(_5, j);
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
    {
      mA = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mA = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mB = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mB = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    mVertices = DataUtil.parseListOfObjects(FPoint.DEFAULT_INSTANCE, m.optJSList(_4), false);
    mOriginalColumnContents = DataUtil.parseListOfObjects(m.optJSList(_5), false);
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
    if (!(mA.equals(other.mA)))
      return false;
    if (!(mB.equals(other.mB)))
      return false;
    if (!(mVertices.equals(other.mVertices)))
      return false;
    if (!(mOriginalColumnContents.equals(other.mOriginalColumnContents)))
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
      r = r * 37 + mA.hashCode();
      r = r * 37 + mB.hashCode();
      for (FPoint x : mVertices)
        if (x != null)
          r = r * 37 + x.hashCode();
      for (String x : mOriginalColumnContents)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mId;
  protected String mDescription;
  protected FPoint mA;
  protected FPoint mB;
  protected List<FPoint> mVertices;
  protected List<String> mOriginalColumnContents;
  protected int m__hashcode;

  public static final class Builder extends Node {

    private Builder(Node m) {
      mId = m.mId;
      mDescription = m.mDescription;
      mA = m.mA;
      mB = m.mB;
      mVertices = DataUtil.mutableCopyOf(m.mVertices);
      mOriginalColumnContents = DataUtil.mutableCopyOf(m.mOriginalColumnContents);
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
      r.mA = mA;
      r.mB = mB;
      r.mVertices = DataUtil.immutableCopyOf(mVertices);
      r.mOriginalColumnContents = DataUtil.immutableCopyOf(mOriginalColumnContents);
      return r;
    }

    @Deprecated
    public Builder id(String x) {
      mId = (x == null) ? "" : x;
      return this;
    }

    @Deprecated
    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
    }

    public Builder a(FPoint x) {
      mA = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder b(FPoint x) {
      mB = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    @Deprecated
    public Builder vertices(List<FPoint> x) {
      mVertices = (x == null) ? new ArrayList(0) : x;
      return this;
    }

    @Deprecated
    public Builder originalColumnContents(List<String> x) {
      mOriginalColumnContents = (x == null) ? new ArrayList(0) : x;
      return this;
    }

  }

  public static final Node DEFAULT_INSTANCE = new Node();

  private Node() {
    mId = "";
    mDescription = "";
    mA = FPoint.DEFAULT_INSTANCE;
    mB = FPoint.DEFAULT_INSTANCE;
    mVertices = DataUtil.emptyList();
    mOriginalColumnContents = DataUtil.emptyList();
  }

}

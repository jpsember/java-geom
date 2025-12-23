package cluster.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class QtreeParam implements AbstractData {

  public int maxNodeCapacity() {
    return mMaxNodeCapacity;
  }

  public int padding() {
    return mPadding;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "max_node_capacity";
  protected static final String _1 = "padding";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mMaxNodeCapacity);
    m.putUnsafe(_1, mPadding);
    return m;
  }

  @Override
  public QtreeParam build() {
    return this;
  }

  @Override
  public QtreeParam parse(Object obj) {
    return new QtreeParam((JSMap) obj);
  }

  private QtreeParam(JSMap m) {
    mMaxNodeCapacity = m.opt(_0, 4);
    mPadding = m.opt(_1, 15);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof QtreeParam))
      return false;
    QtreeParam other = (QtreeParam) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mMaxNodeCapacity == other.mMaxNodeCapacity))
      return false;
    if (!(mPadding == other.mPadding))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mMaxNodeCapacity;
      r = r * 37 + mPadding;
      m__hashcode = r;
    }
    return r;
  }

  protected int mMaxNodeCapacity;
  protected int mPadding;
  protected int m__hashcode;

  public static final class Builder extends QtreeParam {

    private Builder(QtreeParam m) {
      mMaxNodeCapacity = m.mMaxNodeCapacity;
      mPadding = m.mPadding;
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
    public QtreeParam build() {
      QtreeParam r = new QtreeParam();
      r.mMaxNodeCapacity = mMaxNodeCapacity;
      r.mPadding = mPadding;
      return r;
    }

    public Builder maxNodeCapacity(int x) {
      mMaxNodeCapacity = x;
      return this;
    }

    public Builder padding(int x) {
      mPadding = x;
      return this;
    }

  }

  public static final QtreeParam DEFAULT_INSTANCE = new QtreeParam();

  private QtreeParam() {
    mMaxNodeCapacity = 4;
    mPadding = 15;
  }

}

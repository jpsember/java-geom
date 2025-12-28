package cluster.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class QtreeParam implements AbstractData {

  public int targetNodeMaxPop() {
    return mTargetNodeMaxPop;
  }

  public float minNodeDimension() {
    return mMinNodeDimension;
  }

  public boolean disableFruitlessRewrite() {
    return mDisableFruitlessRewrite;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "target_node_max_pop";
  protected static final String _1 = "min_node_dimension";
  protected static final String _2 = "disable_fruitless_rewrite";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mTargetNodeMaxPop);
    m.putUnsafe(_1, mMinNodeDimension);
    m.putUnsafe(_2, mDisableFruitlessRewrite);
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
    mTargetNodeMaxPop = m.opt(_0, 4);
    mMinNodeDimension = m.opt(_1, 2.7E-4f);
    mDisableFruitlessRewrite = m.opt(_2, false);
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
    if (!(mTargetNodeMaxPop == other.mTargetNodeMaxPop))
      return false;
    if (!(mMinNodeDimension == other.mMinNodeDimension))
      return false;
    if (!(mDisableFruitlessRewrite == other.mDisableFruitlessRewrite))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mTargetNodeMaxPop;
      r = r * 37 + (int)mMinNodeDimension;
      r = r * 37 + (mDisableFruitlessRewrite ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected int mTargetNodeMaxPop;
  protected float mMinNodeDimension;
  protected boolean mDisableFruitlessRewrite;
  protected int m__hashcode;

  public static final class Builder extends QtreeParam {

    private Builder(QtreeParam m) {
      mTargetNodeMaxPop = m.mTargetNodeMaxPop;
      mMinNodeDimension = m.mMinNodeDimension;
      mDisableFruitlessRewrite = m.mDisableFruitlessRewrite;
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
      r.mTargetNodeMaxPop = mTargetNodeMaxPop;
      r.mMinNodeDimension = mMinNodeDimension;
      r.mDisableFruitlessRewrite = mDisableFruitlessRewrite;
      return r;
    }

    public Builder targetNodeMaxPop(int x) {
      mTargetNodeMaxPop = x;
      return this;
    }

    public Builder minNodeDimension(float x) {
      mMinNodeDimension = x;
      return this;
    }

    public Builder disableFruitlessRewrite(boolean x) {
      mDisableFruitlessRewrite = x;
      return this;
    }

  }

  public static final QtreeParam DEFAULT_INSTANCE = new QtreeParam();

  private QtreeParam() {
    mTargetNodeMaxPop = 4;
    mMinNodeDimension = 2.7E-4f;
  }

}

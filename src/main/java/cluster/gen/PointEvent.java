package cluster.gen;

import js.data.AbstractData;
import js.geometry.FPoint;
import js.json.JSMap;

public class PointEvent implements AbstractData {

  public FPoint location() {
    return mLocation;
  }

  public float zLoc() {
    return mZLoc;
  }

  public int colorCode() {
    return mColorCode;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "location";
  protected static final String _1 = "z_loc";
  protected static final String _2 = "color_code";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mLocation.toJson());
    m.putUnsafe(_1, mZLoc);
    m.putUnsafe(_2, mColorCode);
    return m;
  }

  @Override
  public PointEvent build() {
    return this;
  }

  @Override
  public PointEvent parse(Object obj) {
    return new PointEvent((JSMap) obj);
  }

  private PointEvent(JSMap m) {
    {
      mLocation = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mLocation = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    mZLoc = m.opt(_1, 0f);
    mColorCode = m.opt(_2, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof PointEvent))
      return false;
    PointEvent other = (PointEvent) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mLocation.equals(other.mLocation)))
      return false;
    if (!(mZLoc == other.mZLoc))
      return false;
    if (!(mColorCode == other.mColorCode))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mLocation.hashCode();
      r = r * 37 + (int)mZLoc;
      r = r * 37 + mColorCode;
      m__hashcode = r;
    }
    return r;
  }

  protected FPoint mLocation;
  protected float mZLoc;
  protected int mColorCode;
  protected int m__hashcode;

  public static final class Builder extends PointEvent {

    private Builder(PointEvent m) {
      mLocation = m.mLocation;
      mZLoc = m.mZLoc;
      mColorCode = m.mColorCode;
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
    public PointEvent build() {
      PointEvent r = new PointEvent();
      r.mLocation = mLocation;
      r.mZLoc = mZLoc;
      r.mColorCode = mColorCode;
      return r;
    }

    public Builder location(FPoint x) {
      mLocation = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder zLoc(float x) {
      mZLoc = x;
      return this;
    }

    public Builder colorCode(int x) {
      mColorCode = x;
      return this;
    }

  }

  public static final PointEvent DEFAULT_INSTANCE = new PointEvent();

  private PointEvent() {
    mLocation = FPoint.DEFAULT_INSTANCE;
  }

}

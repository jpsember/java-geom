package cluster.gen;

import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.FPoint;
import js.json.JSList;
import js.json.JSMap;

public class RoadNetwork implements AbstractData {

  public List<RoadSegment> roadSegments() {
    return mRoadSegments;
  }

  public FPoint origin() {
    return mOrigin;
  }

  public FPoint size() {
    return mSize;
  }

  public FPoint translate() {
    return mTranslate;
  }

  public FPoint scale() {
    return mScale;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "road_segments";
  protected static final String _1 = "origin";
  protected static final String _2 = "size";
  protected static final String _3 = "translate";
  protected static final String _4 = "scale";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSList j = new JSList();
      for (RoadSegment x : mRoadSegments)
        j.add(x.toJson());
      m.put(_0, j);
    }
    m.putUnsafe(_1, mOrigin.toJson());
    m.putUnsafe(_2, mSize.toJson());
    m.putUnsafe(_3, mTranslate.toJson());
    m.putUnsafe(_4, mScale.toJson());
    return m;
  }

  @Override
  public RoadNetwork build() {
    return this;
  }

  @Override
  public RoadNetwork parse(Object obj) {
    return new RoadNetwork((JSMap) obj);
  }

  private RoadNetwork(JSMap m) {
    mRoadSegments = DataUtil.parseListOfObjects(RoadSegment.DEFAULT_INSTANCE, m.optJSList(_0), false);
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
    {
      mTranslate = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mTranslate = FPoint.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mScale = FPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_4);
      if (x != null) {
        mScale = FPoint.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof RoadNetwork))
      return false;
    RoadNetwork other = (RoadNetwork) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mRoadSegments.equals(other.mRoadSegments)))
      return false;
    if (!(mOrigin.equals(other.mOrigin)))
      return false;
    if (!(mSize.equals(other.mSize)))
      return false;
    if (!(mTranslate.equals(other.mTranslate)))
      return false;
    if (!(mScale.equals(other.mScale)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      for (RoadSegment x : mRoadSegments)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mOrigin.hashCode();
      r = r * 37 + mSize.hashCode();
      r = r * 37 + mTranslate.hashCode();
      r = r * 37 + mScale.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected List<RoadSegment> mRoadSegments;
  protected FPoint mOrigin;
  protected FPoint mSize;
  protected FPoint mTranslate;
  protected FPoint mScale;
  protected int m__hashcode;

  public static final class Builder extends RoadNetwork {

    private Builder(RoadNetwork m) {
      mRoadSegments = DataUtil.mutableCopyOf(m.mRoadSegments);
      mOrigin = m.mOrigin;
      mSize = m.mSize;
      mTranslate = m.mTranslate;
      mScale = m.mScale;
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
    public RoadNetwork build() {
      RoadNetwork r = new RoadNetwork();
      r.mRoadSegments = DataUtil.immutableCopyOf(mRoadSegments);
      r.mOrigin = mOrigin;
      r.mSize = mSize;
      r.mTranslate = mTranslate;
      r.mScale = mScale;
      return r;
    }

    public Builder roadSegments(List<RoadSegment> x) {
      mRoadSegments = (x == null) ? new ArrayList(0) : x;
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

    public Builder translate(FPoint x) {
      mTranslate = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder scale(FPoint x) {
      mScale = (x == null) ? FPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final RoadNetwork DEFAULT_INSTANCE = new RoadNetwork();

  private RoadNetwork() {
    mRoadSegments = DataUtil.emptyList();
    mOrigin = FPoint.DEFAULT_INSTANCE;
    mSize = FPoint.DEFAULT_INSTANCE;
    mTranslate = FPoint.DEFAULT_INSTANCE;
    mScale = FPoint.DEFAULT_INSTANCE;
  }

}

package geom.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class MatchParam implements AbstractData {

  public int maxCost() {
    return mMaxCost;
  }

  public boolean simplifyInputPolyline() {
    return mSimplifyInputPolyline;
  }

  public int inputPolylineSimplifyFactor() {
    return mInputPolylineSimplifyFactor;
  }

  public boolean sliceEnabled() {
    return mSliceEnabled;
  }

  public int sliceLength() {
    return mSliceLength;
  }

  public int inputSegmentMinLength() {
    return mInputSegmentMinLength;
  }

  public int candidateSegmentMinLength() {
    return mCandidateSegmentMinLength;
  }

  public boolean angleCostEnabled() {
    return mAngleCostEnabled;
  }

  public float angleCostExponent() {
    return mAngleCostExponent;
  }

  public float angleCostMultiplier() {
    return mAngleCostMultiplier;
  }

  public boolean distanceCostEnabled() {
    return mDistanceCostEnabled;
  }

  public float distanceCostExponent() {
    return mDistanceCostExponent;
  }

  public float distanceCostMultiplier() {
    return mDistanceCostMultiplier;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "max_cost";
  protected static final String _1 = "simplify_input_polyline";
  protected static final String _2 = "input_polyline_simplify_factor";
  protected static final String _3 = "slice_enabled";
  protected static final String _4 = "slice_length";
  protected static final String _5 = "input_segment_min_length";
  protected static final String _6 = "candidate_segment_min_length";
  protected static final String _7 = "angle_cost_enabled";
  protected static final String _8 = "angle_cost_exponent";
  protected static final String _9 = "angle_cost_multiplier";
  protected static final String _10 = "distance_cost_enabled";
  protected static final String _11 = "distance_cost_exponent";
  protected static final String _12 = "distance_cost_multiplier";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mMaxCost);
    m.putUnsafe(_1, mSimplifyInputPolyline);
    m.putUnsafe(_2, mInputPolylineSimplifyFactor);
    m.putUnsafe(_3, mSliceEnabled);
    m.putUnsafe(_4, mSliceLength);
    m.putUnsafe(_5, mInputSegmentMinLength);
    m.putUnsafe(_6, mCandidateSegmentMinLength);
    m.putUnsafe(_7, mAngleCostEnabled);
    m.putUnsafe(_8, mAngleCostExponent);
    m.putUnsafe(_9, mAngleCostMultiplier);
    m.putUnsafe(_10, mDistanceCostEnabled);
    m.putUnsafe(_11, mDistanceCostExponent);
    m.putUnsafe(_12, mDistanceCostMultiplier);
    return m;
  }

  @Override
  public MatchParam build() {
    return this;
  }

  @Override
  public MatchParam parse(Object obj) {
    return new MatchParam((JSMap) obj);
  }

  private MatchParam(JSMap m) {
    mMaxCost = m.opt(_0, 200);
    mSimplifyInputPolyline = m.opt(_1, true);
    mInputPolylineSimplifyFactor = m.opt(_2, 6);
    mSliceEnabled = m.opt(_3, true);
    mSliceLength = m.opt(_4, 15);
    mInputSegmentMinLength = m.opt(_5, 2);
    mCandidateSegmentMinLength = m.opt(_6, 2);
    mAngleCostEnabled = m.opt(_7, true);
    mAngleCostExponent = m.opt(_8, 1.0f);
    mAngleCostMultiplier = m.opt(_9, 2.0f);
    mDistanceCostEnabled = m.opt(_10, true);
    mDistanceCostExponent = m.opt(_11, 2.0f);
    mDistanceCostMultiplier = m.opt(_12, 1.0f);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof MatchParam))
      return false;
    MatchParam other = (MatchParam) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mMaxCost == other.mMaxCost))
      return false;
    if (!(mSimplifyInputPolyline == other.mSimplifyInputPolyline))
      return false;
    if (!(mInputPolylineSimplifyFactor == other.mInputPolylineSimplifyFactor))
      return false;
    if (!(mSliceEnabled == other.mSliceEnabled))
      return false;
    if (!(mSliceLength == other.mSliceLength))
      return false;
    if (!(mInputSegmentMinLength == other.mInputSegmentMinLength))
      return false;
    if (!(mCandidateSegmentMinLength == other.mCandidateSegmentMinLength))
      return false;
    if (!(mAngleCostEnabled == other.mAngleCostEnabled))
      return false;
    if (!(mAngleCostExponent == other.mAngleCostExponent))
      return false;
    if (!(mAngleCostMultiplier == other.mAngleCostMultiplier))
      return false;
    if (!(mDistanceCostEnabled == other.mDistanceCostEnabled))
      return false;
    if (!(mDistanceCostExponent == other.mDistanceCostExponent))
      return false;
    if (!(mDistanceCostMultiplier == other.mDistanceCostMultiplier))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mMaxCost;
      r = r * 37 + (mSimplifyInputPolyline ? 1 : 0);
      r = r * 37 + mInputPolylineSimplifyFactor;
      r = r * 37 + (mSliceEnabled ? 1 : 0);
      r = r * 37 + mSliceLength;
      r = r * 37 + mInputSegmentMinLength;
      r = r * 37 + mCandidateSegmentMinLength;
      r = r * 37 + (mAngleCostEnabled ? 1 : 0);
      r = r * 37 + (int)mAngleCostExponent;
      r = r * 37 + (int)mAngleCostMultiplier;
      r = r * 37 + (mDistanceCostEnabled ? 1 : 0);
      r = r * 37 + (int)mDistanceCostExponent;
      r = r * 37 + (int)mDistanceCostMultiplier;
      m__hashcode = r;
    }
    return r;
  }

  protected int mMaxCost;
  protected boolean mSimplifyInputPolyline;
  protected int mInputPolylineSimplifyFactor;
  protected boolean mSliceEnabled;
  protected int mSliceLength;
  protected int mInputSegmentMinLength;
  protected int mCandidateSegmentMinLength;
  protected boolean mAngleCostEnabled;
  protected float mAngleCostExponent;
  protected float mAngleCostMultiplier;
  protected boolean mDistanceCostEnabled;
  protected float mDistanceCostExponent;
  protected float mDistanceCostMultiplier;
  protected int m__hashcode;

  public static final class Builder extends MatchParam {

    private Builder(MatchParam m) {
      mMaxCost = m.mMaxCost;
      mSimplifyInputPolyline = m.mSimplifyInputPolyline;
      mInputPolylineSimplifyFactor = m.mInputPolylineSimplifyFactor;
      mSliceEnabled = m.mSliceEnabled;
      mSliceLength = m.mSliceLength;
      mInputSegmentMinLength = m.mInputSegmentMinLength;
      mCandidateSegmentMinLength = m.mCandidateSegmentMinLength;
      mAngleCostEnabled = m.mAngleCostEnabled;
      mAngleCostExponent = m.mAngleCostExponent;
      mAngleCostMultiplier = m.mAngleCostMultiplier;
      mDistanceCostEnabled = m.mDistanceCostEnabled;
      mDistanceCostExponent = m.mDistanceCostExponent;
      mDistanceCostMultiplier = m.mDistanceCostMultiplier;
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
    public MatchParam build() {
      MatchParam r = new MatchParam();
      r.mMaxCost = mMaxCost;
      r.mSimplifyInputPolyline = mSimplifyInputPolyline;
      r.mInputPolylineSimplifyFactor = mInputPolylineSimplifyFactor;
      r.mSliceEnabled = mSliceEnabled;
      r.mSliceLength = mSliceLength;
      r.mInputSegmentMinLength = mInputSegmentMinLength;
      r.mCandidateSegmentMinLength = mCandidateSegmentMinLength;
      r.mAngleCostEnabled = mAngleCostEnabled;
      r.mAngleCostExponent = mAngleCostExponent;
      r.mAngleCostMultiplier = mAngleCostMultiplier;
      r.mDistanceCostEnabled = mDistanceCostEnabled;
      r.mDistanceCostExponent = mDistanceCostExponent;
      r.mDistanceCostMultiplier = mDistanceCostMultiplier;
      return r;
    }

    public Builder maxCost(int x) {
      mMaxCost = x;
      return this;
    }

    public Builder simplifyInputPolyline(boolean x) {
      mSimplifyInputPolyline = x;
      return this;
    }

    public Builder inputPolylineSimplifyFactor(int x) {
      mInputPolylineSimplifyFactor = x;
      return this;
    }

    public Builder sliceEnabled(boolean x) {
      mSliceEnabled = x;
      return this;
    }

    public Builder sliceLength(int x) {
      mSliceLength = x;
      return this;
    }

    public Builder inputSegmentMinLength(int x) {
      mInputSegmentMinLength = x;
      return this;
    }

    public Builder candidateSegmentMinLength(int x) {
      mCandidateSegmentMinLength = x;
      return this;
    }

    public Builder angleCostEnabled(boolean x) {
      mAngleCostEnabled = x;
      return this;
    }

    public Builder angleCostExponent(float x) {
      mAngleCostExponent = x;
      return this;
    }

    public Builder angleCostMultiplier(float x) {
      mAngleCostMultiplier = x;
      return this;
    }

    public Builder distanceCostEnabled(boolean x) {
      mDistanceCostEnabled = x;
      return this;
    }

    public Builder distanceCostExponent(float x) {
      mDistanceCostExponent = x;
      return this;
    }

    public Builder distanceCostMultiplier(float x) {
      mDistanceCostMultiplier = x;
      return this;
    }

  }

  public static final MatchParam DEFAULT_INSTANCE = new MatchParam();

  private MatchParam() {
    mMaxCost = 200;
    mSimplifyInputPolyline = true;
    mInputPolylineSimplifyFactor = 6;
    mSliceEnabled = true;
    mSliceLength = 15;
    mInputSegmentMinLength = 2;
    mCandidateSegmentMinLength = 2;
    mAngleCostEnabled = true;
    mAngleCostExponent = 1.0f;
    mAngleCostMultiplier = 2.0f;
    mDistanceCostEnabled = true;
    mDistanceCostExponent = 2.0f;
    mDistanceCostMultiplier = 1.0f;
  }

}

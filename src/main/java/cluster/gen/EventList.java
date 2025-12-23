package cluster.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class EventList implements AbstractData {

  public float sumX() {
    return mSumX;
  }

  public float sumY() {
    return mSumY;
  }

  public int population() {
    return mPopulation;
  }

  public float zLoc() {
    return mZLoc;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "sum_x";
  protected static final String _1 = "sum_y";
  protected static final String _2 = "population";
  protected static final String _3 = "z_loc";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mSumX);
    m.putUnsafe(_1, mSumY);
    m.putUnsafe(_2, mPopulation);
    m.putUnsafe(_3, mZLoc);
    return m;
  }

  @Override
  public EventList build() {
    return this;
  }

  @Override
  public EventList parse(Object obj) {
    return new EventList((JSMap) obj);
  }

  private EventList(JSMap m) {
    mSumX = m.opt(_0, 0f);
    mSumY = m.opt(_1, 0f);
    mPopulation = m.opt(_2, 0);
    mZLoc = m.opt(_3, 0f);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof EventList))
      return false;
    EventList other = (EventList) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mSumX == other.mSumX))
      return false;
    if (!(mSumY == other.mSumY))
      return false;
    if (!(mPopulation == other.mPopulation))
      return false;
    if (!(mZLoc == other.mZLoc))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (int)mSumX;
      r = r * 37 + (int)mSumY;
      r = r * 37 + mPopulation;
      r = r * 37 + (int)mZLoc;
      m__hashcode = r;
    }
    return r;
  }

  protected float mSumX;
  protected float mSumY;
  protected int mPopulation;
  protected float mZLoc;
  protected int m__hashcode;

  public static final class Builder extends EventList {

    private Builder(EventList m) {
      mSumX = m.mSumX;
      mSumY = m.mSumY;
      mPopulation = m.mPopulation;
      mZLoc = m.mZLoc;
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
    public EventList build() {
      EventList r = new EventList();
      r.mSumX = mSumX;
      r.mSumY = mSumY;
      r.mPopulation = mPopulation;
      r.mZLoc = mZLoc;
      return r;
    }

    public Builder sumX(float x) {
      mSumX = x;
      return this;
    }

    public Builder sumY(float x) {
      mSumY = x;
      return this;
    }

    public Builder population(int x) {
      mPopulation = x;
      return this;
    }

    public Builder zLoc(float x) {
      mZLoc = x;
      return this;
    }

  }

  public static final EventList DEFAULT_INSTANCE = new EventList();

  private EventList() {
  }

}

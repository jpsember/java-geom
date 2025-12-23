package cluster.gen;

import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.IRect;
import js.json.JSList;
import js.json.JSMap;

public class Tile implements AbstractData {

  public IRect bounds() {
    return mBounds;
  }

  public List<EventList> events() {
    return mEvents;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "bounds";
  protected static final String _1 = "events";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mBounds.toJson());
    {
      JSList j = new JSList();
      for (EventList x : mEvents)
        j.add(x.toJson());
      m.put(_1, j);
    }
    return m;
  }

  @Override
  public Tile build() {
    return this;
  }

  @Override
  public Tile parse(Object obj) {
    return new Tile((JSMap) obj);
  }

  private Tile(JSMap m) {
    {
      mBounds = IRect.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mBounds = IRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    mEvents = DataUtil.parseListOfObjects(EventList.DEFAULT_INSTANCE, m.optJSList(_1), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Tile))
      return false;
    Tile other = (Tile) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mBounds.equals(other.mBounds)))
      return false;
    if (!(mEvents.equals(other.mEvents)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mBounds.hashCode();
      for (EventList x : mEvents)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected IRect mBounds;
  protected List<EventList> mEvents;
  protected int m__hashcode;

  public static final class Builder extends Tile {

    private Builder(Tile m) {
      mBounds = m.mBounds;
      mEvents = DataUtil.mutableCopyOf(m.mEvents);
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
    public Tile build() {
      Tile r = new Tile();
      r.mBounds = mBounds;
      r.mEvents = DataUtil.immutableCopyOf(mEvents);
      return r;
    }

    public Builder bounds(IRect x) {
      mBounds = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder events(List<EventList> x) {
      mEvents = (x == null) ? new ArrayList(0) : x;
      return this;
    }

  }

  public static final Tile DEFAULT_INSTANCE = new Tile();

  private Tile() {
    mBounds = IRect.DEFAULT_INSTANCE;
    mEvents = DataUtil.emptyList();
  }

}

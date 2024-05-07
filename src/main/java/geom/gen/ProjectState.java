package geom.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class ProjectState implements AbstractData {

  public int version() {
    return mVersion;
  }

  public int addBoxCornerIndex() {
    return mAddBoxCornerIndex;
  }

  public JSMap widgetStateMap() {
    return mWidgetStateMap;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "version";
  protected static final String _1 = "add_box_corner_index";
  protected static final String _2 = "widget_state_map";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mVersion);
    m.putUnsafe(_1, mAddBoxCornerIndex);
    m.putUnsafe(_2, mWidgetStateMap);
    return m;
  }

  @Override
  public ProjectState build() {
    return this;
  }

  @Override
  public ProjectState parse(Object obj) {
    return new ProjectState((JSMap) obj);
  }

  private ProjectState(JSMap m) {
    mVersion = m.opt(_0, 1);
    mAddBoxCornerIndex = m.opt(_1, 2);
    {
      mWidgetStateMap = JSMap.DEFAULT_INSTANCE;
      JSMap x = m.optJSMap(_2);
      if (x != null) {
        mWidgetStateMap = x.lock();
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
    if (object == null || !(object instanceof ProjectState))
      return false;
    ProjectState other = (ProjectState) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mVersion == other.mVersion))
      return false;
    if (!(mAddBoxCornerIndex == other.mAddBoxCornerIndex))
      return false;
    if (!(mWidgetStateMap.equals(other.mWidgetStateMap)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mVersion;
      r = r * 37 + mAddBoxCornerIndex;
      r = r * 37 + mWidgetStateMap.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int mVersion;
  protected int mAddBoxCornerIndex;
  protected JSMap mWidgetStateMap;
  protected int m__hashcode;

  public static final class Builder extends ProjectState {

    private Builder(ProjectState m) {
      mVersion = m.mVersion;
      mAddBoxCornerIndex = m.mAddBoxCornerIndex;
      mWidgetStateMap = m.mWidgetStateMap;
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
    public ProjectState build() {
      ProjectState r = new ProjectState();
      r.mVersion = mVersion;
      r.mAddBoxCornerIndex = mAddBoxCornerIndex;
      r.mWidgetStateMap = mWidgetStateMap;
      return r;
    }

    public Builder version(int x) {
      mVersion = x;
      return this;
    }

    public Builder addBoxCornerIndex(int x) {
      mAddBoxCornerIndex = x;
      return this;
    }

    public Builder widgetStateMap(JSMap x) {
      mWidgetStateMap = (x == null) ? JSMap.DEFAULT_INSTANCE : x;
      return this;
    }

  }

  public static final ProjectState DEFAULT_INSTANCE = new ProjectState();

  private ProjectState() {
    mVersion = 1;
    mAddBoxCornerIndex = 2;
    mWidgetStateMap = JSMap.DEFAULT_INSTANCE;
  }

}

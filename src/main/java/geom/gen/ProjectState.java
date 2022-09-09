package geom.gen;

import js.data.AbstractData;
import js.geometry.IRect;
import js.json.JSMap;

public class ProjectState implements AbstractData {

  public int version() {
    return mVersion;
  }

  public IRect appFrame() {
    return mAppFrame;
  }

  public int currentScriptIndex() {
    return mCurrentScriptIndex;
  }

  public float zoomFactor() {
    return mZoomFactor;
  }

  public int addBoxCornerIndex() {
    return mAddBoxCornerIndex;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "version";
  protected static final String _1 = "app_frame";
  protected static final String _2 = "current_script_index";
  protected static final String _3 = "zoom_factor";
  protected static final String _4 = "add_box_corner_index";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mVersion);
    m.putUnsafe(_1, mAppFrame.toJson());
    m.putUnsafe(_2, mCurrentScriptIndex);
    m.putUnsafe(_3, mZoomFactor);
    m.putUnsafe(_4, mAddBoxCornerIndex);
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
    {
      mAppFrame = IRect.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(_1);
      if (x != null) {
        mAppFrame = IRect.DEFAULT_INSTANCE.parse(x);
      }
    }
    mCurrentScriptIndex = m.opt(_2, 0);
    mZoomFactor = m.opt(_3, 1.0f);
    mAddBoxCornerIndex = m.opt(_4, 2);
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
    if (!(mAppFrame.equals(other.mAppFrame)))
      return false;
    if (!(mCurrentScriptIndex == other.mCurrentScriptIndex))
      return false;
    if (!(mZoomFactor == other.mZoomFactor))
      return false;
    if (!(mAddBoxCornerIndex == other.mAddBoxCornerIndex))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mVersion;
      r = r * 37 + mAppFrame.hashCode();
      r = r * 37 + mCurrentScriptIndex;
      r = r * 37 + (int)mZoomFactor;
      r = r * 37 + mAddBoxCornerIndex;
      m__hashcode = r;
    }
    return r;
  }

  protected int mVersion;
  protected IRect mAppFrame;
  protected int mCurrentScriptIndex;
  protected float mZoomFactor;
  protected int mAddBoxCornerIndex;
  protected int m__hashcode;

  public static final class Builder extends ProjectState {

    private Builder(ProjectState m) {
      mVersion = m.mVersion;
      mAppFrame = m.mAppFrame;
      mCurrentScriptIndex = m.mCurrentScriptIndex;
      mZoomFactor = m.mZoomFactor;
      mAddBoxCornerIndex = m.mAddBoxCornerIndex;
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
      r.mAppFrame = mAppFrame;
      r.mCurrentScriptIndex = mCurrentScriptIndex;
      r.mZoomFactor = mZoomFactor;
      r.mAddBoxCornerIndex = mAddBoxCornerIndex;
      return r;
    }

    public Builder version(int x) {
      mVersion = x;
      return this;
    }

    public Builder appFrame(IRect x) {
      mAppFrame = (x == null) ? IRect.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder currentScriptIndex(int x) {
      mCurrentScriptIndex = x;
      return this;
    }

    public Builder zoomFactor(float x) {
      mZoomFactor = x;
      return this;
    }

    public Builder addBoxCornerIndex(int x) {
      mAddBoxCornerIndex = x;
      return this;
    }

  }

  public static final ProjectState DEFAULT_INSTANCE = new ProjectState();

  private ProjectState() {
    mVersion = 1;
    mAppFrame = IRect.DEFAULT_INSTANCE;
    mZoomFactor = 1.0f;
    mAddBoxCornerIndex = 2;
  }

}

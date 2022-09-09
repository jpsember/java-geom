package geom.gen;

import js.data.AbstractData;
import js.gui.gen.RecentFilesList;
import js.json.JSMap;

public class ScreditDefaults implements AbstractData {

  public RecentFilesList recentProjects() {
    return mRecentProjects;
  }

  public boolean devFeatures() {
    return mDevFeatures;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "recent_projects";
  protected static final String _1 = "dev_features";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mRecentProjects.toJson());
    m.putUnsafe(_1, mDevFeatures);
    return m;
  }

  @Override
  public ScreditDefaults build() {
    return this;
  }

  @Override
  public ScreditDefaults parse(Object obj) {
    return new ScreditDefaults((JSMap) obj);
  }

  private ScreditDefaults(JSMap m) {
    {
      mRecentProjects = RecentFilesList.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(_0);
      if (x != null) {
        mRecentProjects = RecentFilesList.DEFAULT_INSTANCE.parse(x);
      }
    }
    mDevFeatures = m.opt(_1, false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof ScreditDefaults))
      return false;
    ScreditDefaults other = (ScreditDefaults) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mRecentProjects.equals(other.mRecentProjects)))
      return false;
    if (!(mDevFeatures == other.mDevFeatures))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mRecentProjects.hashCode();
      r = r * 37 + (mDevFeatures ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected RecentFilesList mRecentProjects;
  protected boolean mDevFeatures;
  protected int m__hashcode;

  public static final class Builder extends ScreditDefaults {

    private Builder(ScreditDefaults m) {
      mRecentProjects = m.mRecentProjects;
      mDevFeatures = m.mDevFeatures;
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
    public ScreditDefaults build() {
      ScreditDefaults r = new ScreditDefaults();
      r.mRecentProjects = mRecentProjects;
      r.mDevFeatures = mDevFeatures;
      return r;
    }

    public Builder recentProjects(RecentFilesList x) {
      mRecentProjects = (x == null) ? RecentFilesList.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder devFeatures(boolean x) {
      mDevFeatures = x;
      return this;
    }

  }

  public static final ScreditDefaults DEFAULT_INSTANCE = new ScreditDefaults();

  private ScreditDefaults() {
    mRecentProjects = RecentFilesList.DEFAULT_INSTANCE;
  }

}

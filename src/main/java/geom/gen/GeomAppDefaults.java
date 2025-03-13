package geom.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.file.Files;
import js.gui.gen.RecentFilesList;
import js.json.JSList;
import js.json.JSMap;

public class GeomAppDefaults implements AbstractData {

  public RecentFilesList recentFiles() {
    return mRecentFiles;
  }

  public boolean devFeatures() {
    return mDevFeatures;
  }

  public List<File> openScripts() {
    return mOpenScripts;
  }

  public File currentScript() {
    return mCurrentScript;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "recent_files";
  protected static final String _1 = "dev_features";
  protected static final String _2 = "open_scripts";
  protected static final String _3 = "current_script";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mRecentFiles.toJson());
    m.putUnsafe(_1, mDevFeatures);
    {
      JSList j = new JSList();
      for (File x : mOpenScripts)
        j.add(x.toString());
      m.put(_2, j);
    }
    m.putUnsafe(_3, mCurrentScript.toString());
    return m;
  }

  @Override
  public GeomAppDefaults build() {
    return this;
  }

  @Override
  public GeomAppDefaults parse(Object obj) {
    return new GeomAppDefaults((JSMap) obj);
  }

  private GeomAppDefaults(JSMap m) {
    {
      mRecentFiles = RecentFilesList.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mRecentFiles = RecentFilesList.DEFAULT_INSTANCE.parse(x);
      }
    }
    mDevFeatures = m.opt(_1, false);
    {
      List<File> result = new ArrayList<>();
      JSList j = m.optJSList(_2);
      if (j != null) {
        result = DataUtil.parseFileListFrom(j);
      }
      mOpenScripts = DataUtil.immutableCopyOf(result);
    }
    {
      mCurrentScript = Files.DEFAULT;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mCurrentScript = new File(x);
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
    if (object == null || !(object instanceof GeomAppDefaults))
      return false;
    GeomAppDefaults other = (GeomAppDefaults) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mRecentFiles.equals(other.mRecentFiles)))
      return false;
    if (!(mDevFeatures == other.mDevFeatures))
      return false;
    if (!(mOpenScripts.equals(other.mOpenScripts)))
      return false;
    if (!(mCurrentScript.equals(other.mCurrentScript)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mRecentFiles.hashCode();
      r = r * 37 + (mDevFeatures ? 1 : 0);
      for (File x : mOpenScripts)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mCurrentScript.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected RecentFilesList mRecentFiles;
  protected boolean mDevFeatures;
  protected List<File> mOpenScripts;
  protected File mCurrentScript;
  protected int m__hashcode;

  public static final class Builder extends GeomAppDefaults {

    private Builder(GeomAppDefaults m) {
      mRecentFiles = m.mRecentFiles;
      mDevFeatures = m.mDevFeatures;
      mOpenScripts = DataUtil.mutableCopyOf(m.mOpenScripts);
      mCurrentScript = m.mCurrentScript;
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
    public GeomAppDefaults build() {
      GeomAppDefaults r = new GeomAppDefaults();
      r.mRecentFiles = mRecentFiles;
      r.mDevFeatures = mDevFeatures;
      r.mOpenScripts = DataUtil.immutableCopyOf(mOpenScripts);
      r.mCurrentScript = mCurrentScript;
      return r;
    }

    public Builder recentFiles(RecentFilesList x) {
      mRecentFiles = (x == null) ? RecentFilesList.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder devFeatures(boolean x) {
      mDevFeatures = x;
      return this;
    }

    public Builder openScripts(List<File> x) {
      mOpenScripts = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder currentScript(File x) {
      mCurrentScript = (x == null) ? Files.DEFAULT : x;
      return this;
    }

  }

  public static final GeomAppDefaults DEFAULT_INSTANCE = new GeomAppDefaults();

  private GeomAppDefaults() {
    mRecentFiles = RecentFilesList.DEFAULT_INSTANCE;
    mOpenScripts = DataUtil.emptyList();
    mCurrentScript = Files.DEFAULT;
  }

}

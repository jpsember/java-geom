package geom.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class MatchConfig implements AbstractData {

  public boolean quiet() {
    return mQuiet;
  }

  public File inputFiles() {
    return mInputFiles;
  }

  public File outputFiles() {
    return mOutputFiles;
  }

  public File databaseFiles() {
    return mDatabaseFiles;
  }

  public QtreeParam qtreeParam() {
    return mQtreeParam;
  }

  public MatchParam matchParam() {
    return mMatchParam;
  }

  public String nodeIdColumnName() {
    return mNodeIdColumnName;
  }

  public String matchIdColumnName() {
    return mMatchIdColumnName;
  }

  public int matchIdColumn() {
    return mMatchIdColumn;
  }

  public boolean skipCleaningDirectories() {
    return mSkipCleaningDirectories;
  }

  public int maxNodes() {
    return mMaxNodes;
  }

  public boolean devMode() {
    return mDevMode;
  }

  public boolean abbreviateThings() {
    return mAbbreviateThings;
  }

  public File inspectionDir() {
    return mInspectionDir;
  }

  public int inspectionMax() {
    return mInspectionMax;
  }

  public float perturbAmount() {
    return mPerturbAmount;
  }

  public int randomSeed() {
    return mRandomSeed;
  }

  public int perturbDatabaseFactor() {
    return mPerturbDatabaseFactor;
  }

  public boolean rebuildPerturb() {
    return mRebuildPerturb;
  }

  public boolean persist() {
    return mPersist;
  }

  public File injectionSourceFiles() {
    return mInjectionSourceFiles;
  }

  public int injectionMaxNodes() {
    return mInjectionMaxNodes;
  }

  public int maxFilesProcessed() {
    return mMaxFilesProcessed;
  }

  public int maxRunTimeMinutes() {
    return mMaxRunTimeMinutes;
  }

  public int dormantFileSeconds() {
    return mDormantFileSeconds;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "quiet";
  protected static final String _1 = "input_files";
  protected static final String _2 = "output_files";
  protected static final String _3 = "database_files";
  protected static final String _4 = "qtree_param";
  protected static final String _5 = "match_param";
  protected static final String _6 = "node_id_column_name";
  protected static final String _7 = "match_id_column_name";
  protected static final String _8 = "match_id_column";
  protected static final String _9 = "skip_cleaning_directories";
  protected static final String _10 = "max_nodes";
  protected static final String _11 = "dev_mode";
  protected static final String _12 = "abbreviate_things";
  protected static final String _13 = "inspection_dir";
  protected static final String _14 = "inspection_max";
  protected static final String _15 = "perturb_amount";
  protected static final String _16 = "random_seed";
  protected static final String _17 = "perturb_database_factor";
  protected static final String _18 = "rebuild_perturb";
  protected static final String _19 = "persist";
  protected static final String _20 = "injection_source_files";
  protected static final String _21 = "injection_max_nodes";
  protected static final String _22 = "max_files_processed";
  protected static final String _23 = "max_run_time_minutes";
  protected static final String _24 = "dormant_file_seconds";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mQuiet);
    m.putUnsafe(_1, mInputFiles.toString());
    m.putUnsafe(_2, mOutputFiles.toString());
    m.putUnsafe(_3, mDatabaseFiles.toString());
    m.putUnsafe(_4, mQtreeParam.toJson());
    m.putUnsafe(_5, mMatchParam.toJson());
    m.putUnsafe(_6, mNodeIdColumnName);
    m.putUnsafe(_7, mMatchIdColumnName);
    m.putUnsafe(_8, mMatchIdColumn);
    m.putUnsafe(_9, mSkipCleaningDirectories);
    m.putUnsafe(_10, mMaxNodes);
    m.putUnsafe(_11, mDevMode);
    m.putUnsafe(_12, mAbbreviateThings);
    m.putUnsafe(_13, mInspectionDir.toString());
    m.putUnsafe(_14, mInspectionMax);
    m.putUnsafe(_15, mPerturbAmount);
    m.putUnsafe(_16, mRandomSeed);
    m.putUnsafe(_17, mPerturbDatabaseFactor);
    m.putUnsafe(_18, mRebuildPerturb);
    m.putUnsafe(_19, mPersist);
    m.putUnsafe(_20, mInjectionSourceFiles.toString());
    m.putUnsafe(_21, mInjectionMaxNodes);
    m.putUnsafe(_22, mMaxFilesProcessed);
    m.putUnsafe(_23, mMaxRunTimeMinutes);
    m.putUnsafe(_24, mDormantFileSeconds);
    return m;
  }

  @Override
  public MatchConfig build() {
    return this;
  }

  @Override
  public MatchConfig parse(Object obj) {
    return new MatchConfig((JSMap) obj);
  }

  private MatchConfig(JSMap m) {
    mQuiet = m.opt(_0, false);
    {
      mInputFiles = _D1;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mInputFiles = new File(x);
      }
    }
    {
      mOutputFiles = _D2;
      String x = m.opt(_2, (String) null);
      if (x != null) {
        mOutputFiles = new File(x);
      }
    }
    {
      mDatabaseFiles = _D3;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mDatabaseFiles = new File(x);
      }
    }
    {
      mQtreeParam = QtreeParam.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_4);
      if (x != null) {
        mQtreeParam = QtreeParam.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      mMatchParam = MatchParam.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_5);
      if (x != null) {
        mMatchParam = MatchParam.DEFAULT_INSTANCE.parse(x);
      }
    }
    mNodeIdColumnName = m.opt(_6, "node_id");
    mMatchIdColumnName = m.opt(_7, "match_id");
    mMatchIdColumn = m.opt(_8, 1000);
    mSkipCleaningDirectories = m.opt(_9, false);
    mMaxNodes = m.opt(_10, 0);
    mDevMode = m.opt(_11, false);
    mAbbreviateThings = m.opt(_12, false);
    {
      mInspectionDir = Files.DEFAULT;
      String x = m.opt(_13, (String) null);
      if (x != null) {
        mInspectionDir = new File(x);
      }
    }
    mInspectionMax = m.opt(_14, 50);
    mPerturbAmount = m.opt(_15, 0f);
    mRandomSeed = m.opt(_16, 0);
    mPerturbDatabaseFactor = m.opt(_17, 0);
    mRebuildPerturb = m.opt(_18, false);
    mPersist = m.opt(_19, false);
    {
      mInjectionSourceFiles = Files.DEFAULT;
      String x = m.opt(_20, (String) null);
      if (x != null) {
        mInjectionSourceFiles = new File(x);
      }
    }
    mInjectionMaxNodes = m.opt(_21, 50);
    mMaxFilesProcessed = m.opt(_22, 0);
    mMaxRunTimeMinutes = m.opt(_23, 1);
    mDormantFileSeconds = m.opt(_24, 60);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof MatchConfig))
      return false;
    MatchConfig other = (MatchConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mQuiet == other.mQuiet))
      return false;
    if (!(mInputFiles.equals(other.mInputFiles)))
      return false;
    if (!(mOutputFiles.equals(other.mOutputFiles)))
      return false;
    if (!(mDatabaseFiles.equals(other.mDatabaseFiles)))
      return false;
    if (!(mQtreeParam.equals(other.mQtreeParam)))
      return false;
    if (!(mMatchParam.equals(other.mMatchParam)))
      return false;
    if (!(mNodeIdColumnName.equals(other.mNodeIdColumnName)))
      return false;
    if (!(mMatchIdColumnName.equals(other.mMatchIdColumnName)))
      return false;
    if (!(mMatchIdColumn == other.mMatchIdColumn))
      return false;
    if (!(mSkipCleaningDirectories == other.mSkipCleaningDirectories))
      return false;
    if (!(mMaxNodes == other.mMaxNodes))
      return false;
    if (!(mDevMode == other.mDevMode))
      return false;
    if (!(mAbbreviateThings == other.mAbbreviateThings))
      return false;
    if (!(mInspectionDir.equals(other.mInspectionDir)))
      return false;
    if (!(mInspectionMax == other.mInspectionMax))
      return false;
    if (!(mPerturbAmount == other.mPerturbAmount))
      return false;
    if (!(mRandomSeed == other.mRandomSeed))
      return false;
    if (!(mPerturbDatabaseFactor == other.mPerturbDatabaseFactor))
      return false;
    if (!(mRebuildPerturb == other.mRebuildPerturb))
      return false;
    if (!(mPersist == other.mPersist))
      return false;
    if (!(mInjectionSourceFiles.equals(other.mInjectionSourceFiles)))
      return false;
    if (!(mInjectionMaxNodes == other.mInjectionMaxNodes))
      return false;
    if (!(mMaxFilesProcessed == other.mMaxFilesProcessed))
      return false;
    if (!(mMaxRunTimeMinutes == other.mMaxRunTimeMinutes))
      return false;
    if (!(mDormantFileSeconds == other.mDormantFileSeconds))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mQuiet ? 1 : 0);
      r = r * 37 + mInputFiles.hashCode();
      r = r * 37 + mOutputFiles.hashCode();
      r = r * 37 + mDatabaseFiles.hashCode();
      r = r * 37 + mQtreeParam.hashCode();
      r = r * 37 + mMatchParam.hashCode();
      r = r * 37 + mNodeIdColumnName.hashCode();
      r = r * 37 + mMatchIdColumnName.hashCode();
      r = r * 37 + mMatchIdColumn;
      r = r * 37 + (mSkipCleaningDirectories ? 1 : 0);
      r = r * 37 + mMaxNodes;
      r = r * 37 + (mDevMode ? 1 : 0);
      r = r * 37 + (mAbbreviateThings ? 1 : 0);
      r = r * 37 + mInspectionDir.hashCode();
      r = r * 37 + mInspectionMax;
      r = r * 37 + (int)mPerturbAmount;
      r = r * 37 + mRandomSeed;
      r = r * 37 + mPerturbDatabaseFactor;
      r = r * 37 + (mRebuildPerturb ? 1 : 0);
      r = r * 37 + (mPersist ? 1 : 0);
      r = r * 37 + mInjectionSourceFiles.hashCode();
      r = r * 37 + mInjectionMaxNodes;
      r = r * 37 + mMaxFilesProcessed;
      r = r * 37 + mMaxRunTimeMinutes;
      r = r * 37 + mDormantFileSeconds;
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mQuiet;
  protected File mInputFiles;
  protected File mOutputFiles;
  protected File mDatabaseFiles;
  protected QtreeParam mQtreeParam;
  protected MatchParam mMatchParam;
  protected String mNodeIdColumnName;
  protected String mMatchIdColumnName;
  protected int mMatchIdColumn;
  protected boolean mSkipCleaningDirectories;
  protected int mMaxNodes;
  protected boolean mDevMode;
  protected boolean mAbbreviateThings;
  protected File mInspectionDir;
  protected int mInspectionMax;
  protected float mPerturbAmount;
  protected int mRandomSeed;
  protected int mPerturbDatabaseFactor;
  protected boolean mRebuildPerturb;
  protected boolean mPersist;
  protected File mInjectionSourceFiles;
  protected int mInjectionMaxNodes;
  protected int mMaxFilesProcessed;
  protected int mMaxRunTimeMinutes;
  protected int mDormantFileSeconds;
  protected int m__hashcode;

  public static final class Builder extends MatchConfig {

    private Builder(MatchConfig m) {
      mQuiet = m.mQuiet;
      mInputFiles = m.mInputFiles;
      mOutputFiles = m.mOutputFiles;
      mDatabaseFiles = m.mDatabaseFiles;
      mQtreeParam = m.mQtreeParam;
      mMatchParam = m.mMatchParam;
      mNodeIdColumnName = m.mNodeIdColumnName;
      mMatchIdColumnName = m.mMatchIdColumnName;
      mMatchIdColumn = m.mMatchIdColumn;
      mSkipCleaningDirectories = m.mSkipCleaningDirectories;
      mMaxNodes = m.mMaxNodes;
      mDevMode = m.mDevMode;
      mAbbreviateThings = m.mAbbreviateThings;
      mInspectionDir = m.mInspectionDir;
      mInspectionMax = m.mInspectionMax;
      mPerturbAmount = m.mPerturbAmount;
      mRandomSeed = m.mRandomSeed;
      mPerturbDatabaseFactor = m.mPerturbDatabaseFactor;
      mRebuildPerturb = m.mRebuildPerturb;
      mPersist = m.mPersist;
      mInjectionSourceFiles = m.mInjectionSourceFiles;
      mInjectionMaxNodes = m.mInjectionMaxNodes;
      mMaxFilesProcessed = m.mMaxFilesProcessed;
      mMaxRunTimeMinutes = m.mMaxRunTimeMinutes;
      mDormantFileSeconds = m.mDormantFileSeconds;
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
    public MatchConfig build() {
      MatchConfig r = new MatchConfig();
      r.mQuiet = mQuiet;
      r.mInputFiles = mInputFiles;
      r.mOutputFiles = mOutputFiles;
      r.mDatabaseFiles = mDatabaseFiles;
      r.mQtreeParam = mQtreeParam;
      r.mMatchParam = mMatchParam;
      r.mNodeIdColumnName = mNodeIdColumnName;
      r.mMatchIdColumnName = mMatchIdColumnName;
      r.mMatchIdColumn = mMatchIdColumn;
      r.mSkipCleaningDirectories = mSkipCleaningDirectories;
      r.mMaxNodes = mMaxNodes;
      r.mDevMode = mDevMode;
      r.mAbbreviateThings = mAbbreviateThings;
      r.mInspectionDir = mInspectionDir;
      r.mInspectionMax = mInspectionMax;
      r.mPerturbAmount = mPerturbAmount;
      r.mRandomSeed = mRandomSeed;
      r.mPerturbDatabaseFactor = mPerturbDatabaseFactor;
      r.mRebuildPerturb = mRebuildPerturb;
      r.mPersist = mPersist;
      r.mInjectionSourceFiles = mInjectionSourceFiles;
      r.mInjectionMaxNodes = mInjectionMaxNodes;
      r.mMaxFilesProcessed = mMaxFilesProcessed;
      r.mMaxRunTimeMinutes = mMaxRunTimeMinutes;
      r.mDormantFileSeconds = mDormantFileSeconds;
      return r;
    }

    public Builder quiet(boolean x) {
      mQuiet = x;
      return this;
    }

    public Builder inputFiles(File x) {
      mInputFiles = (x == null) ? _D1 : x;
      return this;
    }

    public Builder outputFiles(File x) {
      mOutputFiles = (x == null) ? _D2 : x;
      return this;
    }

    public Builder databaseFiles(File x) {
      mDatabaseFiles = (x == null) ? _D3 : x;
      return this;
    }

    public Builder qtreeParam(QtreeParam x) {
      mQtreeParam = (x == null) ? QtreeParam.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder matchParam(MatchParam x) {
      mMatchParam = (x == null) ? MatchParam.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder nodeIdColumnName(String x) {
      mNodeIdColumnName = (x == null) ? "node_id" : x;
      return this;
    }

    public Builder matchIdColumnName(String x) {
      mMatchIdColumnName = (x == null) ? "match_id" : x;
      return this;
    }

    public Builder matchIdColumn(int x) {
      mMatchIdColumn = x;
      return this;
    }

    public Builder skipCleaningDirectories(boolean x) {
      mSkipCleaningDirectories = x;
      return this;
    }

    public Builder maxNodes(int x) {
      mMaxNodes = x;
      return this;
    }

    public Builder devMode(boolean x) {
      mDevMode = x;
      return this;
    }

    public Builder abbreviateThings(boolean x) {
      mAbbreviateThings = x;
      return this;
    }

    public Builder inspectionDir(File x) {
      mInspectionDir = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder inspectionMax(int x) {
      mInspectionMax = x;
      return this;
    }

    public Builder perturbAmount(float x) {
      mPerturbAmount = x;
      return this;
    }

    public Builder randomSeed(int x) {
      mRandomSeed = x;
      return this;
    }

    public Builder perturbDatabaseFactor(int x) {
      mPerturbDatabaseFactor = x;
      return this;
    }

    public Builder rebuildPerturb(boolean x) {
      mRebuildPerturb = x;
      return this;
    }

    public Builder persist(boolean x) {
      mPersist = x;
      return this;
    }

    public Builder injectionSourceFiles(File x) {
      mInjectionSourceFiles = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder injectionMaxNodes(int x) {
      mInjectionMaxNodes = x;
      return this;
    }

    public Builder maxFilesProcessed(int x) {
      mMaxFilesProcessed = x;
      return this;
    }

    public Builder maxRunTimeMinutes(int x) {
      mMaxRunTimeMinutes = x;
      return this;
    }

    public Builder dormantFileSeconds(int x) {
      mDormantFileSeconds = x;
      return this;
    }

  }

  private static final File _D1 = new File("input_files");
  private static final File _D2 = new File("output_files");
  private static final File _D3 = new File("database_files");

  public static final MatchConfig DEFAULT_INSTANCE = new MatchConfig();

  private MatchConfig() {
    mInputFiles = _D1;
    mOutputFiles = _D2;
    mDatabaseFiles = _D3;
    mQtreeParam = QtreeParam.DEFAULT_INSTANCE;
    mMatchParam = MatchParam.DEFAULT_INSTANCE;
    mNodeIdColumnName = "node_id";
    mMatchIdColumnName = "match_id";
    mMatchIdColumn = 1000;
    mInspectionDir = Files.DEFAULT;
    mInspectionMax = 50;
    mInjectionSourceFiles = Files.DEFAULT;
    mInjectionMaxNodes = 50;
    mMaxRunTimeMinutes = 1;
    mDormantFileSeconds = 60;
  }

}

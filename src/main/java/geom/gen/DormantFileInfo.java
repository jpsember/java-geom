package geom.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class DormantFileInfo implements AbstractData {

  public File file() {
    return mFile;
  }

  public long activeTime() {
    return mActiveTime;
  }

  public long modificationTime() {
    return mModificationTime;
  }

  public long length() {
    return mLength;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "file";
  protected static final String _1 = "active_time";
  protected static final String _2 = "modification_time";
  protected static final String _3 = "length";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mFile.toString());
    m.putUnsafe(_1, mActiveTime);
    m.putUnsafe(_2, mModificationTime);
    m.putUnsafe(_3, mLength);
    return m;
  }

  @Override
  public DormantFileInfo build() {
    return this;
  }

  @Override
  public DormantFileInfo parse(Object obj) {
    return new DormantFileInfo((JSMap) obj);
  }

  private DormantFileInfo(JSMap m) {
    {
      mFile = Files.DEFAULT;
      String x = m.opt(_0, (String) null);
      if (x != null) {
        mFile = new File(x);
      }
    }
    mActiveTime = m.opt(_1, 0L);
    mModificationTime = m.opt(_2, 0L);
    mLength = m.opt(_3, 0L);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof DormantFileInfo))
      return false;
    DormantFileInfo other = (DormantFileInfo) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mFile.equals(other.mFile)))
      return false;
    if (!(mActiveTime == other.mActiveTime))
      return false;
    if (!(mModificationTime == other.mModificationTime))
      return false;
    if (!(mLength == other.mLength))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mFile.hashCode();
      r = r * 37 + (int)mActiveTime;
      r = r * 37 + (int)mModificationTime;
      r = r * 37 + (int)mLength;
      m__hashcode = r;
    }
    return r;
  }

  protected File mFile;
  protected long mActiveTime;
  protected long mModificationTime;
  protected long mLength;
  protected int m__hashcode;

  public static final class Builder extends DormantFileInfo {

    private Builder(DormantFileInfo m) {
      mFile = m.mFile;
      mActiveTime = m.mActiveTime;
      mModificationTime = m.mModificationTime;
      mLength = m.mLength;
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
    public DormantFileInfo build() {
      DormantFileInfo r = new DormantFileInfo();
      r.mFile = mFile;
      r.mActiveTime = mActiveTime;
      r.mModificationTime = mModificationTime;
      r.mLength = mLength;
      return r;
    }

    public Builder file(File x) {
      mFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder activeTime(long x) {
      mActiveTime = x;
      return this;
    }

    public Builder modificationTime(long x) {
      mModificationTime = x;
      return this;
    }

    public Builder length(long x) {
      mLength = x;
      return this;
    }

  }

  public static final DormantFileInfo DEFAULT_INSTANCE = new DormantFileInfo();

  private DormantFileInfo() {
    mFile = Files.DEFAULT;
  }

}

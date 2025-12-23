package geom.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class FileWithinGroup implements AbstractData {

  public String groupId() {
    return mGroupId;
  }

  public File container() {
    return mContainer;
  }

  public File groupDir() {
    return mGroupDir;
  }

  public File file() {
    return mFile;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "group_id";
  protected static final String _1 = "container";
  protected static final String _2 = "group_dir";
  protected static final String _3 = "file";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mGroupId);
    m.putUnsafe(_1, mContainer.toString());
    m.putUnsafe(_2, mGroupDir.toString());
    m.putUnsafe(_3, mFile.toString());
    return m;
  }

  @Override
  public FileWithinGroup build() {
    return this;
  }

  @Override
  public FileWithinGroup parse(Object obj) {
    return new FileWithinGroup((JSMap) obj);
  }

  private FileWithinGroup(JSMap m) {
    mGroupId = m.opt(_0, "");
    {
      mContainer = Files.DEFAULT;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mContainer = new File(x);
      }
    }
    {
      mGroupDir = Files.DEFAULT;
      String x = m.opt(_2, (String) null);
      if (x != null) {
        mGroupDir = new File(x);
      }
    }
    {
      mFile = Files.DEFAULT;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mFile = new File(x);
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
    if (object == null || !(object instanceof FileWithinGroup))
      return false;
    FileWithinGroup other = (FileWithinGroup) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mGroupId.equals(other.mGroupId)))
      return false;
    if (!(mContainer.equals(other.mContainer)))
      return false;
    if (!(mGroupDir.equals(other.mGroupDir)))
      return false;
    if (!(mFile.equals(other.mFile)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mGroupId.hashCode();
      r = r * 37 + mContainer.hashCode();
      r = r * 37 + mGroupDir.hashCode();
      r = r * 37 + mFile.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mGroupId;
  protected File mContainer;
  protected File mGroupDir;
  protected File mFile;
  protected int m__hashcode;

  public static final class Builder extends FileWithinGroup {

    private Builder(FileWithinGroup m) {
      mGroupId = m.mGroupId;
      mContainer = m.mContainer;
      mGroupDir = m.mGroupDir;
      mFile = m.mFile;
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
    public FileWithinGroup build() {
      FileWithinGroup r = new FileWithinGroup();
      r.mGroupId = mGroupId;
      r.mContainer = mContainer;
      r.mGroupDir = mGroupDir;
      r.mFile = mFile;
      return r;
    }

    public Builder groupId(String x) {
      mGroupId = (x == null) ? "" : x;
      return this;
    }

    public Builder container(File x) {
      mContainer = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder groupDir(File x) {
      mGroupDir = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder file(File x) {
      mFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

  }

  public static final FileWithinGroup DEFAULT_INSTANCE = new FileWithinGroup();

  private FileWithinGroup() {
    mGroupId = "";
    mContainer = Files.DEFAULT;
    mGroupDir = Files.DEFAULT;
    mFile = Files.DEFAULT;
  }

}

package geom.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class Command implements AbstractData {

  public String description() {
    return mDescription;
  }

  public ScriptEditState newState() {
    return mNewState;
  }

  public boolean mergeDisabled() {
    return mMergeDisabled;
  }

  public boolean skipUndo() {
    return mSkipUndo;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "description";
  protected static final String _1 = "new_state";
  protected static final String _2 = "merge_disabled";
  protected static final String _3 = "skip_undo";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDescription);
    m.putUnsafe(_1, mNewState.toJson());
    m.putUnsafe(_2, mMergeDisabled);
    m.putUnsafe(_3, mSkipUndo);
    return m;
  }

  @Override
  public Command build() {
    return this;
  }

  @Override
  public Command parse(Object obj) {
    return new Command((JSMap) obj);
  }

  private Command(JSMap m) {
    mDescription = m.opt(_0, "");
    {
      mNewState = ScriptEditState.DEFAULT_INSTANCE;
      Object x = (Object) m.optUnsafe(_1);
      if (x != null) {
        mNewState = ScriptEditState.DEFAULT_INSTANCE.parse(x);
      }
    }
    mMergeDisabled = m.opt(_2, false);
    mSkipUndo = m.opt(_3, false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Command))
      return false;
    Command other = (Command) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(mNewState.equals(other.mNewState)))
      return false;
    if (!(mMergeDisabled == other.mMergeDisabled))
      return false;
    if (!(mSkipUndo == other.mSkipUndo))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDescription.hashCode();
      r = r * 37 + mNewState.hashCode();
      r = r * 37 + (mMergeDisabled ? 1 : 0);
      r = r * 37 + (mSkipUndo ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected String mDescription;
  protected ScriptEditState mNewState;
  protected boolean mMergeDisabled;
  protected boolean mSkipUndo;
  protected int m__hashcode;

  public static final class Builder extends Command {

    private Builder(Command m) {
      mDescription = m.mDescription;
      mNewState = m.mNewState;
      mMergeDisabled = m.mMergeDisabled;
      mSkipUndo = m.mSkipUndo;
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
    public Command build() {
      Command r = new Command();
      r.mDescription = mDescription;
      r.mNewState = mNewState;
      r.mMergeDisabled = mMergeDisabled;
      r.mSkipUndo = mSkipUndo;
      return r;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
    }

    public Builder newState(ScriptEditState x) {
      mNewState = (x == null) ? ScriptEditState.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder mergeDisabled(boolean x) {
      mMergeDisabled = x;
      return this;
    }

    public Builder skipUndo(boolean x) {
      mSkipUndo = x;
      return this;
    }

  }

  public static final Command DEFAULT_INSTANCE = new Command();

  private Command() {
    mDescription = "";
    mNewState = ScriptEditState.DEFAULT_INSTANCE;
  }

}

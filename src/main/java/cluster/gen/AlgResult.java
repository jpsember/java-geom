package cluster.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class AlgResult implements AbstractData {

  public Node candidate() {
    return mCandidate;
  }

  public float score() {
    return mScore;
  }

  public String note() {
    return mNote;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "candidate";
  protected static final String _1 = "score";
  protected static final String _2 = "note";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mCandidate.toJson());
    m.putUnsafe(_1, mScore);
    m.putUnsafe(_2, mNote);
    return m;
  }

  @Override
  public AlgResult build() {
    return this;
  }

  @Override
  public AlgResult parse(Object obj) {
    return new AlgResult((JSMap) obj);
  }

  private AlgResult(JSMap m) {
    {
      mCandidate = Node.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mCandidate = Node.DEFAULT_INSTANCE.parse(x);
      }
    }
    mScore = m.opt(_1, 0f);
    mNote = m.opt(_2, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof AlgResult))
      return false;
    AlgResult other = (AlgResult) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mCandidate.equals(other.mCandidate)))
      return false;
    if (!(mScore == other.mScore))
      return false;
    if (!(mNote.equals(other.mNote)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mCandidate.hashCode();
      r = r * 37 + (int)mScore;
      r = r * 37 + mNote.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Node mCandidate;
  protected float mScore;
  protected String mNote;
  protected int m__hashcode;

  public static final class Builder extends AlgResult {

    private Builder(AlgResult m) {
      mCandidate = m.mCandidate;
      mScore = m.mScore;
      mNote = m.mNote;
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
    public AlgResult build() {
      AlgResult r = new AlgResult();
      r.mCandidate = mCandidate;
      r.mScore = mScore;
      r.mNote = mNote;
      return r;
    }

    public Builder candidate(Node x) {
      mCandidate = (x == null) ? Node.DEFAULT_INSTANCE : x.build();
      return this;
    }

    public Builder score(float x) {
      mScore = x;
      return this;
    }

    public Builder note(String x) {
      mNote = (x == null) ? "" : x;
      return this;
    }

  }

  public static final AlgResult DEFAULT_INSTANCE = new AlgResult();

  private AlgResult() {
    mCandidate = Node.DEFAULT_INSTANCE;
    mNote = "";
  }

}

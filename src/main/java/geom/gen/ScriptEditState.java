package geom.gen;

import geom.EditorElement;
import java.util.Arrays;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.geometry.IPoint;
import js.json.JSList;
import js.json.JSMap;

public class ScriptEditState implements AbstractData {

  public List<EditorElement> elements() {
    return mElements;
  }

  public int[] selectedElements() {
    return mSelectedElements;
  }

  public List<EditorElement> clipboard() {
    return mClipboard;
  }

  public IPoint duplicationOffset() {
    return mDuplicationOffset;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "elements";
  protected static final String _1 = "selected_elements";
  protected static final String _2 = "clipboard";
  protected static final String _3 = "duplication_offset";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSList j = new JSList();
      for (EditorElement x : mElements)
        j.add(x.toJson());
      m.put(_0, j);
    }
    m.putUnsafe(_1, DataUtil.encodeBase64Maybe(mSelectedElements));
    {
      JSList j = new JSList();
      for (EditorElement x : mClipboard)
        j.add(x.toJson());
      m.put(_2, j);
    }
    m.putUnsafe(_3, mDuplicationOffset.toJson());
    return m;
  }

  @Override
  public ScriptEditState build() {
    return this;
  }

  @Override
  public ScriptEditState parse(Object obj) {
    return new ScriptEditState((JSMap) obj);
  }

  private ScriptEditState(JSMap m) {
    mElements = DataUtil.parseListOfObjects(EditorElement.DEFAULT_INSTANCE, m.optJSList(_0), false);
    {
      mSelectedElements = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mSelectedElements = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mClipboard = DataUtil.parseListOfObjects(EditorElement.DEFAULT_INSTANCE, m.optJSList(_2), false);
    {
      mDuplicationOffset = IPoint.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_3);
      if (x != null) {
        mDuplicationOffset = IPoint.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof ScriptEditState))
      return false;
    ScriptEditState other = (ScriptEditState) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mElements.equals(other.mElements)))
      return false;
    if (!(Arrays.equals(mSelectedElements, other.mSelectedElements)))
      return false;
    if (!(mClipboard.equals(other.mClipboard)))
      return false;
    if (!(mDuplicationOffset.equals(other.mDuplicationOffset)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      for (EditorElement x : mElements)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + Arrays.hashCode(mSelectedElements);
      for (EditorElement x : mClipboard)
        if (x != null)
          r = r * 37 + x.hashCode();
      r = r * 37 + mDuplicationOffset.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected List<EditorElement> mElements;
  protected int[] mSelectedElements;
  protected List<EditorElement> mClipboard;
  protected IPoint mDuplicationOffset;
  protected int m__hashcode;

  public static final class Builder extends ScriptEditState {

    private Builder(ScriptEditState m) {
      mElements = DataUtil.mutableCopyOf(m.mElements);
      mSelectedElements = m.mSelectedElements;
      mClipboard = DataUtil.mutableCopyOf(m.mClipboard);
      mDuplicationOffset = m.mDuplicationOffset;
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
    public ScriptEditState build() {
      ScriptEditState r = new ScriptEditState();
      r.mElements = DataUtil.immutableCopyOf(mElements);
      r.mSelectedElements = mSelectedElements;
      r.mClipboard = DataUtil.immutableCopyOf(mClipboard);
      r.mDuplicationOffset = mDuplicationOffset;
      return r;
    }

    public Builder elements(List<EditorElement> x) {
      mElements = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder selectedElements(int[] x) {
      mSelectedElements = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder clipboard(List<EditorElement> x) {
      mClipboard = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder duplicationOffset(IPoint x) {
      mDuplicationOffset = (x == null) ? IPoint.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final ScriptEditState DEFAULT_INSTANCE = new ScriptEditState();

  private ScriptEditState() {
    mElements = DataUtil.emptyList();
    mSelectedElements = DataUtil.EMPTY_INT_ARRAY;
    mClipboard = DataUtil.emptyList();
    mDuplicationOffset = IPoint.DEFAULT_INSTANCE;
  }

}

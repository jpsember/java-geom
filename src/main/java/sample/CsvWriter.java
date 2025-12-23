package match.util;

import js.base.BaseObject;
import js.geometry.MyMath;

import java.util.List;

import static js.base.Tools.*;

public class CsvWriter extends BaseObject {

  private int mState;
  private static final int STATE_DEFINING = 0, STATE_WRITING = 1, STATE_DONE = 2;

  public CsvWriter addColumn(String title) {
    return addColumn(Integer.MAX_VALUE, title);
  }

  public CsvWriter addColumn(int slot, String title) {
    assertState(STATE_DEFINING);
    checkArgument(!mColumnNames.contains(title), "duplicate column name:", title);
    slot = MyMath.clamp(slot, 0, mColumnNames.size());
    mColumnNames.add(slot, title);
    return this;
  }

  public CsvWriter doneColumns() {
    assertState(STATE_DEFINING);
    mState = STATE_WRITING;
    for (var name : mColumnNames) {
      add(name);
    }
    doneRow();
    return this;
  }

  private void assertState(int s) {
    checkState(mState == s);
  }

  private List<String> mColumnNames = arrayList();

  public void add(CharSequence str) {
    add(Integer.MAX_VALUE, str);
  }

  public void add(int slot, CharSequence str) {
    assertState(STATE_WRITING);
    slot = MyMath.clamp(slot, 0, mRowBuffer.size());
    mRowBuffer.add(slot, str.toString());
  }

  public void doneRow() {
    assertState(STATE_WRITING);

    if (mRowBuffer.size() != numColumns()) {
      throw badState("Expected", numColumns(), "items, instead of", mRowBuffer.size());
    }
    var i = INIT_INDEX;
    for (var s : mRowBuffer) {
      i++;
      if (i != 0)
        mSb.append(',');
      mSb.append('"');
      mSb.append(s);
      mSb.append('"');
    }
    mSb.append('\n');
    mRowBuffer.clear();
  }

  public String close() {
    assertState(STATE_WRITING);
    checkArgument(mRowBuffer.isEmpty());
    mContent = mSb.toString();
    mSb = null;
    return mContent;
  }

  public int numColumns() {
    return mColumnNames.size();
  }

  private StringBuilder mSb = new StringBuilder();
  private String mContent;
  private List<String> mRowBuffer = arrayList();

}

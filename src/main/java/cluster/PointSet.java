package cluster;

import js.base.BaseObject;
import js.data.FloatArray;
import js.geometry.FPoint;
import js.json.JSMap;

import java.util.Map;

import static js.base.Tools.*;

public class PointSet extends BaseObject {

  public int add(FPoint pt) {
    checkState(mPointIndexMap != null, "PointSet is frozen");
    var index = mPointIndexMap.get(pt);
    if (index == null) {
      index = 1 + mPointIndexMap.size();
      mPointIndexMap.put(pt, index);
      var b = (FloatArray.Builder) mPointList;
      b.add(pt.x);
      b.add(pt.y);
    }
    return index;
  }

  public void freeze() {
    if (mPointIndexMap != null) {
      mPointIndexMap = null;
      mPointList = mPointList.build();
    }
  }

  @Override
  public JSMap toJson() {
    var n = super.toJson();
    for (int i = 0; i < mPointList.size(); i += 2) {
      int index = (i / 2 + 1);
      var x = mPointList.get(i);
      var y = mPointList.get(i + 1);
      n.put(String.format("%5d", index), String.format("%6.3f %6.3f", x, y));
    }
    return n;
  }

  private Map<FPoint, Integer> mPointIndexMap = hashMap();
  private FloatArray mPointList = FloatArray.newBuilder();
}

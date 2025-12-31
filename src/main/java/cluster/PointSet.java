package cluster;

import js.base.BaseObject;
import js.data.FloatArray;
import js.geometry.FPoint;
import js.json.JSList;
import js.json.JSMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static js.base.Tools.*;

/**
 * Constructs a set of FPoints, with unique ids
 */
public class PointSet extends BaseObject {

  public static PointSet withPoints(Collection<FPoint> pts) {
    var ps = new PointSet();
    for (var pt : pts)
      ps.add(pt);
    return ps.freeze();
  }

  /**
   * Get FPoints from ids
   */
  public List<FPoint> points(int... ids) {
    List<FPoint> out = arrayList();
    for (var i : ids) {
      var pt = get(i);
      out.add(pt);
    }
    return out;
  }

  /**
   * Add a point, if it doesn't already exist; return its id
   */
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

  /**
   * Make PointSet immutable, discarding unnecessary data structures
   */
  public PointSet freeze() {
    if (mutable()) {
      mPointIndexMap = null;
      mPointList = mPointList.build();
    }
    return this;
  }

  public boolean mutable() {
    return mPointIndexMap != null;
  }

  public JSList serialize() {
    return mPointList.toJson();
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

  public FPoint get(int id) {
    int i = (id - 1) << 1;
    return new FPoint(mPointList.get(i), mPointList.get(i + 1));
  }

}

package geom;

import cluster.PointSet;
import js.geometry.FPoint;
import org.junit.Test;

import static js.base.Tools.*;

import js.testutil.MyTestCase;

public class PointSetTest extends MyTestCase {

  @Test
  public void simple() {
    loadTools();
    for (int i = 0; i < 5; i++) {
      add(i);
    }
    ps().freeze();
    assertMessage(ps().toJson());
  }

  @Test
  public void duplicates() {
    for (int j = 0; j < 20; j++) {
      add(random().nextInt(10));
    }
    ps().freeze();
    assertMessage(ps().toJson());
  }

  @Test
  public void verifyIds() {
    for (int i = 0; i < 5; i++) {
      add(i);
    }
    checkState(add(0) == 1);
    checkState(add(4) == 5);
  }

  @Test(expected = IllegalStateException.class)
  public void mutateAfterFreeze() {
    for (int j = 0; j < 20; j++) {
      add(random().nextInt(10));
      if (j == 10)
        ps().freeze();
    }
  }

  private PointSet ps() {
    if (mPs == null) mPs = new PointSet();
    return mPs;
  }

  private int add(int key) {
    float x = (1 + key) * 1.1f;
    float y = (1 + key) * 2.2f;
    var pt = new FPoint(x, y);
    return ps().add(pt);
  }

  private PointSet mPs;

}

package sample;

import testbed.*;
import base.*;

@Deprecated
public class DiscUtil {

  public static final int DISC_CONTAINED = 1 << (EdDisc.USER_FLAG_BITS_DISC - 1);
  public static final int DISC_OVERLAPPING = 1 << (EdDisc.USER_FLAG_BITS_DISC - 2);

  public static boolean contained(EdDisc d) {
    return d.hasFlags(DISC_CONTAINED);
  }

  public static boolean overlapping(EdDisc d) {
    return d.hasFlags(DISC_OVERLAPPING);
  }

  /**
   * Calculate amount disc c2 must be moved so it is inside tangent disc c
   * 
   * @param c
   * @param c2
   * @return
   */
  public static double itanDist(EdDisc c, EdDisc c2) {
    return Math.abs(FPoint2.distance(c2.getOrigin(), c.getOrigin()) + c2.getRadius() - c.getRadius());
  }

  /**
   * Calculate amount disc c2 must be moved so it is outside tangent disc c
   * 
   * @param c
   * @param c2
   * @return
   */
  public static double otanDist(EdDisc c, EdDisc c2) {
    return Math.abs(FPoint2.distance(c2.getOrigin(), c.getOrigin()) - c2.getRadius() - c.getRadius());
  }

  /**
   * Determine if ca is closer to being itangent to c than otangent to c
   * 
   * @param c
   * @param ca
   * @return true if origin of ca is interior to c
   */
  public static boolean itan(EdDisc c, EdDisc ca) {
    return FPoint2.distance(ca.getOrigin(), c.getOrigin()) - c.getRadius() < 0;
  }

}

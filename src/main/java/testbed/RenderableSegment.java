package testbed;

import geom.AlgRenderable;
import js.geometry.FPoint;
import static testbed.Render.*;

public class RenderableSegment implements AlgRenderable {

  public static RenderableSegment with(FPoint a0, FPoint a1) {
    var c = new RenderableSegment();
    c.a0 = a0;
    c.a1 = a1;
    return c;
  }

  @Override
  public void render(Object item) {
    drawDirectedLineSegment(a0, a1, true);
  }

  private FPoint a0, a1;
}
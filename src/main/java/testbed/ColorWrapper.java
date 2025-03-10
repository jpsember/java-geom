package testbed;

import static testbed.Render.*;

import static js.base.Tools.*;

import java.awt.Color;

import geom.AlgRenderable;
import js.geometry.MyMath;

public class ColorWrapper implements AlgRenderable {

  public ColorWrapper(Color color, Object obj) {
    loadTools();
    mColor = color;
    mWrappedObject = obj;
  }

  public static ColorWrapper alphaWrapper(double alpha, Object obj) {
    var w = new ColorWrapper(null, obj);
    w.mApplyAlpha = alpha;
    return w;
  }

  @Override
  public void render(Object item) {
    Color c;
    if (mApplyAlpha != null) {
      var curr = Render.graphics().getColor();
      var alpha = MyMath.clamp((int) (mApplyAlpha * 255.0), 0, 255);
      c = new Color(curr.getRed(), curr.getBlue(), curr.getGreen(), alpha);
    } else {
      c = mColor;
    }
    pushColor(c);

    AlgorithmStepper.sharedInstance().parseAndRender(mWrappedObject);
    pop();
  }

  private Color mColor;
  private Object mWrappedObject;
  private Double mApplyAlpha;

}
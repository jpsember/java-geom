package testbed;

import static js.base.Tools.*;
import static testbed.Render.*;

import java.awt.Color;

import geom.AlgRenderable;

public class ColorWrapper implements AlgRenderable {

  public ColorWrapper(Color color, Object obj) {
    checkNotNull(color);
    mColor = color;
    mWrappedObject = obj;
  }

  private Color mColor;
  private Object mWrappedObject;

  @Override
  public void render(Object item) {
    pushColor(mColor);
    AlgorithmStepper.sharedInstance().parseAndRender(mWrappedObject);
    pop();
  }
}
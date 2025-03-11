package testbed;

import static testbed.Render.*;

import static js.base.Tools.*;

import geom.AlgRenderable;

public class FontWrapper implements AlgRenderable {

  public FontWrapper(int fontIndex, Object obj) {
    loadTools();
    checkArgument(fontIndex >= 0 && fontIndex < FNT_TOTAL, "illegal font index");
    mFontIndex = fontIndex;
    mWrappedObject = obj;
  }

  @Override
  public void render(Object item) {
     pushFont(mFontIndex);
    AlgorithmStepper.sharedInstance().parseAndRender(mWrappedObject);
     pop();
  }

  private Object mWrappedObject;
  private int mFontIndex;

}
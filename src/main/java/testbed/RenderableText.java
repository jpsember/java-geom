package testbed;

import static geom.GeomTools.*;
import static js.base.Tools.*;

import static testbed.Render.*;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import geom.AlgRenderable;
import js.geometry.FPoint;
import js.geometry.Matrix;

public class RenderableText implements AlgRenderable {

  public static RenderableText with(String text) {
    var c = new RenderableText();
    c.mText = text;
    return c;
  }

  public RenderableText at(FPoint loc) {
    this.mLocation = loc;
    return this;
  }

  /**
   * Shift location over by some number of view pixels
   */
  public RenderableText toRight(int viewPix) {
    mHorzShiftViewPix = viewPix;
    return this;
  }

  @Override
  public void render(Object item) {
    if (mLocation == null) {
      alert("No location for text:", mText);
      mLocation = FPoint.DEFAULT_INSTANCE;
    }

    var undoZoom = 1f / geomApp().zoomFactor();
    var l2 = this.mLocation;
    if (mHorzShiftViewPix != 0)
      l2 = l2.withX(l2.x - mHorzShiftViewPix * undoZoom);

    // Modify transform so we ignore the scale factor
    Graphics2D g = graphics();
    AffineTransform savedTransform = g.getTransform();

    var mMoveToOrigin = Matrix.getTranslate(l2.negate());
    var mResetScale = Matrix.getScale(1.4f * undoZoom);
    var mUndoMoveToOrigin = Matrix.getTranslate(l2);
    var mTotal = Matrix.preMultiply(mMoveToOrigin, mResetScale, mUndoMoveToOrigin);

    g.transform(mTotal.toAffineTransform());
    draw(mText, mLocation.x, mLocation.y, mTextFlags);
    g.setTransform(savedTransform);
  }

  public RenderableText frame() {
    mTextFlags |= TX_FRAME;
    return this;
  }

  public RenderableText bgnd() {
    mTextFlags |= TX_BGND;
    return this;
  }

  public RenderableText clamp() {
    mTextFlags |= TX_CLAMP;
    return this;
  }

  private int mTextFlags = 80;
  private String mText;
  private FPoint mLocation;
  private int mHorzShiftViewPix;
}

package testbed;

import static js.base.Tools.*;
import static testbed.Render.*;
import static testbed.Render.pop;

import geom.AlgRenderable;

public class StrokeWrapper implements AlgRenderable {

  public StrokeWrapper(int stroke, Object obj) {
    checkArgument(stroke >= 0 && stroke < STRK_TOTAL);
    mStroke = stroke;
    mWrappedObject = obj;
  }

  private int mStroke;
  private Object mWrappedObject;

  @Override
  public void render(Object item) {
    // This is called when the algorithm step is being rendered.

    // 1) save the current state
    // 2) set the stroke
    // 3) recursively render the wrapped object
    // 4) restore the state

    // Save the current state
    // Set the stroke
    pushStroke(mStroke);

    // Recursively render the wrapped object
    AlgorithmStepper.sharedInstance().parseAndRender(mWrappedObject);
    // Restore the state
    pop();
  }
}
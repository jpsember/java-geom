package testbed;

import static js.base.Tools.*;

import js.guiapp.UserOperation;

public class AlgStepOper extends UserOperation {

  public AlgStepOper(int direction) {
    loadTools();
    mDir = direction;
  }

  @Override
  public void start() {
    C.seti(TBGlobals.TRACESTEP, mNextStep);
  }

  @Override
  public boolean shouldBeEnabled() {
    int current = C.vi(TBGlobals.TRACESTEP);
    int target = 0;
    if (mDir != 0)
      target = Math.max(0, current + mDir);
    mNextStep = target;
    return target != current;
  }

  private final int mDir;
  private int mNextStep;
}
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
    GadgetList C = TestBed.singleton().gadgets();
    
    C.seti(TBGlobals.TRACESTEP, mNextStep);
  }

  @Override
  public boolean shouldBeEnabled() {
    todo("clunky to have to access gadgets like this");
    GadgetList C = TestBed.singleton().gadgets();
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
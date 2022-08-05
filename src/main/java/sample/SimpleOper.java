package sample;

import testbed.*;

public class SimpleOper implements TestBedOperation, Globals {

  public static SimpleOper singleton = new SimpleOper();

  public void addControls() {

    C.sOpenTab("Simple");
    {
      C.sStaticText("Simple operation to demonstrate algorithm tracking");
    }
    C.sCloseTab();
  }

  public void processAction(TBAction a) {
  }

  public void runAlgorithm() {
    if (T.update())
      T.msg("algorithm step 1");

//    EdDisc[] obj = SampleMain.getDiscs();
//    for (EdDisc d : obj) {
//      if (T.update())
//        T.msg("disc"+T.show(d));
//    }

    if (T.update())
      T.msg("algorithm step 2");
  }

  public void paintView() {
  }

}

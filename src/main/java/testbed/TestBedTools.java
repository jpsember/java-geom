package testbed;

public final class TestBedTools {

  public static TestBed testBed() {
    return TestBed.singleton();
  }

  public static GadgetList gadgets() {
    return testBed().gadgets();
  }

}

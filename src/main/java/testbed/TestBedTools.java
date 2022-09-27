package testbed;

import geom.GeomApp;

public final class TestBedTools {

  public static TestBed testBed() {
      return (TestBed) GeomApp.singleton();
  }


}

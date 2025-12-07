package sample;

import js.geometry.IPoint;

public class PointEvent {
  IPoint location;
  int colorCode;

  public PointEvent(IPoint location, int colorCode) {
    this.location = location;
    this.colorCode = colorCode;
  }
}

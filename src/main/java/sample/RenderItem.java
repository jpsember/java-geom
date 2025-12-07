package sample;

import js.geometry.FPoint;

import java.awt.*;

public class RenderItem {
  public RenderItem(FPoint origin, double radius, Color color) {
    this.origin = origin;
    this.radius = (float)radius;
    this.color = color;
  }

  public FPoint origin;
  public float radius;
  public Color color;
}

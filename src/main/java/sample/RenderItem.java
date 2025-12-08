package sample;

import js.geometry.FPoint;

import java.awt.*;

public class RenderItem {
  public RenderItem(FPoint origin, double radius, Color color, float zSort) {
    this.origin = origin;
    this.radius = (float)radius;
    this.color = color;
    this.zSort = zSort;
  }

  public FPoint origin;
  public float radius;
  public Color color;
  public float zSort;
}

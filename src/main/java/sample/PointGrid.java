package sample;

import static js.base.Tools.*;

import static geom.GeomTools.*;
import static js.geometry.MyMath.clamp;
import static js.geometry.MyMath.interpolateBetweenScalars;
import static testbed.Colors.*;
import static testbed.Render.*;
import static sample.ClusterGlobals.*;

import js.base.BaseObject;
import js.base.Pair;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.json.JSMap;
import js.widget.WidgetManager;

import java.awt.*;
import java.util.Map;

public class PointGrid extends BaseObject {

  public PointGrid(int tileSize) {
    loadTools();
    mTileSize = tileSize;
    mTileMap = hashMap();
  }

  public void insert(IPoint pt) {
    var key = keyForPoint(pt);

    var tile = mTileMap.get(key);
    if (tile == null) {
      tile = new Tile();
      mTileMap.put(key, tile);
    }
    tile.insert(pt);
  }


  private static final int TILE_KEY_LOW_BITS = 20;

  private IPoint tileLocation(int tileKey) {
    var gx = tileKey & ((1 << TILE_KEY_LOW_BITS) - 1);
    var gy = tileKey >> TILE_KEY_LOW_BITS;
    var pt = new IPoint(gx * mTileSize, gy * mTileSize);
    return pt;
  }

  private Integer keyForPoint(IPoint pt) {
    var gx = Math.floorDiv(pt.x, mTileSize);
    var gy = Math.floorDiv(pt.y, mTileSize);
    var result =
        (gy << TILE_KEY_LOW_BITS) + gx;
    return result;
  }

  /**
   * Determine which tile contains a point from a higher resolution grid
   *
   * @param tileBounds bounds of tile in higher resolution grid
   * @return if a match found, a pair containing the tile, and a flag that is true iff
   * this (the lower resolution) tile's pointset lies within the higher resolution's tile
   */
  public Pair<Tile, Boolean> getMatchForHigherRes(IRect tileBounds) {
    var auxKey = keyForPoint(tileBounds.location());
    var auxTile = mTileMap.get(auxKey);
    boolean quadrantFlag = false;

    if (auxTile != null) {
      if (auxTile.population() != 0 && tileBounds.contains(auxTile.meanLocation()))
        quadrantFlag = true;
      return pair(auxTile, quadrantFlag);
    }
    return null;
  }

  double radiusForPop(int pop) {
    var radius = 1 / (1 + Math.exp(-pop * 0.3));

    radius = (radius - 0.5) * 2 * 30;
    radius = clamp(radius, 1, 30);
    return radius;
  }

  private static Color[] sampleColors = {
      new Color(255, 20, 20, 128),
      new Color(150, 193, 242, 128),
      new Color(217, 171, 109, 128),
      new Color(245, 244, 119, 128),
  };

  public void render(float interpFactor, PointGrid auxGrid) {
    WidgetManager g = widgets();

    // I am using the zoom feature to perform the scaling, but we need to
    // 'undo' the normal scaling that it does to keep things like the circle
    // radii and stroke thickness remain *constant* throughout zooming
    float zoomCompensation = getScale();

    var discColor = sampleColors[0];

    var pointSetStroke = new BasicStroke(1.5f * zoomCompensation);
    var tileBoundaryStroke = new BasicStroke(0.7f * zoomCompensation);
    Color tileBoundaryColor = new Color(0, 100, 0, 128);

    var interpolate = g.vb(INTERPOLATE);
    var renderTiles = g.vb(RENDER_TILES);
    var tileDims = new IPoint(mTileSize, mTileSize);

    for (var ent : mTileMap.entrySet()) {
      var key = ent.getKey();
      var tile = ent.getValue();
      var tileBounds = IRect.withLocAndSize(tileLocation(key), tileDims);
      if (renderTiles) {
        stroke(tileBoundaryStroke);
        color(tileBoundaryColor);
        drawRect(tileBounds);
      }

      if (tile.population() == 0) continue;

      // Make radius level out asymptotically
      var pop = tile.population();
      var radius = radiusForPop(pop) * zoomCompensation;
      var location = tile.meanLocation();

      var radiusInterp = radius;
      var locationInterp = location;

      // If we're interpolating with a lower resolution grid, do so
      if (interpolate && auxGrid != null) {

        var z = auxGrid.getMatchForHigherRes(tileBounds);
        var auxTile = z.first;
        if (auxTile != null) {
          var useColor = z.second;
          var radiusAux = auxGrid.radiusForPop(auxTile.population()) * zoomCompensation;
          radiusInterp = interpolateBetweenScalars((float) radius, (float) radiusAux, interpFactor);
          locationInterp = IPoint.interp(location, auxTile.meanLocation(), interpFactor);

          // if we're drawing the circles with some transparency, it is tricky to
          // transition smoothly from several overlapping discs at a higher resolution to
          // a single disk at a lower resolution.
          //
          // if the lower resolution's pointset center lies within this (higher resolution) tile,
          // we want to blend to the full alpha value;
          // otherwise, we want the alpha to blend to zero as it merges with the (lower resolution) version

          todo("but do we want to apply a fade to the two grids, based on the interpolation factor? since both are drawn?");

          var normalAlpha = 128;
          var targetAlpha = useColor ? normalAlpha : 0;

          var blendedAlpha = (int) interpolateBetweenScalars(normalAlpha, targetAlpha, interpFactor);

          discColor = new Color(discColor.getRed(), discColor.getGreen(), discColor.getBlue(), blendedAlpha);
        }
      }

      color(discColor);
      stroke(pointSetStroke);

      fillCircle(locationInterp, radiusInterp);

    }
    todo("log which grid resolution is active");
  }

  public static class Tile {

    public void insert(IPoint pt) {
      mSumX += pt.x;
      mSumY += pt.y;
      mPopulation++;
    }

    public IPoint meanLocation() {
      checkState(mPopulation != 0, "tile population is zero");
      return new IPoint(mSumX / mPopulation, mSumY / mPopulation);
    }

    public int population() {
      return mPopulation;
    }

    private int mPopulation;
    private int mSumX, mSumY;


    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    public JSMap toJson() {
      var m = map();
      m.put("pop", population());
      m.put("mean_loc", meanLocation().toJson());
      return m;
    }
  }

  private int mTileSize;
  private Map<Integer, Tile> mTileMap;
}

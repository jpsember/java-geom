package sample;

import static js.base.Tools.*;

import static geom.GeomTools.*;
import static js.geometry.MyMath.clamp;
import static js.geometry.MyMath.interpolateBetweenScalars;
import static testbed.Render.*;
import static sample.ClusterGlobals.*;

import geom.gen.cluster.EventList;
import geom.gen.cluster.PointEvent;
import geom.gen.cluster.Tile;
import js.base.BaseObject;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.widget.WidgetManager;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class PointGrid extends BaseObject {

  public PointGrid(int tileSize, int numColors) {
    mTileSize = tileSize;
    mTileMap = hashMap();
    mNumColors = numColors;
  }


  public void insert(PointEvent evt) {
    var key = keyForPoint(evt.location().toIPoint());
    var tile = mTileMap.get(key);
    if (tile == null) {
      tile = Tile.newBuilder();
      tile.bounds(tileBoundsFromKey(key));
      mTileMap.put(key, tile);

      // Add an empty EventList for each potential color

      for (int i = 0; i < mNumColors; i++) {
        tile.events().add(EventList.newBuilder());
      }
    }

    // Add the point event to the appropriate color's list
    var elb = (EventList.Builder) tile.events().get(evt.colorCode());

    // have the z loc be the minimum of all events in this list
    if (elb.population() == 0 || elb.zLoc() > evt.zLoc())
      elb.zLoc(evt.zLoc());

    elb.population(elb.population() + 1);
    elb.sumX(elb.sumX() + evt.location().x);
    elb.sumY(elb.sumY() + evt.location().y);
  }


  private static final int TILE_KEY_LOW_BITS = 20;

  private IRect tileBoundsFromKey(int tileKey) {
    var gx = tileKey & ((1 << TILE_KEY_LOW_BITS) - 1);
    var gy = tileKey >> TILE_KEY_LOW_BITS;
    var pt = new IPoint(gx * mTileSize, gy * mTileSize);
    return new IRect(pt.x, pt.y, mTileSize, mTileSize);
  }

  private Integer keyForPoint(IPoint loc) {
    var gx = Math.floorDiv(loc.x, mTileSize);
    var gy = Math.floorDiv(loc.y, mTileSize);
    return
        (gy << TILE_KEY_LOW_BITS) + gx;
  }

  /**
   * Determine which tile contains a smaller tile (from a higher resolution grid)
   *
   * @param smallerTileBounds bounds of smaller tile in higher resolution grid
   * @return larger tile, or null
   */
  public Tile.Builder tileContainingTileFromHigherRes(IRect smallerTileBounds) {
    var auxKey = keyForPoint(smallerTileBounds.location());
    return mTileMap.get(auxKey);
  }

  private double radiusForPop(int pop) {
    var radius = 1 / (1 + Math.exp(-pop *  mRadiusFactor  ));

    radius = (radius - 0.5) * 2 * 30;
    radius = clamp(radius, 1, 30);
    //pr("radius for pop:",pop,"is:",radius);
    return radius;
  }

  private final static Color[] sampleColors = {
      new Color(255, 0, 0, 128),
      new Color(0, 0, 255, 128),
      new Color(0, 255, 0, 128),
      new Color(181, 189, 49, 128),
  };

  public void render(float interpFactor, PointGrid auxGrid, List<RenderItem> renderItems ) {
    WidgetManager g = widgets();

    mRadiusFactor = 0.01f + g.vi(RADIUS_FACTOR) / 500f;
    pr("RADIUS_FACTOR:",g.vi(RADIUS_FACTOR),"f:",mRadiusFactor);

    // I am using the zoom feature to perform the scaling, but we need to
    // 'undo' the normal scaling that it does to keep things like the circle
    // radii and stroke thickness remain *constant* throughout zooming
    float zoomCompensation = getScale();

    var tileBoundaryStroke = new BasicStroke(0.7f * zoomCompensation);
    Color tileBoundaryColor = new Color(0, 100, 0, 128);
    Color auxTileBoundaryColor = new Color(0, 80, 80, 128);
    var auxTileBoundaryStroke = new BasicStroke(1.2f * zoomCompensation);

    var interpolate = g.vb(INTERPOLATE);
    var renderTiles = g.vb(RENDER_TILES);

    for (var ent : mTileMap.entrySet()) {
      var tile = ent.getValue();
      if (renderTiles) {
        stroke(tileBoundaryStroke);
        color(tileBoundaryColor);
        drawRect(tile.bounds());
      }

      // Render each color's (nonempty) event list
      var colorIndex = INIT_INDEX;
      for (var evtList : tile.events()) {
        colorIndex++;


        if (evtList.population() == 0) continue;

        var colorInterp = sampleColors[colorIndex];

        // Make radius level out asymptotically
        var pop = evtList.population();
        var mainRadius = radiusForPop(pop);
        var radius = mainRadius * zoomCompensation;
        var location = meanLocation(evtList);

        var radiusInterp = radius;
        var locationInterp = location;

        // If we're interpolating with a coarser resolution grid (one with larger tiles), do so
        if (interpolate && auxGrid != null) {
          var auxTile = auxGrid.tileContainingTileFromHigherRes(tile.bounds());
          if (auxTile != null) {

            if (renderTiles) {
              stroke(auxTileBoundaryStroke);
              color(auxTileBoundaryColor);
              drawRect(auxTile.bounds());
            }

            var auxEvtList = auxTile.events().get(colorIndex);
            var auxPop = auxEvtList.population();
            checkState(auxPop != 0);
            var auxRadius = auxGrid.radiusForPop(auxPop);
            var auxRadiusAdj = auxRadius * zoomCompensation;
            radiusInterp = interpolateBetweenScalars((float) radius, (float) auxRadiusAdj, interpFactor);

          // pr("main pop:",pop,"radius:",mainRadius,"aux pop:",auxPop,"radius:",auxRadius);
            locationInterp = FPoint.interpolate(location, meanLocation(auxEvtList), interpFactor);


            // if we're drawing the circles with some transparency, it is tricky to
            // transition smoothly from several overlapping discs at a higher resolution to
            // a single disk at a lower resolution.
            //
            // if the lower resolution's pointset center lies within this (higher resolution) tile,
            // we want to blend to the full alpha value;
            // otherwise, we want the alpha to blend to zero as it merges with the (lower resolution) version

            // Alpha factor should be
            //
            //    t = (smaller tile pop) / (larger tile pop)
            float proportion = pop / (float) auxPop;

            var normalAlpha = 128;

            // This is correct, but I'm fuzzy as to why
            var blendedAlpha = (int) interpolateBetweenScalars(normalAlpha, normalAlpha * proportion, interpFactor);

            colorInterp = new Color(colorInterp.getRed(), colorInterp.getGreen(), colorInterp.getBlue(), blendedAlpha);
          }
        }
        renderItems.add(new RenderItem(locationInterp, radiusInterp, colorInterp, evtList.zLoc()));
      }

    }
  }

  private static FPoint meanLocation(EventList events) {
    checkArgument(events.population() != 0);
    var s = 1f / events.population();
    return new FPoint(events.sumX() * s, events.sumY() * s);
  }


  private final int mTileSize;
  private final Map<Integer, Tile.Builder> mTileMap;
  private final int mNumColors;
  private float mRadiusFactor ;
}

package cluster;

import static js.base.Tools.*;

import static geom.GeomTools.*;
import static js.geometry.MyMath.*;
import static testbed.Render.*;
import static cluster.ClusterGlobals.*;

import cluster.gen.EventList;
import cluster.gen.PointEvent;
import cluster.gen.Tile;
import js.base.BaseObject;
import js.base.Pair;
import js.geometry.FPoint;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.json.JSMap;
import js.widget.WidgetManager;
import sample.RenderItem;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PointGrid extends BaseObject {


  public PointGrid(int tileSize, int numColors, Collection<PointEvent> pts) {
    mTileSize = tileSize;
    Map<Integer, Tile> tileMap = hashMap();
    mNumColors = numColors;

    var w = widgets();
    var radiusFactW = w.vi(RADIUS_FACTOR);

    var radExp = 1f;

    mRadiusFactor = 0.01f + (radiusFactW / 500f) * radExp;


    // During construction, the tile map contains builders; after
    // construction complete, replace with immutables
    for (var p : pts) {
      insert(p, tileMap);
    }

    // Construct a map that has immutable values
    //
    Map<Integer, Tile> mp = hashMap();
    for (var entry : tileMap.entrySet()) {
      mp.put(entry.getKey(), entry.getValue().build());
    }
    mTileMap = mp;
  }

  public PointGrid withTileTopologyCache(Map<Integer, List<FPoint>> cache) {
    mTileTopologyCache = cache;
    return this;
  }

  @Override
  public JSMap toJson() {
    var m = super.toJson();
    m.put("nc", mNumColors);
    m.put("radius", mRadiusFactor);
    m.put("tile size", mTileSize);
    var z = map();
    m.put("tiles", z);

    for (var ent : mTileMap.entrySet()) {
      z.put("#" + ent.getKey(), ent.getValue().toJson());
    }

    return m;
  }

  public void insert(PointEvent evt, Map<Integer, Tile> mTileMap) {
    var key = keyForPoint(evt.location().toIPoint());
    var tile = (Tile.Builder) mTileMap.get(key);
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
  public Pair<Integer, Tile> tileContainingTileFromHigherRes(IRect smallerTileBounds) {
    var auxKey = keyForPoint(smallerTileBounds.location());
    return pair(auxKey, mTileMap.get(auxKey));
  }

  private float rf(String id) {
    return widgets().vf(id) / 5f;
  }

  private float rf(String id, float defaultValue) {
    var w = widgets();
    var active = id + "_active";
    if (!w.vb(active))
      return defaultValue;
    return widgets().vf(id) / 5f;
  }

  private float radiusForPop(int pop) {

    // The disc radius is a combination of one or more of:
    //
    //  a constant
    //  proportional to tile pop
    //  inversely proportional to tile area
    //  proportional to zoom factor
    //

    float rpop = rf(RAD_TILE_POP_B, 0) + rf(RAD_TILE_POP_M, 1) * pop;

    float tileAreaFactor = mTileSize * mTileSize * 0.1f;

    float rtileArea = rf(RAD_TILE_AREA_B, 0) + rf(RAD_TILE_AREA_M, 0) * tileAreaFactor;
    if (nonZero(rtileArea))
      rpop = rpop * rtileArea + rf(RAD_TILE_AREA_B, 0);

    float r = rf(RAD_CONSTANT, rpop);
    return r;
  }

  private boolean nonZero(float f) {
    return Math.abs(f) > 1e-5f;
  }

  private final static Color[] sampleColors = {
      new Color(255, 0, 0, 128),
      new Color(193, 56, 214, 128),
      new Color(0, 255, 0, 128),
      new Color(181, 189, 49, 128),
  };

  public void render(float interpFactor, PointGrid auxGrid, List<RenderItem> renderItems, QuadTree quadTree) {
    WidgetManager g = widgets();

    // I am using the zoom feature to perform the scaling, but we need to
    // 'undo' the normal scaling that it does to keep things like the circle
    // radii and stroke thickness remain *constant* throughout zooming
    float zoomCompensation = getScale();

    float d = zoomCompensation * 3f;
    float[] dash = {d, d * .5f};
    var tileBoundaryStroke =
        new BasicStroke(0.7f * zoomCompensation, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
    if (false) tileBoundaryStroke =
        new BasicStroke(0.7f * zoomCompensation);
    Color tileBoundaryColor = new Color(0, 100, 0, 128);
    Color auxTileBoundaryColor = new Color(0, 80, 80, 128);
    var auxTileBoundaryStroke = new BasicStroke(0.8f * zoomCompensation);

    var interpolate = g.vb(INTERPOLATE);
    var renderTiles = g.vb(RENDER_TILES);
    var renderPoints = g.vb(RENDER_GRID_POINTS);

    for (var ent : mTileMap.entrySet()) {
      var tile = ent.getValue();
      if (renderTiles) {
        stroke(tileBoundaryStroke);
        color(tileBoundaryColor);
        drawRect(tile.bounds());
      }

      if (!renderPoints) continue;

      // Render each color's (nonempty) event list
      var colorIndex = INIT_INDEX;
      for (var evtList : tile.events()) {
        colorIndex++;


        if (evtList.population() == 0) continue;

        var colorInterp = sampleColors[colorIndex];

        // Make radius level out asymptotically
        var pop = evtList.population();
        var mainRadius = radiusForPop(pop);
        var mainRadiusAdj = mainRadius * zoomCompensation;
        var location = meanLocation(evtList);

        var radiusInterp = mainRadiusAdj;
        var locationInterp = location;

        // If we're interpolating with a coarser resolution grid (one with larger tiles), do so
        if (interpolate && auxGrid != null) {
          var tileEntry = auxGrid.tileContainingTileFromHigherRes(tile.bounds());
          var key = tileEntry.first;
          var auxTile = tileEntry.second;
          // todo("figure out when to invalidate our cache");

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
            radiusInterp = interpolateBetweenScalars(mainRadiusAdj, auxRadiusAdj, interpFactor);
            locationInterp = FPoint.interpolate(location, meanLocation(auxEvtList), interpFactor);

            if (quadTree != null) {
              var segSet = readTileTopology(auxTile, quadTree, key);
              var snappedLoc = MatchUtil.snapPointToSegments(locationInterp, segSet);
              if (snappedLoc != null)
                locationInterp = snappedLoc;
            }


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

  private List<FPoint> readTileTopology(Tile tile, QuadTree quadTree, int cacheKey) {
    List<FPoint> endpoints = null;
    if (mTileTopologyCache != null)
      endpoints = mTileTopologyCache.get(cacheKey);

    if (endpoints == null) {
      float tileMargin = 0.3f;
      var bounds = tile.bounds().toRect().withInset(-tile.bounds().width * tileMargin);

      int[] endpointIds = quadTree.findSegments(bounds);

      endpoints = quadTree.pointSet().points(endpointIds);
      checkArgument(endpoints != null);
      if (mTileTopologyCache != null) {
        mTileTopologyCache.put(cacheKey, endpoints);
      }
    }
    return endpoints;
  }

  private static FPoint meanLocation(EventList events) {
    checkArgument(events.population() != 0);
    var s = 1f / events.population();
    return new FPoint(events.sumX() * s, events.sumY() * s);
  }

  private final int mTileSize;
  private final Map<Integer, Tile> mTileMap;
  private final int mNumColors;
  private final float mRadiusFactor;
  private Map<Integer, List<FPoint>> mTileTopologyCache;
}

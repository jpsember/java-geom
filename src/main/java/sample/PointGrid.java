package sample;

import static js.base.Tools.*;

import static geom.GeomTools.*;
import static js.base.Tools.*;
import static js.geometry.MyMath.clamp;
import static js.geometry.MyMath.interpolateBetweenScalars;
import static testbed.Colors.*;
import static testbed.Render.*;
import static sample.ClusterGlobals.*;

import js.base.BaseObject;
import js.geometry.IPoint;
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


  double radiusForPop(int pop) {
    var radius = 1 / (1 + Math.exp(-pop * 0.3));

    radius = (radius - 0.5) * 2 * 30;
    radius = clamp(radius, 1, 30);
    return  radius;
  }

  public void render(float interpFactor, PointGrid auxGrid) {

    WidgetManager g = widgets();
    var showTiles = g.vb(RENDER_TILES);

    boolean first = true;

    float zoomCompensation = getScale();
    var siteStroke = new BasicStroke( 1.5f * zoomCompensation);

    for (var ent : mTileMap.entrySet()) {
      var key = ent.getKey();
      var tile = ent.getValue();
      var tileLoc = tileLocation(key);

      if (showTiles) {
        stroke(STRK_THIN);
        color(BLUE, 0.2);
        drawRect(tileLoc.x, tileLoc.y, mTileSize, mTileSize);
        if (first) {
          pr("showing tile, tileLoc:", tileLoc, "size:", mTileSize);
        }
      }

      if (tile.population() == 0) continue;

      // Make radius level out asymptotically
      var pop = tile.population();
      var radius = radiusForPop(pop) * zoomCompensation;
      var location = tile.meanLocation();


      var radiusInterp = radius;
      var locationInterp = location;

      // If we're interpolating with a lower resolution grid, do so
      if (auxGrid != null) {
        var auxKey = auxGrid.keyForPoint(tileLoc);
//        if (first) {
//          pr("key within aux grid:", auxKey);
//        }
        var auxTile = auxGrid.mTileMap.get(auxKey);
        if (auxTile != null) {
          if (first) {
//            pr("interp:",interpFactor);
          //  pr("ourTile:", INDENT, tile);
           // pr("auxTile:", INDENT, auxTile);
          }

          var radiusAux = auxGrid.radiusForPop(auxTile.population());


          // Interpolate between them
          radiusInterp = interpolateBetweenScalars((float)radius, (float)radiusAux,  interpFactor);
          locationInterp = IPoint.interp(location, auxTile.meanLocation(), interpFactor);

        }
      }

      todo("The circle radius should adjust for the zoom factor");
      color(RED, 0.8);
      stroke(siteStroke);


      drawCircle(locationInterp, radiusInterp);

      first = false;

    }
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

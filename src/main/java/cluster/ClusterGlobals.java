package cluster;

public final class ClusterGlobals {

  // Display tile edges
  public static final String RENDER_TILES = "c_rendertiles";

  // Plot a background image (e.g. an Apple Maps screenshot)
  public static final String RENDER_BGND_IMAGE = "c_bgnd_image";

  // Plot grid representations of points
  public static final String RENDER_GRID_POINTS = "c_render_gp";

  // Zoom factor; determines resolution of grid, scale of images
  public static final String ZOOM = "c_zoom";

  // Interplate position and size of discs between current grid and the coarser one
  public static final String INTERPOLATE = "c_interpolate";

  public static final String NUM_COLORS = "c_num_colors";

  // Render discs in z-order (assigned randomly)
  public static final String SORT_BY_Z = "c_sort_by_z";

  public static final String RADIUS_FACTOR = "c_radius";

  // Does not have much effect on performance
  public static final String CACHE_GRID = "c_cache_grid";

  public static final String SNAP = "c_snap";

  public static final String RENDER_REPRESENTATIVE = "c_render_repr";
}

package cluster;

public final class ClusterGlobals {

  // Regenerate points with each event
  public static final String GENERATE = "c_generate";

  // Random seed for generating points deterministically
  public static final String SEED = "c_seed";

  // Number of points to generate
  public static final String COUNT = "c_count";

  // How likely next point is to be generated in neighborhood of previous
  public static final String STICKYNESS = "c_stickyness";

  // Bound on how far next point is to be from previous, if 'sticky'
  public static final String NBR_RAD = "c_rad";

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

  public static final String RAD_CONSTANT = "c_rad_const";
  public static final String RAD_TILE_POP_B = "c_rad_tile_pop_b";
  public static final String RAD_TILE_POP_M = "c_rad_tile_pop_m";
  public static final String RAD_TILE_AREA_B = "c_rad_tile_area_b";
  public static final String RAD_TILE_AREA_M = "c_rad_tile_area_m";
  public static final String RAD_TILE_ZOOM_B = "c_rad_tile_zoom_b";
  public static final String RAD_TILE_ZOOM_M = "c_rad_tile_zoom_m";

  // Does not have much effect on performance
  public static final String CACHE_GRID = "c_cache_grid";
}

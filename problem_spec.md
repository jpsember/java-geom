# The Point Aggregation Problem

(Author: Jeff Sember)

It is often useful to represent a large number of *source* points by a single, larger *representative* disc.

An algorithm to achieve this should try to satisfy some goals:

1. The representative point should be 'close to' the locations of the source points.
2. The radius of the representative disc should be related to the number of source points.
3. If one or more parameters are dynamically modified in a 'smooth' manner (e.g., the locations of source points, or the display's zoom factor), the representative discs should also react smoothly.  
4. If the source points lie on particular geographical features (e.g., a network of road segments), then the representative disc should as well.

I've developed two data structures that address the point aggregation problem.

## Point Binning

This data structure addresses the first three goals.  

The geometric plane is partitioned into a standard grid of squares (or 'tiles').  Each source point is 'binned', or added to one of the tiles.  Each *tile record* has these fields:

+ Sx: the sum of the x-coordinates of the points lying with the tile
+ Sy: the sum of the y-coordinates of the points lying with the tile
+ P: the number of points lying within the tile (this will also be referred to as the tile's *population*)

Observe that this tile record uses O(1) space, and can be used to easily calculate the *centroid*, or geometric average of the point locations.

It may be useful to have a tile record maintain more than one set of these fields, e.g., to represent different classes or colors of points within the tile.

### Construction

A hash table is constructed, mapping a tile's position within the plane to the tile's record.

For each source point, we do the following:

1. determine the x and y coordinates of the tile that contains the point 
2. using (x,y) as a key, locate the tile record within the hash table, creating one if necessary 
3. update the tile fields (add the point's coordinates to the x and y sums, and incrementing the population)

### Rendering

We iterate over the hash table's (key, value) pairs.  For each such (K,T), we:

1. Calculate the tile's position Tx,Ty from K
2. Calculate the representative disc's origin Rx,Ry as Rx = (Sx / P, Sy / P)
3. Determine the radius of the representative disc as a function of the population P, and possibly the zoom factor.
4. Render the representative disc.

### Enhancements for zooming

We can acheive a pleasing visual effect by taking these steps.

1. We define a function S(z) that maps the display zoom factor z to an ideal tile (edge) size S.
2. We define a series of tile grids T1, T2, ..., Tn, where each has a fixed edge size, and with the property that each tile t of T(i) is partitioned exactly by four smaller tiles within T(i-1), so that the edge size of Ti is twice that of T(i-1).
3. For the current zoom factor z, we determine the tile grid Tk whose edge size is as close to, but does not exceed, S(z).
4. Construct grid Tk.
5. Construct grid T(k+1).

To render the tiles, we iterate over each tile U in Tk, and:

1. Locate the tile U' within T(k+1) that contains U.
2. Calculate Dk, the representative disc for U.
3. Calculate D(k+1), the representative disc for U'.
4. Calculate interpolation parameter t as the value between 0...1 that corresponds to how close S(z) is to the (edge) size of U and U' respectively.
5. Construct an interpolated disc Di by linearly interpolating the coordinates and radii of Dk and D(k+1) by t.
6. Render Di.

### Dealing with alpha channels

There is one complication that is relevant if the discs are rendered using an alpha channel (i.e. with translucent colors).  If there are two (or more) tiles (Ua, Ub) within Tk that lie within the same tile U' of T(k+1), then as the interpolation value t approaches 1, the interpolated discs Da and Db will move closer together (as they approach the location and radius of D', the representative disc of U'), the discs will overlap, and the overlapping translucent colors will be darker than that of the single disc D'.  As the zoom increases to the point where grid Tk is replaced by T(k+1), the two interpolated discs will be replaced by the single disc D', and the color will suddenly appear much brighter.

To address this problem, the alpha channels of the colors of the two discs (Da, Db) should be scaled by the relative proportions of the their populations and that of D'.  Specifically:

1. Let Aa be the alpha channel of Da, and Pa be the population of Da
2. Let Ab be the alpha channel of Db, and Pb be the population of Db
3. Let A' be the alpha channel of D', and P' be the population of D'

Now, we render Da using the adjusted alpha value ((1 - t) * Aa +  t * (A' * (Pa / P'))), and similarly render Db using alpha value ((1 - t) * Ab +  t * (A' * (Pb / P'))).


## Topology Snapping

The second data structure addresses the fourth goal: ensuring that the representative disc aligns with the geography appropriately.

I implemented a variant of a quadtree (see https://en.wikipedia.org/wiki/Quadtree), based on the implementation I did for the segment matching program.  This tree contains segments, represented as pairs of planar points.  Thus it can efficiently contain a network of roads.  For each representative disc D constructed during the rendering operation described earlier, we 'snap' the disc to a road segment as follows:

1. Construct an axis-aligned query square Q, centered at D's origin, with a size equal to the maximum snap distance (e.g. 30 meters).
2. Query the quadtree for all segments whose minimum bounding boxes intersect Q.
3. If no segments were found, leave D unchanged.
4. Otherwise, iterate over the segments, and choose the one whose closest point p is closest to D's origin.
5. Render D translated so that its origin is at p.

I omit a detailed description of this data structure, save for the following points of interest.

1. Whereas a standard quadtree subdivides a square region into four smaller regions, my tree has a root note that represents an arbitrary (axis aligned) rectangle, each node is subdivided by splitting the rectangle along its shortest dimension into two equal-sized subrectangles. It is thus a binary tree.
2. The data structure maintains a pointer to its root node, and the rectangle that it represents.  The rectangles associated with each node are not stored explicitly in the nodes, but are calculated 'on the fly' with each descent from a parent node to one of its children. 
3. At construction time, the tree assigns a distinct (sequential) id to each unique segment endpoint, and uses this mapping to construct a list of these endpoints.  The endpoint indices, and not the endpoints themselves, are stored in each leaf node.  This is more memory efficient, as a line segment often appears in two (or more) nodes, and can be represented as a pair of (32-bit) integers, instead of as four (32-bit) floating point coordinates.  
4. At construction time, the tree is rewritten to recycle identical leaf nodes.  This reduces its memory footprint.
5. I assume that the tree can be constructed server-side, then sent (in a compact form) to the client browser, as it does not need to be a dynamic data structure.  This will simplify the development and testing of the code, since much of the complexity of the data structure lies in the construction of the tree, and not the query operations that it performs once it is built.
6. It will probably be useful to define a standard file format for the constructed tree, to aid in testing (and transmitting it) to the client.
7. One possible optimization that I haven't implemented is to reduce the memory footprint of the line segments by converting them from floats to fixed point integers, and maybe by storing the x and y coordinates separately to improve compression during transmission.  (I assume that the snapping does not need to be so precise that a reasonable integer approximation of the floating point coordinates, suitably scaled, will make no difference visually.)



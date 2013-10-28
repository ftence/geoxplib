package com.geoxp.geo;

import gnu.trove.list.array.TLongArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.predicate.RectangleIntersects;

public class JTSHelper {
  
  private static double[] resLatOffset = new double[33];
  private static double[] resLonOffset = new double[33];
  
  private static long[] level2GeoCells = { 0x1000000000000000L, 0x1100000000000000L, 0x1200000000000000L, 0x1300000000000000L,
                                           0x1400000000000000L, 0x1500000000000000L, 0x1600000000000000L, 0x1700000000000000L,
                                           0x1800000000000000L, 0x1900000000000000L, 0x1a00000000000000L, 0x1b00000000000000L,
                                           0x1c00000000000000L, 0x1d00000000000000L, 0x1e00000000000000L, 0x1f00000000000000L };
  
  static {
    for (int i = 0; i < 33; i++) {
      resLatOffset[i] = HHCodeHelper.DEGREES_PER_LAT_UNIT * (1 << (32 - i));
      resLonOffset[i] = HHCodeHelper.DEGREES_PER_LON_UNIT * (1 << (32 - i));
    }
  }
  
  private static ThreadLocal<Coordinate[]> coordinateCache = new ThreadLocal<Coordinate[]>() {
    protected Coordinate[] initialValue() {
      return new Coordinate[5];
    };
  };
  
  private static ThreadLocal<GeometryFactory> factoryCache = new ThreadLocal<GeometryFactory>() {
    @Override
    protected GeometryFactory initialValue() {
      return new GeometryFactory();
    }
  };
  
  public static LinearRing hhcodeToLinearRing(long hhcode, int resolution) {
    //Coordinate[] coords = coordinateCache.get();
    Coordinate[] coords = new Coordinate[5];
    
    double[] latlon = HHCodeHelper.getLatLon(hhcode, resolution);
    
    coords[0] = new Coordinate(latlon[1], latlon[0]);
    coords[1] = new Coordinate(latlon[1], latlon[0] + resLatOffset[resolution]);
    coords[2] = new Coordinate(latlon[1] + resLonOffset[resolution], latlon[0] + resLatOffset[resolution]);
    coords[3] = new Coordinate(latlon[1] + resLonOffset[resolution], latlon[0]);
    coords[4] = coords[0];
      
    //return factoryCache.get().createLinearRing(coords);
    return new GeometryFactory().createLinearRing(coords);
  }
  
  public static LinearRing geoCellToLinearRing(long geocell) {
    int resolution = (int) (((geocell & 0xf000000000000000L) >> 60) & 0xf);
    return hhcodeToLinearRing(geocell << 4, resolution << 1);
  }
  
  /**
   * 
   * @param geometry Geometry to cover
   * @param minresolution Coarsest resolution to use for coverage
   * @param maxresolution Finest resolution to use for coverage, if negative, will be the coarsest resolution encountered + maxresolution
   * @param containedOnly Only consider finest resolution cells which are fully contained, useful when subtracting a coverage.
   * @return
   */
  public static Coverage coverGeometry(Geometry geometry, int minresolution, int maxresolution, boolean containedOnly) {
    //
    // Start with the 16 cells at resolution 2
    //
    
    TLongArrayList geocells = new TLongArrayList(100);
    geocells.add(level2GeoCells);
    
    Coverage c = new Coverage();
    
    LinearRing[] empty = new LinearRing[0];
    
    GeometryFactory factory = new GeometryFactory();
    
    while (0 != geocells.size()) {
      //
      // Create the rectangle of the first geocell
      //

      long geocell = geocells.get(0);
      geocells.removeAt(0);

      int cellres = ((int) (((geocell & 0xf000000000000000L) >> 60) & 0xf)) << 1;

      //Polygon cellgeo = new Polygon(JTSHelper.hhcodeToLinearRing(geocell << 4, cellres), empty, factoryCache.get());
      Polygon cellgeo = new Polygon(JTSHelper.hhcodeToLinearRing(geocell << 4, cellres), empty, factory);

      //
      // If the current cell does not intersect 'geometry', ignore the cell and continue
      //
      
      if (!RectangleIntersects.intersects(cellgeo, geometry)) {
        continue;
      }
      
      //
      // If 'cellres' is the maximum resolution, intersecting the geometry is
      // sufficient to include the cell if 'containedOnly' is false
      //
      
      if (maxresolution == cellres && !containedOnly) {
        c.addCell(cellres, geocell << 4);
        continue;
      }
      
      //
      // If the cell is fully contained in 'geometry', add it to the coverage
      //
      
      if (geometry.covers(cellgeo) && cellres >= minresolution) {
        if (maxresolution < 0) {
          maxresolution = cellres - maxresolution;
        }
        c.addCell(cellres, geocell << 4);
        continue;
      }
      
      //
      // Do not further subdivide cells if we've reached the finest resolution
      //
      
      if (maxresolution == cellres) {
        continue;
      }
      
      //
      // Cell is not fully contained, add its 16 children to 'geocells'
      // If cellres is 30, check the 16 children manually as they can't be represented as
      // geocells
      //
      
      if (30 == cellres) {
        long[] subcells = HHCodeHelper.getSubGeoCells(geocell);
        
        for (long hhcode: subcells) {
          LinearRing lr = JTSHelper.hhcodeToLinearRing(hhcode, HHCodeHelper.MAX_RESOLUTION);
          if (geometry.intersects(lr) && !containedOnly || geometry.covers(lr)) {
            c.addCell(HHCodeHelper.MAX_RESOLUTION, hhcode);
          }
        }
      } else {
        geocells.add(HHCodeHelper.getSubGeoCells(geocell));        
      }
    }
    
    return c;
  }
}

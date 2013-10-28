package com.geoxp;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.geoxp.geo.Coverage;
import com.geoxp.geo.HHCodeHelper;
import com.geoxp.geo.JTSHelper;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Wrapper class for library methods.
 * 
 * Library manipulates the following entities
 * 
 *   GeoXPPoint A 64 bits value representing a location with sub centimeter precision
 *   
 *   GeoXPCell  A 64 bits value representing a rectangle area at one of 15 resolutions
 *              The smallest addressable area is about 1 square cm, the largest about 100M square km
 *
 *   GeoXPShape A set of GeoXPCells covering a shape
 *
 */
public final class GeoXPLib {
  
  public static final class GeoXPShape implements Serializable {
    long[] geocells;
  }
  
  /**
   * Converts (lat,lon) coordinates into a GeoXPPoint.
   * 
   * @param lat Latitude in decimal degrees
   * @param lon Longitude in decimal degrees
   * @return A GeoXPPoint representing the same location as (lat,lon)
   */
	public static long toGeoXPPoint(double lat, double lon) {
	  return HHCodeHelper.getHHCodeValue(lat,lon);
	}
	
	/**
	 * Converts (x,y) coordinates as returned by xyFromGeoXPPoint into
	 * a GeoXPPoint.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static long toGeoXPPoint(long x, long y) {
	  return HHCodeHelper.buildHHCode(x, y, HHCodeHelper.MAX_RESOLUTION);
	}
	
	/**
	 * Converts a GeoXPPoint to (lat,lon) coordinates
	 * 
	 * @param geoxppoint GeoXPPoint to convert
	 * @return A pair of lat,lon coordinates in decimal degrees, representing the same location as the GeoXPPoint
	 */
	public static double[] fromGeoXPPoint(long geoxppoint) {
	  return HHCodeHelper.getLatLon(geoxppoint, HHCodeHelper.MAX_RESOLUTION);
	}
	
	/**
	 * Converts a GeoXPPoint to long coordinates representing latitude and longitude
	 * 
	 * @param geoxppoint GeoXPPoint to conver
	 * @return A pair of long coordinates homeomorphous to lat,lon
	 */
	public static long[] xyFromGeoXPPoint(long geoxppoint) {
	  return HHCodeHelper.splitHHCode(geoxppoint, HHCodeHelper.MAX_RESOLUTION);
	}
	
	/**
	 * Determine if a GeoXPPoint is contained in a GeoXPShape
	 * 
	 * @param geoxppoint GeoXPPoint to check
	 * @param geoxpshape GeoXPShape to check
	 * @return true if geoxpshape contains geoxppoint, false otherwise 
	 */
	public static boolean isGeoXPPointInGeoXPShape(long geoxppoint, GeoXPShape geoxpshape) {
	  return Coverage.contains(geoxpshape.geocells, geoxppoint);	  
	}
	
	/**
	 * Converts a JTS Geometry into a GeoXPShape
	 * 
	 * @param geometry The JTS Geometry instance to convert.
	 * @param pctError The precision (in % of the geometry's envelope diagonal)
	 * @param inside Should the compute coverge be completely inside the Geometry (useful when subtracting)
	 * 
	 * @return the resulting GeoXPShape
	 */
	public static GeoXPShape toGeoXPShape(Geometry geometry, double pctError, boolean inside) {
	  //
	  // Compute bbox of 'geometry'
	  //
	  
	  long[] bbox = HHCodeHelper.getBoundingBox(geometry);
	  
	  //
	  // Compute optimal resolution
	  //
	  
	  int res = HHCodeHelper.getOptimalResolution(bbox, pctError);
	  
	  //
	  // Compute Coverage and return its geocells
	  //
	  
	  GeoXPShape geoxpshape = new GeoXPShape();
	  
	  geoxpshape.geocells = JTSHelper.coverGeometry(geometry, 2, res, inside).toGeoCells(res);
	  
	  return geoxpshape;
	}
	
	/**
	 * Return a GeoXPShape which is the intersection of two GeoXPShapes
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static GeoXPShape intersection(GeoXPShape a, GeoXPShape b) {
	  Coverage ca = new Coverage(a.geocells);
	  Coverage cb = new Coverage(b.geocells);
	  
	  Coverage c = Coverage.intersection(ca, cb, false);
	  c.optimize(0L);
	  
	  GeoXPShape intersection = new GeoXPShape();
	  intersection.geocells = c.toGeoCells(HHCodeHelper.MAX_RESOLUTION);
	  
	  return intersection;
	}
	
	/**
	 * Return a GeoXPShape which is the union of two GeoXPShapes
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static GeoXPShape union(GeoXPShape a, GeoXPShape b) {
	  Coverage ca = new Coverage(a.geocells);
	  Coverage cb = new Coverage(b.geocells);
	  
	  ca.merge(cb);
	  ca.dedup();
	  ca.optimize(0L);
	  
	  GeoXPShape union = new GeoXPShape();
	  union.geocells = ca.toGeoCells(HHCodeHelper.MAX_RESOLUTION);
	  
	  return union;
	}
	
	/**
	 * Return a GeoXPShape which is the result of subtraction the second
	 * GeoXP Shape from the first one.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static GeoXPShape subtraction(GeoXPShape a, GeoXPShape b) {
	  Coverage ca = new Coverage(a.geocells);
	  Coverage cb = new Coverage(b.geocells);
	  
	  Coverage c = Coverage.minus(ca, cb, false);
	  c.optimize(0L);
	  
	  GeoXPShape subtraction = new GeoXPShape();
	  subtraction.geocells = c.toGeoCells(HHCodeHelper.MAX_RESOLUTION);
	  
	  return subtraction;
	}
	
	/**
	 * Compute the loxodromic (rhumb line) distance in meters between locations
	 * 'from' and 'to'.
	 * 
	 * @param from First point
	 * @param to Second point
	 * @return The loxodromic (rhumb line) distance between the first and second points
	 */
	public static double loxodromicDistance(long from, long to) {
	  return HHCodeHelper.loxodromicDistance(from, to);
	}
	
	/**
	 * Compute the orthodromic (great circle) distance in meters between locations
	 * 
	 * @param from
	 * @param to
	 * @return The orthodromic distance between the two locations
	 */
	public static double orthodromicDistance(long from, long to) {
	  return HHCodeHelper.orthodromicDistance(from, to);
	}
	
	public static byte[] serializeGeoXPShape(GeoXPShape geoxpshape) {
	  byte[] buf = new byte[geoxpshape.geocells.length * 8];
	  ByteBuffer bb = ByteBuffer.wrap(buf);
	  bb.order(ByteOrder.BIG_ENDIAN);
	  for (int i = 0; i < geoxpshape.geocells.length; i++) {
	    bb.putLong(geoxpshape.geocells[i]);
	  }
	  return buf;
	}
	
	public static byte[] bytesFromGeoXPPoint(long geoxppoint, int resolution) {
	  // Ignore odd resolutions or resolution below 2 and above 32
	  if (resolution < 2 || resolution > 32 || 0 != (resolution & 0x1)) {
	    return null;
	  }
	  
	  byte[] bytes = new byte[(resolution >>> 2) + (0 == (resolution & 2) ? 0 : 1)];
	  
	  int idx = 0;
	  int res = 0;
	  
	  while(res < (resolution << 1)) {
	    bytes[idx] |= (geoxppoint >> (60 - res)) & 0x0f;
	    if (0 == res % 8) {
	      bytes[idx] = (byte) (bytes[idx] << 4);
	    } else {
	      idx++;
	    }
	    res += 4;
	  }
	  
	  return bytes;
	}
}

//
//   GeoXP Lib, library for efficient geo data manipulation
//
//   Copyright 2020-      SenX S.A.S.
//   Copyright 2019-2020  iroise.net S.A.S.
//   Copyright 1999-2019  Mathias Herberts
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package com.geoxp.geo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Computes centroids at various resolutions given an input file.
 * The input file format MUST be sorted and each line has the format:
 * 
 * HHHHHHHHHHHHHHHH <LF>
 * 
 * where HHHHHHHHHHHHHHHH is the hex representation of a HHCode.
 * 
 * The output has the following format:
 * 
 * HHHHHHHHHHHHHHHH X NNNNNNNNNNNNNNNN CCCCCCCCCCCCCCCC
 * 
 * where HHH... is the cell HHCode
 *       X is the resolution 1 -> F (which is the equivalent of 2 to 30, 32 being ignored)
 *       NNN.... is the total weight at centroid (in hex)
 *       CCC.... is the HHCode of the centroid (in hex)
 *       
 */
public class CentroidGenerator {
  
  private static int minResolution = 2;
  private static int maxResolution = 30;
  private static long threshold = 0;
  
  /**
   * Map of currently computed centroids. Key is the cell for which we compute
   * the centroid. Value is the weight/lat/lon of the current centroid
   */
  private static Map<CharSequence, long[]> centroids = new HashMap<CharSequence, long[]>();
  
  /**
   * Markers we keep (below threshold)
   */
  private static Map<CharSequence, List<Long>> markers = new HashMap<CharSequence, List<Long>>();
  
  /**
   * Cells currently being treated at resolutions 2->30 (1->15)
   */
  private static CharSequence[] currentCells = new CharSequence[15];
  
  private static StringBuilder sb = new StringBuilder();
  
  private static void updateCentroids(final long hhcode) {
    // Convert hhcode to Hex
    sb.setLength(0);
    
    sb.append(Long.toHexString(hhcode));
    
    // Pad with leading 0s
    while(sb.length() < 16) {
      sb.insert(0, "0");
    }
    
    // Loop over the 15 enclosing cells (1 to 15 hex digits)    
    long[] h = HHCodeHelper.splitHHCode(hhcode, 32);

    for (int i = minResolution; i <= maxResolution ; i++) {
      CharSequence cs = sb.subSequence(0, i + 1);
      
      // Check if this CharSequence currently exists
      if (centroids.containsKey(cs)) {
        // It does, update centroid
        long[] values = centroids.get(cs);
        values[1] = values[1] * values[0] + h[0];
        values[2] = values[2] * values[0] + h[1];
        // Update total weight
        values[0]++;
        // Divide by new weight
        values[1] /= values[0];
        values[2] /= values[0];
        // Store the marker until we reach threshold...
        if (markers.containsKey(cs)) {
          markers.get(cs).add(hhcode);
          if (markers.get(cs).size() > threshold) {
            markers.remove(cs);
          }
        }
      } else {
        // It does not, flush the current centroid at resolution 'i'
        // and update it with the new one
        if (null != currentCells[i]) {
          long[] values = centroids.get(currentCells[i]);
          // Output cell / weight / centroid if above threshold
          if (values[0] > threshold) {
            System.out.printf("%s %d %x\n", currentCells[i].toString(), values[0], HHCodeHelper.buildHHCode(values[1], values[2], 32));
          } else {
            System.out.printf("%s %d %x", currentCells[i].toString(), values[0], HHCodeHelper.buildHHCode(values[1], values[2], 32));
            for (long hh: markers.get(currentCells[i])) {
              System.out.printf(" %x", hh);
            }
            System.out.println();
          }
          centroids.remove(currentCells[i]);
          markers.remove(currentCells[i]);
        }
        currentCells[i] = cs;
        centroids.put(cs, new long[] { 1, h[0], h[1] });
        markers.put(cs, new ArrayList<Long>() {{ add(hhcode); }});
      }
    }
  }
  
  private static final void flushCentroids() {
    for (int i = minResolution; i <= maxResolution; i++) {
      if (null != currentCells[i]) {
        long[] values = centroids.get(currentCells[i]);       
        // Output cell / weight / centroid if above threshold
        if (values[0] > threshold) {
          System.out.printf("%s %d %x\n", currentCells[i].toString(), values[0], HHCodeHelper.buildHHCode(values[1], values[2], 32));
        } else {
          System.out.printf("%s %d %x", currentCells[i].toString(), values[0], HHCodeHelper.buildHHCode(values[1], values[2], 32));
          for (long hh: markers.get(currentCells[i])) {
            System.out.printf(" %x", hh);
          }            
          System.out.println();
        }
      }
    }
  }
  
  public static void main(String[] args) throws IOException {
    
    minResolution = Integer.valueOf(args[0]);
    
    if (minResolution % 2 != 0 || minResolution < 2 || minResolution > 30) {
      System.out.println("minResolution MUST be even and between 2 and 30");
    }
    
    maxResolution = Integer.valueOf(args[1]);
    
    if (maxResolution % 2 != 0 || maxResolution < 2 || maxResolution > 30 || maxResolution < minResolution) {
      System.out.println("maxResolution MUST be even and between 2 and 30 and greater or equal to minResolution");
    }

    minResolution >>= 1;
    minResolution -= 1;
    
    maxResolution >>= 1;
    maxResolution -= 1;
    
    threshold = Long.valueOf(args[2]);
    
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
    long hhcode;
    
    while(true) {
      String line = br.readLine();
      
      if (null == line) {
        break;
      }
      
      try {
        // Convert HHCode to a long
        hhcode = Long.parseLong(line, 16);
      } catch (NumberFormatException nfe) {
        hhcode = new BigInteger(line, 16).longValue();
      }
      // Update centroids
      updateCentroids(hhcode);
    }
    
    // Flush any remaining centroids
    flushCentroids();
  }
}

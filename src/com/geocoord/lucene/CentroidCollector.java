package com.geocoord.lucene;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import com.geocoord.geo.HHCodeHelper;
import com.geocoord.thrift.data.Constants;

/**
 * Custom Lucene Collector to compute centroids of results.
 */
public class CentroidCollector extends Collector {
  
  private IndexReader reader = null;
  
  private Map<String,Centroid> centroids = null;
  
  private int markerThreshold = 0;
  private int maxVertices = 0;
  
  private FieldSelector fieldSelector = new FieldSelector() {    
    public FieldSelectorResult accept(String name) {
      if (Constants.LUCENE_GEO_FIELD.equals(name)) {
        return FieldSelectorResult.LOAD;
      } else if (Constants.LUCENE_ID_FIELD.equals(name)) {
        return FieldSelectorResult.LAZY_LOAD;
      } else {
        return FieldSelectorResult.NO_LOAD;
      }
    }
  };
  
  public class Centroid {    
    int vertices = 0;    
    Map<String,Long> markers = new HashMap<String,Long>();
    long[] centroidLatLon = new long[2];
  }
  
  /**
   * Create a new Centroid Collector.
   * 
   * @param cells The set of cells (HHCode hex prefixes) for which to compute centroids.
   * @param markerThreshold If a cell has less than that many markers, return a map if ID->HHCode for them
   * @param maxVertices Do not continue computing a centroid after that many points were used for it, this speeds up
   *                    things with a precision penalty. Use 0 to not use this optimization.
   */
  public CentroidCollector(Set<String> cells, int markerThreshold, int maxVertices) {
    this.markerThreshold = markerThreshold;
    this.maxVertices = maxVertices;
    
    this.centroids = new HashMap<String,Centroid>();
    for (String cell: cells) {
      this.centroids.put(cell, new Centroid());
    }
  }
  
  public Map<String,Centroid> getCentroids() {
    return this.centroids;
  }
  
  @Override
  public boolean acceptsDocsOutOfOrder() {
    return true;
  }
  
  @Override
  public void collect(int docId) throws IOException {

    System.out.println(docId);
    
    //
    // Extract TermVector for this document
    //
    
    TermFreqVector tfv = this.reader.getTermFreqVector(docId, Constants.LUCENE_GEO_FIELD);
    
    long hhcode = 0L;
    long[] latlon = null;        

    for (String term: tfv.getTerms()) {
      if (term.length() == 16) {
        try {
          hhcode = Long.valueOf(term, 16);
        } catch (NumberFormatException nfe) {
          hhcode = new BigInteger(term, 16).longValue();
        }
        latlon = HHCodeHelper.splitHHCode(hhcode, 32);
      }
    }

    Document doc = null;
    
    for (String term: tfv.getTerms()) {
      if (!centroids.containsKey(term)) {
        continue;
      }
      
      // Only update centroid if below the maxVertices threshold
      Centroid c = this.centroids.get(term);
      
      if (0 == this.maxVertices || c.vertices < this.maxVertices) {        
        c.centroidLatLon[0] = c.centroidLatLon[0] * c.vertices + latlon[0];
        c.centroidLatLon[1] = c.centroidLatLon[1] * c.vertices + latlon[1];
        c.vertices++;
        c.centroidLatLon[0] /= c.vertices;
        c.centroidLatLon[1] /= c.vertices;
      } else {
        c.vertices++;
      }
      
      if (0 != this.markerThreshold) {        
        if (this.maxVertices > this.markerThreshold && !c.markers.isEmpty()) {
          c.markers.clear();
        } else {
          // Record marker
          
          if (null == doc) {
            //
            // Retrieve doc. I know this is not advised, but for Centroid computation
            // we need to...
            //
            
            doc = this.reader.document(docId, fieldSelector);
          }
          c.markers.put(doc.getFieldable(Constants.LUCENE_ID_FIELD).stringValue(), hhcode);
        }
      }
    }    
  }
    
  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.reader = reader;
  }
  
  @Override
  public void setScorer(Scorer arg0) throws IOException {
    // Ignore scorer.
  }
}

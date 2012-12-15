package com.geoxp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geoxp.geo.HHCodeHelper;
import com.geoxp.lucene.GeoCoordAnalyzer;
import com.geoxp.lucene.GeoCoordIndex;
import com.geoxp.lucene.GeoDataSegmentCache;
import com.geoxp.lucene.UUIDTokenStream;

public class GeoNamesIndexer {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    
    IndexWriter writer = new IndexWriter(FSDirectory.open(new File(args[0])), new GeoCoordAnalyzer(24), true, MaxFieldLength.UNLIMITED);
    writer.setUseCompoundFile(false);
    int count = 0;
    
    UUIDTokenStream uuidTokenStream = new UUIDTokenStream();
    
    IndexReader reader = null;
    
    UUID deluuid = null;
    
    while(true) {
      String line = br.readLine();
      
      if (null == line) {
        break;
      }

      if (line.startsWith("RC")) {
        continue;
      }
      
      String[] tokens = line.split("\\t");

      long hhcode = HHCodeHelper.getHHCodeValue(Double.valueOf(tokens[3]), Double.valueOf(tokens[4]));

      Document doc = new Document();
      
      //
      // Reset UUIDTokenStream
      //
      
      UUID uuid = UUID.randomUUID();
      
      uuidTokenStream.reset(uuid,hhcode,System.currentTimeMillis());
      Field field = new Field(GeoCoordIndex.ID_FIELD, uuidTokenStream);      
      doc.add(field);
                        
      StringBuilder sb = new StringBuilder();
      
      sb.append(HHCodeHelper.toString(hhcode));
      
      field = new Field(GeoCoordIndex.GEO_FIELD, sb.toString(), Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO);      
      doc.add(field);

      if (!"".equals(tokens[10])) {
        field = new Field(GeoCoordIndex.ATTR_FIELD, "dsg:" + tokens[10], Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO);
        doc.add(field);
      }

      field = new Field(GeoCoordIndex.TAGS_FIELD, tokens[23], Store.NO, Index.ANALYZED_NO_NORMS, TermVector.NO);
      doc.add(field);

      writer.addDocument(doc);

      count++;
      if (count % 10000 == 0) {
        //if (Math.random() < 0.25) {
        //  deluuid = uuid;
        //}
        if (count % 100000 == 0) {
          writer.commit();
        }
        //
        // Reopen reader every 10000 updates
        //
        
        IndexReader oldreader = reader;
        reader = writer.getReader();
        if (null != oldreader) {
          oldreader.close();
        }
        System.out.print("*");
      }
    }
    
    //
    // Delete random doc
    //
    
    if (null != deluuid) {
      GeoDataSegmentCache.deleteByUUID(writer, deluuid.getMostSignificantBits(), deluuid.getLeastSignificantBits());
    }
    writer.commit();
    
    IndexReader oldreader = reader;
    reader = writer.getReader();
    if (null != oldreader) {
      oldreader.close();
    }

    GeoDataSegmentCache.stats();
    writer.close();
  }
}

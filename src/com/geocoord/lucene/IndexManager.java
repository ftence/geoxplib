package com.geocoord.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexManager {
  
  private static final Logger logger = LoggerFactory.getLogger(IndexManager.class);
  
  /**
   * Map of borrowing counts by IndexReader
   */
  private Map<IndexReader,AtomicInteger> borrowed = new HashMap<IndexReader, AtomicInteger>();
  
  /**
   * Map of IndexReader creation times.
   */
  private Map<IndexReader,Long> creation = new HashMap<IndexReader, Long>();
  
  private Map<IndexReader, Boolean> hasWrites = new HashMap<IndexReader, Boolean>();
  
  private static final long READER_MAX_AGE = 5000L;
  
  private static final long WRITER_COMMIT_INTERVAL = 10000L;
  
  private IndexReader reader;
  
  private final IndexWriter writer;
  
  private IndexSearcher searcher;
  
  private long lastCommit = 0L;
  
  public IndexManager() throws IOException {
    this.writer = new IndexWriter(FSDirectory.open(new File("/opt/GeoXP/index")), new GeoCoordAnalyzer(24), false, MaxFieldLength.UNLIMITED);
    this.writer.setUseCompoundFile(false);
    this.lastCommit = System.currentTimeMillis();
    
    // Add shutdown hook to close the index
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          writer.close();
        } catch (IOException ioe) {
          logger.error("IndexManager.ShutDownHook", ioe);
        }
      }
    });
  }
  
  public synchronized IndexWriter getWriter() {
    
    // Commit writer if there were changes
    if (this.hasWrites.get(this.reader) && (System.currentTimeMillis() - lastCommit > WRITER_COMMIT_INTERVAL)) {
      try {
        // FIXME(hbs): add Commit data
        this.writer.commit();        
      } catch (IOException ioe) {
        logger.error("getWriter", ioe);
        return null;
      }
    }
    
    // Mark reader as having writes
    this.hasWrites.put(this.reader, Boolean.TRUE);
    return this.writer;
  }
  
  /**
   * Borrow the current IndexSearchr.
   * @return
   */
  public synchronized IndexSearcher borrowSearcher() {
    //
    // If there are no readers yet or if the last reader is stale,
    // retrieve a new one
    //
    
    if ((System.currentTimeMillis() - creation.get(this.reader) > READER_MAX_AGE) && hasWrites.get(this.reader)) {
      // The reader is old and the writer was retrieved, so there might be some changes,
      // get a new reader
      
      IndexReader oldReader = this.reader;
      
      try {
        this.reader = writer.getReader();
        this.creation.put(this.reader, System.currentTimeMillis());
        this.borrowed.put(this.reader, new AtomicInteger(0));
        this.hasWrites.put(this.reader, Boolean.FALSE);
        this.searcher = new IndexSearcher(this.reader);
        
        //
        // If the old reader is not currently borrowed, close it
        //
        
        if (0 == this.borrowed.get(oldReader).get()) {
          oldReader.close();
          this.borrowed.remove(oldReader);
          this.hasWrites.remove(oldReader);
          this.creation.remove(oldReader);
        }
        
        this.borrowed.get(this.reader).incrementAndGet();
        
        return this.searcher;
      } catch (IOException ioe) {
        logger.error("borrowSearcher", ioe);
        return null;
      }
    } else {
      // Increment borrowing count and return the current searcher
      this.borrowed.get(this.reader).incrementAndGet();      
      return this.searcher;
    }
  }
  
  public synchronized void returnSearcher(IndexSearcher searcher) {
    // Decrement borrowing count
    int count = this.borrowed.get(searcher.getIndexReader()).decrementAndGet();
    
    //
    // If the reader has changed and this one is no longer borrowed, close it
    //
    
    if (0 == count && this.reader != searcher.getIndexReader()) {
      try {
        searcher.getIndexReader().close();
        this.borrowed.remove(searcher.getIndexReader());
        this.hasWrites.remove(searcher.getIndexReader());
        this.creation.remove(searcher.getIndexReader());
      } catch (IOException ioe){
        logger.error("returnSearcher", ioe);
      }
    }
  }
}

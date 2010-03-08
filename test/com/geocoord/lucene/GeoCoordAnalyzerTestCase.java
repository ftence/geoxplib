package com.geocoord.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import junit.framework.TestCase;

public class GeoCoordAnalyzerTestCase extends TestCase {
  
  public void testTokenStream_WhiteSpaceAnalyzedFields() throws IOException {
    GeoCoordAnalyzer gca = new GeoCoordAnalyzer();
    WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
    
    String WSATestString = "urn:geocoord:id:CamelCase042-1 urn:geocoord:id:CamelCase042-1 urn:geocoord:id:CamelCase042-1";
    
    Set<String> fields = new HashSet<String>();
    fields.add(GeoCoordIndex.ATTR_FIELD);
    fields.add(GeoCoordIndex.ID_FIELD);
    fields.add(GeoCoordIndex.LAYER_FIELD);
    fields.add(GeoCoordIndex.USER_FIELD);
    fields.add(GeoCoordIndex.TYPE_FIELD);
    fields.add(GeoCoordIndex.TSHIGH_FIELD);
    fields.add(GeoCoordIndex.TSLOW_FIELD);
    fields.add(GeoCoordIndex.TSMID_FIELD);

    //
    // Compare token streams from GeoCoordAnalyzer and WhiteSpaceAnalyzer
    //
    
    for (String field: fields) {
      StringReader reader1 = new StringReader(WSATestString);
      StringReader reader2 = new StringReader(WSATestString);
      
      TokenStream ts1 = gca.tokenStream(field, reader1);
      TokenStream ts2 = wsa.tokenStream(field, reader2);
      
      while(ts1.incrementToken()) {
        assertTrue(ts2.incrementToken());
        // Compare term length
        assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
        assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
        // Compare term value
        assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
        assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
      }
      
      assertFalse(ts2.incrementToken());      
    }
  }
  
  public void testTokenStream_TAGSField() throws IOException {
    GeoCoordAnalyzer gca = new GeoCoordAnalyzer();
    StandardAnalyzer sa = new StandardAnalyzer(Version.LUCENE_30);
    
    String WSATestString = "the quick brown fox jumped over the lazy dog jumping étoile des neiges";

    StringReader reader1 = new StringReader(WSATestString);
    StringReader reader2 = new StringReader(WSATestString);
    
    TokenStream ts1 = gca.tokenStream(GeoCoordIndex.TAGS_FIELD, reader1);
    // We swap ASCIIFoldingFilter and PorterStemFilter to detect weirdnesses
    TokenStream ts2 = new ASCIIFoldingFilter(new PorterStemFilter(sa.tokenStream(GeoCoordIndex.TAGS_FIELD, reader2)));
    
    while(ts1.incrementToken()) {
      assertTrue(ts2.incrementToken());
      // Compare term length
      assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
      assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
      // Compare term value
      assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
      assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
    }
    
    assertFalse(ts2.incrementToken());          
  }

  public void testTokenStream_GEOField() throws IOException {
    GeoCoordAnalyzer gca = new GeoCoordAnalyzer();
    WhitespaceAnalyzer wsa = new WhitespaceAnalyzer();
    
    String WSATestString = "#0123456789ABCDEF fedcba9876543210";

    StringReader reader1 = new StringReader(WSATestString);
    StringReader reader2 = new StringReader(WSATestString);
    
    TokenStream ts1 = gca.tokenStream(GeoCoordIndex.GEO_FIELD, reader1);
    TokenStream ts2 = new HHCodeTokenStream(wsa.tokenStream(GeoCoordIndex.GEO_FIELD, reader2));
    
    while(ts1.incrementToken()) {
      assertTrue(ts2.incrementToken());
      // Compare term length
      assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
      assertEquals(ts1.getAttribute(TermAttribute.class).termLength(), ts2.getAttribute(TermAttribute.class).termLength());
      // Compare term value
      assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
      assertEquals(ts1.getAttribute(TermAttribute.class).term(), ts2.getAttribute(TermAttribute.class).term());
    }
    
    assertFalse(ts2.incrementToken());          
  }
}

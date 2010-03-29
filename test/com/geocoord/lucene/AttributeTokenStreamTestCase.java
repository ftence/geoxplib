package com.geocoord.lucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.junit.Assert;
import org.junit.Test;


public class AttributeTokenStreamTestCase {

  @Test
  public void test() throws IOException {
    StringReader reader = new StringReader("0 1 2 3");
    AttributeTokenStream ats = new AttributeTokenStream(new WhitespaceAnalyzer().tokenStream(GeoCoordIndex.ATTR_FIELD, reader));
    
    StringBuilder sb = new StringBuilder();
    
    while(ats.incrementToken()) {
      TermAttribute term = ats.getAttribute(TermAttribute.class);
      if (sb.length() > 0) {
        sb.append(" ");
      }
      sb.append(term.term());
    }
    
    Assert.assertEquals("r2OtTIYBnK8= r2OsTIYBmvw= r2OvTIYBoBU= r2OuTIYBnmI=", sb.toString());
  }
}

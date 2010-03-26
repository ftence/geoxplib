package com.geocoord.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

public class HHCodeHelperTestCase extends TestCase {
  
  public void testGetHHCodeValue() {    
    assertEquals(0xb570707070707071L, HHCodeHelper.getHHCodeValue(48.0, -4.5));
    assertEquals(0xc000000000000000L, HHCodeHelper.getHHCodeValue(0.0, 0.0));
    assertEquals(0x0000000000000000L, HHCodeHelper.getHHCodeValue(-90.0, -180.0));
    assertEquals(0xffffffffffffffffL, HHCodeHelper.getHHCodeValue(90.0, 180.0));
    assertEquals(0xffffffffffffffffL, HHCodeHelper.getHHCodeValue(91.0, 181.0));
  }
  
  public void testSplitHHCode() {
    long[] coords = HHCodeHelper.splitHHCode(0xc000000000000000L, 32);
    
    assertEquals(0x80000000L, coords[0]);
    assertEquals(0x80000000L, coords[1]);

    coords = HHCodeHelper.splitHHCode(0xffffffffffffffffL, 16);

    assertEquals(0xffff0000L, coords[0]);
    assertEquals(0xffff0000L, coords[1]);
    
    coords = HHCodeHelper.splitHHCode(0xe036a70a028aa0aaL, 10);
    System.out.println(coords[1]);

    long hhcode = HHCodeHelper.buildHHCode(0x7fffffffL, 0x7ce09800L, 32);
    System.out.println(HHCodeHelper.getLatLon(hhcode, 32)[0]);
  }
  
  public void testAbove() {
    
    //
    // Test even levels for cell 0
    //
    
    assertEquals(0x0000000000000002L, HHCodeHelper.northHHCode(0L, 32));
    assertEquals(0x0000000000000020L, HHCodeHelper.northHHCode(0L, 30));
    assertEquals(0x0000000000000200L, HHCodeHelper.northHHCode(0L, 28));
    assertEquals(0x0000000000002000L, HHCodeHelper.northHHCode(0L, 26));
    assertEquals(0x0000000000020000L, HHCodeHelper.northHHCode(0L, 24));
    assertEquals(0x0000000000200000L, HHCodeHelper.northHHCode(0L, 22));
    assertEquals(0x0000000002000000L, HHCodeHelper.northHHCode(0L, 20));
    assertEquals(0x0000000020000000L, HHCodeHelper.northHHCode(0L, 18));
    assertEquals(0x0000000200000000L, HHCodeHelper.northHHCode(0L, 16));
    assertEquals(0x0000002000000000L, HHCodeHelper.northHHCode(0L, 14));
    assertEquals(0x0000020000000000L, HHCodeHelper.northHHCode(0L, 12));
    assertEquals(0x0000200000000000L, HHCodeHelper.northHHCode(0L, 10));
    assertEquals(0x0002000000000000L, HHCodeHelper.northHHCode(0L, 8));
    assertEquals(0x0020000000000000L, HHCodeHelper.northHHCode(0L, 6));
    assertEquals(0x0200000000000000L, HHCodeHelper.northHHCode(0L, 4));
    assertEquals(0x2000000000000000L, HHCodeHelper.northHHCode(0L, 2));
    
    //
    // Test above cell at level 32 for cells 1-f
    //
    
    assertEquals(0x0000000000000003L, HHCodeHelper.northHHCode(1L, 32));
    assertEquals(0x0000000000000006L, HHCodeHelper.northHHCode(4L, 32));
    assertEquals(0x0000000000000007L, HHCodeHelper.northHHCode(5L, 32));
    assertEquals(0x0000000000000008L, HHCodeHelper.northHHCode(2L, 32));
    assertEquals(0x0000000000000009L, HHCodeHelper.northHHCode(3L, 32));
    assertEquals(0x000000000000000cL, HHCodeHelper.northHHCode(6L, 32));
    assertEquals(0x000000000000000dL, HHCodeHelper.northHHCode(7L, 32));
    assertEquals(0x000000000000000aL, HHCodeHelper.northHHCode(8L, 32));
    assertEquals(0x000000000000000bL, HHCodeHelper.northHHCode(9L, 32));
    assertEquals(0x000000000000000eL, HHCodeHelper.northHHCode(0xcL, 32));
    assertEquals(0x000000000000000fL, HHCodeHelper.northHHCode(0xdL, 32));
    assertEquals(0x0000000000000020L, HHCodeHelper.northHHCode(0xaL, 32));
    assertEquals(0x0000000000000021L, HHCodeHelper.northHHCode(0xbL, 32));
    assertEquals(0x0000000000000024L, HHCodeHelper.northHHCode(0xeL, 32));
    assertEquals(0x0000000000000025L, HHCodeHelper.northHHCode(0xfL, 32));

    
    //
    // Test wrap around
    //
    
    assertEquals(0x0000000000000000L, HHCodeHelper.northHHCode(0xaaaaaaaaaaaaaaaaL, 32));
    assertEquals(0x1111111111111111L, HHCodeHelper.northHHCode(0xbbbbbbbbbbbbbbbbL, 32));
    assertEquals(0x4444444444444444L, HHCodeHelper.northHHCode(0xeeeeeeeeeeeeeeeeL, 32));
    assertEquals(0x5555555555555555L, HHCodeHelper.northHHCode(0xffffffffffffffffL, 32));
  }
  
  public void testBelow() {
    
    assertEquals(0x0000000000000000L, HHCodeHelper.southHHCode(0x0000000000000002L, 32));
    assertEquals(0x0000000000000001L, HHCodeHelper.southHHCode(0x0000000000000003L, 32));
    assertEquals(0x0000000000000004L, HHCodeHelper.southHHCode(0x0000000000000006L, 32));
    assertEquals(0x0000000000000005L, HHCodeHelper.southHHCode(0x0000000000000007L, 32));
    assertEquals(0x0000000000000002L, HHCodeHelper.southHHCode(0x0000000000000008L, 32));
    assertEquals(0x0000000000000003L, HHCodeHelper.southHHCode(0x0000000000000009L, 32));
    assertEquals(0x0000000000000006L, HHCodeHelper.southHHCode(0x000000000000000cL, 32));
    assertEquals(0x0000000000000007L, HHCodeHelper.southHHCode(0x000000000000000dL, 32));
    assertEquals(0x0000000000000008L, HHCodeHelper.southHHCode(0x000000000000000aL, 32));
    assertEquals(0x0000000000000009L, HHCodeHelper.southHHCode(0x000000000000000bL, 32));
    assertEquals(0x000000000000000cL, HHCodeHelper.southHHCode(0x000000000000000eL, 32));
    assertEquals(0x000000000000000dL, HHCodeHelper.southHHCode(0x000000000000000fL, 32));
    assertEquals(0x000000000000000aL, HHCodeHelper.southHHCode(0x0000000000000020L, 32));
    assertEquals(0x000000000000000bL, HHCodeHelper.southHHCode(0x0000000000000021L, 32));
    assertEquals(0x000000000000000eL, HHCodeHelper.southHHCode(0x0000000000000024L, 32));
    assertEquals(0x000000000000000fL, HHCodeHelper.southHHCode(0x0000000000000025L, 32));
    
    // Test wrap around
    
    assertEquals(0xaaaaaaaaaaaaaaaaL, HHCodeHelper.southHHCode(0x0000000000000000L, 32));
    assertEquals(0xbbbbbbbbbbbbbbbbL, HHCodeHelper.southHHCode(0x1111111111111111L, 32));
    assertEquals(0xeeeeeeeeeeeeeeeeL, HHCodeHelper.southHHCode(0x4444444444444444L, 32));
    assertEquals(0xffffffffffffffffL, HHCodeHelper.southHHCode(0x5555555555555555L, 32));
    
    //
    // Test some random hhcodes at random resolutions.
    // This test assumes testAbove succeeded
    //
    
    for (int r = 32; r > 0; r--) {
      for (int i = 0; i < 100; i++) {
        long hhcode = Math.round(Math.random() * (1L << 64));
        
        assertEquals(hhcode, HHCodeHelper.southHHCode(HHCodeHelper.northHHCode(hhcode, r), r));
      }      
    }
  }
  
  public void testRight() {
    assertEquals(0x1L, HHCodeHelper.eastHHCode(0L, 32));
    assertEquals(0x4L, HHCodeHelper.eastHHCode(1L, 32));
    assertEquals(0x5L, HHCodeHelper.eastHHCode(4L, 32));
    assertEquals(0x3L, HHCodeHelper.eastHHCode(2L, 32));
    assertEquals(0x6L, HHCodeHelper.eastHHCode(3L, 32));
    assertEquals(0x7L, HHCodeHelper.eastHHCode(6L, 32));
    assertEquals(0x9L, HHCodeHelper.eastHHCode(8L, 32));
    assertEquals(0xcL, HHCodeHelper.eastHHCode(9L, 32));
    assertEquals(0xdL, HHCodeHelper.eastHHCode(0xcL, 32));
    
    assertEquals(0x10L, HHCodeHelper.eastHHCode(5L, 32));
    assertEquals(0x12L, HHCodeHelper.eastHHCode(7L, 32));
    assertEquals(0x18L, HHCodeHelper.eastHHCode(0xdL, 32));
    assertEquals(0x1aL, HHCodeHelper.eastHHCode(0xfL, 32));

    // Test wrap around
    
    assertEquals(0x0000000000000000L, HHCodeHelper.eastHHCode(0x5555555555555555L, 32));
    assertEquals(0x2222222222222222L, HHCodeHelper.eastHHCode(0x7777777777777777L, 32));
    assertEquals(0x8888888888888888L, HHCodeHelper.eastHHCode(0xddddddddddddddddL, 32));
    assertEquals(0xaaaaaaaaaaaaaaaaL, HHCodeHelper.eastHHCode(0xffffffffffffffffL, 32));
  }

  public void testLeft() {
    assertEquals(0x1L, HHCodeHelper.westHHCode(4L, 32));
    assertEquals(0x4L, HHCodeHelper.westHHCode(5L, 32));
    assertEquals(0x5L, HHCodeHelper.westHHCode(0x10L, 32));
    assertEquals(0x3L, HHCodeHelper.westHHCode(6L, 32));
    assertEquals(0x6L, HHCodeHelper.westHHCode(7L, 32));
    assertEquals(0x7L, HHCodeHelper.westHHCode(0x12L, 32));
    assertEquals(0x9L, HHCodeHelper.westHHCode(0xcL, 32));
    assertEquals(0xcL, HHCodeHelper.westHHCode(0xdL, 32));
    assertEquals(0xdL, HHCodeHelper.westHHCode(0x18L, 32));
    
    assertEquals(0x0L, HHCodeHelper.westHHCode(1L, 32));
    assertEquals(0x2L, HHCodeHelper.westHHCode(3L, 32));
    assertEquals(0x8L, HHCodeHelper.westHHCode(9L, 32));
    assertEquals(0xaL, HHCodeHelper.westHHCode(0xbL, 32));
    
    // Test random values, assuming testRight passed
    
    for (int r = 32; r > 0; r--) {
      for (int i = 0; i < 100; i++) {
        long hhcode = Math.round(Math.random() * (1L << 64));
        
        assertEquals(hhcode, HHCodeHelper.westHHCode(HHCodeHelper.eastHHCode(hhcode, r), r));        
      }
    }
    
    //
    // Test wrap around
    //
    
    assertEquals(0x5555555555555555L, HHCodeHelper.westHHCode(0x0000000000000000L, 32));
    assertEquals(0x7777777777777777L, HHCodeHelper.westHHCode(0x2222222222222222L, 32));
    assertEquals(0xddddddddddddddddL, HHCodeHelper.westHHCode(0x8888888888888888L, 32));
    assertEquals(0xffffffffffffffffL, HHCodeHelper.westHHCode(0xaaaaaaaaaaaaaaaaL, 32));
  }
  
  public void testToString() {
    assertEquals("0000000000000000", HHCodeHelper.toString(0L));
    assertEquals("123456789abcdef0", HHCodeHelper.toString(0x123456789abcdef0L));
    
    assertEquals("1", HHCodeHelper.toString(0x123456789abcdef0L, 2));
    assertEquals("12", HHCodeHelper.toString(0x123456789abcdef0L, 4));
    assertEquals("123", HHCodeHelper.toString(0x123456789abcdef0L, 6));
    assertEquals("1234", HHCodeHelper.toString(0x123456789abcdef0L, 8));
    assertEquals("12345", HHCodeHelper.toString(0x123456789abcdef0L, 10));
    assertEquals("123456", HHCodeHelper.toString(0x123456789abcdef0L, 12));
    assertEquals("1234567", HHCodeHelper.toString(0x123456789abcdef0L, 14));
    assertEquals("12345678", HHCodeHelper.toString(0x123456789abcdef0L, 16));
    assertEquals("123456789", HHCodeHelper.toString(0x123456789abcdef0L, 18));
    assertEquals("123456789a", HHCodeHelper.toString(0x123456789abcdef0L, 20));
    assertEquals("123456789ab", HHCodeHelper.toString(0x123456789abcdef0L, 22));
    assertEquals("123456789abc", HHCodeHelper.toString(0x123456789abcdef0L, 24));
    assertEquals("123456789abcd", HHCodeHelper.toString(0x123456789abcdef0L, 26));
    assertEquals("123456789abcde", HHCodeHelper.toString(0x123456789abcdef0L, 28));
    assertEquals("123456789abcdef", HHCodeHelper.toString(0x123456789abcdef0L, 30));
  }
  
  public void testCoverRectangle() {
    Coverage coverage = HHCodeHelper.coverRectangle(-90,-180,90,180);
    coverage.optimize(0L);
    assertEquals("f b 1 5 3 7 9 d e 0 4 2 6 8 c a", coverage.toString());
    coverage = HHCodeHelper.coverRectangle(-90,-180,90.0,-0.0000001);
    coverage.optimize(0L);
    assertEquals("b a 9 8 3 2 1 0", coverage.toString());
    coverage = HHCodeHelper.coverRectangle(-90,-180,-0.0000001,-0.0000001);
    coverage.optimize(0L);
    assertEquals("3 2 1 0", coverage.toString());
    coverage = HHCodeHelper.coverRectangle(0, 0, 90, 180);
    coverage.optimize(0L);
    assertEquals("f e d c", coverage.toString());
    coverage = HHCodeHelper.coverRectangle(43, -5.5, 51.2, 6.1);
    coverage.optimize(0L);
    assertEquals("e01 b5d e02 e03 caa cab e08 e09 b55 b57 9ff e00", coverage.toString());
    //assertEquals("b570 b571 b574 b575 e020 e021 e024 b572 b573 b576 b577 e022 e023 e026", HHCodeHelper.getCoverageString(HHCodeHelper.optimize(HHCodeHelper.coverRectangle(48, -5, 49, 4), 0L)));
  }
  
    
  public void testCoverPolyline() {

    List<Long> vertices = new ArrayList<Long>() {{
      add(HHCodeHelper.getHHCodeValue(-45.0, -90.0));
      //add(HHCodeHelper.getHHCodeValue(-90.0, 180.0));
      //add(HHCodeHelper.getHHCodeValue(90.0, 180.0));
      add(HHCodeHelper.getHHCodeValue(-50.0, 90.0));      
    }};

    long nano = System.nanoTime();
    Coverage coverage = HHCodeHelper.coverPolyline(vertices, 0,false);
    System.out.println((System.nanoTime() - nano)/1000000.0);
    System.out.println(coverage);
    //HHCodeHelper.optimize(coverage, 0x0000000000000000L);

  }
  
  public void testCoverPolygon() {
    
    List<Long> vertices = new ArrayList<Long>() {{
      add(HHCodeHelper.getHHCodeValue(-90.0, -180.0));
      add(HHCodeHelper.getHHCodeValue(-90.0, 180.0));
      add(HHCodeHelper.getHHCodeValue(90.0, 180.0));
      add(HHCodeHelper.getHHCodeValue(90.0, -180.0));      
    }};
    
    long nano = System.nanoTime();
    Coverage coverage = HHCodeHelper.coverPolygon(vertices, 10);
    System.out.println(System.nanoTime() - nano);
    coverage.optimize(0x0000000000000000L);
    //System.out.println(coverage);
    System.out.println(coverage.toString());
  
  
    
    vertices = new ArrayList<Long>() {{
      add(HHCodeHelper.getHHCodeValue(51.344338660599234,2.548828125));
      add(HHCodeHelper.getHHCodeValue(48.574789910928864,-5.537109375));
      add(HHCodeHelper.getHHCodeValue(43.45291889355465,-1.93359375));
      add(HHCodeHelper.getHHCodeValue(42.09822241118974,3.515625));
      add(HHCodeHelper.getHHCodeValue(43.89789239125797,8.876953125));
      add(HHCodeHelper.getHHCodeValue(49.0954521625348,8.701171875));
    }};

    nano = System.nanoTime();
    coverage = HHCodeHelper.coverPolygon(vertices, 12);
    System.out.println("Time for coverPolygon=" + (System.nanoTime() - nano));
    coverage.optimize(0x0000000000000000L);
    //System.out.println(coverage);
    int ncells = 0;
    for (int resolution = 2; resolution <= 32; resolution += 2) {
      ncells += coverage.getCells(resolution).size();
    }
    System.out.println(ncells + " cells");
    //System.out.println("POST OPT " + HHCodeHelper.getCoverageString(coverage));
  }
    
  public void testToIndexableString() {
    assertEquals("0123456789abcdef 0 01 012 0123 01234 012345 0123456 01234567 012345678 0123456789 0123456789a 0123456789ab 0123456789abc 0123456789abcd 0123456789abcde", HHCodeHelper.toIndexableString(0x0123456789abcdefL));
    assertEquals("0123456789abcdef 0 01", HHCodeHelper.toIndexableString(0x0123456789abcdefL, 2, 4));
    assertEquals("0123456789abcdef 012 0123 01234", HHCodeHelper.toIndexableString(0x0123456789abcdefL, 6, 10));
  }
  
  public void testFromString() {
    assertEquals(0xffffffffffffffffL, HHCodeHelper.fromString("ffffffffffffffff"));
    assertEquals(0xfffffffffffffff0L, HHCodeHelper.fromString("fffffffffffffff"));
    assertEquals(0xffffffffffffff00L, HHCodeHelper.fromString("ffffffffffffff"));
    assertEquals(0xfffffffffffff000L, HHCodeHelper.fromString("fffffffffffff"));
    assertEquals(0xffffffffffff0000L, HHCodeHelper.fromString("ffffffffffff"));
    assertEquals(0xfffffffffff00000L, HHCodeHelper.fromString("fffffffffff"));    
    assertEquals(0xffffffffff000000L, HHCodeHelper.fromString("ffffffffff"));    
    assertEquals(0xfffffffff0000000L, HHCodeHelper.fromString("fffffffff"));    
    assertEquals(0xffffffff00000000L, HHCodeHelper.fromString("ffffffff"));    
    assertEquals(0xfffffff000000000L, HHCodeHelper.fromString("fffffff"));    
    assertEquals(0xffffff0000000000L, HHCodeHelper.fromString("ffffff"));    
    assertEquals(0xfffff00000000000L, HHCodeHelper.fromString("fffff"));    
    assertEquals(0xffff000000000000L, HHCodeHelper.fromString("ffff"));    
    assertEquals(0xfff0000000000000L, HHCodeHelper.fromString("fff"));    
    assertEquals(0xff00000000000000L, HHCodeHelper.fromString("ff"));    
    assertEquals(0xf000000000000000L, HHCodeHelper.fromString("f"));    
  }
}


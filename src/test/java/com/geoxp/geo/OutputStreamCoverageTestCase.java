//
//  GeoXP Lib, library for efficient geo data manipulation
//
//  Copyright (C) 1999-2016  Mathias Herberts
//
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Affero General Public License as
//  published by the Free Software Foundation, either version 3 of the
//  License, or (at your option) any later version and under the terms
//  of the GeoXP License Exception.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

package com.geoxp.geo;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

import org.junit.Test;

public class OutputStreamCoverageTestCase {
  @Test
  public void test() throws Exception {
    Coverage c = new OutputStreamCoverage(new FileOutputStream("/var/tmp/coverage"));
    
    GeoParser.parseCircle("48.0:-4.5:5000", 24, c);    
  }
  
  @Test
  public void testOptimize() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("a0\n");
    sb.append("a1\n");
    sb.append("a2\n");
    sb.append("a3\n");
    sb.append("a4\n");
    sb.append("a5\n");
    sb.append("a6\n");
    sb.append("a7\n");
    sb.append("a8\n");
    sb.append("a9\n");
    sb.append("aa\n");
    sb.append("ab\n");
    sb.append("ac\n");
    //sb.append("ad\n");
    sb.append("ae\n");
    //sb.append("af\n");

    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
    
    InputStream is = new FileInputStream("/var/tmp/coverage.sorted6");
    OutputStream os = new FileOutputStream("/var/tmp/coverage.sorted.opt6");
    OutputStreamCoverage.optimize(is, os, 0x0L, 0);
  }
  
  @Test
  public void testNormalize() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("a\n");

    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());

    OutputStreamCoverage.normalize(bais, System.out, 14);
  }
  
  @Test
  public void testMinus() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("a\n");
    sb.append("b\n");
    sb.append("c\n");
    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());

    sb.setLength(0);
    sb.append("d\n");
    sb.append("bb\n");
    sb.append("d\n");
    ByteArrayInputStream minus = new ByteArrayInputStream(sb.toString().getBytes());
    
    OutputStreamCoverage.minus(bais, minus, System.out);   
  }

  @Test
  public void testIntersection() throws Exception {
    StringBuilder sb = new StringBuilder();
    sb.append("a\n");
    sb.append("b\n");
    sb.append("c\n");
    ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());

    sb.setLength(0);
    sb.append("e\n");
    sb.append("e\n");
    sb.append("e\n");
    sb.append("e\n");
    ByteArrayInputStream intersect = new ByteArrayInputStream(sb.toString().getBytes());
    
    OutputStreamCoverage.intersection(bais, intersect, System.out);   
  }

  @Test
  public void testParse() throws Exception {
    String DEF="+circle:48.0:-4.5:5000 -circle:48.0:-4.55:3000";
    //String DEF="+circle:48.0:-4.5:300";
    long nano = System.nanoTime();
    OutputStreamCoverage.parse(DEF, new FileOutputStream("/var/tmp/testParse-22"), 22);
    //OutputStreamCoverage.prune(new FileInputStream("/var/tmp/testParse-22"), new FileOutputStream("/var/tmp/testParse-pruned"), 0xffffffffffffffffL, 18);
    System.out.println((System.nanoTime() - nano) / 1000000.0);
  }
  
  @Test
  public void testToKML() throws Exception {
    Writer writer = new FileWriter("/var/tmp/testParse.kml");
    OutputStreamCoverage.toKML(new FileInputStream("/var/tmp/testParse-22"), writer);
    //OutputStreamCoverage.toKML(new FileInputStream("/var/tmp/testParse-pruned"), writer);
    writer.close();
  }
}

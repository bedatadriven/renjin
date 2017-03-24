/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.io.serialization;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.sexp.*;
import org.renjin.sexp.PairList.Builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;


public class RDataWriterTest extends EvalTestCase {


  @Test
  public void NAs() throws IOException {
    assertReRead(new DoubleArrayVector(DoubleVector.NA));

  }

  @Test
  public void test() throws IOException {

    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    list.add("foo", new StringArrayVector("zefer", "fluuu"));
    list.setAttribute("categories", new IntArrayVector(3));


    PairList.Builder file = new PairList.Builder();
    file.add("a", new StringArrayVector("who", "am", "i", StringVector.NA));
    file.add("b", new IntArrayVector(1, 2, 3, IntVector.NA, 4));
    file.add("c", new LogicalArrayVector(Logical.NA, Logical.FALSE, Logical.TRUE));
    file.add("d", new DoubleArrayVector(3.14, 6.02, DoubleVector.NA));
    file.add("l", list.build());


    assertReRead(file.build());
   // write("test.rdata", file.build());
  }

  @Test
  public void testVerySimple() throws IOException {
    PairList.Builder pl = new PairList.Builder();
    pl.add("serialized", new IntArrayVector(1,2,3,4));

    PairList list = pl.build();

    assertReRead(list);
 //   write("testsimple.rdata", list);
  }
  
  @Test
  public void specialSymbols() throws IOException {
    assertReRead(Symbol.MISSING_ARG);
    assertReRead(Symbol.UNBOUND_VALUE);
  }

  
  @Test
  public void sharedEnvironmentBetweenClosures() throws IOException {
        
    Environment child = Environment.createChildEnvironment(topLevelContext.getGlobalEnvironment()).build();
        
    Closure f = new Closure(child, 
          PairList.Node.singleton("x", Symbol.MISSING_ARG),
          new DoubleArrayVector(42));
    Closure g = new Closure(child, 
          PairList.Node.singleton("y", Symbol.MISSING_ARG), 
          new DoubleArrayVector(52));
    
    ListVector list = new ListVector(f, g, new DoubleArrayVector(1,2,3));

    ListVector relist = (ListVector) writeAndReRead(list);
    
    assertThat(relist.getElementAsSEXP(0), instanceOf(Closure.class));
    assertThat(relist.getElementAsSEXP(1), instanceOf(Closure.class));
    assertThat(relist.getElementAsSEXP(2), equalTo(c(1,2,3)));

  }

  @Test
  public void testClosure() throws IOException {
    
    PairList.Builder formals = new Builder();
    formals.add("x", new IntArrayVector(1));
    formals.add("y", new IntArrayVector(2));
    
    FunctionCall body = FunctionCall.newCall(Symbol.get("+"), Symbol.get("x"), Symbol.get("y"));
    
    Closure closure = new Closure(topLevelContext.getGlobalEnvironment(), formals.build(), body);
    
    assertReRead(closure);
    
//    PairList.Builder list = new Builder();
//    list.add("renjin.f", closure);
//    
//    write("closure.rdata", list.build());
  }
  
  @Test
  public void closureWithPromise() throws IOException {
    eval("g <- function(x) x");
    eval("f <- function(x, fn = g) fn(x) ");
    
    assertReRead(topLevelContext.getEnvironment().getVariable(topLevelContext, "f"));
  } 

  @Test
  public void attributesCompatiblityWithCR() throws IOException {
    eval("x <- 1:10");
    eval("attr(x,'foo') <- 'bar'");
    
    write("target/attributesCompatiblityWithCR.RData", eval("x"));
  }
  
  @Test
  public void closureEnclosedByClosure() throws IOException {
    eval("f <- function(x) x*2 ");
    eval("g <- function(fn) function(x) fn(x) ");
    
    // won't be equal with equals() because the deserialized environment's
    // identity will not be preserved
    writeAndReRead(eval("g(f)"));
  }

  private void write(String fileName, SEXP exp) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    GZIPOutputStream zos = new GZIPOutputStream(fos);
    RDataWriter writer = new RDataWriter(topLevelContext, zos);
    writer.save(exp);
    zos.close();
  }

  private void assertReRead(SEXP exp) throws IOException {
    assertThat(writeAndReRead(exp), equalTo(exp));
  }

  private SEXP writeAndReRead(SEXP exp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(topLevelContext, baos);
    writer.save(exp);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    RDataReader reader = new RDataReader(topLevelContext, bais);
    SEXP resexp = reader.readFile();
    return resexp;
  }
  
  @Test
  public void testAsciiSafe() {
    // test of the idea used in RDataWriter.writeCharSexp
    assertTrue(asciiSafe("ABC"));
    assertTrue(asciiSafe("abcXYZ~"));
    assertFalse(asciiSafe("L’enquête du « Monde » permet d’identifier le modèle"));
    assertFalse(asciiSafe("اختيارات"));
  }


  static boolean asciiSafe(String string) {

    byte[] bytes = string.getBytes(Charsets.UTF_8);
    return string.length() == bytes.length;
  }
}

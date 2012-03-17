/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.io.serialization;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static r.util.CDefines.eval;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.primitives.io.serialization.RDataWriter;

import r.EvalTestCase;
import r.lang.Closure;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.FunctionCall;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.Logical;
import r.lang.LogicalVector;
import r.lang.PairList;
import r.lang.Symbol;
import r.lang.PairList.Builder;
import r.lang.SEXP;
import r.lang.StringVector;

public class RDataWriterTest extends EvalTestCase {


  @Test
  public void test() throws IOException {

    ListVector.NamedBuilder list = new ListVector.NamedBuilder();
    list.add("foo", new StringVector("zefer", "fluuu"));
    list.setAttribute("categories", new IntVector(3));


    PairList.Builder file = new PairList.Builder();
    file.add("a", new StringVector("who", "am", "i", StringVector.NA));
    file.add("b", new IntVector(1, 2, 3, IntVector.NA, 4));
    file.add("c", new LogicalVector(Logical.NA, Logical.FALSE, Logical.TRUE));
    file.add("d", new DoubleVector(3.14, 6.02, DoubleVector.NA));
    file.add("l", list.build());


    assertReRead(file.build());
   // write("test.rdata", file.build());
  }

  @Test
  public void testVerySimple() throws IOException {
    PairList.Builder pl = new PairList.Builder();
    pl.add("serialized", new IntVector(1,2,3,4));

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
  public void testClosure() throws IOException {
    
    PairList.Builder formals = new Builder();
    formals.add("x", new IntVector(1));
    formals.add("y", new IntVector(2));
    
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
    
    assertReRead(topLevelContext.getEnvironment().getVariable("f"));
  } 

  
  private void write(String fileName, SEXP exp) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    GZIPOutputStream zos = new GZIPOutputStream(fos);
    RDataWriter writer = new RDataWriter(topLevelContext, zos);
    writer.writeFile(exp);
    zos.close();
  }

  private void assertReRead(SEXP exp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(topLevelContext, baos);
    writer.writeFile(exp);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    RDataReader reader = new RDataReader(topLevelContext, bais);
    SEXP resexp = reader.readFile();

    assertThat(resexp, equalTo(exp));
  }
}

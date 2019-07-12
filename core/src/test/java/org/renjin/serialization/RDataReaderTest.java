/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.serialization;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.Types;
import org.renjin.repackaged.guava.io.ByteSource;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;


public class RDataReaderTest extends EvalTestCase {

  /**
   *
   * Saved workspace image containing x = c(1,2,3,4)
   *
   * 
   * @throws IOException
   */
  @Test
  public void loadVerySimple() throws IOException {
    InputStream in = getClass().getResourceAsStream("/simple.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP exp = reader.readFile();
    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    assertThat(pairList.length(), equalTo(1));
    assertThat(pairList.getValue(), elementsIdenticalTo( c(1,2,3,4) ));
  }

  @Test
  public void loadSimpleHashEnvironment() throws IOException {
    InputStream in = getClass().getResourceAsStream("/HashedEnvironment.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP exp = reader.readFile();
    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    Environment env = (Environment) pairList.getValue();
    assertThat(pairList.length(), equalTo(1));
    assertThat(env.getVariable(topLevelContext, "yyyy0yyyyy"), elementsIdenticalTo( c_i(1, 2, 3) ));
    assertThat(env.getVariable(topLevelContext, "yyyy8yyyyy"), elementsIdenticalTo( c(8) ));
    assertThat(env.getVariable(topLevelContext, "yyyy3yyyyy"), elementsIdenticalTo( c("a","b") ));
  }
  @Test
  public void isRDataFile() throws IOException {
    ByteSource rdata = new ByteSource() {
      
      @Override
      public InputStream openStream() throws IOException {
        InputStream in = getClass().getResourceAsStream("/simple.RData");
        return new GZIPInputStream(in);
      }
    }; 
    ByteSource notRData = new ByteSource() {
      
      @Override
      public InputStream openStream() throws IOException {
        return getClass().getResourceAsStream("/jarfiletest.jar");
      }
    }; 
    
    assertThat(RDataReader.isRDataFile(notRData), equalTo(false));
  }

  @Test
  public void loadComplete() throws IOException {
    InputStream in = getClass().getResourceAsStream("/complete.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP exp = reader.readFile();
    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    assertThat(pairList.findByTag(symbol("a")), elementsIdenticalTo( eval("1:99") ));
    assertThat(pairList.findByTag(symbol("b")), elementsIdenticalTo( eval("sqrt(1:25) ") ));
    assertThat(pairList.findByTag(symbol("c")), elementsIdenticalTo( c(Logical.NA )));
    assertThat(pairList.findByTag(symbol("d")), elementsIdenticalTo( list(c(Logical.NA), DoubleVector.NA, IntVector.NA, NULL )));

    ListVector d = (ListVector) pairList.findByTag(symbol("d"));
    DoubleVector d_2 = (DoubleVector) d.getElementAsSEXP(1);

    System.out.println(Long.toHexString(Double.doubleToRawLongBits(d_2.getElementAsDouble(0))));
    assertThat(DoubleVector.isNA(d_2.getElementAsDouble(0)), equalTo(true));
  }
  
  @Test
  public void loadNA() throws IOException {
    InputStream in = getClass().getResourceAsStream("/na.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP exp = reader.readFile();

    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    DoubleVector x = (DoubleVector) pairList.findByTag(symbol("x"));

    assertThat(DoubleVector.isNA(x.getElementAsDouble(0)), equalTo(true));
  }

  @Test
  public void loadNA2() throws IOException {
    InputStream in = getClass().getResourceAsStream("/na2.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP exp = reader.readFile();

    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    ListVector df = (ListVector) pairList.findByTag(symbol("df"));
    DoubleVector x = (DoubleVector) df.getElementAsSEXP(0);
    
    assertThat(x.isElementNA(2), equalTo(true));
  }

  @Test
  public void loadBytecode() throws IOException {
    
    // bytecode.rds was generated using GNU R 3.2.0 as follows:
    // library(compiler)
    // f <- cmpfun(function(x) x*x))
    // saveRDS(f, "bytecode.rds")
    
    InputStream in = getClass().getResourceAsStream("bytecode.rds");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "f", reader.readFile());
    
    assertThat(eval("identical(body(f), quote(x*x))"), elementsIdenticalTo(c(true)));
    assertThat(eval("f(8)"), elementsIdenticalTo(c(64)));
  }
  
  @Test
  public void loadComplicatedBytecode() throws IOException {
    // bytecode2.rds was generated using GNU R 3.2.0 as follows:
    // saveRDS(list(a=ls,b=data.frame,c=42), "bytecode2.rds")
    
    InputStream in = getClass().getResourceAsStream("bytecode2.rds");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);

    SEXP list = reader.readFile();
    assertThat(list.getElementAsSEXP(0), instanceOf(Closure.class));
    assertThat(list.getElementAsSEXP(1), instanceOf(Closure.class));
    assertThat(list.getElementAsSEXP(2), elementsIdenticalTo(c(42)));

  }
  
  @Test
  public void loadDataFrameWithGnuRCompactRowNames() throws IOException {
    SEXP df = readRds("rownames.rds");

    assertThat(df.getS3Class().getElementAsString(0), equalTo("data.frame"));
    assertThat(df.getAttribute(Symbol.get("row.names")).length(), equalTo(1000));
  }

  @Test
  public void loadObjectWithS4BitSet() throws IOException {
    SEXP x = readRds("s4.rds");

    assertThat(x, elementsIdenticalTo(c(1)));
    assertTrue("s4 bit is true", Types.isS4(x));
  }


  @Test
  public void loadObjectWithOldS4Attribute() throws IOException {
    SEXP x = readRds("old-s4.rds");

    assertThat(x, elementsIdenticalTo(c(1)));
    assertTrue("s4 bit is true", Types.isS4(x));
  }

  @Test
  public void loadEnvWithAttributes() throws IOException {
    SEXP env = readRds("env_attr.rds");

    assertThat(env, instanceOf(Environment.class));
    assertThat(env.getAttributes().getClassVector(), elementsIdenticalTo(c("MyRefClass")));
  }

  @Test
  public void readLockedEnvironment() throws IOException {

    // Environment is locked, but not bindings, so they can be changed

    Environment env = (Environment) readRds("locked-env.rds");

    assertTrue(env.isLocked());
    assertFalse(env.bindingIsLocked(Symbol.get("a")));
    assertFalse(env.bindingIsLocked(Symbol.get("b")));
  }

  @Test
  @Ignore
  public void readLockedEnvironmentAndBindings() throws IOException {

    // Environment is locked, but not bindings, so they can be changed
    // TODO:

    Environment env = (Environment) readRds("locked-env-bindings.rds");

    assertTrue(env.isLocked());
    assertTrue(env.bindingIsLocked(Symbol.get("a")));
  }

  @Test
  public void readEnvironmentWithActiveBindings() throws IOException {

    // Read environment created with:
    //  rho <- new.env()
    // f <- function(val) 42
    // makeActiveBinding("f", f, rho)

    Environment env = (Environment) readRds("activebinding.rds");
    assertThat( env.getVariable(topLevelContext, "f"), elementsIdenticalTo(c(42)));

    // Assigning should have no effect
    env.setVariable(topLevelContext, "f", c(99));
    assertThat( env.getVariable(topLevelContext, "f"), elementsIdenticalTo(c(42)));
  }

  protected Symbol symbol(String name){
    return Symbol.get(name);
  }

  private SEXP readRds(String resourceName) throws IOException {
    InputStream in = getClass().getResourceAsStream(resourceName);
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);
    return reader.readFile();
  }

}

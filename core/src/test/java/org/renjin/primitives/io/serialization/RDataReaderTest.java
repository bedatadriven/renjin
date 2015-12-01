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

import com.google.common.io.ByteSource;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;


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
    assertThat(pairList.getValue(), equalTo( c(1,2,3,4) ));
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
    assertThat(pairList.findByTag(symbol("a")), elementsEqualTo( eval("1:99") ));
    assertThat(pairList.findByTag(symbol("b")), elementsEqualTo( eval("sqrt(1:25) ") ));
    assertThat(pairList.findByTag(symbol("c")), elementsEqualTo( c(Logical.NA )));
    assertThat(pairList.findByTag(symbol("d")), equalTo( list(c(Logical.NA), DoubleVector.NA, IntVector.NA, NULL )));

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

    topLevelContext.getGlobalEnvironment().setVariable("f", reader.readFile());
    
    assertThat(eval("identical(body(f), quote(x*x))"), equalTo(c(true)));
    assertThat(eval("f(8)"), equalTo(c(64)));
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
    assertThat(list.getElementAsSEXP(2), equalTo(c(42)));

  }
  
  @Test
  public void loadDataFrameWithGnuRCompactRowNames() throws IOException {
    InputStream in = getClass().getResourceAsStream("rownames.rds");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(topLevelContext, gzipIn);
    SEXP df = reader.readFile();

    assertThat(df.getS3Class().getElementAsString(0), equalTo("data.frame"));
    assertThat(df.getAttribute(Symbol.get("row.names")).length(), equalTo(1000));
  }

  protected Symbol symbol(String name){
    return Symbol.get(name);
  }


}

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

package r.io;

import org.junit.Test;
import r.EvalTestCase;
import r.lang.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class DatafileReaderTest extends EvalTestCase {

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
    DatafileReader reader = new DatafileReader(topLevelContext, topLevelContext.getGlobalEnvironment(), gzipIn);

    SEXP exp = reader.readFile();
    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    assertThat(pairList.length(), equalTo(1));
    assertThat(pairList.getValue(), equalTo( c(1,2,3,4) ));
  }

  @Test
  public void loadComplete() throws IOException {
    InputStream in = getClass().getResourceAsStream("/complete.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    DatafileReader reader = new DatafileReader(topLevelContext, topLevelContext.getGlobalEnvironment(), gzipIn);

    SEXP exp = reader.readFile();
    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    assertThat(pairList.findByTag(symbol("a")), equalTo( eval("1:99") ));
    assertThat(pairList.findByTag(symbol("b")), equalTo( eval("sqrt(1:25) ") ));
    assertThat(pairList.findByTag(symbol("c")), equalTo( c(Logical.NA )));
    assertThat(pairList.findByTag(symbol("d")), equalTo( list(Logical.NA, DoubleVector.NA, IntVector.NA, NULL )));

    ListVector d = (ListVector) pairList.findByTag(symbol("d"));
    DoubleVector d_2 = (DoubleVector) d.getElementAsSEXP(1);

    System.out.println(Long.toHexString(Double.doubleToRawLongBits(d_2.getElementAsDouble(0))));
    assertThat(DoubleVector.isNA(d_2.getElementAsDouble(0)), equalTo(true));
  }

  @Test
  public void loadNA() throws IOException {
    InputStream in = getClass().getResourceAsStream("/na.RData");
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    DatafileReader reader = new DatafileReader(topLevelContext, topLevelContext.getGlobalEnvironment(), gzipIn);

    SEXP exp = reader.readFile();

    assertThat(exp, instanceOf(PairList.Node.class));

    PairList.Node pairList = (PairList.Node) exp;
    DoubleVector x = (DoubleVector) pairList.findByTag(symbol("x"));

    assertThat(DoubleVector.isNA(x.getElementAsDouble(0)), equalTo(true));
  }


  protected Symbol symbol(String name){
    return new Symbol(name);
  }
}

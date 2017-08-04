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
package org.renjin.parser;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.*;
import org.renjin.sexp.PairList.Node;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class RdParserTest extends EvalTestCase {

  @Test
  public void simple() throws IOException {
    
    RdParser parser = new RdParser();
    ListVector result = (ListVector) parser.R_ParseRd(new StringReader("\\name{plotf}"), Null.INSTANCE, false);
    assertThat(result.length(), equalTo(1));
 
    ListVector header = (ListVector) result.getElementAsSEXP(0);
    assertThat(header.getAttribute(Symbol.get("Rd_tag")), elementsIdenticalTo(c("\\name")));

    StringVector name = (StringVector) header.getElementAsSEXP(0);
    assertThat(name, elementsIdenticalTo(c("plotf")));
    //assertThat(name.getAttribute(Symbol.get("Rd_tag")), equalTo(c("VERB")));
    
  }
  
  @Test
  public void comparison() throws IOException {
    
    // output from C-R 2.12.1
    RDataReader reader = new RDataReader(topLevelContext, getClass().getResourceAsStream("expected.Rdata"));
    PairList.Node contents = (Node) reader.readFile();
    ListVector expected = contents.getElementAsSEXP(0);
    
    assertThat(expected.length(), equalTo(3));
    
    compareTree("test1.Rd", expected.get(0));  
    compareTree("test2.Rd", expected.get(1));
    compareTree("test3.Rd", expected.get(2));
  }

  private void compareTree(String resourceName, SEXP expected) throws IOException {
    InputStream in = getClass().getResourceAsStream(resourceName);
    InputStreamReader reader = new InputStreamReader(in);
    
    RdParser parser = new RdParser();
    ListVector result = (ListVector) parser.R_ParseRd(reader, Null.INSTANCE, false);

    compareNode(resourceName, result, expected);
  }

  private void compareNode(String node, SEXP result, SEXP expected) {
    for(PairList.Node attribute : expected.getAttributes().nodes()) {
      // TODO: srcref at root node is missing
      if(!attribute.getName().equals("srcref")) {
        assertThat(node + "/attribute[" + attribute.getTag() + "]", result.getAttribute(attribute.getTag()), identicalTo(attribute.getValue()));
      }
    }
    if(expected instanceof ListVector) {
      if(!(result instanceof ListVector) || result.length() != expected.length()) {
        assertThat(node, result, identicalTo(expected));
      } else {
        for(int i=0;i!=expected.length();++i) {
          compareNode(describeChildNode(node, (ListVector)expected, i), 
              result.getElementAsSEXP(i), ((ListVector) expected).getElementAsSEXP(i));
        }
      }
    } else if(result instanceof StringVector && expected instanceof StringVector && result.length() == 1 && expected.length() == 1) {
      String s1 = ((StringVector) result).getElementAsString(0);
      String s2 = ((StringVector) expected).getElementAsString(0);
      assertThat(node, s1.replace("\r\n", "\n"), equalTo(s2));
    } else {
      assertThat(node, result, identicalTo(expected));
    }
  }
  
  private String describeChildNode(String parent, ListVector vector, int index) {
    StringBuilder sb = new StringBuilder();
    if(!parent.isEmpty()) {
      sb.append(parent).append("/");
    }
    sb.append("list{");
    boolean needsComma = false;
    for(PairList.Node attrib : vector.getAttributes().nodes()) {
      if(!attrib.getName().equals("srcref")) {
        if(needsComma) {
          sb.append(", ");
        } else {
          needsComma = true;
        }
        sb.append(attrib.getName() + "=" + attrib.getValue());
      }
    }
    sb.append("}/element[");
    sb.append(index);
    sb.append("]");
    return sb.toString();
  }
}

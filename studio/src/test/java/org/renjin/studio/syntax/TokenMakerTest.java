/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.studio.syntax;

import org.fife.ui.rsyntaxtextarea.RenjinTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.text.Segment;

@Ignore
public class TokenMakerTest {

  
  @Test
  public void testUnderstanding() {
    
    String code = "f = function(x) {\n return(1+x);\n}";
    JavaScriptTokenMaker tokenMaker = new JavaScriptTokenMaker();
    Token tokenList = tokenMaker.getTokenList(new Segment(code.toCharArray(), 0, code.length()), 0, 0);
    
    tokenList = dumpTokenList(tokenList);
  }
  
  @Test
  public void testRenjin() {
    
    String code = "f <- function(x) {\n return(1+x);\n}";
    RenjinTokenMaker tokenMaker = new RenjinTokenMaker();
    Token tokenList = tokenMaker.getTokenList(new Segment(code.toCharArray(), 0, code.length()), 0, 0);
    
    tokenList = dumpTokenList(tokenList);
  }

  private Token dumpTokenList(Token tokenList) {
    while(tokenList != null) {
      System.out.println("=====Token====");
      System.out.println(tokenList.getLexeme());
      System.out.println("type = " + tokenList.type);
//      System.out.println(new String(tokenList.text));
//      System.out.println("--------------");
//      System.out.println("offset:     " + tokenList.offset);
//      System.out.println("textCount:  " + tokenList.textCount);
//      System.out.println("textOffset: " + tokenList.textOffset);
//      System.out.println("type:       " + tokenList.type);
      tokenList = tokenList.getNextToken();
    }
    return tokenList;
  }
}

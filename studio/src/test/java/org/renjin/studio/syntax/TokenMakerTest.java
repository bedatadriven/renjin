package org.renjin.studio.syntax;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.RenjinTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;
import org.junit.Ignore;
import org.junit.Test;

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

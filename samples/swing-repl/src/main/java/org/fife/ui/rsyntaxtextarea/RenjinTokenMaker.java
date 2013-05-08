package org.fife.ui.rsyntaxtextarea;

import java.io.CharArrayReader;

import javax.swing.text.Segment;

import org.renjin.parser.ParseOptions;
import org.renjin.parser.ParseState;
import org.renjin.parser.RLexer;
import org.renjin.parser.RParser;

public class RenjinTokenMaker extends TokenMakerBase {

  @Override
  public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
    ParseOptions parseOptions = new ParseOptions();
    ParseState parseState = new ParseState();
    RLexer lexer = new RLexer(parseOptions, parseState, 
        new CharArrayReader(text.array, text.getBeginIndex(), text.getEndIndex()));
    
    
    int token = Integer.MAX_VALUE;
    do {
      token = lexer.yylex();
    }
    while(token != RParser.END_OF_INPUT);
    
    throw new UnsupportedOperationException();
  }

  private Token nextToken(RLexer lexer) {
    int token = lexer.yylex();
    if(token == RParser.END_OF_INPUT) {
      return null;
    }
    Token token = new tok
  }
  
  @Override
  public boolean getCurlyBracesDenoteCodeBlocks() {
    return true;
  }
  
}

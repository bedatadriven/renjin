package org.fife.ui.rsyntaxtextarea;

import static org.renjin.parser.RParser.AND;
import static org.renjin.parser.RParser.AND2;
import static org.renjin.parser.RParser.EQ;
import static org.renjin.parser.RParser.EQ_ASSIGN;
import static org.renjin.parser.RParser.FOR;
import static org.renjin.parser.RParser.FUNCTION;
import static org.renjin.parser.RParser.GE;
import static org.renjin.parser.RParser.GT;
import static org.renjin.parser.RParser.IN;
import static org.renjin.parser.RParser.LE;
import static org.renjin.parser.RParser.LEFT_ASSIGN;
import static org.renjin.parser.RParser.LT;
import static org.renjin.parser.RParser.NE;
import static org.renjin.parser.RParser.OR;
import static org.renjin.parser.RParser.OR2;
import static org.renjin.parser.RParser.REPEAT;
import static org.renjin.parser.RParser.RIGHT_ASSIGN;
import static org.renjin.parser.RParser.SPECIAL;
import static org.renjin.parser.RParser.WHILE;

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
    
    if(startOffset != text.getBeginIndex()) {
      System.out.println("startOffset = " + startOffset);
    }
    
    RLexer lexer = new RLexer(parseOptions, parseState, 
        new CharArrayReader(text.array, text.getBeginIndex(), text.getEndIndex() - text.getBeginIndex()));
    
    Token head = null;
    Token tail = null;
    do {
      Token token = nextToken(text, lexer);
      if(head == null) {
        head = tail = token;
      } else {
        tail.setNextToken(token);
        tail = token;
      } 
    } while(tail.type != 0);
    return head;
  }

  private Token nextToken(Segment segment, RLexer lexer) {
    int startPos = lexer.getCharacterPos();
    int token = lexer.yylex();
    if(token == RParser.END_OF_INPUT) {
      return new DefaultToken();
    }
    int endPos = lexer.getCharacterPos();
    return new DefaultToken(segment.array,
        segment.offset + startPos,
        segment.offset + endPos - 1,
        segment.offset + startPos,
        mapTokenType(token));
  }
  
  private int mapTokenType(int token) {
    switch(token) {
    case RParser.RIGHT_ASSIGN:
    case RParser.LEFT_ASSIGN:  
    case RParser.UMINUS:
    case RParser.UPLUS:
    case RParser.TILDE:
    case RParser.UNOT:
    case EQ_ASSIGN:
    case '+':
    case '-':
    case '*':
    case '/':
    case '^':
    case LT:
    case LE:
    case GE:
    case GT:
    case EQ:
    case NE:
    case OR:
    case AND:
    case OR2:
    case AND2:
    case '?':
    case '!':
    case '=':
    case ':':
    case '~':
    case '$':
    case '@':
    case '[':
    case ']':
        return TokenTypes.OPERATOR;
          
    case RParser.SYMBOL:
      return TokenTypes.VARIABLE;
    
    case RParser.STR_CONST:
      return TokenTypes.LITERAL_STRING_DOUBLE_QUOTE;
    
    case RParser.NULL_CONST:
      return TokenTypes.RESERVED_WORD;
      
    case RParser.NUM_CONST:
      return TokenTypes.LITERAL_NUMBER_DECIMAL_INT;
      
    case RParser.FUNCTION:
    case RParser.IF:
    case RParser.ELSE:
    case SPECIAL:
    case WHILE:
    case REPEAT:
    case FOR:
    case IN:
      return TokenTypes.RESERVED_WORD;
     
    case '(':
    case ')':
    case '{':
    case '}':
    case ',':
      return TokenTypes.SEPARATOR;
      
      
    default:
      return TokenTypes.SEPARATOR;
    }
  }

  @Override
  public boolean getCurlyBracesDenoteCodeBlocks() {
    return true;
  }
}

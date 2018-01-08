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
package org.fife.ui.rsyntaxtextarea;

import org.renjin.parser.ParseOptions;
import org.renjin.parser.ParseState;
import org.renjin.parser.RLexer;
import org.renjin.parser.RParser;

import javax.swing.text.Segment;
import java.io.CharArrayReader;

import static org.renjin.parser.RParser.*;

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

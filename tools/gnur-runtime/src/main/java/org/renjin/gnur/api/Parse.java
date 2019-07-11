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
// Initial template generated from Parse.h from R 3.2.2
package org.renjin.gnur.api;

import org.renjin.gcc.runtime.Ptr;
import org.renjin.parser.RParser;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

@SuppressWarnings("unused")
public final class Parse {

  private Parse() { }

  private static final int PARSE_NULL = 0;
  private static final int PARSE_OK = 1;
  private static final int PARSE_INCOMPLETE = 2;
  private static final int PARSE_ERROR = 3;
  private static final int PARSE_EOF = 4;


  public static SEXP R_ParseVector(SEXP text, int n, Ptr status, SEXP srcfile) {
    StringVector textVector = (StringVector) text;
    StringBuilder source = new StringBuilder();
    for (String line : textVector) {
      source.append(line);
      if(source.length() > 0 && source.charAt(source.length() - 1) != '\n') {
        source.append('\n');
      }
    }
    Reader reader = new StringReader(source.toString());
    RParser parser = new RParser(reader);

    List<SEXP> exprList = Lists.newArrayList();
    try {
      while(true) {
        if(parser.isEof()) {
          break;
        }
        if(!parser.parse()) {
          switch (parser.getResultStatus()) {
            case EMPTY:
              status.setInt(PARSE_NULL);
              break;
            case OK:
              status.setInt(PARSE_OK);
              break;
            case INCOMPLETE:
              status.setInt(PARSE_INCOMPLETE);
              break;
            case ERROR:
              status.setInt(PARSE_ERROR);
              break;
            case EOF:
              status.setInt(PARSE_EOF);
              break;
          }
          return Null.INSTANCE;
        }
        exprList.add(parser.getResult());
      }
    } catch (IOException e) {
      status.setInt(PARSE_ERROR);
      return Null.INSTANCE;
    }

    if(exprList.size() > 0) {
      status.setInt(PARSE_OK);
    }

    return new ExpressionVector(exprList);
  }

}
